package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.resources.app.components.DataSourceRegistry;
import org.gooru.nucleus.handlers.resources.processors.exceptions.InvalidUserException;
import org.gooru.nucleus.handlers.resources.processors.exceptions.InvalidInputException;
import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.responses.transformers.ResponseTransformerBuilder;
import com.hazelcast.client.impl.exceptionconverters.GenericClientExceptionConverter;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PGobject;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ashish on 29/12/15.
 */
public class AJResourceRepo implements ResourceRepo {
  private String userId;
  private JsonObject prefs;

  public AJResourceRepo(String userId, JsonObject prefs) {
    this.userId = userId;
    this.prefs = prefs;
  }
  
  private static final Logger LOGGER = LoggerFactory.getLogger(AJResourceRepo.class);

  // jsonb fields relevant to resource
  private static final String[] JSONB_FIELDS = { "metadata", "taxonomy", "depth_of_knowledge", "copyright_owner" };

  // not null fields in db
  private static final String[] NOTNULL_FIELDS =
          { "id", "title", "creator_id", "original_creator_id", "content_format", "content_subformat", "visible_on_profile", "is_deleted" };

  // <TBD> - Need to decide
  // only owner (original creator of the resource) can change, which will have
  // to update all the copied records of the resource
  private static final String[] OWNER_SPECIFIC_FIELDS = { "title", "url", "description", "depth_of_knowledge", "content_format", "content_subformat" };


  /*
   * <TBD> Need to decide on owner specific editable fields, non-owner
   * non-editable and common fields UUID generation In update, Check if the user
   * is the owner of the resource, if so then allow editing
   * OWNER_SPECIFIC_FIELDS & COMMON_EDITABLE_FIELDS and update all
   * COMMON_EDITABLE_FIELDS in the copied records and keep OWNER_SPECIFIC_FIELDS
   * changes locally If the user is not owner, then allow editing the
   * COMMON_EDITABLE_FIELDS but keep changes locally 
   * Taxonomy changes - which I am not clear - something to do with user preference while updating
   * Exception handling
   */

  @Override
  public JsonObject getResourceById(String resourceId) {
    final String [] attributes = {"id", "title", "url", "creator_id", "narration",
                                  "description", "content_subformat", "metadata", 
                                  "taxonomy", "depth_of_knowledge", "thumbnail", "original_content_id",
                                  "is_frame_breaker", "is_broken", "is_deleted", "original_creator_id",
                                  "is_copyright_owner", "copyright_owner", "visible_on_profile" };

    String sql = "SELECT " + String.join(", ", attributes) + " FROM CONTENT WHERE id = '"
                    + resourceId + "' AND content_format ='resource'";

    Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
    
    LazyList<AJEntityResource> result = AJEntityResource.findBySQL(sql);
    LOGGER.debug("getResourceById : {} ", result.toString());
    
    JsonObject returnValue = new JsonObject();
    if (result.size() > 0) {
      returnValue = new AJResponseJsonTransformer().transform(result.get(0).toJson(false, attributes));
    } else {
      LOGGER.warn("getResourceById : Resource with id : {} : not found", resourceId );
    }
    
    Base.close();
    
    LOGGER.debug("getResourceById : Return Value : {} ", returnValue);
    
    return returnValue;
  }

  @Override
  public JsonObject createResource(JsonObject resourceData) {
    Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());

