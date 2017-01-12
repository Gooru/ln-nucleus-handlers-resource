package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.EntityConstants;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceDeleteHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceDeleteHelper.class);

    private ResourceDeleteHelper() {
        throw new AssertionError();
    }

    public static int deleteResourceReferences(String creatorId, String originalResourceId) {

        Objects.requireNonNull(creatorId);
        Objects.requireNonNull(originalResourceId);

        int numRecsUpdated;
        List<Object> params = new ArrayList<>();
        String updateStmt = AJEntityResource.IS_DELETED + "= ? ";
        params.add(true);
        params.add(originalResourceId);
        params.add(creatorId);

        numRecsUpdated =
            AJEntityResource.update(updateStmt, AJEntityResource.FILTER_FETCH_REFERENCES_OF_ORIGINAL, params.toArray());
        LOGGER.debug("Deleted '{}' references for resource id '{}'", numRecsUpdated, originalResourceId);
        return numRecsUpdated;

    }

    public static ExecutionResult<MessageResponse> updateCollectionTimeStamp(AJEntityResource resource,
        ProcessorContext context) {
        Object collectionId = resource.get(AJEntityResource.COLLECTION_ID);
        if (collectionId != null) {
            int rows = Base.exec(AJEntityResource.UPDATE_CONTAINER_TIMESTAMP, collectionId);
            if (rows != 1) {
                LOGGER.warn("update of the collection timestamp failed for collection '{}' with resource '{}'",
                    collectionId, context.resourceId());
                return new ExecutionResult<>(
                    MessageResponseFactory.createInternalErrorResponse("Interaction with store failed"),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    public static ExecutionResult<MessageResponse> markResourceRefAsDeleted(AJEntityResource resource,
        ProcessorContext context) {
        TypeHelper.setPGObject(resource, AJEntityResource.MODIFIER_ID, EntityConstants.UUID_TYPE, context.userId());
        if (resource.hasErrors()) {
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(resource.errors()),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        resource.set(AJEntityResource.IS_DELETED, true);
        if (!resource.save()) {
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(resource.errors()),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

}