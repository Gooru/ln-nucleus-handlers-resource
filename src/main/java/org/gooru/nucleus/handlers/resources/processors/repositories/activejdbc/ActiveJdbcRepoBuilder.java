package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc;


import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 29/12/15.
 */
public class ActiveJdbcRepoBuilder {

  public ResourceRepo buildResourceRepo(String userId, JsonObject prefs) {
    return new AJResourceRepo(userId, prefs);
  }

}