    LOGGER.debug("createResource : Resource  to create: " + resourceData);
    try {
      AJEntityResource createRes = new AJEntityResource();
      populateEntityFromJson(resourceData, createRes);

      LOGGER.debug("createResource : Creating resource From MAP  : {}", createRes.toInsert());
      JsonObject resourceIdWithURLDuplicates = getResourceByURL(createRes.getString("url"));
      if (resourceIdWithURLDuplicates != null && !resourceIdWithURLDuplicates.isEmpty()) {
        LOGGER.debug("createResource : URL Exists <TBD> so cannot go ahead!");
        return resourceIdWithURLDuplicates;
      }
      
      Base.openTransaction();
      if (createRes.insert()) {
        LOGGER.debug("createResource : Created resource ID: " + createRes.getString("id") );
        Base.commitTransaction();
        return new JsonObject().put("id", createRes.getString("id") );
      } else {
        LOGGER.error("createResource : Failed to create <TBD> get cause from error object!");
        Base.rollbackTransaction();
      }
      return null;
    } catch (SQLException e) {
      LOGGER.warn("createResource : Caught sqlExceptions", e);
      Base.rollbackTransaction();
      return null;
    } catch (Throwable throwable) {
      LOGGER.warn("createResource : Caught unexpected exception here", throwable);
      Base.rollbackTransaction();
      return null;
    } finally {
      Base.close();
    }
  }


  @Override
  public JsonObject updateResource(JsonObject resourceData) {
    String resourceId = null;
    AJEntityResource updateRes = null;
    JsonObject ownerDataToPropogateToCopies = null;
    try {
      LOGGER.debug("updateResource : Resource to update : {} ", resourceData);
      if (resourceData == null) {
        LOGGER.error("updateResource : Invalid resource data input. Cannot update resource.");
        throw new IllegalArgumentException("Invalid arguments");
      }

      resourceId = resourceData.getString("id");
      if (resourceId == null || resourceId.isEmpty()) {
        LOGGER.error("updateResource : Invalid resource ID input. Cannot update resource.");
        throw new IllegalArgumentException("Invalid arguments");
      }

      // fetch resource from DB based on Id received
      JsonObject fetchDBResourceData = getResourceById(resourceId);
      if (fetchDBResourceData == null) {
        LOGGER.error("updateResource : Object to update is not found in DB! Input resource ID: {} ", resourceId);
        throw new IllegalStateException("Object to update is not found in DB!");
      }

      // check if owner and current user are the same
      boolean isOwner = false;
      String originalCreator = fetchDBResourceData.getString("original_creator_id");
      LOGGER.debug("updateResource : Original creator from DB = {}.", originalCreator);
      
      if ((originalCreator != null) && !originalCreator.isEmpty() )
        isOwner = (userId.compareToIgnoreCase(originalCreator) == 0) ? true : false;      
      LOGGER.debug("updateResource : Ok! So, who is trying to update content? {}.", (isOwner) ? "owner" : "someone else");
      
      String mapValue = null;

      // now mandatory field checks on input resource data and if contains
      // owner-Specific editable fields
      // compare input value and collect only changed attributes in new model
      // that we will use to update
      updateRes = new AJEntityResource();
      updateRes.set("id", resourceId);
            
      LOGGER.debug("updateResource : Iterate through the input Json now.");
      
      for (Map.Entry<String, Object> entry : resourceData) {
        LOGGER.debug("updateResource : checking the key & values..before collection. Key: {}", entry.getKey() );
        
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if (Arrays.asList(NOTNULL_FIELDS).contains(entry.getKey())) {
          if (mapValue == null) {
            LOGGER.error("Failed to update resource. Field : {} : is mandatory field and cannot be null.", entry.getKey());
            throw new IllegalStateException("Failed to update resource. Field : " + entry.getKey() + " : is mandatory field and cannot be null.");
          }
        }

        Object dbResValue = fetchDBResourceData.getValue(entry.getKey());
        boolean valueChanged = hasValueChanged(entry.getKey(), entry.getValue(), dbResValue);
        
        LOGGER.debug("updateResource : value changed? Key: {}", valueChanged );
        
        // mandatory and owner specific items may be overlapping...so do a
        // separate check not as ELSE condition
        if (!isOwner && Arrays.asList(OWNER_SPECIFIC_FIELDS).contains(entry.getKey())) {

          LOGGER.debug("updateResource : Not owner but changing owner specific fields?");
          // compare input value and db value before deciding on throwing an
          // error
          // it is possible that value has not changed but passed here based on
          // an earlier GET resource call
          if (valueChanged) {
            LOGGER.error("Error updating resource. Field: {} : can be updated only by owner of the resource.", entry.getKey());
            throw new IllegalStateException("Error updating resource. Field : " + entry.getKey() + ": can be updated only by owner of the resource.");
          }
        } else if (isOwner && Arrays.asList(OWNER_SPECIFIC_FIELDS).contains(entry.getKey())) {
          // collect the DB fields to update for owner specific fields across all copies of this resource
          LOGGER.debug("updateResource : need to propagate this : {} : to other resources. ", entry.getKey() );
          if (ownerDataToPropogateToCopies == null)
            ownerDataToPropogateToCopies = new JsonObject();
          
          ownerDataToPropogateToCopies.put(entry.getKey(), entry.getValue());
        }

        // collect the attributes and values in the model.
        if (valueChanged) {
          
          if (entry.getKey().equalsIgnoreCase("content_format")) {
            if (mapValue == null || mapValue.isEmpty()) {
              LOGGER.error("updateResource : content format is null! : {} ", entry.getKey());
              return null;
            } else {
              PGobject contentFormat = new PGobject();
              contentFormat.setType("content_format_type");
              contentFormat.setValue(mapValue);
              updateRes.set(entry.getKey(), contentFormat);
            }
          } else if (entry.getKey().equalsIgnoreCase("content_subformat")) {
            if (mapValue == null || mapValue.isEmpty()) {
              LOGGER.error("updateResource : content subformat is null! : {} ", entry.getKey());
              return null;
            } else {
              PGobject contentSubformat = new PGobject();
              contentSubformat.setType("content_subformat_type");
              contentSubformat.setValue(mapValue);
              updateRes.set(entry.getKey(), contentSubformat);
            }
          } else if (Arrays.asList(JSONB_FIELDS).contains(entry.getKey())) {
            if (Arrays.asList(NOTNULL_FIELDS).contains(entry.getKey())) {
              if (mapValue == null || mapValue.isEmpty()) {
                LOGGER.error("updateResource : mandatory fields are null! : {} ", entry.getKey());
                return null;
              } else {
                PGobject jsonbFields = new PGobject();
                jsonbFields.setType("jsonb");
                jsonbFields.setValue(mapValue);
                updateRes.set(entry.getKey(), jsonbFields);
              }
            }

          } else {
            if (mapValue == null || mapValue.isEmpty()) {
              LOGGER.error("updateResource : mandatory fields in else is null! : {} ", entry.getKey());
              return null;
            } else {
              updateRes.set(entry.getKey(), entry.getValue()); // intentionally
                                                               // kept
                                                               // entry.getValue
                                                               // instead of
                                                               // mapValue as it
                                                               // needs to
                                                               // handle other
                                                               // datatypes like
                                                               // boolean
            }
          }
        }
      }
    } catch (IllegalArgumentException iae) {
      LOGGER.error(iae.getMessage());
      return null;
    } catch (IllegalStateException ise) {
      LOGGER.error(ise.getMessage());
      return null;
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      return null;
    }

    // now ready to commit to DB
    try {
      Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
      Base.openTransaction();

      if (updateRes != null) {
        if (!updateRes.save()) {
          if (updateRes.hasErrors() ){
            Map<String,String> errors = updateRes.errors();
            for (Map.Entry<String, String> entry : errors.entrySet()) {
              LOGGER.error("updateResource : Validation error. Field {} validation failed.", entry.getKey());              
            }
          }
          LOGGER.error("updateResource : Failed to update the database for the resource: {}", updateRes);
          throw new SQLException("Update failed. Validation errors.");
        } else {
          if (ownerDataToPropogateToCopies != null)
            updateOwnerDataToCopies(resourceId, ownerDataToPropogateToCopies);
        }
      }

      Base.commitTransaction();
      // Base.close();

    } catch (SQLException se) {
      LOGGER.warn("updateResource : Caught SQLException", se);
      Base.rollbackTransaction();
      return null;
    } catch (IllegalArgumentException iae) {
      LOGGER.warn("updateResource : Caught IllegalArgumentException", iae);
      Base.rollbackTransaction();
      return null;
    } catch (Throwable throwable) {
      LOGGER.warn("updateResource : Caught unexpected exception here", throwable);
      Base.rollbackTransaction();
      return null;
    } finally {
      Base.close();
    }
    return resourceData;
  }

  
  private boolean hasValueChanged(String fieldName, Object input, Object dbValue) {
    return true;
  }

  /*
   * updateOwnerDataToCopies: as a consequence of primary resource update, 
   *      we need to update the copies of this resource - but ONLY owner specific data items.
   *      
   *      NOTE: This method does not do a lot of null checks etc; as all checks are already done by
   *      UpdateResource() method.
   */
  private int updateOwnerDataToCopies(String ownerResourceId, JsonObject dataToBePropogated) {
    LOGGER.debug("updateOwnerDataToCopies: OwnerResourceID {}", ownerResourceId);
    int numRecsUpdated = 0;
    String mapValue = null;
    try {
      
      List<Object> params = new ArrayList<Object>();
      String updateStmt = null;
      for (Map.Entry<String, Object> entry : dataToBePropogated) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        LOGGER.debug("updateOwnerDataToCopies: OwnerResourceID {}", entry.getKey());
        
        updateStmt = (updateStmt == null) ? 
                                  entry.getKey() + " = ?" : 
                                  updateStmt + ", " + entry.getKey() + " = ?";
        
        if (entry.getKey().equalsIgnoreCase("content_format")) {
          if (!mapValue.equalsIgnoreCase("resource")) { 
            throw new InvalidInputException("content format should always be a 'resource' but {} has been sent: " + mapValue);
          }
          else {
            PGobject contentFormat = new PGobject();
            contentFormat.setType("content_format_type");
            contentFormat.setValue(entry.getValue().toString());
            params.add(contentFormat);
          }
        } else if (entry.getKey().equalsIgnoreCase("content_subformat")) {
          if (!mapValue.contains("resource")) { 
            throw new InvalidInputException("content sub format is not a valid resource format ; {} has been sent: " + mapValue);
          }
          else {
            PGobject contentSubformat = new PGobject();
            contentSubformat.setType("content_subformat_type");
            contentSubformat.setValue(entry.getValue().toString());
            params.add(contentSubformat);
          }
        } else if (Arrays.asList(JSONB_FIELDS).contains(entry.getKey())) {
          PGobject jsonbFields = new PGobject();
          jsonbFields.setType("jsonb");
          jsonbFields.setValue(entry.getValue().toString());
          params.add(jsonbFields);
        } else {
          params.add(entry.getValue());
        }
      }
            
      LOGGER.debug("updateOwnerDataToCopies: Statement {}", updateStmt);
      
      if (updateStmt != null) {
        params.add(ownerResourceId);
        numRecsUpdated = AJEntityResource.update(updateStmt, "original_content_id = ?", params.toArray() );
        LOGGER.debug("updateOwnerDataToCopies : Update successful. Number of records updated: {}", numRecsUpdated);        
      }
      
   } catch (SQLException se) {
     LOGGER.warn("updateOwnerDataToCopies : Caught SQLException", se);
     return numRecsUpdated;
   } catch (IllegalArgumentException iae) {
     LOGGER.warn("updateOwnerDataToCopies : Caught IllegalArgumentException", iae);
     return numRecsUpdated;
   } catch (Throwable throwable) {
     LOGGER.warn("updateOwnerDataToCopies : Caught unexpected exception here", throwable);
     return numRecsUpdated;
   }
    
   return numRecsUpdated;
  }
  
  /*
   * populateEntityFromJson : throws exceptions
   */
  private void populateEntityFromJson(JsonObject inputJson, AJEntityResource resource) throws InvalidInputException, SQLException {
    String mapValue = null;
    for (Map.Entry<String, Object> entry : inputJson) {
      mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
      if (mapValue == null || mapValue.isEmpty()) {
        throw new InvalidInputException("Null value input for : " + entry.getKey());
      }

      if (entry.getKey().equalsIgnoreCase("content_format")) {
        if (!mapValue.equalsIgnoreCase("resource")) { 
          throw new InvalidInputException("content format should always be a 'resource' but {} has been sent: " + mapValue);
        }
        else {
          PGobject contentFormat = new PGobject();
          contentFormat.setType("content_format_type");
          contentFormat.setValue(mapValue);
          resource.set(entry.getKey(), contentFormat);
        }
      } else if (entry.getKey().equalsIgnoreCase("content_subformat")) {
        if (!mapValue.contains("resource")) { 
          throw new InvalidInputException("content sub format is not a valid resource format ; {} has been sent: " + mapValue);
        }
        else {
          PGobject contentSubformat = new PGobject();
          contentSubformat.setType("content_subformat_type");
          contentSubformat.setValue(mapValue);
          resource.set(entry.getKey(), contentSubformat);
        }
      } else if (Arrays.asList(JSONB_FIELDS).contains(entry.getKey())) {
        if (Arrays.asList(NOTNULL_FIELDS).contains(entry.getKey())) {
          PGobject jsonbFields = new PGobject();
          jsonbFields.setType("jsonb");
          jsonbFields.setValue(mapValue);
          resource.set(entry.getKey(), jsonbFields);
        }
      } else {
        resource.set(entry.getKey(), entry.getValue());
      }
    }
    return;
  }

  private JsonObject getResourceByURL(String inputURL) {
    String sql = "select id from content where url = '" + inputURL + "' AND content_format ='resource' AND original_content_id is null";
    LazyList<AJEntityResource> result = AJEntityResource.findBySQL(sql);
    LOGGER.debug("getResourceById ! : {} ", result.toString());

    JsonObject returnValue = null;
    if (result.size() > 0) {
      returnValue = new JsonObject().put("duplicate_ids", new JsonArray(result.collect("id")));
      return returnValue;
    }
    return returnValue;
  }

}