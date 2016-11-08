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
public class DirectUpdateScenario implements Scenario {
    private final ResponseBuilder responseBuilder = new ResponseBuilder();
    private final String resourceId = "923f4703-8973-4dd3-8e46-851851fd9c47";

    @Override
    public void playScenario(TestContext context, EventBus eventBus) {
        Async async = context.async();
        System.out.println("Testing direct update resource");
        eventBus.send(MessagebusEndpoints.MBEP_RESOURCE, RequestBuilder.buildUpdateRequest(),
            DeliveryOptionsBuilder.buildDeliveryOptionsForUpdateResource(resourceId), reply -> {
                if (reply.succeeded()) {
                    System.out.println(reply.result().body());
                    ResponseBuilder.Response response = responseBuilder.buildResponse(reply);
                    context.assertEquals(HttpConstants.HttpStatus.NO_CONTENT.getCode(), response.httpStatus());
                    async.complete();
                } else {
                    context.fail("Reply did not succeed");
                }
            });
    }
}
