package org.gooru.nucleus.handlers.resources.bootstrap;

import org.gooru.nucleus.handlers.resources.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.handlers.resources.bootstrap.shutdown.Finalizers;
import org.gooru.nucleus.handlers.resources.bootstrap.startup.Initializer;
import org.gooru.nucleus.handlers.resources.bootstrap.startup.Initializers;
import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.gooru.nucleus.handlers.resources.constants.MessagebusEndpoints;
import org.gooru.nucleus.handlers.resources.processors.ProcessorBuilder;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class ResourceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceVerticle.class);

    @Override
    public void start(Future<Void> voidFuture) throws Exception {

        EventBus eb = vertx.eventBus();
        vertx.executeBlocking(blockingFuture -> {
            startApplication();
            blockingFuture.complete();
        }, startApplicationFuture -> {
            if (startApplicationFuture.succeeded()) {
                eb.consumer(MessagebusEndpoints.MBEP_RESOURCE, message -> {
                    LOGGER.debug("Received message: " + message.body());
                    vertx.executeBlocking(future -> {
                        MessageResponse result = new ProcessorBuilder(message).build().process();
                        future.complete(result);
                    }, res -> {
                        MessageResponse result = (MessageResponse) res.result();
                        LOGGER.debug("Returning message: " + result.reply());
                        message.reply(result.reply(), result.deliveryOptions());
                        LOGGER.debug("Event Data : " + result.event());
                        JsonObject eventData = result.event();
                        if (eventData != null) {
                            String sessionToken =
                                ((JsonObject) message.body()).getString(MessageConstants.MSG_HEADER_TOKEN);
                            if (sessionToken != null && !sessionToken.isEmpty()) {
                                eventData.put(MessageConstants.MSG_HEADER_TOKEN, sessionToken);
                            } else {
                                LOGGER.warn("Invalid session token received");
                            }
                            eb.publish(MessagebusEndpoints.MBEP_EVENT, eventData);
                        }
                    });
                }).completionHandler(result -> {
                    if (result.succeeded()) {
                        LOGGER.info("Resource end point ready to listen");
                        voidFuture.complete();
                    } else {
                        LOGGER.error("Error registering the resource handler. Halting the Resource machinery");
                        voidFuture.fail(result.cause());
                        Runtime.getRuntime().halt(1);
                    }
                });
            } else {
                voidFuture.fail("Not able to initialize the Resource machinery properly");
            }
        });

    }

    @Override
    public void stop() throws Exception {
        shutDownApplication();
        super.stop();
    }

    private void startApplication() {
        Initializers initializers = new Initializers();
        try {
            for (Initializer initializer : initializers) {
                initializer.initializeComponent(vertx, config());
            }
        } catch (IllegalStateException ie) {
            LOGGER.error("Error initializing application", ie);
            Runtime.getRuntime().halt(1);
        }
    }

    private void shutDownApplication() {
        Finalizers finalizers = new Finalizers();
        for (Finalizer finalizer : finalizers) {
            finalizer.finalizeComponent();
        }

    }

}
