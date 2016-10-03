package org.gooru.nucleus.handlers.resources.tasks;

import java.util.UUID;

import org.gooru.nucleus.handlers.resources.bootstrap.utilities.DeliveryOptionsBuilder;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.EventBusChainableSender;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.RequestBuilder;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder;
import org.gooru.nucleus.handlers.resources.constants.HttpConstants;
import org.gooru.nucleus.handlers.resources.constants.MessagebusEndpoints;

import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 3/10/16.
 */
public class FetchResourceNonExistingTask extends AbstractChainableTaskWithEventBusResponseHandler {
    public FetchResourceNonExistingTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        super(name, eventBus, context, executor);
    }

    @Override
    public void execute(TestContext context, EventBus eventBus, ChainableTaskExecutor executor) {
        System.out.println("Testing fetch resource for non existing resources");
        final String resourceId = UUID.randomUUID().toString();
        EventBusChainableSender.sendMessage(context, eventBus, RequestBuilder.buildEmptyDefaultRequest(),
            MessagebusEndpoints.MBEP_RESOURCE, DeliveryOptionsBuilder.buildDeliveryOptionsForFetchResource(resourceId),
            this, executor);
    }

    @Override
    protected void doAssertions(TestContext context, ResponseBuilder.Response response,
        ChainableTaskExecutor executor) {
        context.assertEquals(HttpConstants.HttpStatus.NOT_FOUND.getCode(), response.httpStatus());
    }
}
