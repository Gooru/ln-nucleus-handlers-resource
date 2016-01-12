package org.gooru.nucleus.handlers.resources.processors.repositories;

import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 29/12/15.
 */
public interface ResourceRepo {
 
    MessageResponse getResourceById(String resourceId);
    MessageResponse createResource(JsonObject resourceData);
    MessageResponse updateResource(JsonObject resourceData);
}
