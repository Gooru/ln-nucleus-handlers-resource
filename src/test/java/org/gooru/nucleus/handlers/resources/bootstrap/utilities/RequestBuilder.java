package org.gooru.nucleus.handlers.resources.bootstrap.utilities;

import java.util.UUID;

import org.gooru.nucleus.handlers.resources.constants.MessageConstants;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public final class RequestBuilder {

    private RequestBuilder() {
        throw new AssertionError();
    }

    public static JsonObject buildEmptyDefaultRequest() {
        return createDefaultRequestWithSpecifiedPayload(new JsonObject());
    }

    public static JsonObject buildEmptyAnonymousRequest() {
        return createAnonymousRequestWithSpecifiedPayload(new JsonObject());
    }

    public static JsonObject buildEmptyUnauthorizedRequest() {
        return createUnauthorizedRequestWithSpecifiedPayload(new JsonObject());
    }

    public static JsonObject buildCreateRequest() {
        JsonObject httpBody = generateDefaultCreateRequestData();
        return createDefaultRequestWithSpecifiedPayload(httpBody);
    }

    public static JsonObject buildCreateRequestForAnonymousUser() {
        JsonObject httpBody = generateDefaultCreateRequestData();
        return createAnonymousRequestWithSpecifiedPayload(httpBody);
    }

    public static JsonObject buildUpdateRequestForResourceRef() {
        JsonObject httpBody = generateResourceRefUpdateRequestData();
        return createDefaultRequestWithSpecifiedPayload(httpBody);
    }

    public static JsonObject buildUpdateRequest() {
        JsonObject httpBody = generateDefaultUpdateRequestData();
        return createDefaultRequestWithSpecifiedPayload(httpBody);
    }

    public static JsonObject buildUpdateAnonymousRequest() {
        JsonObject httpBody = generateDefaultUpdateRequestData();
        return createAnonymousRequestWithSpecifiedPayload(httpBody);
    }

    public static JsonObject buildUpdateUnauthorizedRequest() {
        JsonObject httpBody = generateDefaultUpdateRequestData();
        return createUnauthorizedRequestWithSpecifiedPayload(httpBody);
    }

    private static JsonObject generateResourceRefUpdateRequestData() {
        JsonObject taxonomy = new JsonObject("{\"C4.K12.SS-D3-GES-04\": {\"code\": \"D3.1.9-12\", \"title\": \"Gather"
            + " relevant information from multiple sources representing a wide range of views while using the origin,"
            + " authority, structure, context, and corroborative value of the sources to guide the selection.\", "
            + "\"description\": \"\", \"parent_title\": \"Social Sciences\", \"framework_code\": \"C4\"}, "
            + "\"C4.K12.SS-D2-HIST-04\": {\"code\": \"D2.His.1.9-12\", \"title\": \"Evaluate how historical events "
            + "and developments were shaped by unique circumstances of time and place as well as broader historical "
            + "contexts.\", \"description\": \"\", \"parent_title\": \"Social Sciences\", \"framework_code\": "
            + "\"C4\"}, \"C4.K12.SS-D2-HIST-48\": {\"code\": \"D2.His.14.9-12\", \"title\": \"Analyze multiple and "
            + "complex causes and effects of events in the past.\", \"description\": \"\", \"parent_title\": \"Social"
            + " Sciences\", \"framework_code\": \"C4\"}}");

        return new JsonObject().put("narration", "This resource is used to do integration test for resource - update")
            .put("metadata",
                new JsonObject().put("educational_use", new JsonArray("[21, 22]"))
                    .put("audience", new JsonArray("[3,4]"))).put("taxonomy", taxonomy)
            .put("thumbnail", "3b11d618-6091-4c3b-8eba-badcd6f4afd3.png");

    }

    private static JsonObject generateDefaultUpdateRequestData() {
        JsonObject taxonomy = new JsonObject("{\"C4.K12.SS-D3-GES-04\": {\"code\": \"D3.1.9-12\", \"title\": \"Gather"
            + " relevant information from multiple sources representing a wide range of views while using the origin,"
            + " authority, structure, context, and corroborative value of the sources to guide the selection.\", "
            + "\"description\": \"\", \"parent_title\": \"Social Sciences\", \"framework_code\": \"C4\"}, "
            + "\"C4.K12.SS-D2-HIST-04\": {\"code\": \"D2.His.1.9-12\", \"title\": \"Evaluate how historical events "
            + "and developments were shaped by unique circumstances of time and place as well as broader historical "
            + "contexts.\", \"description\": \"\", \"parent_title\": \"Social Sciences\", \"framework_code\": "
            + "\"C4\"}, \"C4.K12.SS-D2-HIST-48\": {\"code\": \"D2.His.14.9-12\", \"title\": \"Analyze multiple and "
            + "complex causes and effects of events in the past.\", \"description\": \"\", \"parent_title\": \"Social"
            + " Sciences\", \"framework_code\": \"C4\"}}");

        String url = String.format("http://%s.integrationtest.example.com", UUID.randomUUID().toString());
        return new JsonObject().put("title", "Resource Create Integration test - update").put("url", url)
            .put("narration", "This resource is used to do integration test for resource - update")
            .put("content_subformat", "webpage_resource")
            .put("metadata",
                new JsonObject().put("educational_use", new JsonArray("[21, 22]"))
                    .put("audience", new JsonArray("[3,4]"))).put("taxonomy", taxonomy)
            .put("thumbnail", "3b11d618-6091-4c3b-8eba-badcd6f4afd3.png");

        //        return new JsonObject().put("title", "Updated:Resource Create Integration test")
        //            .put("narration", "Updated:This resource is used to do integration test for resource")
        //            .put("thumbnail", "3b11d618-6091-4c3b-8eba-badcd6f4dfa3.gif");
    }

    private static JsonObject createDefaultRequestWithSpecifiedPayload(JsonObject httpBody) {
        JsonObject request = new JsonObject();
        JsonObject prefs = new JsonObject().put(TestConstants.EMAIL, TestConstants.EMAIL_DEFAULT_VALUE);
        request.put(MessageConstants.MSG_HTTP_BODY, httpBody).put(MessageConstants.MSG_KEY_PREFS, prefs)
            .put(MessageConstants.MSG_USER_ID, TestConstants.USER_ID_DEFAULT_VALUE)
            .put(MessageConstants.MSG_HEADER_TOKEN, TestConstants.HEADER_TOKEN);
        return request;
    }

    private static JsonObject createAnonymousRequestWithSpecifiedPayload(JsonObject httpBody) {
        JsonObject request = new JsonObject();
        JsonObject prefs = new JsonObject().put(TestConstants.EMAIL, TestConstants.EMAIL_DEFAULT_VALUE);
        request.put(MessageConstants.MSG_HTTP_BODY, httpBody).put(MessageConstants.MSG_KEY_PREFS, prefs)
            .put(MessageConstants.MSG_USER_ID, TestConstants.ANONYMOUS)
            .put(MessageConstants.MSG_HEADER_TOKEN, TestConstants.HEADER_TOKEN);
        return request;
    }

    private static JsonObject createUnauthorizedRequestWithSpecifiedPayload(JsonObject httpBody) {
        JsonObject request = new JsonObject();
        JsonObject prefs = new JsonObject().put(TestConstants.EMAIL, TestConstants.EMAIL_DEFAULT_VALUE);
        request.put(MessageConstants.MSG_HTTP_BODY, httpBody).put(MessageConstants.MSG_KEY_PREFS, prefs)
            .put(MessageConstants.MSG_USER_ID, UUID.randomUUID().toString())
            .put(MessageConstants.MSG_HEADER_TOKEN, TestConstants.HEADER_TOKEN);
        return request;
    }

    private static JsonObject generateDefaultCreateRequestData() {
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
        JsonObject taxonomy = new JsonObject("{\"C3.K12.SS-D3-GES-04\": {\"code\": \"D3.1.9-12\", \"title\": \"Gather"
            + " relevant information from multiple sources representing a wide range of views while using the origin,"
            + " authority, structure, context, and corroborative value of the sources to guide the selection.\", "
            + "\"description\": \"\", \"parent_title\": \"Social Sciences\", \"framework_code\": \"C3\"}, "
            + "\"C3.K12.SS-D2-HIST-04\": {\"code\": \"D2.His.1.9-12\", \"title\": \"Evaluate how historical events "
            + "and developments were shaped by unique circumstances of time and place as well as broader historical "
            + "contexts.\", \"description\": \"\", \"parent_title\": \"Social Sciences\", \"framework_code\": "
            + "\"C3\"}, \"C3.K12.SS-D2-HIST-48\": {\"code\": \"D2.His.14.9-12\", \"title\": \"Analyze multiple and "
            + "complex causes and effects of events in the past.\", \"description\": \"\", \"parent_title\": \"Social"
            + " Sciences\", \"framework_code\": \"C3\"}}");

        String url = String.format("http://%s.integrationtest.example.com", UUID.randomUUID().toString());
        return new JsonObject().put("title", "Resource Create Integration test").put("url", url)
            .put("narration", "This resource is used to do integration test for resource")
            .put("content_subformat", "webpage_resource").put("metadata",
                new JsonObject().put("educational_use", new JsonArray("[20, 24]"))
                    .put("audience", new JsonArray("[1,2,3,4]"))).put("taxonomy", taxonomy)
            .put("thumbnail", "3b11d618-6091-4c3b-8eba-badcd6f4dfa3.png");
    }
}

