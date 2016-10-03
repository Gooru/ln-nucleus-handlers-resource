package org.gooru.nucleus.handlers.resources.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 30/9/16.
 */
public abstract class AbstractChainableTaskExecutor implements ChainableTaskExecutor {

    protected final List<ChainableTask> tasks = new ArrayList<>();
    int currentTask = 0;

    @Override
    public void executeNextTask(TestContext context, EventBus eventBus) {
        if (currentTask < tasks.size()) {
            tasks.get(currentTask++).execute(context, eventBus, this);
        }
    }

    @Override
    public ChainableTask getTaskByName(String name) {
        Optional<ChainableTask> element = tasks.stream().filter(task -> task.name().equals(name)).findFirst();
        return element.isPresent() ? element.get() : null;
    }

}
