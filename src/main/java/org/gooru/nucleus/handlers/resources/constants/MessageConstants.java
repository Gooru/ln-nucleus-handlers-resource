package org.gooru.nucleus.handlers.resources.constants;

public final class MessageConstants {

    public static final String MSG_HEADER_OP = "mb.operation";
    public static final String MSG_HEADER_TOKEN = "session.token";
    public static final String MSG_OP_AUTH_WITH_PREFS = "auth.with.prefs";
    public static final String MSG_OP_STATUS = "mb.operation.status";
    public static final String MSG_KEY_PREFS = "prefs";
    public static final String MSG_OP_STATUS_SUCCESS = "success";
    public static final String MSG_OP_STATUS_ERROR = "error";
    public static final String MSG_OP_STATUS_VALIDATION_ERROR = "error.validation";
    public static final String MSG_USER_ANONYMOUS = "anonymous";
    public static final String MSG_USER_ID = "user_id";
    public static final String MSG_HTTP_STATUS = "http.status";
    public static final String MSG_HTTP_BODY = "http.body";
    public static final String MSG_HTTP_RESPONSE = "http.response";
    public static final String MSG_HTTP_ERROR = "http.error";
    public static final String MSG_HTTP_VALIDATION_ERROR = "http.validation.error";
    public static final String MSG_HTTP_HEADERS = "http.headers";
    public static final String MSG_MESSAGE = "message";

    // Event message structure : Operation + Body
    public static final String MSG_EVENT_NAME = "event.name";
    public static final String MSG_EVENT_BODY = "event.body";

    // Operation names: Also need to be updated in corresponding handlers
    public static final String MSG_OP_RES_GET = "resource.get";
    public static final String MSG_OP_RES_CREATE = "resource.create";
    public static final String MSG_OP_RES_UPDATE = "resource.update";
    public static final String MSG_OP_RES_DELETE = "resource.delete";
    // Containers for different responses
    public static final String RESP_CONTAINER_MBUS = "mb.container";
    public static final String RESP_CONTAINER_EVENT = "mb.event";

    public static final String RESOURCE_ID = "resourceId";

    // Event operation names:
    public static final String MSG_OP_EVT_RES_CREATE = "event.resource.create";
    public static final String MSG_OP_EVT_RES_UPDATE = "event.resource.update";
    public static final String MSG_OP_EVT_RES_DELETE = "event.resource.delete";

    private MessageConstants() {
        throw new AssertionError();
    }
}
