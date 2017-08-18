package org.gooru.nucleus.handlers.resources.processors.commands;

import static org.gooru.nucleus.handlers.resources.processors.utils.ValidationUtils.isIdInvalid;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 2/1/17.
 */
class ResourceUpdateProcessor extends AbstractCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUpdateProcessor.class);

    public ResourceUpdateProcessor(ProcessorContext context) {
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
        if (context.request() == null || context.request().isEmpty()) {
            LOGGER.error("Invalid request, json not available. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid Json");
        }
        return RepoBuilder.buildResourceRepo(context).updateResource();
    }
}
