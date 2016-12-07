package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbauth;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.ResourceHolder;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;

/**
 * @author ashish on 17/10/16.
 */
public final class AuthorizerBuilder {

    private AuthorizerBuilder() {
        throw new AssertionError();
    }

    public static Authorizer<ResourceHolder> buildUpdateAuthorizer(ProcessorContext context) {
        return new UpdateAuthorizer(context);
    }

    public static Authorizer<ResourceHolder> buildDeleteAuthorizer(ProcessorContext context) {
        return new DeleteAuthorizer(context);
    }

    public static Authorizer<ResourceHolder> buildCreateAuthorizer(ProcessorContext context) {
        // Upstream checks should make sure that user is not anonymous
        return model -> new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

}
