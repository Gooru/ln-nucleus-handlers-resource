package org.gooru.nucleus.handlers.resources.tasks;

import org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 30/9/16.
 */
final class DepedencySatisfierNoOpTask extends AbstractChainableTaskWithEventBusResponseHandler {

    public DepedencySatisfierNoOpTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor, JsonObject input, ResponseBuilder.Response output) {
        super(name, eventBus, context, executor);
        this.request = input;
        this.response = output;
    }

    @Override
    public void execute(TestContext context, EventBus eventBus, ChainableTaskExecutor executor) {
        this.async.complete();
    }

    @Override
    protected void doAssertions(TestContext context, ResponseBuilder.Response response,
        ChainableTaskExecutor executor) {

    }
}
