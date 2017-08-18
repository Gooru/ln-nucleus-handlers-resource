package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbauth;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.libs.tenant.TenantTree;
import org.gooru.nucleus.libs.tenant.TenantTreeBuilder;
import org.gooru.nucleus.libs.tenant.contents.ContentTenantAuthorization;
import org.gooru.nucleus.libs.tenant.contents.ContentTenantAuthorizationBuilder;
import org.gooru.nucleus.libs.tenant.contents.ContentTreeAttributes;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 17/1/17.
 */
public class TenantResourceRefAuthorizer implements Authorizer<AJEntityResource> {
    private final ProcessorContext context;
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantAuthorizer.class);

    public TenantResourceRefAuthorizer(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> authorize(AJEntityResource model) {
        TenantTree userTenantTree = TenantTreeBuilder.build(context.tenant(), context.tenantRoot());
        TenantTree contentTenantTree = TenantTreeBuilder.build(model.getTenant(), model.getTenantRoot());

        ContentTenantAuthorization authorization = ContentTenantAuthorizationBuilder
            .build(contentTenantTree, userTenantTree, ContentTreeAttributes.build(model.isResourcePublished()));

        if (authorization.canRead()) {
            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
        }
        return checkAuthBasedOnParentBeingPublished(model, userTenantTree, contentTenantTree);
    }

    private ExecutionResult<MessageResponse> checkAuthBasedOnParentBeingPublished(AJEntityResource model,
        TenantTree userTenantTree, TenantTree contentTenantTree) {
        ExecutionResult<MessageResponse> result =
            checkAuthBasedOnParents(model, userTenantTree, contentTenantTree, AJEntityResource.TABLE_COURSE,
                model.getCourseId());
        if (result.continueProcessing()) {
            return result;
        }
        return checkAuthBasedOnParents(model, userTenantTree, contentTenantTree, AJEntityResource.TABLE_COLLECTION,
            model.getCollectionId());
    }

    private ExecutionResult<MessageResponse> checkAuthBasedOnParents(AJEntityResource model, TenantTree userTenantTree,
        TenantTree contentTenantTree, String table, String id) {
        ContentTenantAuthorization authorization;
        if (id != null) {
            try {
                long published = Base.count(table, AJEntityResource.PUBLISHED_FILTER, id);
                if (published >= 1) {
                    if (!model.isResourcePublished()) {
                        authorization = ContentTenantAuthorizationBuilder
                            .build(contentTenantTree, userTenantTree, ContentTreeAttributes.build(true));
                        if (authorization.canRead()) {
                            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
                        }
                    }
                }
            } catch (DBException e) {
                LOGGER.error("Error checking authorization for fetch for question '{}' in {} with id '{}'",
                    context.resourceId(), table, id, e);
                return new ExecutionResult<>(
                    MessageResponseFactory.createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }
        return new ExecutionResult<>(
            MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
            ExecutionResult.ExecutionStatus.FAILED);

    }
}
