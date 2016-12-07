package org.gooru.nucleus.handlers.resources.bootstrap.scenarios;

import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 30/9/16.
 */
public interface Scenario {

    void playScenario(TestContext context, EventBus eventBus);
}
