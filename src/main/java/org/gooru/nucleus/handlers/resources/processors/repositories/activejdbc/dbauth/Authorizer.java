package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbauth;

import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;

/**
 * @author ashish on 17/10/16.
 */
public interface Authorizer<ResourceHolder> {

    ExecutionResult<MessageResponse> authorize(ResourceHolder model);

}
