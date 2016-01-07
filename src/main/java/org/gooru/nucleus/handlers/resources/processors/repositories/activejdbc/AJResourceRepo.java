package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.gooru.nucleus.handlers.resources.app.components.DataSourceRegistry;
import org.gooru.nucleus.handlers.resources.processors.exceptions.InvalidUserException;
import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.responses.transformers.ResponseTransformerBuilder;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PGobject;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.impl.exceptionconverters.GenericClientExceptionConverter;

import org.slf4j.Logger;

/**
 * Created by ashish on 29/12/15.
 */
public class AJResourceRepo implements ResourceRepo {

  private static final Logger LOGGER = LoggerFactory.getLogger(AJResourceRepo.class);
  
  // jsonb fields relevant to resource
  private static final String [] JSONB_FIELDS = {"metadata", "taxonomy", "depth_of_knowledge", "copyright_owner"};

  // not null fields in db 
  private static final String [] NOTNULL_FIELDS = {"content_id", "title", "creator_id", "original_creator_id", "content_format", "content_subformat", "visible_on_profile", "is_deleted"};
  
  // <TBD> - Need to decide
  // only owner (original creator of the resource) can change, which will have to update all the copied records of the resource
  private static final String [] OWNER_SPECIFIC_FIELDS = {"depth_of_knowledge", "content_format", "content_subformat"};
  
  // <TBD> - Need to decide
  // owner and other copied users can change them, but the changes will be restricted to that record only
  private static final String [] COMMON_EDITABLE_FIELDS = {"metadata", "taxonomy", "title", "description"};
  
  /* <TBD>
  * Need to decide on owner specific editable fields, non-owner non-editable and common fields
  * UUID generation
  * In update, Check if the user is the owner of the resource, if so then allow editing OWNER_SPECIFIC_FIELDS & C OMMON_EDITABLE_FIELDS
  * and update all COMMON_EDITABLE_FIELDS in the copied records and keep OWNER_SPECIFIC_FIELDS changes locally
  * If the user is not owner, then allow editing the COMMON_EDITABLE_FIELDS but keep changes locally
  * Taxonomy changes - which I am not clear - something to do with user preference while updating
  * Exception handling
  */
 
  @Override
  public JsonObject getResourceById(String resourceId) {
    String sql =
            "SELECT content_id, title, url, creator_id, narration, description, content_subformat, metadata, taxonomy, depth_of_knowledge, thumbnail, is_frame_breaker, is_broken, is_deleted, is_copyright_owner, copyright_owner, visible_on_profile FROM CONTENT WHERE content_id = '"
                    + resourceId + "' AND content_format ='resource'";

    Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
    LazyList<AJEntityResource> result = AJEntityResource.findBySQL(sql);
    LOGGER.debug("getResourceById ! : {} ", result.toString());
    JsonObject returnValue = new JsonObject();
    if (result.size() > 0) {
       returnValue = new AJResponseJsonTransformer().transform(result.get(0).toJson(false, "content_id", "title", "url", "creator_id", "narration",
              "description", "content_subformat", "metadata", "taxonomy", "depth_of_knowledge", "thumbnail", "is_frame_breaker", "is_broken",
              "is_deleted", "is_copyright_owner", "copyright_owner", "visible_on_profile"));
    } else {
      LOGGER.debug("Resource not found{} ", result.toString());
    }
    Base.close();
    return returnValue;
  }

  @Override
  public JsonObject createResource(JsonObject resourceData) {
    Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
    Base.openTransaction();
    LOGGER.debug("Created resource  " + resourceData);
    String mapValue = null;
    try {
      AJEntityResource createRes = new AJEntityResource();
      for (Map.Entry<String, Object> entry : resourceData) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        
        if (entry.getKey().equalsIgnoreCase("content_format")) {
          PGobject contentFormat = new PGobject();
          contentFormat.setType("content_format");
          if (mapValue == null || mapValue.isEmpty() ) {
            LOGGER.error("content format is null! : {} ", entry.getKey());
            return null;
          } else {
            contentFormat.setValue(mapValue);
            createRes.set(entry.getKey(), contentFormat);
          }
        } else if (entry.getKey().equalsIgnoreCase("content_subformat") ){
          PGobject contentSubformat = new PGobject();
          contentSubformat.setType("content_subformat");
          if (mapValue == null || mapValue.isEmpty() ) {
            LOGGER.error("content subformat is null! : {} ", entry.getKey());
            return null;
          } else { 
            contentSubformat.setValue(mapValue);
            createRes.set(entry.getKey(), contentSubformat);
          }
        } else if (Arrays.asList(JSONB_FIELDS).contains(entry.getKey())) {
          PGobject jsonbFields = new PGobject();
          jsonbFields.setType("jsonb");
          if (Arrays.asList(NOTNULL_FIELDS).contains(entry.getKey())) {
            if (mapValue == null || mapValue.isEmpty() ) {
              LOGGER.error("mandatory fields are null! : {} ", entry.getKey());
              return null;
            }
            else {
              jsonbFields.setValue(mapValue);
              createRes.set(entry.getKey(), jsonbFields);
            }
          }
          
        } else {
          if (mapValue != null || !mapValue.isEmpty() ) {
            createRes.set(entry.getKey(), entry.getValue()); // intentionally kept entry.getValue instead of mapValue as it needs to handle other datatypes like boolean
          } else {
            LOGGER.error("mandatory fields in else is null! : {} ", entry.getKey());
            return null;
          }
          
        }
      }
      LOGGER.debug("Creating resource From MAP  : {}", createRes.toInsert());
      String resourceId = createRes.getString("content_id");
      JsonObject resourceIdWhereURLExists = getResourceByURL(createRes.getString("url"));
      if (resourceIdWhereURLExists == null || resourceIdWhereURLExists.isEmpty()) {
        if (createRes.insert()) {
          LOGGER.debug("Created resource ID: " + resourceId);
          Base.commitTransaction();
          return new JsonObject().put("id", resourceId);
        } else {
          LOGGER.error("Failed to create <TBD> get cause from error object!");
          Base.rollbackTransaction();
        }
      } else {
        LOGGER.debug("URL Exists <TBD> so cannot go ahead!");
        return resourceIdWhereURLExists;
      }
      
     return null;
    } catch (SQLException e) {
      LOGGER.warn("Caught sqlExceptions", e);
      Base.rollbackTransaction();
      return null;
    } catch (Throwable throwable) {
      LOGGER.warn("Caught unexpected exception here", throwable);
      Base.rollbackTransaction();
      return null;
    } finally {
      Base.close();
    }
  }
  
 private JsonObject getResourceByURL(String inputURL){
    String sql = "select content_id from content where url = '" + inputURL + "' AND content_format ='resource' AND original_content_id is null" ;

    LazyList<AJEntityResource> result = AJEntityResource.findBySQL(sql);
    LOGGER.debug("getResourceById ! : {} ", result.toString());
    JsonObject returnValue = null;
    if (result.size() > 0) {
      returnValue = new JsonObject().put("duplicate_ids", new JsonArray(result.collect("content_id")));
      return returnValue;
    }
    return returnValue;
  }
}