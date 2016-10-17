package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.sql.SQLException;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public final class ResourceRetrieveHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRetrieveHelper.class);

    public ResourceRetrieveHelper() {
        throw new AssertionError();
    }

    public static AJEntityResource getResourceById(String resourceId) {
        try {
            PGobject contentFormat = new PGobject();
            contentFormat.setType(AJEntityResource.CONTENT_FORMAT_TYPE);
            contentFormat.setValue(AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);

            LazyList<AJEntityResource> result =
                AJEntityResource.findBySQL(AJEntityResource.SQL_GETRESOURCEBYID, resourceId, contentFormat);
            LOGGER.debug("getResourceById : {} ", result.toString());

            if (result.size() > 0) {
                if (result.size() > 1) {
                    LOGGER.error("getResourceById : {} GOT MORE RESULTS FOR THE SAME ID", result.toString());
                }
                return result.get(0);
            }

            LOGGER.warn("getResourceById : Resource with id : {} : not found", resourceId);
        } catch (SQLException se) {
            LOGGER.error("getResourceById : SQL Exception caught ! : {} ", se);
        }
        return null;
    }

    public static AJEntityResource getResourceDetailUpForDeletion(String resourceId) {
        LazyList<AJEntityResource> result = AJEntityResource
            .findBySQL(AJEntityResource.SQL_GETRESOURCEDETAILUPFORDELETION, resourceId,
                AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);
        LOGGER.debug("getResourceDetailUpForDeletion : {} ", result.toString());

        if (result.size() > 0) {
            if (result.size() > 1) {
                LOGGER.error("getResourceDetailUpForDeletion : {} GOT MORE RESULTS FOR THE SAME ID", result.toString());
            }
            return result.get(0);
        }

        LOGGER.warn("getResourceDetailUpForDeletion : Resource with id : {} : not found", resourceId);
        return null;
    }


    /*
     * getDuplicateResourcesByURL: returns NULL if no duplicates
     */

    public static JsonObject getDuplicateResourcesByURL(String inputURL) {
        JsonObject returnValue = null;

        try {
            PGobject contentFormat = new PGobject();
            contentFormat.setType(AJEntityResource.CONTENT_FORMAT_TYPE);
            contentFormat.setValue(AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);

            LazyList<AJEntityResource> result =
                AJEntityResource.findBySQL(AJEntityResource.SQL_GETDUPLICATERESOURCESBYURL, inputURL, contentFormat);
            LOGGER.debug("getDuplicateResourcesByURL ! : {} ", result.toString());

            if (result.size() > 0) {
                JsonArray retArray = new JsonArray();
                for (AJEntityResource model : result) {
                    retArray.add(model.get(AJEntityResource.RESOURCE_ID).toString());
                }
                returnValue = new JsonObject().put("duplicate_ids", retArray);
            }
        } catch (SQLException se) {
            LOGGER.error("getDuplicateResourcesByURL ! : {} ", se);
        }
        return returnValue;
    }

    public static JsonObject getCopiesOfAResource(AJEntityResource resource, String originalResourceId) {
        JsonObject returnValue = null;

        TypeHelper.setPGObject(resource, AJEntityResource.CONTENT_FORMAT, AJEntityResource.CONTENT_FORMAT_TYPE,
            AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);

        LazyList<AJEntityResource> result = AJEntityResource
            .findBySQL(AJEntityResource.SQL_GETCOPIESOFARESOURCE, AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE,
                originalResourceId);
        if (result.size() > 0) {
            JsonArray idArray = new JsonArray();
            JsonArray collectionIdArray = new JsonArray();
            String collectionId;
            for (AJEntityResource model : result) {
                idArray.add(model.get(AJEntityResource.RESOURCE_ID).toString());
                collectionId = model.getString(AJEntityResource.COLLECTION_ID);
                if (collectionId != null && !collectionId.isEmpty()) {
                    collectionIdArray.add(collectionId);
                }
            }
            returnValue = new JsonObject().put("resource_copy_ids", idArray)
                .put(AJEntityResource.COLLECTION_ID, collectionIdArray).put("id", originalResourceId);
            LOGGER.debug("getCopiesOfAResource ! : {} ", returnValue.toString());
        }
        return returnValue;
    }
}