package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.util.Arrays;
import java.util.List;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author ashish on 2/11/16.
 */
public final class ResourceMetadataHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceMetadataHelper.class);

    private static final List<String> METADATA_FIELDS_TO_FLATTEN =
        Arrays.asList(AJEntityOriginalResource.AUDIENCE, AJEntityOriginalResource.EDUCATIONAL_USE);

    private static void populateFields(JsonObject request, JsonObject metadata) {
        METADATA_FIELDS_TO_FLATTEN.forEach(field -> {
            if (request.getJsonArray(field) == null) {
                JsonArray result = metadata.getJsonArray(field);
                if (result != null && !result.isEmpty()) {
                    request.put(field, result);
                }
            }
        });
    }

    public static void flattenMetadataFields(JsonObject request) {
        JsonObject metadata = request.getJsonObject(AJEntityOriginalResource.METADATA);
        populateFields(request, metadata);
    }

    private ResourceMetadataHelper() {
        throw new AssertionError();
    }
}
