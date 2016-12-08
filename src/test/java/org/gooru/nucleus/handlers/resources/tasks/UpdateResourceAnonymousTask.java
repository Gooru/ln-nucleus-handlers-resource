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
public class UpdateResourceAnonymousTask extends AbstractChainableTaskWithEventBusResponseHandler {

    public UpdateResourceAnonymousTask(String name, EventBus eventBus, TestContext context,
        ChainableTaskExecutor executor) {
        super(name, eventBus, context, executor);
    }

    @Override
    protected void doAssertions(TestContext context, ResponseBuilder.Response response,
        ChainableTaskExecutor executor) {
        context.assertEquals(HttpConstants.HttpStatus.FORBIDDEN.getCode(), response.httpStatus());
    }

    @Override
    public void execute(TestContext context, EventBus eventBus, ChainableTaskExecutor executor) {
        this.currentStatus = Status.RUNNING;
        System.out.println("Testing update resource for anonymous user");

        request = RequestBuilder.buildUpdateAnonymousRequest();
        final String resourceEventBusEndPoint = MessagebusEndpoints.MBEP_RESOURCE;
        final String resourceId = provider.resourceId();
        final DeliveryOptions options = DeliveryOptionsBuilder.buildDeliveryOptionsForUpdateResource(resourceId);
        EventBusChainableSender
            .sendMessage(context, eventBus, request, resourceEventBusEndPoint, options, this, executor);
    }
}
