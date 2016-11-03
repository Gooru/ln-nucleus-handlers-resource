package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.util.Map;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.EntityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public final class DBHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBHelper.class);

    private DBHelper() {
        throw new AssertionError();
    }

    /*
     * populateEntityFromJson : throws exceptions
     */
    public static void populateEntityFromJson(JsonObject inputJson, AJEntityResource resource)
        throws IllegalArgumentException {
        String mapValue;

        for (Map.Entry<String, Object> entry : inputJson) {
            mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;

            if (AJEntityResource.NOTNULL_FIELDS.contains(entry.getKey())) {
                if (mapValue == null || mapValue.isEmpty()) {
                    throw new IllegalArgumentException("Null value input for : " + entry.getKey());
                }
            }

            if (AJEntityResource.CONTENT_FORMAT.equalsIgnoreCase(entry.getKey())) {
                if (!AJEntityResource.CONTENT_FORMAT_RESOURCE.equalsIgnoreCase(mapValue)) {
                    throw new IllegalArgumentException(
                        "content format should always be a 'resource' but {} has been sent: " + mapValue);
                } else {
                    TypeHelper
                        .setPGObject(resource, AJEntityResource.CONTENT_FORMAT, AJEntityResource.CONTENT_FORMAT_TYPE,
                            mapValue);
                }
            } else if (AJEntityResource.CONTENT_SUBFORMAT.equalsIgnoreCase(entry.getKey())) {
                if (mapValue == null || mapValue.isEmpty() || !mapValue
                    .endsWith(AJEntityResource.CONTENT_FORMAT_RESOURCE)) {
                    throw new IllegalArgumentException(
                        "content sub format is not a valid resource format ; {} has been sent: " + mapValue);
                } else {
                    TypeHelper.setPGObject(resource, AJEntityResource.CONTENT_SUBFORMAT,
                        AJEntityResource.CONTENT_SUBFORMAT_TYPE, mapValue);
                }
            } else {
                if (AJEntityResource.JSONB_FIELDS.contains(entry.getKey())) {
                    TypeHelper.setPGObject(resource, entry.getKey(), EntityConstants.JSONB_FORMAT, mapValue);
                } else if (AJEntityResource.UUID_FIELDS.contains(entry.getKey())) {
                    TypeHelper.setPGObject(resource, entry.getKey(), EntityConstants.UUID_TYPE, mapValue);
                } else {
                    resource.set(entry.getKey(), entry.getValue());
                }
            }
        }
    }

}
