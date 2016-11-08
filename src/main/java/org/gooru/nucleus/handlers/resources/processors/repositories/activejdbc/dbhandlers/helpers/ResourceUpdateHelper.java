package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.EntityConstants;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.ResourceHolder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public final class ResourceUpdateHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUpdateHelper.class);

    private ResourceUpdateHelper() {
        throw new AssertionError();
    }

    public static ExecutionResult<MessageResponse> validatePayload(ResourceHolder holder, ProcessorContext context) {
        if (holder.getCategory() == ResourceHolder.RESOURCE_CATEGORY.RESOURCE_ORIGINAL) {
            return validatePayloadForOriginalResource(holder.getOriginalResource(), context);
        } else {
            return ResourceRefUpdateHelper.validatePayloadForResourceRef(holder.getResource(), context);
        }
    }

    private static ExecutionResult<MessageResponse> validatePayloadForOriginalResource(
        AJEntityOriginalResource resource, ProcessorContext context) {
        JsonObject errors = new DefaultPayloadValidator()
            .validatePayload(context.request(), AJEntityOriginalResource.editFieldSelector(),
                AJEntityOriginalResource.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    public static ExecutionResult<MessageResponse> updateResource(ResourceHolder holder, ProcessorContext context) {
        if (holder.getCategory() == ResourceHolder.RESOURCE_CATEGORY.RESOURCE_ORIGINAL) {
            return updateOriginalResource(holder.getOriginalResource(), context);
        } else {
            return ResourceRefUpdateHelper.updateResourceRef(holder.getResource(), context);
        }
    }

    private static ExecutionResult<MessageResponse> updateOriginalResource(AJEntityOriginalResource resource,
        ProcessorContext context) {
        ResourceMetadataHelper.flattenMetadataFields(context.request());
        ResourceTaxonomyHelper.populateGutCodes(resource, context.request());

        new DefaultOriginalResourceBuilder()
            .build(resource, context.request(), AJEntityOriginalResource.getConverterRegistry());

        LicenseHelper.populateLicense(resource);

        TypeHelper
            .setPGObject(resource, AJEntityOriginalResource.MODIFIER_ID, EntityConstants.UUID_TYPE, context.userId());
        ExecutionResult<MessageResponse> result = processUrlToUpdateForOriginalResource(resource, context);
        if (result.hasFailed()) {
            return result;
        }

        result = saveOriginalResource(resource, context);
        if (result.hasFailed()) {
            return result;
        }

        return ResourceRefUpdateHelper.updateResourceRefsForGivenOriginal(resource, context);

    }

    private static ExecutionResult<MessageResponse> saveOriginalResource(AJEntityOriginalResource resource,
        ProcessorContext context) {
        if (!resource.save()) {
            LOGGER.error("Update original resource failed! {} ", resource.errors());
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(resource.errors()),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    private static ExecutionResult<MessageResponse> processUrlToUpdateForOriginalResource(
        AJEntityOriginalResource resource, ProcessorContext context) {
        String url = context.request().getString(AJEntityOriginalResource.URL);
        if (url != null && !url.isEmpty()) {
            ExecutionResult<MessageResponse> result = ResourceUrlHelper.handleUrl(resource, context.request());
            if (result.hasFailed()) {
                return result;
            }

            JsonObject resourceIdWithURLDuplicates =
                ResourceRetrieveHelper.getDuplicateResourcesByUrl(resource, context.request());
            if (resourceIdWithURLDuplicates != null && !resourceIdWithURLDuplicates.isEmpty()) {
                LOGGER.error("validateRequest : Duplicate resource URL found. So cannot go ahead with updating url! "
                    + "URL : {}", resource.getString(AJEntityResource.URL));
                LOGGER.error("validateRequest : Duplicate resources : {}", resourceIdWithURLDuplicates);
                return new ExecutionResult<>(
                    MessageResponseFactory.createValidationErrorResponse(resourceIdWithURLDuplicates),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    private static class DefaultOriginalResourceBuilder implements EntityBuilder<AJEntityOriginalResource> {
    }

}