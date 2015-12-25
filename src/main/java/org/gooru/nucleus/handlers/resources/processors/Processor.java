package org.gooru.nucleus.handlers.resources.processors;

import io.vertx.core.json.JsonObject;

public interface Processor {
  public JsonObject process();
}
