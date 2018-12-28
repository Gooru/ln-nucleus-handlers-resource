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
    private static final String PUBLISH_STATUS = "publish_status";
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
    public static final String PRIMARY_LANGUAGE = "primary_language";

    public static final String COURSE_ID = "course_id";
    public static final String UNIT_ID = "unit_id";
    public static final String LESSON_ID = "lesson_id";
    public static final String COLLECTION_ID = "collection_id";
    public static final String SEQUENCE_ID = "sequence_id";
    public static final String TENANT = "tenant";
    public static final String TENANT_ROOT = "tenant_root";

    public static final String CONTENT_FORMAT_RESOURCE = "resource";
    private static final String PUBLISH_STATUS_PUBLISHED = "published";

    // only owner (original creator of the resource) can change, which will have
    // to update all the copied records of the resource
    // Any update in this list should also be reflected in UpdateResourceRef update query
    public static final List<String> OWNER_SPECIFIC_FIELDS =
        new ArrayList<>(Arrays.asList(TITLE, URL, DESCRIPTION, CONTENT_SUBFORMAT, INFO, DISPLAY_GUIDE, ACCESSIBILITY));
    public static final List<String> OWNER_SPECIFIC_FIELDS_TYPES = new ArrayList<>(Arrays
        .asList(null, null, null, CONTENT_SUBFORMAT_TYPE, EntityConstants.JSONB_FORMAT, EntityConstants.JSONB_FORMAT,
            EntityConstants.JSONB_FORMAT));

    //Ideally Resource copies are allowed to update only these fields,
    //However this needs to changed at FE as well, hence allowing more fields to update
    //public static final Set<String> EDITABLE_FIELDS =
    //    new HashSet<>(Arrays.asList(NARRATION, THUMBNAIL, METADATA, TAXONOMY, INFO, DISPLAY_GUIDE, ACCESSIBILITY));
    public static final Set<String> EDITABLE_FIELDS = new HashSet<>(Arrays
        .asList(CONTENT_SUBFORMAT, COPYRIGHT_OWNER, DESCRIPTION, IS_COPYRIGHT_OWNER, TITLE, VISIBLE_ON_PROFILE,
            NARRATION, THUMBNAIL, METADATA, TAXONOMY, INFO, DISPLAY_GUIDE, ACCESSIBILITY, PRIMARY_LANGUAGE));

    public static final String FETCH_RESOURCE_BY_ID =
        " SELECT id, title, url, creator_id, modifier_id, narration, description, content_format, content_subformat, "
            + "metadata, taxonomy, original_content_id, original_creator_id, is_deleted, is_copyright_owner, "
            + "copyright_owner, visible_on_profile, thumbnail, info, display_guide, accessibility, course_id, "
            + "unit_id, lesson_id, collection_id, license, publish_status, tenant, tenant_root, primary_language FROM content WHERE id "
            + "= ?::uuid AND content_format = ?::content_format_type AND is_deleted = false";

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
    
    public static final String UPDATE_REFERENCES_OF_ORIGINAL =
        "UPDATE content SET title = ?, url = ?, description = ?, content_subformat = ?::content_subformat_type, info = ?::jsonb,"
        + " display_guide = ?::jsonb, accessibility = ?::jsonb where original_content_id = ?::uuid AND is_deleted = false";

    // owner or collaborator at course or collection level are authorized to
    // delete the resource.
    public static final String TABLE_COURSE = "course";
    public static final String TABLE_RESOURCE = "content";
    public static final String TABLE_COLLECTION = "collection";
    public static final String AUTH_VIA_COLLECTION_FILTER =
        "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?)";
    public static final String AUTH_VIA_COURSE_FILTER = "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?)";
    public static final String PUBLISHED_FILTER = "id = ?::uuid and publish_status = 'published'::publish_status_type;";

    public static final String UPDATE_CONTAINER_TIMESTAMP =
        "update collection set updated_at = now() where id = ?::uuid and is_deleted = 'false'";

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
        converterMap.put(COPYRIGHT_OWNER, (FieldConverter::convertFieldToJson));
        converterMap.put(TENANT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(TENANT_ROOT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));

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
        validatorMap.put(COPYRIGHT_OWNER, FieldValidator::validateJsonArrayIfPresent);
        validatorMap.put(INFO, FieldValidator::validateJsonIfPresent);
        validatorMap.put(DISPLAY_GUIDE, FieldValidator::validateJsonIfPresent);
        validatorMap.put(ACCESSIBILITY, FieldValidator::validateJsonIfPresent);
        validatorMap.put(VISIBLE_ON_PROFILE, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(IS_COPYRIGHT_OWNER, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(CONTENT_SUBFORMAT, AJEntityOriginalResource.RESOURCE_TYPES::contains);
        validatorMap.put(TENANT, (FieldValidator::validateUuid));
        validatorMap.put(TENANT_ROOT, (FieldValidator::validateUuid));
        validatorMap.put(PRIMARY_LANGUAGE, FieldValidator::validateLanguageIfPresent);
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

    public void setTenant(String tenant) {
        setFieldUsingConverter(TENANT, tenant);
    }

    public void setTenantRoot(String tenantRoot) {
        setFieldUsingConverter(TENANT_ROOT, tenantRoot);
    }

    public String getCourseId() {
        return this.getString(COURSE_ID);
    }

    public String getCollectionId() {
        return this.getString(COLLECTION_ID);
    }

    public String getTenant() {
        return this.getString(TENANT);
    }

    public String getTenantRoot() {
        return this.getString(TENANT_ROOT);
    }

    public boolean isResourcePublished() {
        String publishStatus = this.getString(PUBLISH_STATUS);
        return PUBLISH_STATUS_PUBLISHED.equalsIgnoreCase(publishStatus);
    }

    private void setFieldUsingConverter(String fieldName, Object fieldValue) {
        FieldConverter fc = converterRegistry.get(fieldName);
        if (fc != null) {
            this.set(fieldName, fc.convertField(fieldValue));
        } else {
            this.set(fieldName, fieldValue);
        }
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
