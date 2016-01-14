package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;


class UpdateResourceHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateResourceHandler.class);
  private static boolean isOwner = false;
  private final ProcessorContext context;
  private JsonObject ownerDataToPropogateToCopies;
  private AJEntityResource updateRes;

  public UpdateResourceHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.debug("checkSanity() failed with invalid json. ");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }
    if (context.resourceId() == null || context.resourceId().isEmpty()) {
      LOGGER.debug("checkSanity() failed with invalid resourceid. ");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() passed");
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    try {
      // fetch resource from DB based on Id received
      AJEntityResource fetchDBResourceData = DBHelper.getInstance().getResourceById(context.resourceId());
      if (fetchDBResourceData == null) {
        LOGGER.error("validateRequest : updateResource : Object to update is not found in DB! Input resource ID: {} ", context.resourceId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionResult.ExecutionStatus.FAILED);
      }
      String originalCreator = fetchDBResourceData.getString(ResourceRepo.ORIGINAL_CREATOR_ID);
      LOGGER.debug("validateRequest : updateResource : Original creator from DB = {}.", originalCreator);

      if ((originalCreator != null) && !originalCreator.isEmpty()) {
        isOwner = originalCreator.equalsIgnoreCase(context.userId());
      }
      LOGGER.debug("validateRequest : updateResource : Ok! So, who is trying to update content? {}.", (isOwner) ? "owner" : "someone else");

      String mapValue;

      // now mandatory field checks on input resource data and if contains
      // owner-Specific editable fields
      // compare input value and collect only changed attributes in new model
      // that we will use to update
      updateRes = new AJEntityResource();
      updateRes.set(ResourceRepo.RESOURCE_ID, context.resourceId());

      LOGGER.debug("validateRequest updateResource : Iterate through the input Json now.");

      for (Map.Entry<String, Object> entry : context.request()) {
        LOGGER.debug("validateRequest updateResource : checking the key & values..before collection. Key: {}", entry.getKey());

        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if (Arrays.asList(ResourceRepo.NOTNULL_FIELDS).contains(entry.getKey())) {
          if (mapValue == null) {
            LOGGER.error("validateRequest Failed to update resource. Field : {} : is mandatory field and cannot be null.", entry.getKey());
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue())),
              ExecutionResult.ExecutionStatus.FAILED);
          }
        }

        // mandatory and owner specific items may be overlapping...so do a
        // separate check not as ELSE condition
        if (!isOwner && Arrays.asList(ResourceRepo.OWNER_SPECIFIC_FIELDS).contains(entry.getKey())) {
          // LOGGER.debug("validateRequest updateResource : Not owner but changing owner specific fields?");
          LOGGER.error("Error updating resource. Field: {} : can be updated only by owner of the resource.", entry.getKey());
          return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionResult.ExecutionStatus.FAILED);
        } else if (isOwner && Arrays.asList(ResourceRepo.OWNER_SPECIFIC_FIELDS).contains(entry.getKey())) {
          // collect the DB fields to update for owner specific fields across
          // all
          // copies of this resource
          LOGGER.debug("updateResource : need to propagate this : {} : to other resources. ", entry.getKey());
          if (ownerDataToPropogateToCopies == null) {
            ownerDataToPropogateToCopies = new JsonObject();
          }

          ownerDataToPropogateToCopies.put(entry.getKey(), entry.getValue());
        }

        // collect the attributes and values in the model.
        if (entry.getKey().equalsIgnoreCase(ResourceRepo.CONTENT_FORMAT)) {
          if (mapValue == null || mapValue.isEmpty()) {
            LOGGER.error("updateResource : content format is null! : {} ", entry.getKey());
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue())),
              ExecutionResult.ExecutionStatus.FAILED);
          } else {
            if (!mapValue.equalsIgnoreCase(ResourceRepo.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
              return new ExecutionResult<>(
                MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue())),
                ExecutionResult.ExecutionStatus.FAILED);
            } else {
              PGobject contentFormat = new PGobject();
              contentFormat.setType(ResourceRepo.CONTENT_FORMAT_TYPE);
              contentFormat.setValue(mapValue);
              updateRes.set(entry.getKey(), contentFormat);
            }
          }
        } else if (entry.getKey().equalsIgnoreCase(ResourceRepo.CONTENT_SUBFORMAT)) {
          if (mapValue == null || mapValue.isEmpty()) {
            LOGGER.error("updateResource : content subformat is null! : {} ", entry.getKey());
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue())),
              ExecutionResult.ExecutionStatus.FAILED);
          } else {
            if (!mapValue.contains(ResourceRepo.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
              return new ExecutionResult<>(
                MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue())),
                ExecutionResult.ExecutionStatus.FAILED);
            } else {
              PGobject contentSubformat = new PGobject();
              contentSubformat.setType(ResourceRepo.CONTENT_SUBFORMAT_TYPE);
              contentSubformat.setValue(mapValue);
              updateRes.set(entry.getKey(), contentSubformat);
            }
          }
        } else if (Arrays.asList(ResourceRepo.JSONB_FIELDS).contains(entry.getKey())) {
          if (Arrays.asList(ResourceRepo.NOTNULL_FIELDS).contains(entry.getKey())) {
            if (mapValue == null || mapValue.isEmpty()) {
              LOGGER.error("updateResource : mandatory fields is null! : {} ", entry.getKey());
              return new ExecutionResult<>(
                MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue())),
                ExecutionResult.ExecutionStatus.FAILED);
            } else {
              PGobject jsonbFields = new PGobject();
              jsonbFields.setType(ResourceRepo.JSONB_FORMAT);
              jsonbFields.setValue(mapValue);
              updateRes.set(entry.getKey(), jsonbFields);
            }
          }

        } else {
          if (mapValue == null || mapValue.isEmpty()) {
            LOGGER.error("updateResource : mandatory fields in else is null! : {} ", entry.getKey());
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue())),
              ExecutionResult.ExecutionStatus.FAILED);
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
    } catch (SQLException e) {
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(e.getMessage()), ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    if (updateRes != null) {
      if (!updateRes.save()) {
        LOGGER.debug("executeRequest : Update resource failed! ");
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(updateRes.errors()),
          ExecutionResult.ExecutionStatus.FAILED);
      } else {
        if (ownerDataToPropogateToCopies != null) {
          try {
            DBHelper.getInstance().updateOwnerDataToCopies(context.resourceId(), ownerDataToPropogateToCopies, context.userId());
            LOGGER.debug("executeRequest : Updated resource ID: " + updateRes.getString(ResourceRepo.RESOURCE_ID));
            return new ExecutionResult<>(MessageResponseFactory.createPutSuccessResponse("Location", updateRes.getString(ResourceRepo.RESOURCE_ID)),
              ExecutionResult.ExecutionStatus.SUCCESSFUL);
          } catch (IllegalArgumentException | SQLException e) {
            return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(e.getMessage()), ExecutionResult.ExecutionStatus.FAILED);
          }
        }
      }
    }
    return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(updateRes.errors()), ExecutionResult.ExecutionStatus.FAILED);

  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
}
