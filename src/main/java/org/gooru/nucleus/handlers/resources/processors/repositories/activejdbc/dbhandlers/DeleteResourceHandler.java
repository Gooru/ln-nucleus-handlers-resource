package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers.ResourceDeleteHelper;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers
    .ResourceRetrieveHelper;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers.SanityCheckerHelper;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers.TypeHelper;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.EntityConstants;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.ResourceHolder;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class DeleteResourceHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteResourceHandler.class);
    private final ProcessorContext context;
    private ResourceHolder resourceHolder;

    public DeleteResourceHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        ExecutionResult<MessageResponse> result = SanityCheckerHelper.verifyResourceId(context);
        if (result.hasFailed()) {
            return result;
        }
        return SanityCheckerHelper.verifyUserExcludeAnonymous(context);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {

        this.resourceHolder = ResourceRetrieveHelper.getResourceToDelete(context.resourceId());
        if (this.resourceHolder == null) {
            LOGGER.error("Resource not found to delete: {} ", context.resourceId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return AuthorizerBuilder.buildDeleteAuthorizer(context).authorize(resourceHolder);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        if (this.resourceHolder.getCategory() == ResourceHolder.RESOURCE_CATEGORY.RESOURCE_ORIGINAL) {
            return deleteOriginalResource();
        } else {
            return deleteResourceReference();
        }
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private ExecutionResult<MessageResponse> deleteResourceReference() {
        AJEntityResource resource = resourceHolder.getResource();
        ExecutionResult<MessageResponse> result;
        try {
            result = ResourceDeleteHelper.markResourceRefAsDeleted(resource, context);
            if (result.hasFailed()) {
                return result;
            }

            result = ResourceDeleteHelper.updateCollectionTimeStamp(resource, context);
            if (result.hasFailed()) {
                return result;
            }
            return new ExecutionResult<>(
                MessageResponseFactory.createDeleteSuccessResponse(new JsonObject().put("id", context.resourceId())),
                ExecutionResult.ExecutionStatus.SUCCESSFUL);

        } catch (Throwable t) {
            LOGGER.error("exception while delete resource", t);
            return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }
    }

    private ExecutionResult<MessageResponse> deleteOriginalResource() {
        AJEntityOriginalResource resource = this.resourceHolder.getOriginalResource();
        try {
            TypeHelper
                .setPGObject(resource, AJEntityResource.MODIFIER_ID, EntityConstants.UUID_TYPE, this.context.userId());
            if (resource.hasErrors()) {
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(resource.errors()),
                    ExecutionResult.ExecutionStatus.FAILED);
            }

            resource.set(AJEntityResource.IS_DELETED, true);
            if (!resource.save()) {
                LOGGER.info("error in delete resource, returning errors");
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(resource.errors()),
                    ExecutionResult.ExecutionStatus.FAILED);
            }

            LOGGER.info("original resource marked as deleted successfully");
            JsonObject resourceCopyIds = ResourceRetrieveHelper.getCopiesOfAResource(resource, context.resourceId());
            if (resourceCopyIds != null && !resourceCopyIds.isEmpty()) {
                int deletedResourceCopies =
                    ResourceDeleteHelper.deleteResourceReferences(resource, context.resourceId());
                if (deletedResourceCopies >= 0) {
                    return new ExecutionResult<>(MessageResponseFactory.createDeleteSuccessResponse(resourceCopyIds),
                        ExecutionResult.ExecutionStatus.SUCCESSFUL);
                } else {
                    return new ExecutionResult<>(
                        MessageResponseFactory.createInternalErrorResponse("Resource Copies were not deleted"),
                        ExecutionResult.ExecutionStatus.FAILED);
                }
            } else {
                LOGGER.info("Resource '{}' deleted, no copies found", context.resourceId());
                return new ExecutionResult<>(MessageResponseFactory
                    .createDeleteSuccessResponse(new JsonObject().put("id", context.resourceId())),
                    ExecutionResult.ExecutionStatus.SUCCESSFUL);
            }

        } catch (IllegalArgumentException e) {
            LOGGER.error("executeRequest : Update resource failed to propagate changes to other copies!", e);
            return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(e.getMessage()),
                ExecutionResult.ExecutionStatus.FAILED);
        } catch (Throwable t) {
            LOGGER.error("exception while delete resource", t);
            return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()),
                ExecutionResult.ExecutionStatus.FAILED);
        }
    }

}
