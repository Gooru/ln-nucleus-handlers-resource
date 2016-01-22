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
  public static final String SEQUENCE_ID = "sequence_id";
  
  
  public static final String VALID_CONTENT_FORMAT_FOR_RESOURCE = "resource";
  public static final String JSONB_FORMAT = "jsonb";


  public static final List<String> RESOURCE_SPECIFIC_FIELDS = new ArrayList<>(Arrays.asList(RESOURCE_ID,
          RESOURCE_TITLE,
          RESOURCE_URL,
          CREATOR_ID,
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

  // not null fields in db
  public static final List<String> NOTNULL_FIELDS = new ArrayList<>(Arrays.asList(RESOURCE_TITLE,
          CREATOR_ID,
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
          ACCESSIBILITY));
  
  
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


  public static final String[] attributes = { RESOURCE_ID,
          RESOURCE_TITLE,
          RESOURCE_URL,
          CREATOR_ID,
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
}
