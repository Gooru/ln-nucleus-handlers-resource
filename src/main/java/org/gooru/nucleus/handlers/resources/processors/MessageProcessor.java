package org.gooru.nucleus.handlers.resources.processors;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.gooru.nucleus.handlers.resources.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MessageProcessor implements Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
  private final Message<Object> message;
  private String userId;
  private JsonObject prefs;
  private JsonObject request;

  public MessageProcessor(Message<Object> message) {
    this.message = message;
  }

  @Override
  public MessageResponse process() {
    MessageResponse result;
    try {
      // Validate the message itself
      ExecutionResult<MessageResponse> validateResult = validateAndInitialize();
      if (validateResult.isCompleted()) {
        return validateResult.result();
      }

      final String msgOp = message.headers().get(MessageConstants.MSG_HEADER_OP);
      switch (msgOp) {
        case MessageConstants.MSG_OP_RES_CREATE:
          result = processResourceCreate();
          break;
        case MessageConstants.MSG_OP_RES_GET:
          result = processResourceGet();
          break;
        case MessageConstants.MSG_OP_RES_UPDATE:
          result = processResourceUpdate();
          break;
        case MessageConstants.MSG_OP_RES_DELETE:
          result = processResourceDelete();
          break;
        default:
          LOGGER.error("Invalid operation type passed in, not able to handle");
          return MessageResponseFactory.createInvalidRequestResponse("Invalid resource id");
      }
      return result;
    } catch (Throwable e) {
      LOGGER.error("Unhandled exception in processing", e);
      return MessageResponseFactory.createInternalErrorResponse();
    }
  }

  private MessageResponse processResourceDelete() {
    ProcessorContext context = createContext();
    if (context.resourceId() == null || context.resourceId().isEmpty()) {
      LOGGER.error("Invalid request, resource id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid resource id");
    }
     return new RepoBuilder().buildResourceRepo(context).deleteResource();
  }
  private MessageResponse processResourceUpdate() {
    ProcessorContext context = createContext();
    if (context.resourceId() == null || context.resourceId().isEmpty()) {
      LOGGER.error("Invalid request, resource id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid resource id");
    }
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.error("Invalid request, json not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid Json");
    }
    return new RepoBuilder().buildResourceRepo(context).updateResource();
  }

  private MessageResponse processResourceGet() {
    ProcessorContext context = createContext();
    if (context.resourceId() == null || context.resourceId().isEmpty()) {
      LOGGER.error("Invalid request, resource id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid resource id");
    }
    return new RepoBuilder().buildResourceRepo(context).fetchResource(); // TODO Auto-generated method stub
  }

  private MessageResponse processResourceCreate() {
    ProcessorContext context = createContext();

    return new RepoBuilder().buildResourceRepo(context).createResource();
  }

  private ProcessorContext createContext() {
    String resourceId = message.headers().get(MessageConstants.RESOURCE_ID);
    return new ProcessorContext(userId, prefs, request, resourceId);
  }

  private ExecutionResult<MessageResponse> validateAndInitialize() {
    if (message == null || !(message.body() instanceof JsonObject)) {
      LOGGER.error("Invalid message received, either null or body of message is not JsonObject ");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    userId = ((JsonObject) message.body()).getString(MessageConstants.MSG_USER_ID);
    if (userId == null) {
      LOGGER.error("Invalid user id passed. Not authorized.");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }
    prefs = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_KEY_PREFS);
    request = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_HTTP_BODY);

    if (prefs == null || prefs.isEmpty()) {
      LOGGER.error("Invalid preferences obtained, probably not authorized properly");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    if (request == null) {
      LOGGER.error("Invalid JSON payload on Message Bus");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    // All is well, continue processing
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }
}
