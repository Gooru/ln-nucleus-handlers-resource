package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc;


import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;

/**
 * Created by ashish on 29/12/15.
 */
public class ActiveJdbcRepoBuilder {

  public ResourceRepo buildResourceRepo() {
    return new AJResourceRepo();
  }

}
