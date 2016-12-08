package org.gooru.nucleus.handlers.resources.tasks;

import org.gooru.nucleus.handlers.resources.bootstrap.utilities.DeliveryOptionsBuilder;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.EventBusChainableSender;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.RequestBuilder;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder;
import org.gooru.nucleus.handlers.resources.constants.HttpConstants;
import org.gooru.nucleus.handlers.resources.constants.MessagebusEndpoints;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 3/10/16.
 */
final class CreateResourceAnonymousTask extends AbstractChainableTaskWithEventBusResponseHandler {

    public CreateResourceAnonymousTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        super(name, eventBus, context, executor);

    }

    @Override
    public void execute(TestContext context, EventBus eventBus, ChainableTaskExecutor executor) {
        this.currentStatus = Status.RUNNING;
        System.out.println("Testing create resource for anonymous user");

        request = RequestBuilder.buildCreateRequestForAnonymousUser();
        final String resourceEventBusEndPoint = MessagebusEndpoints.MBEP_RESOURCE;
        final DeliveryOptions options = DeliveryOptionsBuilder.buildDeliveryOptionsForCreateResource();
        EventBusChainableSender
            .sendMessage(context, eventBus, request, resourceEventBusEndPoint, options, this, executor);
    }

    protected void doAssertions(TestContext context, ResponseBuilder.Response response,
        ChainableTaskExecutor executor) {
        this.response = response;
        context.assertEquals(HttpConstants.HttpStatus.FORBIDDEN.getCode(), response.httpStatus());
    }
}
