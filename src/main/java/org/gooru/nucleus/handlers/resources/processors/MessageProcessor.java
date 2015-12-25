package org.gooru.nucleus.handlers.resources.processors;

import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.gooru.nucleus.handlers.resources.processors.exceptions.InvalidRequestException;
import org.gooru.nucleus.handlers.resources.processors.exceptions.InvalidUserException;
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
  public JsonObject process() {
    JsonObject result;
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
      // TODO: handle exception
    } catch (InvalidUserException e) {
      // TODO: handle exception
    }

    return null;
  }

  private JsonObject processResourceUpdate() {
    // TODO Auto-generated method stub
    String resourceId = message.headers().get(MessageConstants.RESOURCE_ID);
    
    return null;    
  }

  private JsonObject processResourceGet() {
    // TODO Auto-generated method stub
    String resourceId = message.headers().get(MessageConstants.RESOURCE_ID);
    
    return null;
  }

  private JsonObject processResourceCreate() {
    // TODO Auto-generated method stub
    
    return null;    
  }

}
