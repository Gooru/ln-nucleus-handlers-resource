package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 29/12/15.
 */
@Table("content")
public class AJEntityResource extends Model {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityResource.class);
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
  public static final String DEPTH_OF_KNOWLEDGE = "depth_of_knowledge";
  public static final String THUMBNAIL = "thumbnail";
  public static final String IS_COPYRIGHT_OWNER = "is_copyright_owner";
  public static final String COPYRIGHT_OWNER = "copyright_owner";
  public static final String RESOURCE_INFO = "info";
  public static final String VISIBLE_ON_PROFILE = "visible_on_profile";
  public static final String DISPLAY_GUIDE = "display_guide";
  public static final String ACCESSIBILITY = "accessibility";
  public static final String IS_DELETED = "is_deleted";
 
  public static final String COURSE_ID = "course_id";
  public static final String UNIT_ID = "unit_id";
  public static final String LESSON_ID = "lesson_id";
  public static final String COLLECTION_ID = "collection_id";
  public static final String SEQUENCE_ID ="sequence_id";
  public static final String VALID_CONTENT_FORMAT_FOR_RESOURCE = "resource";
  public static final String JSONB_FORMAT = "jsonb";

  public static final String TABLE_COURSE = "course";
  public static final String TABLE_COLLECTION = "collection";
  public static final String MODIFIER_ID = "modifier_id";

  
  public static final String UUID_TYPE = "uuid";

  public void setModifierId(String modifier) {
    setPGObject(MODIFIER_ID, UUID_TYPE, modifier);
  }

  public void setCreatorId(String creatorId) {
    setPGObject(CREATOR_ID, UUID_TYPE, creatorId);
  }
  
  public void setOriginalCreatorId(String originalCreatorId) {
    setPGObject(ORIGINAL_CREATOR_ID, UUID_TYPE, originalCreatorId);
  }
  
  public static final String AUTH_VIA_COLLECTION_FILTER = "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?);";

  public static final String AUTH_VIA_COURSE_FILTER = "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?)";

  public static final List<String> RESOURCE_SPECIFIC_FIELDS = new ArrayList<>(Arrays.asList(RESOURCE_ID,
          RESOURCE_TITLE,
          RESOURCE_URL,
          CREATOR_ID,
          MODIFIER_ID,
          ORIGINAL_CREATOR_ID,
          ORIGINAL_CONTENT_ID,
          PUBLISH_DATE,
          NARRATION,
          DESCRIPTION,
          CONTENT_FORMAT,
          CONTENT_SUBFORMAT,
          METADATA,
          TAXONOMY,
          DEPTH_OF_KNOWLEDGE,
          THUMBNAIL,
          RESOURCE_INFO,
          IS_COPYRIGHT_OWNER,
          COPYRIGHT_OWNER,
          VISIBLE_ON_PROFILE,
          RESOURCE_INFO,
          VISIBLE_ON_PROFILE,
          DISPLAY_GUIDE,
          ACCESSIBILITY));

  // jsonb fields relevant to resource
  public static final List<String> JSONB_FIELDS = new ArrayList<>(Arrays.asList(METADATA,
          TAXONOMY,
          DEPTH_OF_KNOWLEDGE,
          COPYRIGHT_OWNER,
          RESOURCE_INFO,
          DISPLAY_GUIDE,
          ACCESSIBILITY));
  
  // jsonb fields relevant to resource
  public static final List<String> UUID_FIELDS = new ArrayList<>(Arrays.asList(RESOURCE_ID,
          CREATOR_ID,
          MODIFIER_ID,
          ORIGINAL_CONTENT_ID,
          ORIGINAL_CREATOR_ID));

  // not null fields in db
  public static final List<String> NOTNULL_FIELDS = new ArrayList<>(Arrays.asList(RESOURCE_TITLE,
          CREATOR_ID,
          MODIFIER_ID,
          ORIGINAL_CREATOR_ID,
          CONTENT_FORMAT,
          CONTENT_SUBFORMAT));


  // <TBD> - Need to decide
  // only owner (original creator of the resource) can change, which will have
  // to update all the copied records of the resource
  public static final List<String> OWNER_SPECIFIC_FIELDS = new ArrayList<>(Arrays.asList(RESOURCE_TITLE,
          RESOURCE_URL,
          DESCRIPTION,
          DEPTH_OF_KNOWLEDGE,
          CONTENT_FORMAT,
          CONTENT_SUBFORMAT,
          RESOURCE_INFO,
          DISPLAY_GUIDE,
          ACCESSIBILITY,
          ORIGINAL_CONTENT_ID));
  
  
  public static final List<String> VALID_UPDATE_FIELDS = new ArrayList<>(Arrays.asList(RESOURCE_TITLE,
          RESOURCE_URL,
          ORIGINAL_CREATOR_ID,
          ORIGINAL_CONTENT_ID,
          PUBLISH_DATE,
          NARRATION,
          DESCRIPTION,
          METADATA,
          TAXONOMY,
          DEPTH_OF_KNOWLEDGE,
          THUMBNAIL,
          RESOURCE_INFO,
          IS_COPYRIGHT_OWNER,
          COPYRIGHT_OWNER,
          VISIBLE_ON_PROFILE,
          RESOURCE_INFO,
          VISIBLE_ON_PROFILE,
          DISPLAY_GUIDE,
          ACCESSIBILITY));


  public static final String[] attributes_for_create_update_fetch = { RESOURCE_ID,
          RESOURCE_TITLE,
          RESOURCE_URL,
          CREATOR_ID,
          MODIFIER_ID,
          NARRATION,
          DESCRIPTION,
          CONTENT_FORMAT,
          CONTENT_SUBFORMAT,
          METADATA,
          TAXONOMY,
          DEPTH_OF_KNOWLEDGE,
          ORIGINAL_CONTENT_ID,
          ORIGINAL_CREATOR_ID,
          IS_DELETED,
          IS_COPYRIGHT_OWNER,
          COPYRIGHT_OWNER,
          VISIBLE_ON_PROFILE,
          THUMBNAIL,
          RESOURCE_INFO,
          DISPLAY_GUIDE,
          ACCESSIBILITY };
  
  public static final String[] attributes_for_delete = { RESOURCE_ID,
      CREATOR_ID,
      CONTENT_FORMAT,
      MODIFIER_ID,
      ORIGINAL_CONTENT_ID,
      ORIGINAL_CREATOR_ID,
      IS_DELETED,
      COURSE_ID,
      UNIT_ID,
      LESSON_ID,
      COLLECTION_ID };

  public static final String SQL_GETRESOURCEBYID = " SELECT " + String.join(", ", attributes_for_create_update_fetch) + 
                                                   " FROM content WHERE id = ?::uuid  AND content_format = ?::content_format_type AND is_deleted = false";
  
  public static final String SQL_GETDUPLICATERESOURCESBYURL = "SELECT id FROM content WHERE url = ? AND content_format = ?::content_format_type AND original_content_id is null AND is_deleted = false";
  
  public static final String SQL_GETRESOURCEDETAILUPFORDELETION = "SELECT " + String.join(", ", attributes_for_delete) 
                                                                + " FROM content WHERE id=?::uuid AND content_format = ?::content_format_type AND original_content_id is null AND is_deleted = false";

  public static final String SQL_UPDATEOWNERDATATOCOPIES_WHERECLAUSE = "original_content_id = ?::uuid AND original_creator_id = ?::uuid AND is_deleted = false";
  
  public static final String SQL_GETCOPIESOFARESOURCE = " SELECT id FROM content WHERE content_format = ?::content_format_type AND original_content_id = ?::uuid AND is_deleted = false";

  public static final String SQL_DELETERESOURCECOPIES_WHERECLAUSE = " content_format = ?::content_format_type AND original_content_id = ?::uuid AND is_deleted = false";

//NOTE:
 // We do not deal with nested objects, only first level ones
 // We do not check for forbidden fields, it should be done before this
 public void setAllFromJson(JsonObject input) {
   input.getMap().forEach((s, o) -> {
     // Note that special UUID cases for modifier and creator should be handled internally and not via map, so we do not care
     if (o instanceof JsonObject) {
       this.setPGObject(s, JSONB_FORMAT, ((JsonObject) o).toString());
     } else if (o instanceof JsonArray) {
       this.setPGObject(s, JSONB_FORMAT, ((JsonArray) o).toString());
     } else {
       this.set(s, o);
     }
   });
 }

 
  private void setPGObject(String field, String type, String value) {
    PGobject pgObject = new PGobject();
    pgObject.setType(type);
    try {
      pgObject.setValue(value);
      this.set(field, pgObject);
    } catch (SQLException e) {
      LOGGER.error("Not able to set value for field: {}, type: {}, value: {}", field, type, value);
      this.errors().put(field, value);
    }
  }
}
