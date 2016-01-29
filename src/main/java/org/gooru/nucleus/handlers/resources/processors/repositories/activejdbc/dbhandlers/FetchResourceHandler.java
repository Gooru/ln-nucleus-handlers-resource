package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FetchResourceHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchResourceHandler.class);
  private final ProcessorContext context;

  public FetchResourceHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.resourceId() == null) {
      LOGGER.error("checkSanity() failed. ResourceID is null!");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    } else if (context.resourceId().isEmpty()) {
      LOGGER.error("checkSanity() failed. ResourceID is empty!");
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    // we need some valid user -- anonymous will also do
    if (context.userId() == null || context.userId().isEmpty()) {
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Invalid user context"), ExecutionResult.ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() passed");
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    AJEntityResource result = DBHelper.getResourceById(context.resourceId());

    if (result != null) {
      return new ExecutionResult<>(MessageResponseFactory.createGetSuccessResponse(new JsonObject(
        new org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.formatter.JsonFormatterBuilder()
          .buildSimpleJsonFormatter(false, AJEntityResource.RESOURCE_SPECIFIC_FIELDS).toJson(result))), ExecutionResult.ExecutionStatus.SUCCESSFUL);

    }

    LOGGER.warn("FetchResourceHandler : Resource with id : {} : not found", context.resourceId());
    return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionResult.ExecutionStatus.FAILED);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
