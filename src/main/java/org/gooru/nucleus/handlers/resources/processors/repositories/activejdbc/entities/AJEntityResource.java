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
 * Created by ashish on 29/12/15.
 */
@Table("content")
public class AJEntityResource extends Model {

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String URL = "url";
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
    public static final String INFO = "info";
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

    // only owner (original creator of the resource) can change, which will have
    // to update all the copied records of the resource
    public static final List<String> OWNER_SPECIFIC_FIELDS =
        new ArrayList<>(Arrays.asList(TITLE, URL, DESCRIPTION, CONTENT_SUBFORMAT, INFO, DISPLAY_GUIDE, ACCESSIBILITY));
    public static final List<String> OWNER_SPECIFIC_FIELDS_TYPES = new ArrayList<>(Arrays
        .asList(null, null, null, CONTENT_SUBFORMAT_TYPE, EntityConstants.JSONB_FORMAT, EntityConstants.JSONB_FORMAT,
            EntityConstants.JSONB_FORMAT));

    public static final Set<String> EDITABLE_FIELDS =
        new HashSet<>(Arrays.asList(NARRATION, THUMBNAIL, METADATA, TAXONOMY, INFO, DISPLAY_GUIDE, ACCESSIBILITY));

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
    public static final String TABLE_RESOURCE = "content";
    public static final String TABLE_COLLECTION = "collection";
    public static final String AUTH_VIA_COLLECTION_FILTER =
        "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?)";
    public static final String AUTH_VIA_COURSE_FILTER = "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?)";

    public static final String UPDATE_CONTAINER_TIMESTAMP =
        "update collection set updated_at = now() where id = ?::uuid and is_deleted = 'false'";

    // jsonb fields relevant to resource
    public static final List<String> JSONB_FIELDS =
        new ArrayList<>(Arrays.asList(METADATA, TAXONOMY, COPYRIGHT_OWNER, INFO, DISPLAY_GUIDE, ACCESSIBILITY));

    // jsonb fields relevant to resource
    public static final List<String> UUID_FIELDS = new ArrayList<>(Arrays
        .asList(ID, CREATOR_ID, MODIFIER_ID, ORIGINAL_CONTENT_ID, ORIGINAL_CREATOR_ID, COURSE_ID, UNIT_ID, LESSON_ID,
            COLLECTION_ID));

    // not null fields in db
    public static final List<String> NOTNULL_FIELDS =
        new ArrayList<>(Arrays.asList(TITLE, CREATOR_ID, MODIFIER_ID, ORIGINAL_CREATOR_ID, CONTENT_SUBFORMAT));

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
        converterMap.put(METADATA, (FieldConverter::convertFieldToJson));
        converterMap.put(TAXONOMY, (FieldConverter::convertFieldToJson));
        converterMap.put(CREATOR_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(MODIFIER_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(INFO, (FieldConverter::convertFieldToJson));
        converterMap.put(DISPLAY_GUIDE, (FieldConverter::convertFieldToJson));
        converterMap.put(ACCESSIBILITY, (FieldConverter::convertFieldToJson));

        return Collections.unmodifiableMap(converterMap);
    }

    private static Map<String, FieldValidator> initializeValidators() {
        Map<String, FieldValidator> validatorMap = new HashMap<>();
        validatorMap.put(ID, (FieldValidator::validateUuid));
        validatorMap.put(TITLE, (value) -> FieldValidator.validateString(value, 1000));
        validatorMap.put(URL, (value) -> FieldValidator.validateStringIfPresent(value, 2000));
        validatorMap.put(NARRATION, (value) -> FieldValidator.validateStringIfPresent(value, 5000));
        validatorMap.put(DESCRIPTION, (value) -> FieldValidator.validateStringIfPresent(value, 20000));
        validatorMap.put(THUMBNAIL, (value) -> FieldValidator.validateStringIfPresent(value, 2000));
        validatorMap.put(METADATA, FieldValidator::validateJsonIfPresent);
        validatorMap.put(TAXONOMY, FieldValidator::validateJsonIfPresent);
        validatorMap.put(COPYRIGHT_OWNER, FieldValidator::validateJsonIfPresent);
        validatorMap.put(INFO, FieldValidator::validateJsonIfPresent);
        validatorMap.put(DISPLAY_GUIDE, FieldValidator::validateJsonIfPresent);
        validatorMap.put(ACCESSIBILITY, FieldValidator::validateJsonIfPresent);
        validatorMap.put(VISIBLE_ON_PROFILE, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(IS_COPYRIGHT_OWNER, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(CONTENT_SUBFORMAT, AJEntityOriginalResource.RESOURCE_TYPES::contains);
        return Collections.unmodifiableMap(validatorMap);
    }

    public static FieldSelector editFieldSelector() {
        return () -> Collections.unmodifiableSet(EDITABLE_FIELDS);
    }

    public static ValidatorRegistry getValidatorRegistry() {
        return new ResourceRefValidatorRegistry();
    }

    public static ConverterRegistry getConverterRegistry() {
        return new ResourceRefConverterRegistry();
    }

    private static class ResourceRefValidatorRegistry implements ValidatorRegistry {
        @Override
        public FieldValidator lookupValidator(String fieldName) {
            return validatorRegistry.get(fieldName);
        }
    }

    private static class ResourceRefConverterRegistry implements ConverterRegistry {
        @Override
        public FieldConverter lookupConverter(String fieldName) {
            return converterRegistry.get(fieldName);
        }
    }

}
