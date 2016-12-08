package org.gooru.nucleus.handlers.resources.bootstrap.utilities;

import static org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder.Response;

import org.gooru.nucleus.handlers.resources.tasks.ChainableTaskExecutor;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 29/9/16.
 */
public final class EventBusChainableSender {
    private static final ResponseBuilder responseBuilder = new ResponseBuilder();

    private EventBusChainableSender() {
        throw new AssertionError();
    }

    public static void sendMessage(TestContext context, EventBus eventBus, JsonObject request,
        String resourceEventBusEndPoint, DeliveryOptions options, EventBusChainableResposeHandler handler,
        ChainableTaskExecutor executor) {
        {
            Async async = context.async();
            eventBus.send(resourceEventBusEndPoint, request, options, reply -> {
                if (reply.succeeded()) {
                    System.out.println(reply.result().body());

                    Response response = responseBuilder.buildResponse(reply);
                    handler.handleResponse(context, eventBus, response, executor);
                    async.complete();
                } else {
                    context.fail("Reply did not succeed");
                }
            });
        }
    }
}
