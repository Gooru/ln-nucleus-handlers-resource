package org.gooru.nucleus.handlers.resources.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 7/1/16.
 */
public class ProcessorContext {

    private final String userId;
    private final JsonObject session;
    private final JsonObject request;
    private final String resourceId;
    private final MultiMap requestHeaders;

    public ProcessorContext(String userId, JsonObject session, JsonObject request, String resourceId, MultiMap headers) {
        if (session == null || userId == null || session.isEmpty() || headers == null || headers.isEmpty()) {
            throw new IllegalStateException("Processor Context creation failed because of invalid values");
        }
        this.userId = userId;
        this.session = session.copy();
        this.request = request != null ? request.copy() : null;
        this.requestHeaders = headers;
        // resource id can be null in case of create and hence can't validate
        // them unless we know the op type also
        // Do not want to build dependency on op for this context to work and
        // hence is open ended. Worst case would be RTE, so beware
        this.resourceId = resourceId;
    }

    public String userId() {
        return this.userId;
    }

    public JsonObject session() {
        return this.session.copy();
    }

    public JsonObject request() {
        return this.request;
    }

    public String resourceId() {
        return this.resourceId;
    }

    public MultiMap requestHeaders() {
        return this.requestHeaders;
    }
}
