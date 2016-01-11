package org.gooru.nucleus.handlers.resources.processors.repositories;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.ActiveJdbcRepoBuilder;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 29/12/15.
 */
public class RepoBuilder {
  public ResourceRepo buildResourceRepo(String userId, JsonObject prefs) {
    return new ActiveJdbcRepoBuilder().buildResourceRepo(userId, prefs);
  }

}
