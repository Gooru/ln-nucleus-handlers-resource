package org.gooru.nucleus.handlers.resources.tasks;

import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 29/9/16.
 */
public interface ChainableTaskExecutor {
    void executeNextTask(TestContext context, EventBus eventBus);

    ChainableTask getTaskByName(String name);

}
