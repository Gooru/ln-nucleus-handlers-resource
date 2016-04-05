package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;

/**
 * Created by ashish on 7/1/16.
 */
public final class AJRepoBuilder {

  public static ResourceRepo buildResourceRepo(ProcessorContext context) {
    return new AJResourceRepo(context);
  }

  private AJRepoBuilder() {
    throw new AssertionError();
  }

}
