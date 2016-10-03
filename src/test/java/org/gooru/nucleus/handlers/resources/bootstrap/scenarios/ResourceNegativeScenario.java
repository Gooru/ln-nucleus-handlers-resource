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
 * @author ashish on 3/10/16.
 */
public class ResourceNegativeScenario extends AbstractChainableTaskExecutor implements Scenario {

    @Override
    public void playScenario(TestContext context, EventBus eventBus) {
        System.out.println("Playing Negative Scenarios");
        initializeTasks(context, eventBus);
        executeNextTask(context, eventBus);
    }

    private void initializeTasks(TestContext context, EventBus eventBus) {
        ChainableTask createTaskAnonymous = ChainableTaskBuilder
            .buildCreateResourceAnonymousTask("ResourceNegativeScenario:CreateResource", eventBus, context, this);
        ChainableTask createTask = ChainableTaskBuilder
            .buildCreateResourceTask("ResourceNegativeScenario:CreateResource", eventBus, context, this);

        ChainableTask fetchTask = ChainableTaskBuilder
            .buildFetchResourceTask("ResourceNegativeScenario:FetchResource", eventBus, context, this);
        ChainableTask fetchTaskNonExisting = ChainableTaskBuilder
            .buildFetchResourceNonExistingTask("ResourceNegativeScenario:FetchResourceNonExisting", eventBus, context,
                this);

        ChainableTask updateTask = ChainableTaskBuilder
            .buildUpdateResourceTask("ResourceNegativeScenario:UpdateResource", eventBus, context, this);
        ChainableTask updateTaskAnonymous = ChainableTaskBuilder
            .buildUpdateResourceAnonymousTask("ResourceNegativeScenario:UpdateResourceAnonymous", eventBus, context,
                this);
        ChainableTask updateTaskUnauthorized = ChainableTaskBuilder
            .buildUpdateResourceUnauthorizedTask("ResourceNegativeScenario:UpdateResourceUnauthorized", eventBus,
                context, this);

        ChainableTask fetchTaskPostUpdate = ChainableTaskBuilder
            .buildFetchResourceTask("ResourceNegativeScenario:FetchResourcePostUpdate", eventBus, context, this);

        ChainableTask deleteTaskAnonymous = ChainableTaskBuilder
            .buildDeleteResourceAnonymousTask("ResourceNegativeScenario:DeleteResource", eventBus, context, this);
        ChainableTask deleteTaskUnauthorized = ChainableTaskBuilder
            .buildDeleteResourceUnauthorizedTask("ResourceNegativeScenario:DeleteResource", eventBus, context, this);
        ChainableTask deleteTask = ChainableTaskBuilder
            .buildDeleteResourceTask("ResourceNegativeScenario:DeleteResource", eventBus, context, this);

        DependencyDataProvider provider = getDependencyDataProvider(createTask, fetchTask);

        fetchTask.setProvider(provider);
        updateTask.setProvider(provider);
        updateTaskAnonymous.setProvider(provider);
        updateTaskUnauthorized.setProvider(provider);
        fetchTaskPostUpdate.setProvider(provider);
        deleteTaskAnonymous.setProvider(provider);
        deleteTaskUnauthorized.setProvider(provider);
        deleteTask.setProvider(provider);

        this.tasks.add(createTaskAnonymous);
        this.tasks.add(createTask);
        this.tasks.add(fetchTask);
        this.tasks.add(fetchTaskNonExisting);
        this.tasks.add(updateTask);
        this.tasks.add(updateTaskAnonymous);
        this.tasks.add(updateTaskUnauthorized);
        this.tasks.add(fetchTaskPostUpdate);
        this.tasks.add(deleteTaskAnonymous);
        this.tasks.add(deleteTaskUnauthorized);
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
