package org.gooru.nucleus.handlers.resources.processors.responses.transformers;

import io.vertx.core.json.JsonObject;

public class ResponseTransformerBuilder {

  public ResponseTransformer build(JsonObject inputToTransform) {
    return new MessageBusResponseTransformer(inputToTransform);
  }

  public ResponseTransformer build(JsonObject inputToTransform, JsonObject eventToTransform) {
    return new MessageBusResponseTransformer(inputToTransform, eventToTransform);
  }
  
  public ResponseTransformer build(Throwable cause) {
    throw new RuntimeException("Not implemented!!!");
  }

}
