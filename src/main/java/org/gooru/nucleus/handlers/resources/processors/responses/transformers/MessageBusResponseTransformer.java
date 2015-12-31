package org.gooru.nucleus.handlers.resources.processors.responses.transformers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.resources.constants.HttpConstants;
import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MessageBusResponseTransformer implements ResponseTransformer {

  static final Logger LOG = LoggerFactory.getLogger(ResponseTransformer.class);
  private JsonObject inputToTransform;
  private JsonObject inputEventData;
  private JsonObject transformedOutput;
  private boolean transformed = false;

  public MessageBusResponseTransformer(JsonObject inputToTransform) {
    this.inputToTransform = inputToTransform;
    if (inputToTransform == null) {
      LOG.error("Invalid or null JsonObject for initialization");
      throw new IllegalArgumentException("Invalid or null JsonObject for initialization");
    }
  }
  
  public MessageBusResponseTransformer(JsonObject inputToTransform, JsonObject inputEventData) {
    this.inputToTransform = inputToTransform;
    if (inputToTransform == null) {
      LOG.error("Invalid or null JsonObject for initialization");
      throw new IllegalArgumentException("Invalid or null JsonObject for initialization");
    }
    if (inputEventData != null) {
      this.inputEventData = inputEventData;
    }
  }

  @Override
  public JsonObject transform() {
    processTransformation();
    return this.transformedOutput;
  }

  private void processTransformation() {
    if (!this.transformed) {
      transformedOutput = new JsonObject();
      transformedOutput.put(MessageConstants.MSG_OP_STATUS, MessageConstants.MSG_OP_STATUS_SUCCESS);
      transformedOutput.put(MessageConstants.RESP_CONTAINER_MBUS, getTransformedResponse());
      
      if (this.inputEventData != null) {
        transformedOutput.put(MessageConstants.RESP_CONTAINER_EVENT, this.inputEventData);
      }
      this.transformed = true;
    }
  }
    
  private JsonObject getTransformedResponse() {
    JsonObject transformedResponse = new JsonObject();
    transformedResponse.put(MessageConstants.MSG_HTTP_STATUS, HttpConstants.HttpStatus.SUCCESS.getCode());
    JsonObject headers = getHttpHeaders();
    if (headers != null) {
      transformedResponse.put(MessageConstants.MSG_HTTP_HEADERS, headers);
    } else {
      transformedResponse.put(MessageConstants.MSG_HTTP_HEADERS, new JsonObject());
    }
    JsonObject body = getHttpBody();
    transformedResponse.put(MessageConstants.MSG_HTTP_BODY, body);
    return transformedResponse;
  }

  private JsonObject getHttpHeaders() {
    // TODO: Do we want to send out any caching headers as well?
    // We never return null from this method
    return new JsonObject().put(HttpConstants.HEADER_CONTENT_TYPE, HttpConstants.CONTENT_TYPE_JSON);
  }

  private JsonObject getHttpBody() {
    // We never return null from this method
    if (inputToTransform != null ) {
      return new JsonObject().put(MessageConstants.MSG_HTTP_RESPONSE, inputToTransform);
    } else {
      return new JsonObject().put(MessageConstants.MSG_HTTP_RESPONSE, new JsonObject());
    }
  }

}
