package org.gooru.nucleus.handlers.resources.tasks;

import org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 29/9/16.
 */
public final class ChainableTaskBuilder {

    private ChainableTaskBuilder() {
        throw new AssertionError();
    }

    public static ChainableTask buildCreateResourceTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        return new CreateResourceTask(name, eventBus, context, executor);
    }

    public static ChainableTask buildUpdateResourceTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        return new UpdateResourceTask(name, eventBus, context, executor);
    }

    public static ChainableTask buildFetchResourceTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        return new FetchResourceTask(name, eventBus, context, executor);
    }

    public static ChainableTask buildDeleteResourceTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        return new DeleteResourceTask(name, eventBus, context, executor);
    }

    public static ChainableTask buildDependencySatisfierNoOpTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor, JsonObject input, ResponseBuilder.Response output) {
        return new DepedencySatisfierNoOpTask(name, eventBus, context, executor, input, output);
    }
}
