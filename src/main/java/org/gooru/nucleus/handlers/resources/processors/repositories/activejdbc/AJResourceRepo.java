package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;


/**
 * Created by ashish on 29/12/15.
 */
public class AJResourceRepo implements ResourceRepo {
 
  private final ProcessorContext context;
  
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
  public AJResourceRepo(ProcessorContext context) {
    this.context = context;
  }
  
  @Override
  public MessageResponse createResource() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildCreateResourceHandler(context));
  }

  @Override
  public MessageResponse updateResource() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildUpdateResourceHandler(context));
  }

  @Override
  public MessageResponse fetchResource() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchResourceHandler(context));

  }
  
   /*
  @Override
  public MessageResponse updateResource(JsonObject resourceData) {
    String resourceId = null;
    AJEntityResource updateRes = null;
    JsonObject ownerDataToPropogateToCopies = null;
    try {
      LOGGER.debug("updateResource : Resource to update : {} ", resourceData);
      if (resourceData == null) {
        LOGGER.error("updateResource : Invalid resource data input. Cannot update resource.");
        return MessageResponseFactory.createInvalidRequestResponse();
      }

      resourceId = resourceData.getString("id");
      if (resourceId == null || resourceId.isEmpty()) {
        LOGGER.error("updateResource : Invalid resource ID input. Cannot update resource.");
        return MessageResponseFactory.createInvalidRequestResponse();
      }

      // fetch resource from DB based on Id received
      JsonObject fetchDBResourceData = null;//getResourceById(resourceId).reply();
      if (fetchDBResourceData == null) {
        LOGGER.error("updateResource : Object to update is not found in DB! Input resource ID: {} ", resourceId);
        return MessageResponseFactory.createNotFoundResponse();
      }

      // check if owner and current user are the same
      boolean isOwner = false;
      String originalCreator = fetchDBResourceData.getString("original_creator_id");
      LOGGER.debug("updateResource : Original creator from DB = {}.", originalCreator);
      
      if ((originalCreator != null) && !originalCreator.isEmpty() )
        isOwner = ((context.userId()).compareToIgnoreCase(originalCreator) == 0) ? true : false;      
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
            return MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue()));
          }
        }

        // mandatory and owner specific items may be overlapping...so do a
        // separate check not as ELSE condition
        if (!isOwner && Arrays.asList(OWNER_SPECIFIC_FIELDS).contains(entry.getKey())) {
          LOGGER.debug("updateResource : Not owner but changing owner specific fields?");
          LOGGER.error("Error updating resource. Field: {} : can be updated only by owner of the resource.", entry.getKey());
          return MessageResponseFactory.createForbiddenResponse();
        } else if (isOwner && Arrays.asList(OWNER_SPECIFIC_FIELDS).contains(entry.getKey())) {
          // collect the DB fields to update for owner specific fields across all copies of this resource
          LOGGER.debug("updateResource : need to propagate this : {} : to other resources. ", entry.getKey() );
          if (ownerDataToPropogateToCopies == null)
            ownerDataToPropogateToCopies = new JsonObject();
          
          ownerDataToPropogateToCopies.put(entry.getKey(), entry.getValue());
        }

        // collect the attributes and values in the model.
        if (entry.getKey().equalsIgnoreCase("content_format")) {
          if (mapValue == null || mapValue.isEmpty()) {
            LOGGER.error("updateResource : content format is null! : {} ", entry.getKey());
            return MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue()));
          } else {
            if (!mapValue.equalsIgnoreCase("resource")) { 
              return MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), "Unknown format!"));
            }
            else {
              PGobject contentFormat = new PGobject();
              contentFormat.setType("content_format_type");
              contentFormat.setValue(mapValue);
              updateRes.set(entry.getKey(), contentFormat);
            }
          }
        } else if (entry.getKey().equalsIgnoreCase("content_subformat")) {
          if (mapValue == null || mapValue.isEmpty()) {
            LOGGER.error("updateResource : content subformat is null! : {} ", entry.getKey());
            return MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue()));
          } else {
            if (!mapValue.contains("resource")) { 
              return MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), "Unknown subformat!"));
            }
            else {
              PGobject contentSubformat = new PGobject();
              contentSubformat.setType("content_subformat_type");
              contentSubformat.setValue(mapValue);
              updateRes.set(entry.getKey(), contentSubformat);
            }
          }
        } else if (Arrays.asList(JSONB_FIELDS).contains(entry.getKey())) {
          if (Arrays.asList(NOTNULL_FIELDS).contains(entry.getKey())) {
            if (mapValue == null || mapValue.isEmpty()) {
              LOGGER.error("updateResource : mandatory fields is null! : {} ", entry.getKey());
              return MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue()));
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
            return MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue()));
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
    } catch (IllegalArgumentException iae) {
      LOGGER.error(iae.getMessage());
      return MessageResponseFactory.createInvalidRequestResponse();
    } catch (IllegalStateException ise) {
      LOGGER.error(ise.getMessage());
      return MessageResponseFactory.createInternalErrorResponse();
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
      return MessageResponseFactory.createValidationErrorResponse(new JsonObject().put("error", e.getMessage()));
    }

    // now ready to commit to DB
    try {
      Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
      Base.openTransaction();

      if (updateRes != null) {
        if (!updateRes.save()) {
          LOGGER.error("updateResource : Failed to update the database for the resource: {}", updateRes);
          return MessageResponseFactory.createValidationErrorResponse(updateRes.errors());
        } else {
         // if (ownerDataToPropogateToCopies != null)
            //updateOwnerDataToCopies(resourceId, ownerDataToPropogateToCopies);
        }
      }

      Base.commitTransaction();
      // Base.close();

    } catch (IllegalArgumentException iae) {
      LOGGER.warn("updateResource : Caught IllegalArgumentException", iae);
      Base.rollbackTransaction();
      return MessageResponseFactory.createInvalidRequestResponse();
    } catch (Throwable throwable) {
      LOGGER.warn("updateResource : Caught unexpected exception here", throwable);
      Base.rollbackTransaction();
      return MessageResponseFactory.createInternalErrorResponse();
    } finally {
      Base.close();
    }
    return MessageResponseFactory.createPutSuccessResponse("id", resourceData.getString("id"));
  }

  */
  
}