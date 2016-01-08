package org.gooru.nucleus.handlers.resources.processors.repositories;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.ActiveJdbcRepoBuilder;

/**
 * Created by ashish on 29/12/15.
 */
public class RepoBuilder {
  public ResourceRepo buildResourceRepo() {
    return new ActiveJdbcRepoBuilder().buildResourceRepo();
  }

}
