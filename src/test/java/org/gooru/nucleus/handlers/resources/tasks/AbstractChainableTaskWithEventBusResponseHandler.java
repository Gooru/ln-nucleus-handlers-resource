package org.gooru.nucleus.handlers.resources.tasks;

import org.gooru.nucleus.handlers.resources.bootstrap.utilities.DependencyDataProvider;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.EventBusChainableResposeHandler;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 30/9/16.
 */
abstract class AbstractChainableTaskWithEventBusResponseHandler
    implements ChainableTask, EventBusChainableResposeHandler {
    protected final String name;
    protected final TestContext context;
    protected final EventBus eventBus;
    protected final ChainableTaskExecutor executor;
    protected final Async async;
    protected Status currentStatus = Status.PENDING;
    protected JsonObject request;
    protected ResponseBuilder.Response response;
    DependencyDataProvider provider;

    public AbstractChainableTaskWithEventBusResponseHandler(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        this.name = name;
        this.eventBus = eventBus;
        this.context = context;
        this.async = context.async();
        this.executor = executor;
    }

    @Override
    public Status status() {
        return currentStatus;
    }

    @Override
    public JsonObject input() {
        return request;
    }

    @Override
    public ResponseBuilder.Response outcome() {
        return response;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void setProvider(DependencyDataProvider provider) {
        this.provider = provider;
    }

    @Override
    public void handleResponse(TestContext context, EventBus eventBus, ResponseBuilder.Response response,
        ChainableTaskExecutor executor) {
        doAssertions(context, response, executor);
        this.currentStatus = Status.COMPLETED;
        executor.executeNextTask(context, eventBus);
        async.complete();
    }

    protected abstract void doAssertions(TestContext context, ResponseBuilder.Response response,
        ChainableTaskExecutor executor);
}
