package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbauth;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.ResourceHolder;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;

/**
 * @author ashish on 17/10/16.
 */
final class UpdateAuthorizer implements Authorizer<ResourceHolder> {

    private final ProcessorContext context;

    public UpdateAuthorizer(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> authorize(ResourceHolder model) {
        return null;
    }
}
