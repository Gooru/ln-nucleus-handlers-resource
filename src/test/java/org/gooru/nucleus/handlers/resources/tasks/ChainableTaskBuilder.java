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

    public static ChainableTask buildCreateResourceAnonymousTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        return new CreateResourceAnonymousTask(name, eventBus, context, executor);
    }

    public static ChainableTask buildFetchResourceNonExistingTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        return new FetchResourceNonExistingTask(name, eventBus, context, executor);
    }

    public static ChainableTask buildUpdateResourceAnonymousTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        return new UpdateResourceAnonymousTask(name, eventBus, context, executor);
    }

    public static ChainableTask buildUpdateResourceUnauthorizedTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        return new UpdateResourceUnauthorizedTask(name, eventBus, context, executor);
    }

    public static ChainableTask buildDeleteResourceAnonymousTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        return new DeleteResourceAnonymousTask(name, eventBus, context, executor);
    }

    public static ChainableTask buildDeleteResourceUnauthorizedTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        return new DeleteResourceUnauthorizedTask(name, eventBus, context, executor);
    }
}
