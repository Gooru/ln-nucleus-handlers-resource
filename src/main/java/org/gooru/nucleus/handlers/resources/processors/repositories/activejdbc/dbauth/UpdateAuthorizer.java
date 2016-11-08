package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbauth;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.ResourceHolder;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 17/10/16.
 */
final class UpdateAuthorizer implements Authorizer<ResourceHolder> {

    private ResourceHolder holder;

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateAuthorizer.class);
    private final ProcessorContext context;

    public UpdateAuthorizer(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> authorize(ResourceHolder model) {
        this.holder = model;
        if (model.getCategory() == ResourceHolder.RESOURCE_CATEGORY.RESOURCE_ORIGINAL) {
            return authorizeForOriginalResource();
        } else {
            return authorizeForResourceReference();
        }
    }

    private ExecutionResult<MessageResponse> authorizeForResourceReference() {
        AJEntityResource resource = this.holder.getResource();
        String course = resource.getString(AJEntityResource.COURSE_ID);
        String collection = resource.getString(AJEntityResource.COLLECTION_ID);

        if (course != null) {
            return authorizeForResourceRefByCourse(course);
        } else if (collection != null) {
            return authorizeForResourceRefByCollection(collection);
        } else {
            throw new IllegalStateException(
                "Resource ref " + context.resourceId() + " is neither assigned to course nor to collection");
        }
    }

    private ExecutionResult<MessageResponse> authorizeForResourceRefByCollection(String collection) {
        final Long count =
            Base.count(AJEntityResource.TABLE_COLLECTION, AJEntityResource.AUTH_VIA_COLLECTION_FILTER, collection,
                context.userId(), context.userId());
        if (count >= 1) {
            LOGGER.debug("Auth check to update '{}' successful based on collection: {}", context.resourceId(),
                collection);
            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
        }
        return new ExecutionResult<>(
            MessageResponseFactory.createForbiddenResponse("User must be owner/collaborator of the collection"),
            ExecutionResult.ExecutionStatus.FAILED);
    }

    private ExecutionResult<MessageResponse> authorizeForResourceRefByCourse(String course) {
        final Long count =
            Base.count(AJEntityResource.TABLE_COURSE, AJEntityResource.AUTH_VIA_COURSE_FILTER, course, context.userId(),
                context.userId());
        if (count >= 1) {
            LOGGER.debug("Auth check to update resource '{}' successful based on course: {}", context.resourceId(),
                course);
            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
        }
        return new ExecutionResult<>(
            MessageResponseFactory.createForbiddenResponse("User must be owner/collaborator of the course"),
            ExecutionResult.ExecutionStatus.FAILED);
    }

    private ExecutionResult<MessageResponse> authorizeForOriginalResource() {
        AJEntityOriginalResource resource = this.holder.getOriginalResource();
        String creator = resource.getString(AJEntityOriginalResource.CREATOR_ID);
        if (creator != null && creator.equalsIgnoreCase(context.userId())) {
            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
        } else {
            LOGGER.info("Update Authorization failed for original resource '{}' for user '{}'", context.resourceId(),
                context.userId());
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse("User must be owner of the resource"),
                ExecutionResult.ExecutionStatus.FAILED);
        }
    }
}
