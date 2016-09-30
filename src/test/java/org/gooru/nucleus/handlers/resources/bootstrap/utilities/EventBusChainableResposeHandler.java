package org.gooru.nucleus.handlers.resources.bootstrap.utilities;

import static org.gooru.nucleus.handlers.resources.bootstrap.utilities.ResponseBuilder.Response;

import org.gooru.nucleus.handlers.resources.tasks.ChainableTaskExecutor;

import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 29/9/16.
 */
public interface EventBusChainableResposeHandler {

    void handleResponse(TestContext context, EventBus eventBus, Response response, ChainableTaskExecutor executor);
}
