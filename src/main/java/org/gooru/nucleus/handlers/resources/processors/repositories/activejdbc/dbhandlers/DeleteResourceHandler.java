package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import java.sql.SQLException;

import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class DeleteResourceHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteResourceHandler.class);
  private final ProcessorContext context;
  private AJEntityResource resource;
  public DeleteResourceHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.resourceId() == null) {
      LOGGER.error("checkSanity() failed. ResourceID is null!");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    } else if ( context.resourceId().isEmpty()) {
      LOGGER.error("checkSanity() failed. ResourceID is empty!");
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Anonymous user denied this action"),
        ExecutionResult.ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() passed");
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {

    this.resource = DBHelper.getResourceDetailUpForDeletion(context.resourceId());
    if (this.resource == null) {
      LOGGER.error("validateRequest : deleteResource : Object to update is not found in DB! Input resource ID: {} ", context.resourceId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    if (!authorized()) {
      // Update is forbidden
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Need to be owner/collaborator on course/collection"),
        ExecutionResult.ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject resourceCopyIds = new JsonObject();
    try {
      DBHelper.setPGObject(this.resource, AJEntityResource.MODIFIER_ID, AJEntityResource.UUID_TYPE, this.context.userId());
      if (this.resource.hasErrors()) {
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(this.resource.errors()), ExecutionResult.ExecutionStatus.FAILED);
      }

      this.resource.set(AJEntityResource.IS_DELETED, true);
      if (!this.resource.save()) {
        LOGGER.info("error in delete resource, returning errors");
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(this.resource.errors()),
                ExecutionResult.ExecutionStatus.FAILED);
      }

      resourceCopyIds.put("id", this.resource.getId().toString());  // convert to String as we get UUID here
      String creator = this.resource.getString(AJEntityResource.CREATOR_ID);
      if (creator != null &&
          creator.equalsIgnoreCase(context.userId()) &&
          (this.resource.getString(AJEntityResource.ORIGINAL_CONTENT_ID) == null)) {
        LOGGER.info("original resource marked as deleted successfully");
        resourceCopyIds = DBHelper.getCopiesOfAResource(this.resource, context.resourceId());
        if (resourceCopyIds != null && !resourceCopyIds.isEmpty()) {
          int deletedResourceCopies = DBHelper.deleteResourceCopies(this.resource,context.resourceId());
          if (deletedResourceCopies >= 0) {
              return new ExecutionResult<>(MessageResponseFactory.createDeleteSuccessResponse(resourceCopyIds), ExecutionResult.ExecutionStatus.SUCCESSFUL);
          }
          else {
            return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Resource Copies were not deleted"), ExecutionResult.ExecutionStatus.FAILED);
          }
        }
      }

      return new ExecutionResult<>(MessageResponseFactory.createDeleteSuccessResponse(resourceCopyIds), ExecutionResult.ExecutionStatus.FAILED);
    } catch (IllegalArgumentException e) {
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

  private boolean authorized() {
    String creator = this.resource.getString(AJEntityResource.CREATOR_ID);
    String course = this.resource.getString(AJEntityResource.COURSE_ID);
    String collection = this.resource.getString(AJEntityResource.COLLECTION_ID);
    if (creator != null && creator.equalsIgnoreCase(context.userId()) && course == null && collection == null) {
      // Since the creator is modifying, and it is not part of any collection or course, then owner should be able to modify
      return true;
    } else {
      // The ownership and rights flows from either collection or course
      long authRecordCount;
      if (course != null) {
        // Check if user is one of collaborator on course, we do not need to check the owner as course owner should be resource creator
        authRecordCount =
          Base.count(AJEntityResource.TABLE_COURSE, AJEntityResource.AUTH_VIA_COURSE_FILTER, course, context.userId(), context.userId());
        if (authRecordCount >= 1) {
          // Auth check successful
          LOGGER.debug("Auth check successful based on course: {}", course);
          return true;
        }
      } else if (collection != null) {
        // Check if the user is one of collaborator on collection, we do not need to check about course now
        authRecordCount =
          Base.count(AJEntityResource.TABLE_COLLECTION, AJEntityResource.AUTH_VIA_COLLECTION_FILTER, collection, context.userId(), context.userId());
        if (authRecordCount >= 1) {
          LOGGER.debug("Auth check successful based on collection: {}", collection);
          return true;
        }
      }
    }

    return false;
  }





}
