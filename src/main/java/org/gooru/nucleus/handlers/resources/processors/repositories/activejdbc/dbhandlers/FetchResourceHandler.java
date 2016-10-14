package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
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
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        AJEntityResource result = DBHelper.getResourceById(context.resourceId());

        if (result != null) {
            return new ExecutionResult<>(MessageResponseFactory.createGetSuccessResponse(new JsonObject(
                JsonFormatterBuilder.buildSimpleJsonFormatter(false, AJEntityResource.RESOURCE_SPECIFIC_FIELDS)
                    .toJson(result))), ExecutionResult.ExecutionStatus.SUCCESSFUL);

        }

        LOGGER.warn("FetchResourceHandler : Resource with id : {} : not found", context.resourceId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
            ExecutionResult.ExecutionStatus.FAILED);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

}
