package org.gooru.nucleus.handlers.resources.processors.commands;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;

/**
 * @author ashish on 2/1/17.
 */
class ResourceCreateProcessor extends AbstractCommandProcessor {
    public ResourceCreateProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {

    }

    @Override
    protected MessageResponse processCommand() {
        return RepoBuilder.buildResourceRepo(context).createResource();
    }
}
