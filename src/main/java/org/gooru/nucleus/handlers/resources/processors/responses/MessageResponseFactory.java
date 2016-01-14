package org.gooru.nucleus.handlers.resources.processors.responses;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.javalite.activejdbc.Errors;

import java.util.Map;

/**
 * Created by ashish on 6/1/16.
 */
public class MessageResponseFactory {
  public static MessageResponse createInvalidRequestResponse() {
    return new MessageResponse.Builder().failed().setStatusBadRequest().build();
  }

  public static MessageResponse createForbiddenResponse() {
    return new MessageResponse.Builder().failed().setStatusForbidden().build();
  }

  public static MessageResponse createInternalErrorResponse() {
    return new MessageResponse.Builder().failed().setStatusInternalError().build();
  }

  public static MessageResponse createNotFoundResponse() {
    return new MessageResponse.Builder().failed().setStatusNotFound().build();
  }

  public static MessageResponse createValidationErrorResponse(Errors errs) {
    JsonObject errObject = new JsonObject();
    for (Map.Entry<String, String> entry : errs.entrySet()) {
      errObject.put(entry.getKey(), entry.getValue());
    }
    return new MessageResponse.Builder().validationFailed().setStatusBadRequest().setResponseBody(errObject).build();
  }

  public static MessageResponse createValidationErrorResponse(JsonObject errorJson) {
    return new MessageResponse.Builder().validationFailed().setStatusBadRequest().setResponseBody(errorJson).build();
  }

  public static MessageResponse createGetSuccessResponse(JsonObject responseBody) {
    return new MessageResponse.Builder().successful().setStatusOkay().setContentTypeJson().setResponseBody(responseBody).build();
  }

  public static MessageResponse createPutSuccessResponse(String key, String value) {
    return new MessageResponse.Builder().successful().setStatusNoOutput().setHeader(key, value).build();
  }

  public static MessageResponse createPostSuccessResponse(String key, String value) {
    return new MessageResponse.Builder().successful().setStatusCreated().setHeader(key, value).build();
  }

  public static MessageResponse createDeleteSuccessResponse(String key, String value) {
    return new MessageResponse.Builder().successful().setStatusNoOutput().setHeader(key, value).build();
  }

  public static MessageResponse createInvalidRequestResponse(String message) {
    return new MessageResponse.Builder().failed().setStatusBadRequest().setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message))
                                        .build();
  }

  public static MessageResponse createForbiddenResponse(String message) {
    return new MessageResponse.Builder().failed().setStatusForbidden().setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message))
                                        .build();
  }

  public static MessageResponse createInternalErrorResponse(String message) {
    return new MessageResponse.Builder().failed().setStatusInternalError()
                                        .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message)).build();
  }
}
