package org.gooru.nucleus.handlers.resources.tasks;

import org.gooru.nucleus.handlers.resources.bootstrap.utilities.DependencyDataProvider;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 29/9/16.
 */
public interface ChainableTask {
    void execute(TestContext context, EventBus eventBus, ChainableTaskExecutor executor);

    Status status();

    JsonObject input();

    ResponseBuilder.Response outcome();

    String name();

    void setProvider(DependencyDataProvider provider);

    enum Status {
        PENDING,
        RUNNING,
        COMPLETED
    }
}
