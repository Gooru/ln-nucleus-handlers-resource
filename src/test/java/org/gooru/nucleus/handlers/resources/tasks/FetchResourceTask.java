package org.gooru.nucleus.handlers.resources.tasks;

import org.gooru.nucleus.handlers.resources.bootstrap.utilities.DeliveryOptionsBuilder;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.EventBusChainableSender;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.RequestBuilder;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder;
import org.gooru.nucleus.handlers.resources.constants.HttpConstants;
import org.gooru.nucleus.handlers.resources.constants.MessagebusEndpoints;

import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 30/9/16.
 */
final class FetchResourceTask extends AbstractChainableTaskWithEventBusResponseHandler {
    public FetchResourceTask(String name, EventBus eventBus, TestContext context, ChainableTaskExecutor executor) {
        super(name, eventBus, context, executor);
    }

    @Override
    public void execute(TestContext context, EventBus eventBus, ChainableTaskExecutor executor) {
        final String resourceId = provider.resourceId();
        EventBusChainableSender.sendMessage(context, eventBus, RequestBuilder.buildEmptyDefaultRequest(),
            MessagebusEndpoints.MBEP_RESOURCE, DeliveryOptionsBuilder.buildDeliveryOptionsForFetchResource(resourceId),
            this, executor);
    }

    @Override
    protected void doAssertions(TestContext context, ResponseBuilder.Response response,
        ChainableTaskExecutor executor) {
        context.assertEquals(HttpConstants.HttpStatus.SUCCESS.getCode(), response.httpStatus());
        //TODO: Other asserts for the matching data of fetch with create
    }
}
