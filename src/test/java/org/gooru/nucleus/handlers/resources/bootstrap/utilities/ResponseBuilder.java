package org.gooru.nucleus.handlers.resources.bootstrap.utilities;

import org.gooru.nucleus.handlers.resources.constants.MessageConstants;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class ResponseBuilder {

    public Response buildResponse(AsyncResult<Message<Object>> reply) {
        JsonObject result = (JsonObject) reply.result().body();
        return new Response() {
            @Override
            public JsonObject httpBody() {
                return result.getJsonObject(MessageConstants.MSG_HTTP_BODY);
            }

            @Override
            public JsonObject httpHeaders() {
                return result.getJsonObject(MessageConstants.MSG_HTTP_HEADERS);
            }

            @Override
            public int httpStatus() {
                return result.getInteger(MessageConstants.MSG_HTTP_STATUS);
            }
        };
    }

    public interface Response {
        JsonObject httpBody();

        JsonObject httpHeaders();

        int httpStatus();
    }
}