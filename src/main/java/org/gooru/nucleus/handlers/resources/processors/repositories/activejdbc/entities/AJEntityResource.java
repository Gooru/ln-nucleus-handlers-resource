package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by ashish on 29/12/15.
 */
@Table("content")
public class AJEntityResource extends Model {

    public static final String RESOURCE_ID = "id";
    public static final String RESOURCE_TITLE = "title";
    public static final String RESOURCE_URL = "url";
    public static final String CREATOR_ID = "creator_id";
    public static final String ORIGINAL_CREATOR_ID = "original_creator_id";
    public static final String ORIGINAL_CONTENT_ID = "original_content_id";
    public static final String PUBLISH_DATE = "publish_date";
    public static final String NARRATION = "narration";
    public static final String DESCRIPTION = "description";
    public static final String CONTENT_FORMAT = "content_format";
    public static final String CONTENT_FORMAT_TYPE = "content_format_type";
    public static final String CONTENT_SUBFORMAT = "content_subformat";
    public static final String CONTENT_SUBFORMAT_TYPE = "content_subformat_type";
    public static final String METADATA = "metadata";
    public static final String TAXONOMY = "taxonomy";
    public static final String THUMBNAIL = "thumbnail";
    public static final String IS_COPYRIGHT_OWNER = "is_copyright_owner";
    public static final String COPYRIGHT_OWNER = "copyright_owner";
    public static final String RESOURCE_INFO = "info";
    public static final String VISIBLE_ON_PROFILE = "visible_on_profile";
    public static final String DISPLAY_GUIDE = "display_guide";
    public static final String ACCESSIBILITY = "accessibility";
    public static final String IS_DELETED = "is_deleted";
    public static final String MODIFIER_ID = "modifier_id";
    public static final String LICENSE = "license";

    public static final String COURSE_ID = "course_id";
    public static final String UNIT_ID = "unit_id";
    public static final String LESSON_ID = "lesson_id";
    public static final String COLLECTION_ID = "collection_id";
    public static final String SEQUENCE_ID = "sequence_id";

    public static final String CONTENT_FORMAT_RESOURCE = "resource";

    // jsonb fields relevant to resource
    public static final List<String> JSONB_FIELDS = new ArrayList<>(
        Arrays.asList(METADATA, TAXONOMY, COPYRIGHT_OWNER, RESOURCE_INFO, DISPLAY_GUIDE, ACCESSIBILITY));

    // jsonb fields relevant to resource
    public static final List<String> UUID_FIELDS = new ArrayList<>(Arrays
        .asList(RESOURCE_ID, CREATOR_ID, MODIFIER_ID, ORIGINAL_CONTENT_ID, ORIGINAL_CREATOR_ID, COURSE_ID, UNIT_ID,
            LESSON_ID, COLLECTION_ID));

    // not null fields in db
    public static final List<String> NOTNULL_FIELDS =
        new ArrayList<>(Arrays.asList(RESOURCE_TITLE, CREATOR_ID, MODIFIER_ID, ORIGINAL_CREATOR_ID, CONTENT_SUBFORMAT));

    // <TBD> - Need to decide
    // only owner (original creator of the resource) can change, which will have
    // to update all the copied records of the resource
    public static final List<String> OWNER_SPECIFIC_FIELDS = new ArrayList<>(Arrays
        .asList(RESOURCE_TITLE, RESOURCE_URL, DESCRIPTION, CONTENT_FORMAT, CONTENT_SUBFORMAT, RESOURCE_INFO,
            DISPLAY_GUIDE, ACCESSIBILITY, ORIGINAL_CONTENT_ID));

    public static final List<String> VALID_UPDATE_FIELDS = new ArrayList<>(Arrays
        .asList(RESOURCE_TITLE, RESOURCE_URL, ORIGINAL_CREATOR_ID, ORIGINAL_CONTENT_ID, PUBLISH_DATE, NARRATION,
            DESCRIPTION, METADATA, TAXONOMY, THUMBNAIL, RESOURCE_INFO, IS_COPYRIGHT_OWNER, COPYRIGHT_OWNER,
            VISIBLE_ON_PROFILE, RESOURCE_INFO, VISIBLE_ON_PROFILE, DISPLAY_GUIDE, ACCESSIBILITY, COURSE_ID, UNIT_ID,
            LESSON_ID, COLLECTION_ID, LICENSE));

    public static final List<String> COPY_UPDATE_FIELDS = Arrays.asList(TAXONOMY, NARRATION, RESOURCE_INFO, METADATA);

    public static final String FETCH_RESOURCE_BY_ID =
        " SELECT id, title, url, creator_id, modifier_id, narration, description, content_format, content_subformat, "
            + "metadata, taxonomy, original_content_id, original_creator_id, is_deleted, is_copyright_owner, "
            + "copyright_owner, visible_on_profile, thumbnail, info, display_guide, accessibility, course_id, "
            + "unit_id, lesson_id, collection_id, license FROM content WHERE id = ?::uuid AND content_format = "
            + "?::content_format_type AND is_deleted = false";

    public static final String FETCH_DUPLICATE_RESOURCES_BY_URL =
        "SELECT id FROM content WHERE url = ? AND content_format = ?::content_format_type AND original_content_id is "
            + "null AND is_deleted = false";

    public static final String FETCH_RESOURCE_TO_DELETE =
        "SELECT id, creator_id, original_content_id, original_creator_id, course_id, unit_id, lesson_id, "
            + "collection_id FROM content WHERE id=?::uuid AND content_format = ?::content_format_type AND is_deleted"
            + " = false";

    public static final String FILTER_FETCH_REFERENCES_OF_ORIGINAL =
        "content_format = 'resource'::content_format_type AND original_content_id = ?::uuid AND original_creator_id ="
            + " ?::uuid AND is_deleted = false";

    public static final String FETCH_REFERENCES_OF_ORIGINAL =
        " SELECT id, collection_id FROM content WHERE content_format = ?::content_format_type AND original_content_id"
            + " = ?::uuid AND is_deleted = false";

    // owner or collaborator at course or collection level are authorized to
    // delete the resource.
    public static final String TABLE_COURSE = "course";
    public static final String TABLE_COLLECTION = "collection";
    public static final String AUTH_VIA_COLLECTION_FILTER =
        "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?)";
    public static final String AUTH_VIA_COURSE_FILTER = "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?)";

    public static final String UPDATE_CONTAINER_TIMESTAMP =
        "update collection set updated_at = now() where id = ?::uuid and is_deleted = 'false'";
}
