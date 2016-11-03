package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author ashish on 1/11/16.
 */
public final class ResourceTaxonomyHelper {

    private ResourceTaxonomyHelper() {
        throw new AssertionError();
    }

    public static void populateGutCodes(AJEntityOriginalResource resource, JsonObject request) {
        JsonObject taxonomy = request.getJsonObject(AJEntityOriginalResource.TAXONOMY);
        if (taxonomy != null && !taxonomy.isEmpty()) {
            JsonArray result = new JsonArray();
            taxonomy.fieldNames().forEach(result::add);
            request.put(AJEntityOriginalResource.GUT_CODES, result);
        }
    }
}

