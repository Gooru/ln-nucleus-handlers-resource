package org.gooru.nucleus.handlers.resources.processors.commands;

import static org.gooru.nucleus.handlers.resources.processors.utils.ValidationUtils.isIdInvalid;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;

/**
 * @author ashish on 2/1/17.
 */
class ResourceDeleteProcessor extends AbstractCommandProcessor {
    public ResourceDeleteProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {

    }

    @Override
    protected MessageResponse processCommand() {
        if (isIdInvalid(context)) {
            return MessageResponseFactory.createInvalidRequestResponse("Invalid request id");
        }
        return RepoBuilder.buildResourceRepo(context).deleteResource();
    }
}
