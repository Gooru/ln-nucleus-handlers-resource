package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.sql.SQLException;
import java.util.Objects;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.ResourceHolder;
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
                AJEntityResource.findBySQL(AJEntityResource.FETCH_RESOURCE_BY_ID, resourceId, contentFormat);
            LOGGER.debug("getResourceById : {} ", result.toString());

            if (!result.isEmpty()) {
                return result.get(0);
            }

            LOGGER.warn("getResourceById : Resource with id : {} : not found", resourceId);
        } catch (SQLException se) {
            LOGGER.error("getResourceById : SQL Exception caught ! : {} ", se);
        }
        return null;
    }

    public static ResourceHolder getResourceToDelete(String resourceId) {
        Objects.requireNonNull(resourceId);
        ResourceHolder resourceHolder = getResourceRefToDelete(resourceId);
        if (resourceHolder != null) {
            return resourceHolder;
        }
        return getOriginalResourceToDelete(resourceId);
    }

    private static ResourceHolder getOriginalResourceToDelete(String resourceId) {
        AJEntityOriginalResource resource;
        LazyList<AJEntityOriginalResource> result =
            AJEntityOriginalResource.findBySQL(AJEntityOriginalResource.FETCH_RESOURCE_FOR_DELETE, resourceId);
        if (!result.isEmpty()) {
            resource = result.get(0);
            return new ResourceHolder(resource);
        }
        return null;
    }

    private static ResourceHolder getResourceRefToDelete(String resourceId) {
        AJEntityResource resource;
        LazyList<AJEntityResource> result = AJEntityResource
            .findBySQL(AJEntityResource.FETCH_RESOURCE_TO_DELETE, resourceId,
                AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);

        if (!result.isEmpty()) {
            resource = result.get(0);
            return new ResourceHolder(resource);
        }
        return null;
    }


    public static JsonObject getDuplicateResourcesByUrl(String inputUrl) {
        JsonObject returnValue = null;

        try {
            PGobject contentFormat = new PGobject();
            contentFormat.setType(AJEntityResource.CONTENT_FORMAT_TYPE);
            contentFormat.setValue(AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);

            LazyList<AJEntityResource> result =
                AJEntityResource.findBySQL(AJEntityResource.FETCH_DUPLICATE_RESOURCES_BY_URL, inputUrl, contentFormat);
            LOGGER.debug("getDuplicateResourcesByURL ! : {} ", result.toString());

            if (!result.isEmpty()) {
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

    public static JsonObject getCopiesOfAResource(AJEntityOriginalResource resource, String originalResourceId) {
        JsonObject returnValue = null;

        TypeHelper.setPGObject(resource, AJEntityResource.CONTENT_FORMAT, AJEntityResource.CONTENT_FORMAT_TYPE,
            AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);

        LazyList<AJEntityResource> result = AJEntityResource.findBySQL(AJEntityResource.FETCH_REFERENCES_OF_ORIGINAL,
            AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE, originalResourceId);
        if (!result.isEmpty()) {
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