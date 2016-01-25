package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.util.Map;
import java.util.StringJoiner;
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
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.info("invalid request received to create resource");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to create resource"),
        ExecutionStatus.FAILED);
    }

    JsonObject request = context.request();
    StringJoiner missingFields = new StringJoiner(", ");
    StringJoiner resourceIrrelevantFields = new StringJoiner(", ");
    String mapValue;
    for (Map.Entry<String, Object> entry : request) {
      mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
      if (AJEntityResource.NOTNULL_FIELDS.contains(entry.getKey())) {
        if (mapValue == null || mapValue.isEmpty()){
          missingFields.add(entry.getKey());
        }
      } else if (!AJEntityResource.RESOURCE_SPECIFIC_FIELDS.contains(entry.getKey())) {
        resourceIrrelevantFields.add(entry.getKey());
      }
    }
    
    // TODO: May be need to revisit this logic of validating fields and
    // returning error back for all validation failed in one go
    if (!missingFields.toString().isEmpty()) {
      LOGGER.info("request data validation failed for '{}'", missingFields.toString());
      return new ExecutionResult<>(
        MessageResponseFactory.createInvalidRequestResponse("mandatory field(s) '" + missingFields.toString() + "' missing"),
        ExecutionStatus.FAILED);
    }
    
    if (!resourceIrrelevantFields.toString().isEmpty()) {
      LOGGER.info("request data validation failed for '{}'", resourceIrrelevantFields.toString());
      return new ExecutionResult<>(
        MessageResponseFactory.createInvalidRequestResponse("Resource irrelevant fields are being sent in the request '" + resourceIrrelevantFields.toString() + "'"),
        ExecutionStatus.FAILED);
    }
    
    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    try {

      createRes = new AJEntityResource();
      DBHelper.populateEntityFromJson(context.request(), createRes);
      
//      createRes.set(AJEntityResource.RESOURCE_ID, "1a7f8890-c90e-4d1c-a73b-12e50bb54085");
      createRes.set(AJEntityResource.RESOURCE_ID, UUID.randomUUID());
      LOGGER.debug("validateRequest : Creating resource From MAP  : {}", createRes.toInsert());

      JsonObject resourceIdWithURLDuplicates = DBHelper.getDuplicateResourcesByURL(createRes.getString(AJEntityResource.RESOURCE_URL));
      if (resourceIdWithURLDuplicates != null && !resourceIdWithURLDuplicates.isEmpty()) {
        LOGGER.error("validateRequest : Duplicate resource URL found. So cannot go ahead with creating new resource! URL : {}", createRes.getString(AJEntityResource.RESOURCE_URL));
        LOGGER.error("validateRequest : Duplicate resources : {}", resourceIdWithURLDuplicates); 
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(resourceIdWithURLDuplicates), ExecutionResult.ExecutionStatus.FAILED);
      }

    } catch (SQLException |  IllegalArgumentException e) {
      LOGGER.error("CheckSanity : {} ", e);
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
        LOGGER.debug("executeRequest : Created resource ID: " + createRes.getString(AJEntityResource.RESOURCE_ID));
        return new ExecutionResult<>(MessageResponseFactory.createPostSuccessResponse("Location", createRes.getString(AJEntityResource.RESOURCE_ID)),ExecutionResult.ExecutionStatus.SUCCESSFUL);        
      } catch (DBException e) {
        // this can potentially mean errors in DB insert....
        // in odd case this might be due to UUID conflict...
        // check if this is PRIMARY KEY VIOLATION issue and retry...
        LOGGER.error("executeRequest : Create resource failed! {}", e);
        LOGGER.error("executeRequest : Create resource failed! {}", (e.getCause() != null) ? e.getCause().getMessage() : "Unknown error!");
        LOGGER.debug("executeRequest : Retry with new UUID value.");

        UUIDretries--;
        createRes.set(AJEntityResource.RESOURCE_ID, UUID.randomUUID());
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
