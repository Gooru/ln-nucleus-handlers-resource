package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;

import io.vertx.core.json.JsonObject;

class AJResponseJsonTransformer {
   
  public JsonObject transform(String ajResult) {
    JsonObject result = new JsonObject(ajResult);
    if (ajResult == null || ajResult.isEmpty()) {
      return result;
    }
    
    for (String fieldName: ResourceRepo.JSONB_FIELDS) {
      String valueToXform = result.getString(fieldName);
      if (valueToXform != null && !valueToXform.isEmpty()) {
        JsonObject xformedValue = new JsonObject(valueToXform);
        result.remove(fieldName);
        result.put(fieldName, xformedValue);
      }
    }
    return result;
  }
}
