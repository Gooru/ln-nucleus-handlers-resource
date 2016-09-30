package org.gooru.nucleus.handlers.resources.bootstrap.utilities;

import org.gooru.nucleus.handlers.resources.constants.MessageConstants;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public final class RequestBuilder {

    private RequestBuilder() {
        throw new AssertionError();
    }

    public static JsonObject buildEmptyRequest() {
        return createDefaultRequestWithSpecifiedPayload(new JsonObject());
    }

    public static JsonObject buildCreateRequest() {
        /*
        {
            "title": "Resource Create Integration test",
            "url": "http://integrationtest.example.com",
            "narration": "This resource is used to do integration test for resource",
            "description": "This resource is used to do integration test for resource",
            "content_subformat": "webpage_resource",
            "metadata": {
                "educational_Use": [20, 24]
            },
            "depth_of_knowledge": [123, 124],
            "thumbnail": "3b11d618-6091-4c3b-8eba-badcd6f4dfa3.png"
        }
         */
        JsonObject httpBody = new JsonObject().put("title", "Resource Create Integration test")
            .put("url", "http://integrationtest.example.com")
            .put("narration", "This resource is used to do integration test for resource")
            .put("content_subformat", "webpage_resource")
            .put("metadata", new JsonObject().put("educational_use", new JsonArray("[20, 24]")))
            .put("thumbnail", "3b11d618-6091-4c3b-8eba-badcd6f4dfa3.png");
        return createDefaultRequestWithSpecifiedPayload(httpBody);
    }

    public static JsonObject buildUpdateRequest() {
        JsonObject httpBody = new JsonObject().put("title", "Updated:Resource Create Integration test")
            .put("narration", "Updated:This resource is used to do integration test for resource")
            .put("thumbnail", "3b11d618-6091-4c3b-8eba-badcd6f4dfa3.gif");
        return createDefaultRequestWithSpecifiedPayload(httpBody);
    }

    private static JsonObject createDefaultRequestWithSpecifiedPayload(JsonObject httpBody) {
        JsonObject request = new JsonObject();
        JsonObject prefs = new JsonObject().put(TestConstants.EMAIL, TestConstants.EMAIL_DEFAULT_VALUE);
        request.put(MessageConstants.MSG_HTTP_BODY, httpBody);
        request.put(MessageConstants.MSG_KEY_PREFS, prefs);
        request.put(MessageConstants.MSG_USER_ID, TestConstants.USER_ID_DEFAULT_VALUE);
        request.put(MessageConstants.MSG_HEADER_TOKEN, TestConstants.HEADER_TOKEN);
        return request;
    }
}