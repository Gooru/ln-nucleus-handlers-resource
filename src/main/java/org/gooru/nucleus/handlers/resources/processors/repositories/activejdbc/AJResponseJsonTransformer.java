package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc;

import io.vertx.core.json.JsonObject;

class AJResponseJsonTransformer {
  private static final String [] JSON_FIELDS_TO_XFORM = {"metadata", "taxonomy", "depth_of_knowledge", "copyright_owner"};
  
  public JsonObject transform(String ajResult) {
    JsonObject result = new JsonObject(ajResult);
    if (ajResult == null || ajResult.isEmpty()) {
      return result;
    }
    
    for (String fieldName: JSON_FIELDS_TO_XFORM) {
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
