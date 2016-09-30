package org.gooru.nucleus.handlers.resources.bootstrap.scenarios;

import org.gooru.nucleus.handlers.resources.bootstrap.utilities.DeliveryOptionsBuilder;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.RequestBuilder;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder;
import org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder.Response;
import org.gooru.nucleus.handlers.resources.constants.HttpConstants;
import org.gooru.nucleus.handlers.resources.constants.MessagebusEndpoints;

import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class NonExistingDeleteScenario implements Scenario {

    private final ResponseBuilder responseBuilder = new ResponseBuilder();

    @Override
    public void playScenario(TestContext context, EventBus eventBus) {
        Async async = context.async();
        System.out.println("Testing delete resource");
        eventBus.send(MessagebusEndpoints.MBEP_RESOURCE, RequestBuilder.buildEmptyRequest(),
            DeliveryOptionsBuilder.buildDeliveryOptionsForDeleteResource(), reply -> {
                if (reply.succeeded()) {
                    System.out.println(reply.result().body());
                    Response response = responseBuilder.buildResponse(reply);
                    context.assertEquals(HttpConstants.HttpStatus.NOT_FOUND.getCode(), response.httpStatus());
                    async.complete();
                } else {
                    context.fail("Reply did not succeed");
                }
            });
    }
}