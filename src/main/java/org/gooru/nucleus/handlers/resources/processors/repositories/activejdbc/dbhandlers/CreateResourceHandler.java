package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import java.sql.SQLException;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 11/1/16.
 */
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
      DBHelper.getInstance().populateEntityFromJson(context.request(), createRes);
      
      LOGGER.debug("validateRequest : Creating resource From MAP  : {}", createRes.toInsert());
      
      JsonObject resourceIdWithURLDuplicates = DBHelper.getInstance().getDuplicateResourcesByURL(createRes.getString(ResourceRepo.RESOURCE_URL));
      if (resourceIdWithURLDuplicates != null && !resourceIdWithURLDuplicates.isEmpty()) {
        LOGGER.debug("validateRequest : URL Exists <TBD> so cannot go ahead!");
        // <TBD>...what type of message to post back...
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(resourceIdWithURLDuplicates), ExecutionResult.ExecutionStatus.FAILED);
      }

    } catch (SQLException e) {
      LOGGER.warn("CheckSanity : {} ", e );
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    } catch (IllegalArgumentException iae) {
      LOGGER.warn("CheckSanity : {} ", iae );
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }
    
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);    
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    if (!createRes.insert()) {
      LOGGER.debug("executeRequest : Create resource failed! ");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(createRes.errors()), ExecutionResult.ExecutionStatus.FAILED);    
    } 
    
    LOGGER.debug("executeRequest : Created resource ID: " + createRes.getString("id") );
    return new ExecutionResult<>(MessageResponseFactory.createPostSuccessResponse(ResourceRepo.RESOURCE_ID, createRes.getString(ResourceRepo.RESOURCE_ID)), ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
      
}
