package org.gooru.nucleus.handlers.resources.processors.repositories;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.AJRepoBuilder;

/**
 * Created by ashish on 29/12/15.
 */
public final class RepoBuilder {

    public static ResourceRepo buildResourceRepo(ProcessorContext context) {
        return AJRepoBuilder.buildResourceRepo(context);
    }

    private RepoBuilder() {
        throw new AssertionError();
    }
}
