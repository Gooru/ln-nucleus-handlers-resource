package org.gooru.nucleus.handlers.resources.processors;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 7/1/16.
 */
public class ProcessorContext {

    final private String userId;
    final private JsonObject prefs;
    final private JsonObject request;
    final private String resourceId;

    public ProcessorContext(String userId, JsonObject prefs, JsonObject request, String resourceId) {
        if (prefs == null || userId == null || prefs.isEmpty()) {
            throw new IllegalStateException("Processor Context creation failed because of invalid values");
        }
        this.userId = userId;
        this.prefs = prefs.copy();
        this.request = request != null ? request.copy() : null;
        // resource id can be null in case of create and hence can't validate
        // them unless we know the op type also
        // Do not want to build dependency on op for this context to work and
        // hence is open ended. Worst case would be RTE, so beware
        this.resourceId = resourceId;
    }

    public String userId() {
        return this.userId;
    }

    public JsonObject prefs() {
        return this.prefs.copy();
    }

    public JsonObject request() {
        return this.request;
    }

    public String resourceId() {
        return this.resourceId;
    }

}
