package org.gooru.nucleus.handlers.resources.processors.repositories;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.AJRepoBuilder;

/**
 * Created by ashish on 29/12/15.
 */
public class RepoBuilder {

  public ResourceRepo buildResourceRepo(ProcessorContext context) {
    return new AJRepoBuilder().buildResourceRepo(context);
  }

}
