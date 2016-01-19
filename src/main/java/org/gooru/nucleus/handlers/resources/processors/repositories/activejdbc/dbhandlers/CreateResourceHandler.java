package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.ResourceEntityConstants;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.UUID;

class CreateResourceHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateResourceHandler.class);
  private final ProcessorContext context;
  private AJEntityResource createRes;

  public CreateResourceHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    try {

      createRes = new AJEntityResource();
      DBHelper.populateEntityFromJson(context.request(), createRes);
      
      createRes.set("id", UUID.randomUUID());
      LOGGER.debug("validateRequest : Creating resource From MAP  : {}", createRes.toInsert());

      JsonObject resourceIdWithURLDuplicates = DBHelper.getDuplicateResourcesByURL(createRes.getString(ResourceEntityConstants.RESOURCE_URL));
      if (resourceIdWithURLDuplicates != null && !resourceIdWithURLDuplicates.isEmpty()) {
        LOGGER.error("validateRequest : Duplicate resource URL found. So cannot go ahead with creating new resource! URL : {}", createRes.getString(ResourceEntityConstants.RESOURCE_URL));
        LOGGER.error("validateRequest : Duplicate resources : {}", resourceIdWithURLDuplicates); 
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(resourceIdWithURLDuplicates), ExecutionResult.ExecutionStatus.FAILED);
      }

    } catch (SQLException e) {
      LOGGER.error("CheckSanity : {} ", e);
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    } catch (IllegalArgumentException iae) {
      LOGGER.error("CheckSanity : {} ", iae);
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {

    int UUIDretries = DBHelper.NUM_RETRIES;
    
    while (UUIDretries != 0) {
      try {
        if (!createRes.insert()) {
          if (createRes.hasErrors()) {
            LOGGER.error("executeRequest : Create resource failed for input object. Errors: {}", createRes.errors());
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(createRes.errors()), ExecutionResult.ExecutionStatus.FAILED);
          } else {
            LOGGER.error("executeRequest : Create resource failed for input object: {}", context.request());
            return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(), ExecutionResult.ExecutionStatus.FAILED);
          }
        }
        
        // successful...
        LOGGER.debug("executeRequest : Created resource ID: " + createRes.getString("id"));
        return new ExecutionResult<>(MessageResponseFactory.createPostSuccessResponse("Location", createRes.getString(ResourceEntityConstants.RESOURCE_ID)),ExecutionResult.ExecutionStatus.SUCCESSFUL);        
      } catch (DBException e) {
        // this can potentially mean errors in DB insert....
        // in odd case this might be due to UUID conflict...
        // check if this is PRIMARY KEY VIOLATION issue and retry...
        LOGGER.error("executeRequest : Create resource failed! {}", e);
        LOGGER.debug("executeRequest : Retry with new UUID value.");

        UUIDretries--;
        createRes.set("id", UUID.randomUUID());
        //return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(), ExecutionResult.ExecutionStatus.FAILED);
      }      
    }
    
    // failed after retries, too...so give up now
    return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(), ExecutionResult.ExecutionStatus.FAILED);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
