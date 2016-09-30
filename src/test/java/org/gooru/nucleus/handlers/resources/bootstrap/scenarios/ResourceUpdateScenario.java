package org.gooru.nucleus.handlers.resources.bootstrap.scenarios;

import org.gooru.nucleus.handlers.resources.bootstrap.utilities.DependencyDataProvider;
import org.gooru.nucleus.handlers.resources.constants.HttpConstants;
import org.gooru.nucleus.handlers.resources.tasks.AbstractChainableTaskExecutor;
import org.gooru.nucleus.handlers.resources.tasks.ChainableTask;
import org.gooru.nucleus.handlers.resources.tasks.ChainableTaskBuilder;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

/**
 * @author ashish on 30/9/16.
 */
public class ResourceUpdateScenario extends AbstractChainableTaskExecutor implements Scenario {

    @Override
    public void playScenario(TestContext context, EventBus eventBus) {
        System.out.println("Playing Update Scenario");
        initializeTasks(context, eventBus);
        executeNextTask(context, eventBus);
    }

    private void initializeTasks(TestContext context, EventBus eventBus) {
        ChainableTask createTask = ChainableTaskBuilder
            .buildCreateResourceTask("ResourceUpdateScenario:CreateResource", eventBus, context, this);
        ChainableTask fetchTask = ChainableTaskBuilder
            .buildFetchResourceTask("ResourceUpdateScenario:FetchResource", eventBus, context, this);
        ChainableTask updateTask = ChainableTaskBuilder
            .buildUpdateResourceTask("ResourceUpdateScenario:UpdateResource", eventBus, context, this);
        ChainableTask fetchTaskPostUpdate = ChainableTaskBuilder
            .buildFetchResourceTask("ResourceUpdateScenario:FetchResourcePostUpdate", eventBus, context, this);
        ChainableTask deleteTask = ChainableTaskBuilder
            .buildDeleteResourceTask("ResourceUpdateScenario:DeleteResource", eventBus, context, this);

        DependencyDataProvider provider = getDependencyDataProvider(createTask, fetchTask);

        fetchTask.setProvider(provider);
        updateTask.setProvider(provider);
        fetchTaskPostUpdate.setProvider(provider);
        deleteTask.setProvider(provider);

        this.tasks.add(createTask);
        this.tasks.add(fetchTask);
        this.tasks.add(updateTask);
        this.tasks.add(fetchTaskPostUpdate);
        this.tasks.add(deleteTask);
    }

    private DependencyDataProvider getDependencyDataProvider(final ChainableTask createTask,
        final ChainableTask fetchTask) {
        return new DependencyDataProvider() {
            @Override
            public String resourceId() {
                return createTask.outcome().httpHeaders().getString(HttpConstants.HEADER_LOCATION);
            }

            @Override
            public JsonObject resource() {
                return fetchTask.outcome().httpBody();
            }
        };
    }

}
