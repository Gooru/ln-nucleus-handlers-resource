package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.sql.Array;
import java.sql.SQLException;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public final class FetchResourceResponseDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchResourceResponseDecorator.class);
    private static final String IS_FRAME_BREAKER = "is_frame_breaker";

    private FetchResourceResponseDecorator() {
        throw new AssertionError();
    }

    public static void processOriginalResourceFetchResponse(AJEntityOriginalResource resource, JsonObject result) {
        decorateMetadataForOriginalResource(resource, result);
        // bundle is_frame_breaker and is_broken into display_guide
        decorateDisplayGuideForOriginalResource(resource, result);
        // handle framebreaking based on either self or domain
        rectifyFrameBreakingAttribute(resource, result);
    }

    private static void rectifyFrameBreakingAttribute(AJEntityOriginalResource resource, JsonObject result) {
        boolean isRemote = resource.getBoolean(AJEntityOriginalResource.IS_REMOTE);
        if (!isRemote) {
            return;
        }
        boolean isFrameBreaker = resource.getBoolean(AJEntityOriginalResource.IS_IFRAME_BREAKER);
        if (isFrameBreaker) {
            return;
        }
        String domain = resource.getString(AJEntityOriginalResource.HTTP_DOMAIN);
        if (domain == null) {
            return;
        }
        isFrameBreaker = isDomainFrameBreaker(domain);
        if (!isFrameBreaker) {
            return;
        }
        JsonObject displayGuide = result.getJsonObject(AJEntityOriginalResource.DISPLAY_GUIDE);
        if (displayGuide == null) {
            displayGuide = new JsonObject();
        }
        displayGuide.put(IS_FRAME_BREAKER, isFrameBreaker ? 1 : 0);
    }

    private static boolean isDomainFrameBreaker(String domain) {
        long count = Base.count("framebreaker_domain", "domain = ?", domain);
        return count > 0;
    }

    private static void decorateDisplayGuideForOriginalResource(AJEntityOriginalResource resource, JsonObject result) {
        populateDisplayGuideFromDbField(resource, result, AJEntityOriginalResource.IS_BROKEN);
        populateDisplayGuideFromDbField(resource, result, AJEntityOriginalResource.IS_IFRAME_BREAKER, IS_FRAME_BREAKER);
    }

    private static void populateDisplayGuideFromDbField(AJEntityOriginalResource resource, JsonObject result,
        String fieldName) {
        populateDisplayGuideFromDbField(resource, result, fieldName, fieldName);
    }

    private static void populateDisplayGuideFromDbField(AJEntityOriginalResource resource, JsonObject result,
        String fieldName, String targetFieldName) {
        boolean fieldValue = resource.getBoolean(fieldName);
        JsonObject displayGuide = result.getJsonObject(AJEntityOriginalResource.DISPLAY_GUIDE);
        if (displayGuide == null) {
            displayGuide = new JsonObject();
            result.put(AJEntityOriginalResource.DISPLAY_GUIDE, displayGuide);
        }
        displayGuide.put(targetFieldName, fieldValue ? 1 : 0);
    }

    private static void decorateMetadataForOriginalResource(AJEntityOriginalResource resource, JsonObject result) {
        populateMetadataFromDbField(resource, result, AJEntityOriginalResource.EDUCATIONAL_USE);
        populateMetadataFromDbField(resource, result, AJEntityOriginalResource.AUDIENCE);
    }

    private static void populateMetadataFromDbField(AJEntityOriginalResource resource, JsonObject result,
        String dbFieldName) {
        Object dbFieldValue = resource.get(dbFieldName);
        if (dbFieldValue != null) {
            try {
                Integer[] intArray = (Integer[]) (((Array) dbFieldValue).getArray());
                if (intArray != null && intArray.length > 0) {
                    JsonObject metadata = result.getJsonObject(AJEntityOriginalResource.METADATA);
                    if (metadata == null) {
                        metadata = new JsonObject();
                    }
                    JsonArray fieldValue = new JsonArray();
                    for (Integer anIntArray : intArray) {
                        fieldValue.add(anIntArray);
                    }
                    metadata.put(dbFieldName, fieldValue);
                }
            } catch (SQLException e) {
                LOGGER.warn("Not able to convert JDBC array to Java array for field: {}", dbFieldName);
            }
        }
    }

    public static void processResourceRefFetchResponse(AJEntityResource resource, JsonObject result) {

        String originalContentId = resource.getString(AJEntityResource.ORIGINAL_CONTENT_ID);
        if (originalContentId == null) {
            LOGGER.warn("Resource reference does not have original content id: {}",
                result.getString(AJEntityResource.ID));
            return;
        }
        LazyList<AJEntityOriginalResource> originalResources = AJEntityOriginalResource
            .findBySQL(AJEntityOriginalResource.FETCH_RESOURCE_FOR_BROKEN_DETECTION, originalContentId);
        if (originalResources.isEmpty()) {
            LOGGER.warn("Not able to find original resource with id: {}", originalContentId);
            return;
        }
        AJEntityOriginalResource originalResource = originalResources.get(0);

        JsonObject displayGuide = result.getJsonObject(AJEntityResource.DISPLAY_GUIDE);
        if (displayGuide == null) {
            displayGuide = new JsonObject();
            result.put(AJEntityResource.DISPLAY_GUIDE, displayGuide);
        }
        boolean isRemote = originalResource.getBoolean(AJEntityOriginalResource.IS_REMOTE);
        if (isRemote) {
            processResourceRefBrokenStatus(resource, originalResource, result, displayGuide);
            processResourceRefFrameBreakerStatus(resource, originalResource, result, displayGuide);

        }

    }

    private static void processResourceRefFrameBreakerStatus(AJEntityResource resource,
        AJEntityOriginalResource originalResource, JsonObject result, JsonObject displayGuide) {

        int isFrameBreaker = displayGuide.getInteger(IS_FRAME_BREAKER, 0);
        if (isFrameBreaker == 1) {
            return;
        }
        
        boolean originalIsFrameBreaker = originalResource.getBoolean(AJEntityOriginalResource.IS_IFRAME_BREAKER);
        if (originalIsFrameBreaker) {
            displayGuide.put(IS_FRAME_BREAKER, 1);
            return;
        }
        String domain = originalResource.getString(AJEntityOriginalResource.HTTP_DOMAIN);
        if (isDomainFrameBreaker(domain)) {
            displayGuide.put(IS_FRAME_BREAKER, 1);
        }
    }

    private static void processResourceRefBrokenStatus(AJEntityResource resource,
        AJEntityOriginalResource originalResource, JsonObject result, JsonObject displayGuide) {
        int isBroken = displayGuide.getInteger(AJEntityOriginalResource.IS_BROKEN, 0);
        if (isBroken == 1) {
            return;
        }
        
        boolean originalIsBroken = originalResource.getBoolean(AJEntityOriginalResource.IS_BROKEN);
        if (!originalIsBroken) {
            return;
        }
        displayGuide.put(AJEntityOriginalResource.IS_BROKEN, isBroken);
    }
}