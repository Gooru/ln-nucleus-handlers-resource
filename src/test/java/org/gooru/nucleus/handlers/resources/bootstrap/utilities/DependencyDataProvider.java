package org.gooru.nucleus.handlers.resources.bootstrap.utilities;

import io.vertx.core.json.JsonObject;

/**
 * @author ashish on 30/9/16.
 */
public interface DependencyDataProvider {

    String resourceId();

    default JsonObject resource() {
        return null;
    }
}
