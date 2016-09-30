package org.gooru.nucleus.handlers.resources.bootstrap.scenarios;

import org.gooru.nucleus.handlers.resources.bootstrap.utilities.DependencyDataProvider;
import org.gooru.nucleus.handlers.resources.constants.HttpConstants;
import org.gooru.nucleus.handlers.resources.tasks.AbstractChainableTaskExecutor;
import org.gooru.nucleus.handlers.resources.tasks.ChainableTask;
import org.gooru.nucleus.handlers.resources.tasks.ChainableTaskBuilder;

import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 29/9/16.
 */
public class ResourceCreateScenario extends AbstractChainableTaskExecutor implements Scenario {

    @Override
    public void playScenario(TestContext context, EventBus eventBus) {
        System.out.println("Playing Create Scenario");
        initializeTasks(context, eventBus);
        executeNextTask(context, eventBus);
    }

    private void initializeTasks(TestContext context, EventBus eventBus) {
        ChainableTask createTask = ChainableTaskBuilder
            .buildCreateResourceTask("ResourceCreateScenario:CreateResource", eventBus, context, this);
        ChainableTask fetchTask = ChainableTaskBuilder
            .buildFetchResourceTask("ResourceCreateScenario:FetchResource", eventBus, context, this);
        ChainableTask deleteTask = ChainableTaskBuilder
            .buildDeleteResourceTask("ResourceCreateScenario:DeleteResource", eventBus, context, this);

        DependencyDataProvider provider =
            () -> createTask.outcome().httpHeaders().getString(HttpConstants.HEADER_LOCATION);
        fetchTask.setProvider(provider);
        deleteTask.setProvider(provider);

        this.tasks.add(createTask);
        this.tasks.add(fetchTask);
        this.tasks.add(deleteTask);
    }
}
