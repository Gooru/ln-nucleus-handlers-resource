package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers
    .ResourceRetrieveHelper;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers.ResourceUpdateHelper;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers.SanityCheckerHelper;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.ResourceHolder;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class UpdateResourceHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateResourceHandler.class);
    private boolean isOwner = false;
    private final ProcessorContext context;
    private ResourceHolder resourceHolder;

    private JsonObject ownerDataToPropogateToCopies;
    private AJEntityResource updateRes;

    public UpdateResourceHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        ExecutionResult<MessageResponse> result = SanityCheckerHelper.verifyRequestBody(context);
        if (result.hasFailed()) {
            return result;
        }
        result = SanityCheckerHelper.verifyUserExcludeAnonymous(context);
        if (result.hasFailed()) {
            return result;
        }
        result = SanityCheckerHelper.verifyResourceId(context);
        if (result.hasFailed()) {
            return result;
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        this.resourceHolder = ResourceRetrieveHelper.getResource(context.resourceId());
        if (this.resourceHolder == null) {
            LOGGER.error("Resource not found to update: {} ", context.resourceId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        ExecutionResult<MessageResponse> result = ResourceUpdateHelper.validatePayload(resourceHolder, context);
        if (result.hasFailed()) {
            return result;
        }

        return AuthorizerBuilder.buildUpdateAuthorizer(context).authorize(resourceHolder);

    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {

        ExecutionResult<MessageResponse> result = ResourceUpdateHelper.updateResource(resourceHolder, context);
        if (result.hasFailed()) {
            return result;
        }

        return new ExecutionResult<>(MessageResponseFactory.createPutSuccessResponse("Location", context.resourceId()),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

}
