package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;
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
    if (context.resourceId() == null || context.resourceId().isEmpty()) {
      LOGGER.debug("checkSanity() failed");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
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
    AJEntityResource result = DBHelper.getInstance().getResourceById(context.resourceId());

    if (result != null) {
      JsonObject toReturn = new AJResponseJsonTransformer().transform(result.toJson(false, ResourceRepo.attributes));
      LOGGER.debug("FetchResourceHandler : Return Value : {} ", toReturn);
      return new ExecutionResult<>(MessageResponseFactory.createGetSuccessResponse(toReturn), ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    LOGGER.warn("FetchResourceHandler : Resource with id : {} : not found", context.resourceId());
    return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionResult.ExecutionStatus.FAILED);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
