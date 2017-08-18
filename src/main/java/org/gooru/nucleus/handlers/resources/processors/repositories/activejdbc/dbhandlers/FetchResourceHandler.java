package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers
    .FetchResourceResponseDecorator;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers
    .ResourceRetrieveHelper;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers.SanityCheckerHelper;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.EntityConstants;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.ResourceHolder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class FetchResourceHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchResourceHandler.class);
    private final ProcessorContext context;
    private ResourceHolder resourceHolder;

    public FetchResourceHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        ExecutionResult<MessageResponse> result = SanityCheckerHelper.verifyResourceId(context);
        if (result.hasFailed()) {
            return result;
        }
        return SanityCheckerHelper.verifyUserAllowAnonymous(context);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {

        this.resourceHolder = ResourceRetrieveHelper.getResource(context.resourceId());
        if (this.resourceHolder == null) {
            LOGGER.error("Resource not found to delete: {} ", context.resourceId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return checkTenantAuthoriation();
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        if (this.resourceHolder.getCategory() == ResourceHolder.RESOURCE_CATEGORY.RESOURCE_ORIGINAL) {
            JsonObject result = new JsonObject(
                JsonFormatterBuilder.buildSimpleJsonFormatter(false, EntityConstants.RESOURCE_FETCH_FIELDS, false)
                    .toJson(resourceHolder.getOriginalResource()));
            FetchResourceResponseDecorator
                .processOriginalResourceFetchResponse(resourceHolder.getOriginalResource(), result);
            return new ExecutionResult<>(MessageResponseFactory.createGetSuccessResponse(result),
                ExecutionResult.ExecutionStatus.SUCCESSFUL);
        } else {
            JsonObject result = new JsonObject(
                JsonFormatterBuilder.buildSimpleJsonFormatter(false, EntityConstants.RESOURCE_FETCH_FIELDS, true)
                    .toJson(resourceHolder.getResource()));
            FetchResourceResponseDecorator.processResourceRefFetchResponse(resourceHolder.getResource(), result);
            return new ExecutionResult<>(MessageResponseFactory.createGetSuccessResponse(result),
                ExecutionResult.ExecutionStatus.SUCCESSFUL);
        }

    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

    private ExecutionResult<MessageResponse> checkTenantAuthoriation() {
        if (resourceHolder.isHoldingOriginalResource()) {
            return AuthorizerBuilder.buildTenantAuthorizer(context).authorize(resourceHolder.getOriginalResource());
        } else {
            return AuthorizerBuilder.buildTenantResourceRefAuthorizer(context).authorize(resourceHolder.getResource());
        }
    }

}
