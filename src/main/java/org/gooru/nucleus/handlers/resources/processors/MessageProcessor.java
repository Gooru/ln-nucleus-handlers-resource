package org.gooru.nucleus.handlers.resources.processors;

import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.resources.processors.responses.transformers.ResponseTransformerBuilder;
import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.gooru.nucleus.handlers.resources.processors.exceptions.InvalidRequestException;
import org.gooru.nucleus.handlers.resources.processors.exceptions.InvalidUserException;
import org.gooru.nucleus.handlers.resources.processors.repositories.RepoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

class MessageProcessor implements Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
  private Message<Object> message;
  String userId;
  JsonObject prefs;
  JsonObject request;
  
  public MessageProcessor(Message<Object> message) {
    this.message = message;
  }
  
  @Override
  public MessageResponse process() {
    MessageResponse result;
    JsonObject eventData = null;
    try {
      if (message == null || !(message.body() instanceof JsonObject)) {
        LOGGER.error("Invalid message received, either null or body of message is not JsonObject ");
        throw new InvalidRequestException();
      }
      
      final String msgOp = message.headers().get(MessageConstants.MSG_HEADER_OP);
      userId = ((JsonObject)message.body()).getString(MessageConstants.MSG_USER_ID);
      if (userId == null) {
        LOGGER.error("Invalid user id passed. Not authorized.");
        throw new InvalidUserException();
      }
      prefs = ((JsonObject)message.body()).getJsonObject(MessageConstants.MSG_KEY_PREFS);
      request = ((JsonObject)message.body()).getJsonObject(MessageConstants.MSG_HTTP_BODY);
      switch (msgOp) {
      case MessageConstants.MSG_OP_RES_CREATE:
        result = processResourceCreate();
        eventData = generateEventForCreate(result.event());
        break;
      case MessageConstants.MSG_OP_RES_GET:
        result = processResourceGet();
        break;
      case MessageConstants.MSG_OP_RES_UPDATE:
        result = processResourceUpdate();
        break;
      default:
        LOGGER.error("Invalid operation type passed in, not able to handle");
        throw new InvalidRequestException();
      }
      return result;
    } catch (InvalidRequestException e) {
      LOGGER.warn("Caught Invalid Request exception while processing", e);
      return MessageResponseFactory.createInvalidRequestResponse();
    } catch (InvalidUserException e) {
      LOGGER.warn("Caught Invalid User while processing", e);
      return MessageResponseFactory.createForbiddenResponse();
    } catch (Throwable throwable) {
      LOGGER.warn("Caught unexpected exception here", throwable);
      return MessageResponseFactory.createInternalErrorResponse();
    }
  }

  private MessageResponse processResourceUpdate() {
    JsonObject inputData = ((JsonObject)message.body()).getJsonObject(MessageConstants.MSG_HTTP_BODY);
     return new RepoBuilder().buildResourceRepo(userId, prefs).updateResource(inputData);    
  }

  private MessageResponse processResourceGet() {
    // TODO Auto-generated method stub
    String resourceId = message.headers().get(MessageConstants.RESOURCE_ID);
    
    MessageResponse result = new RepoBuilder().buildResourceRepo(userId, prefs).getResourceById(resourceId);
    
    return result;
  }

  private MessageResponse processResourceCreate() {
    // TODO Auto-generated method stub
    JsonObject inputData = ((JsonObject)message.body()).getJsonObject(MessageConstants.MSG_HTTP_BODY);
    return new RepoBuilder().buildResourceRepo(userId, prefs).createResource(inputData);    
  }

  private JsonObject generateEventForCreate(JsonObject input) {
    JsonObject result = new JsonObject();
    result.put(MessageConstants.MSG_EVENT_NAME, MessageConstants.MSG_OP_EVT_RES_CREATE);
    result.put(MessageConstants.MSG_EVENT_BODY, input);
    return result;
  }

}
