package org.gooru.nucleus.handlers.resources.bootstrap.scenarios;

import org.gooru.nucleus.handlers.resources.bootstrap.utilities.DeliveryOptionsBuilder;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.RequestBuilder;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder;
import org.gooru.nucleus.handlers.resources.constants.HttpConstants;
import org.gooru.nucleus.handlers.resources.constants.MessagebusEndpoints;

import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 2/11/16.
 */
public class DirectFetchScenario implements Scenario {
    private final ResponseBuilder responseBuilder = new ResponseBuilder();
    private final String resourceId = "7112dc75-1e37-4f2a-879a-779a55d48c17";

    @Override
    public void playScenario(TestContext context, EventBus eventBus) {
        Async async = context.async();
        System.out.println("Testing direct fetch resource");
        eventBus.send(MessagebusEndpoints.MBEP_RESOURCE, RequestBuilder.buildEmptyDefaultRequest(),
            DeliveryOptionsBuilder.buildDeliveryOptionsForFetchResource(resourceId), reply -> {
                if (reply.succeeded()) {
                    System.out.println(reply.result().body());
                    ResponseBuilder.Response response = responseBuilder.buildResponse(reply);
                    context.assertEquals(HttpConstants.HttpStatus.SUCCESS.getCode(), response.httpStatus());
                    async.complete();
                } else {
                    context.fail("Reply did not succeed");
                }
            });
    }
}
