package org.gooru.nucleus.handlers.resources.processors.repositories;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 29/12/15.
 */
public interface ResourceRepo {
 
    JsonObject getResourceById(String resourceId);
    JsonObject createResource(JsonObject resourceData);
    JsonObject updateResource(JsonObject resourceData);
}
