package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities;

import static org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ashish on 2/11/16.
 */
public final class EntityConstants {

    private EntityConstants() {
        throw new AssertionError();
    }

    public static final String JSONB_FORMAT = "jsonb";
    public static final String UUID_TYPE = "uuid";

    public static final List<String> RESOURCE_FETCH_FIELDS = new ArrayList<>(Arrays
        .asList(RESOURCE_ID, RESOURCE_TITLE, RESOURCE_URL, CREATOR_ID, MODIFIER_ID, ORIGINAL_CREATOR_ID,
            ORIGINAL_CONTENT_ID, PUBLISH_DATE, NARRATION, DESCRIPTION, CONTENT_SUBFORMAT, METADATA, TAXONOMY, THUMBNAIL,
            RESOURCE_INFO, IS_COPYRIGHT_OWNER, COPYRIGHT_OWNER, VISIBLE_ON_PROFILE, RESOURCE_INFO, VISIBLE_ON_PROFILE,
            DISPLAY_GUIDE, ACCESSIBILITY, COURSE_ID, UNIT_ID, LESSON_ID, COLLECTION_ID, LICENSE));

}
