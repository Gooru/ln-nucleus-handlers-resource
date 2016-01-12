package org.gooru.nucleus.handlers.resources.processors;

import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;

import io.vertx.core.json.JsonObject;

public interface Processor {
  public MessageResponse process();
}
