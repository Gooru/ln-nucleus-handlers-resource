package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities;

import java.util.*;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by ashish on 17/10/2016.
 */
@Table("original_resource")
public class AJEntityOriginalResource extends Model {

    // Field names
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String URL = "url";
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

    private static final String IFRAME_BREAKER_REASON_TYPE_NAME = "iframe_breaker_type";
    private static final List<String> RESOURCE_TYPES = Arrays
        .asList("video_resource", "webpage_resource", "interactive_resource", "image_resource", "text_resource",
            "audio_resource");
    private static final List<String> IFRAME_BREAKER_TYPES =
        Arrays.asList("x-frame-options", "frame-breaker", "csp-frame-ancestors");
    public static final String HTTP_PROTOCOL_TYPE = "http_protocol_type";

    public static final String FETCH_RESOURCE_FOR_DELETE =
        "select id, creator_id from original_resource where id = ?::uuid and is_deleted = false";
    public static final String FETCH_DUPLICATE_RESOURCES =
        "SELECT id FROM original_resource WHERE http_domain = ? AND http_path = ? AND http_query = ? AND is_deleted ="
            + " false";
    public static final String FETCH_RESOURCE =
        "select id, title, url, is_remote, http_domain, is_broken, is_iframe_breaker, "
            + "iframe_breaker_reason, creator_id, modifier_id, narration, description, publish_status, publish_date, "
            + "subject, language, narration, description, content_subformat, audience, educational_use, metadata, "
            + "taxonomy, thumbnail, is_copyright_owner, copyright_owner, info, visible_on_profile, display_guide,"
            + "accessibility, license, creator_system from original_resource where id = ?::uuid and is_deleted = false";

    public static final String FETCH_RESOURCE_FOR_BROKEN_DETECTION = "select is_remote, http_domain, is_broken, "
        + "is_iframe_breaker from original_resource where id = ?::uuid and is_deleted = false";

    public static final Set<String> EDITABLE_FIELDS = new HashSet<>(Arrays
        .asList(TITLE, URL, IS_REMOTE, IS_BROKEN, IS_IFRAME_BREAKER, IFRAME_BREAKER_REASON, SUBJECT, LANGUAGE,
            NARRATION, DESCRIPTION, CONTENT_SUBFORMAT, AUDIENCE, EDUCATIONAL_USE, THUMBNAIL, METADATA, TAXONOMY,
            IS_COPYRIGHT_OWNER, COPYRIGHT_OWNER, RESOURCE_INFO, VISIBLE_ON_PROFILE, DISPLAY_GUIDE, ACCESSIBILITY,
            LICENSE, CREATOR_SYSTEM));
    public static final Set<String> CREATABLE_FIELDS = EDITABLE_FIELDS;

    public static final Set<String> MANDATORY_FIELDS = new HashSet<>(Arrays.asList(TITLE));

    private static final Map<String, FieldValidator> validatorRegistry;
    private static final Map<String, FieldConverter> converterRegistry;

    static {
        validatorRegistry = initializeValidators();
        converterRegistry = initializeConverters();
    }

    private static Map<String, FieldConverter> initializeConverters() {
        Map<String, FieldConverter> converterMap = new HashMap<>();
        converterMap.put(ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(CONTENT_SUBFORMAT,
            (fieldValue -> FieldConverter.convertFieldToNamedType(fieldValue, CONTENT_SUBFORMAT_TYPE)));
        converterMap.put(IFRAME_BREAKER_REASON,
            (fieldValue -> FieldConverter.convertFieldToNamedType(fieldValue, IFRAME_BREAKER_REASON_TYPE_NAME)));
        converterMap.put(SUBJECT, (FieldConverter::convertFieldToJson));
        converterMap.put(METADATA, (FieldConverter::convertFieldToJson));
        converterMap.put(TAXONOMY, (FieldConverter::convertFieldToJson));
        converterMap.put(AUDIENCE, (FieldConverter::convertFieldToIntArray));
        converterMap.put(EDUCATIONAL_USE, (FieldConverter::convertFieldToIntArray));
        converterMap.put(GUT_CODES, (FieldConverter::convertFieldToTextArray));
        converterMap.put(CREATOR_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(MODIFIER_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(COPYRIGHT_OWNER, (FieldConverter::convertFieldToJson));
        converterMap.put(RESOURCE_INFO, (FieldConverter::convertFieldToJson));
        converterMap.put(DISPLAY_GUIDE, (FieldConverter::convertFieldToJson));
        converterMap.put(ACCESSIBILITY, (FieldConverter::convertFieldToJson));

        return Collections.unmodifiableMap(converterMap);
    }

    private static Map<String, FieldValidator> initializeValidators() {
        Map<String, FieldValidator> validatorMap = new HashMap<>();
        validatorMap.put(ID, (FieldValidator::validateUuid));
        validatorMap.put(TITLE, (value) -> FieldValidator.validateString(value, 1000));
        validatorMap.put(URL, (value) -> FieldValidator.validateStringIfPresent(value, 2000));
        validatorMap.put(LANGUAGE, (value) -> FieldValidator.validateStringIfPresent(value, 255));
        validatorMap.put(NARRATION, (value) -> FieldValidator.validateStringIfPresent(value, 5000));
        validatorMap.put(DESCRIPTION, (value) -> FieldValidator.validateStringIfPresent(value, 20000));
        validatorMap.put(THUMBNAIL, (value) -> FieldValidator.validateStringIfPresent(value, 2000));
        validatorMap.put(METADATA, FieldValidator::validateJsonIfPresent);
        validatorMap.put(TAXONOMY, FieldValidator::validateJsonIfPresent);
        validatorMap.put(SUBJECT, FieldValidator::validateJsonIfPresent);
        validatorMap.put(COPYRIGHT_OWNER, FieldValidator::validateJsonIfPresent);
        validatorMap.put(RESOURCE_INFO, FieldValidator::validateJsonIfPresent);
        validatorMap.put(DISPLAY_GUIDE, FieldValidator::validateJsonIfPresent);
        validatorMap.put(ACCESSIBILITY, FieldValidator::validateJsonIfPresent);
        validatorMap.put(VISIBLE_ON_PROFILE, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(IS_BROKEN, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(IS_REMOTE, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(IS_IFRAME_BREAKER, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(IS_COPYRIGHT_OWNER, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(CONTENT_SUBFORMAT, RESOURCE_TYPES::contains);
        validatorMap.put(IFRAME_BREAKER_REASON, IFRAME_BREAKER_TYPES::contains);
        return Collections.unmodifiableMap(validatorMap);
    }

    public static FieldSelector createFieldSelector() {
        return new FieldSelector() {
            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(CREATABLE_FIELDS);
            }

            @Override
            public Set<String> mandatoryFields() {
                return Collections.unmodifiableSet(MANDATORY_FIELDS);
            }
        };
    }

    public static FieldSelector editFieldSelector() {
        return () -> Collections.unmodifiableSet(EDITABLE_FIELDS);
    }

    public static ValidatorRegistry getValidatorRegistry() {
        return new OriginalResourceValidatorRegistry();
    }

    public static ConverterRegistry getConverterRegistry() {
        return new OriginalResourceConverterRegistry();
    }

    private static class OriginalResourceValidatorRegistry implements ValidatorRegistry {
        @Override
        public FieldValidator lookupValidator(String fieldName) {
            return validatorRegistry.get(fieldName);
        }
    }

    private static class OriginalResourceConverterRegistry implements ConverterRegistry {
        @Override
        public FieldConverter lookupConverter(String fieldName) {
            return converterRegistry.get(fieldName);
        }
    }

}
