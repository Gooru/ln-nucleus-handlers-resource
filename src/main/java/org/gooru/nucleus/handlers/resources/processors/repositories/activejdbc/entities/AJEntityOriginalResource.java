package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by ashish on 17/10/2016.
 */
@Table("original_resource")
public class AJEntityOriginalResource extends Model {
	
	// Field names
    public static final String RESOURCE_ID = "id";
    public static final String RESOURCE_TITLE = "title";
    public static final String RESOURCE_URL = "url";
    public static final String IS_REMOTE = "is_remote";
    public static final String HTTP_PROTOCOL = "http_protocol";
    public static final String HTTP_HOST = "http_host";
    public static final String HTTP_PORT = "http_port";
    public static final String HTTP_DOMAIN = "http_domain";
    public static final String HTTP_PATH = "http_path";
    public static final String HTTP_QUERY = "http_query";
    public static final String IS_BROKEN = "is_broken";
    public static final String IS_IFRAME_BREAKER = "is_iframe_breaker";
    public static final String IFRAME_BREAKER_REASON = "iframe_breaker_reason";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String CREATOR_ID = "creator_id";
    public static final String MODIFIER_ID = "modifier_id";
    public static final String PUBLISH_DATE = "publish_date";
    public static final String PUBLISH_STATUS = "publish_status";
    public static final String SUBJECT = "subject";
    public static final String LANGUAGE = "language";
    public static final String NARRATION = "narration";
    public static final String DESCRIPTION = "description";
    public static final String CONTENT_SUBFORMAT = "content_subformat";
    public static final String CONTENT_SUBFORMAT_TYPE = "content_subformat_type";
    public static final String AUDIENCE = "audience";
    public static final String EDUCATIONAL_USE = "educational_use";
    public static final String METADATA = "metadata";
    public static final String TAXONOMY = "taxonomy";
    public static final String GUT_CODES = "gut_codes";
    public static final String THUMBNAIL = "thumbnail";
    public static final String IS_COPYRIGHT_OWNER = "is_copyright_owner";
    public static final String COPYRIGHT_OWNER = "copyright_owner";
    public static final String RESOURCE_INFO = "info";
    public static final String VISIBLE_ON_PROFILE = "visible_on_profile";
    public static final String DISPLAY_GUIDE = "display_guide";
    public static final String ACCESSIBILITY = "accessibility";
    public static final String IS_DELETED = "is_deleted";
    public static final String EDITORIAL_FLAGS = "editorial_flags";
    public static final String LICENSE = "license";
    public static final String CREATOR_SYSTEM = "creator_system";
    public static final String VALID_CONTENT_FORMAT_FOR_RESOURCE = "resource";
    public static final String JSONB_FORMAT = "jsonb";
    public static final String UUID_TYPE = "uuid";
}
