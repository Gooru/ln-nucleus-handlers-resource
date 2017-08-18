package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 07-Aug-2017
 */
public final class ResourceLinkoutHelper {

    private static final String IS_FRAME_BREAKER = "is_frame_breaker";

    private ResourceLinkoutHelper() {
        throw new AssertionError();
    }

    public static void populateLinkout(AJEntityOriginalResource resource, JsonObject request) {
        JsonObject displayGuide = request.getJsonObject(AJEntityOriginalResource.DISPLAY_GUIDE);
        if (displayGuide != null && !displayGuide.isEmpty()) {
            if (displayGuide.containsKey(AJEntityOriginalResource.IS_BROKEN)) {
                int broken = displayGuide.getInteger(AJEntityOriginalResource.IS_BROKEN);
                resource.setBoolean(AJEntityOriginalResource.IS_BROKEN, (broken == 1) ? true : false);
            }

            if (displayGuide.containsKey(IS_FRAME_BREAKER)) {
                int iFrameBreaker = displayGuide.getInteger(IS_FRAME_BREAKER);
                resource.setBoolean(AJEntityOriginalResource.IS_IFRAME_BREAKER, (iFrameBreaker == 1) ? true : false);
            }
        }
    }
}
