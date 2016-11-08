package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers.*;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.EntityConstants;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class CreateResourceHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateResourceHandler.class);
    private final ProcessorContext context;
    private AJEntityOriginalResource resource;

    public CreateResourceHandler(ProcessorContext context) {
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

        JsonObject errors = new DefaultPayloadValidator()
            .validatePayload(context.request(), AJEntityOriginalResource.createFieldSelector(),
                AJEntityOriginalResource.getValidatorRegistry());
        if ((errors != null) && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        try {

            this.resource = new AJEntityOriginalResource();
            ResourceMetadataHelper.flattenMetadataFields(this.context.request());
            ResourceTaxonomyHelper.populateGutCodes(this.resource, this.context.request());

            new DefaultOriginalResourceBuilder()
                .build(resource, context.request(), AJEntityOriginalResource.getConverterRegistry());

            ExecutionResult<MessageResponse> result = ResourceUrlHelper.handleUrl(resource, context.request());
            if (result.hasFailed()) {
                return result;
            }

            LicenseHelper.populateLicense(this.resource);

            TypeHelper.setPGObject(this.resource, AJEntityOriginalResource.CREATOR_ID, EntityConstants.UUID_TYPE,
                context.userId());
            TypeHelper.setPGObject(this.resource, AJEntityOriginalResource.MODIFIER_ID, EntityConstants.UUID_TYPE,
                context.userId());

            JsonObject resourceIdWithURLDuplicates =
                ResourceRetrieveHelper.getDuplicateResourcesByUrl(this.resource, this.context.request());
            if (resourceIdWithURLDuplicates != null && !resourceIdWithURLDuplicates.isEmpty()) {
                LOGGER.error(
                    "validateRequest : Duplicate resource URL found. So cannot go ahead with creating new resource! "
                        + "URL : {}", resource.getString(AJEntityResource.URL));
                LOGGER.error("validateRequest : Duplicate resources : {}", resourceIdWithURLDuplicates);
                return new ExecutionResult<>(
                    MessageResponseFactory.createValidationErrorResponse(resourceIdWithURLDuplicates),
                    ExecutionResult.ExecutionStatus.FAILED);
            }

        } catch (IllegalArgumentException e) {
            LOGGER.error("validateRequest : {} ", e);
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return AuthorizerBuilder.buildCreateAuthorizer(null).authorize(null);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        if (!this.resource.insert()) {
            if (this.resource.hasErrors()) {
                LOGGER.error("executeRequest : Create resource failed for input object. Errors: {}",
                    this.resource.errors());
                return new ExecutionResult<>(
                    MessageResponseFactory.createValidationErrorResponse(this.resource.errors()),
                    ExecutionResult.ExecutionStatus.FAILED);
            } else {
                LOGGER.error("executeRequest : Create resource failed for input object: {}", context.request());
                return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }

        // successful...
        LOGGER.debug("executeRequest : Created resource ID: " + this.resource.getString(AJEntityResource.ID));
        return new ExecutionResult<>(
            MessageResponseFactory.createPostSuccessResponse("Location", this.resource.getString(AJEntityResource.ID)),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    private static class DefaultOriginalResourceBuilder implements EntityBuilder<AJEntityOriginalResource> {
    }

}
