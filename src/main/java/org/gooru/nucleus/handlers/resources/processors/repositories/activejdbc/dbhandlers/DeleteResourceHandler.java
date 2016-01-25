package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import java.sql.SQLException;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class DeleteResourceHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteResourceHandler.class);
  private final ProcessorContext context;
  private AJEntityResource deleteRes;
  public DeleteResourceHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.resourceId() == null || context.resourceId().isEmpty()) {
      LOGGER.error("checkSanity() failed. ResourceID is null!");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }
    LOGGER.debug("checkSanity() passed");
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }
  
  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    deleteRes = DBHelper.getResourceById(context.resourceId());
    if (deleteRes == null) {
      LOGGER.error("validateRequest : deleteResource : Object to update is not found in DB! Input resource ID: {} ", context.resourceId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }
    
    if(deleteRes.getBoolean(AJEntityResource.IS_DELETED)) {
      LOGGER.info("resource {} is already deleted. Aborting", context.resourceId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Resource you are trying to delete is already deleted"),
              ExecutionResult.ExecutionStatus.FAILED);
    }
    
    if ( !deleteRes.getString(AJEntityResource.CREATOR_ID).equalsIgnoreCase(context.userId())){
      LOGGER.info("user is anonymous or not owner of Resource for delete. aborting");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }
      
    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      deleteRes.set(AJEntityResource.IS_DELETED, true);
      if (!deleteRes.save()) {
        LOGGER.info("error in delete resource, returning errors");
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(deleteRes.errors()),
                ExecutionResult.ExecutionStatus.FAILED);
      }
      
      LOGGER.info("original resource marked as deleted successfully");
      JsonObject resourceCopyIds = DBHelper.getCopiesOfAResource(context.resourceId());
      if (resourceCopyIds != null && !resourceCopyIds.isEmpty()) {
        int deletedResourceCopies = DBHelper.deleteResourceCopies(context.resourceId());
        if (deletedResourceCopies >= 0) {
          resourceCopyIds.put("id", deleteRes.getId());
          
          return new ExecutionResult<>(MessageResponseFactory.createDeleteSuccessResponse(resourceCopyIds), ExecutionResult.ExecutionStatus.SUCCESSFUL);
        }
        else {
          return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Resource Copies were not deleted"), ExecutionResult.ExecutionStatus.FAILED);
        }
      }
      return new ExecutionResult<>(MessageResponseFactory.createDeleteSuccessResponse(resourceCopyIds), ExecutionResult.ExecutionStatus.FAILED);
    } catch (IllegalArgumentException | SQLException e) {
      LOGGER.error("executeRequest : Update resource failed to propagate changes to other copies!", e);
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(e.getMessage()), ExecutionResult.ExecutionStatus.FAILED);
    } catch (Throwable t) {
      LOGGER.error("exception while delete resource", t);
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()), ExecutionResult.ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
 
}
