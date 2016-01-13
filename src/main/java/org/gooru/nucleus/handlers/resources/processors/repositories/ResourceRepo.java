package org.gooru.nucleus.handlers.resources.processors.repositories;

import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;


/**
 * Created by ashish on 29/12/15.
 */
public interface ResourceRepo {
 
     String RESOURCE_ID = "id";
     String RESOURCE_TITLE = "title";
     String RESOURCE_URL = "url";
     String CREATOR_ID = "creator_id";
     String ORIGINAL_CREATOR_ID = "original_creator_id";
     String ORIGINAL_CONTENT_ID = "original_content_id";
     String PUBLISH_DATE = "publish_date";
     String NARRATION = "narration";
     String DESCRIPTION = "description";
     String CONTENT_FORMAT = "content_format";
     String CONTENT_FORMAT_TYPE = "content_format_type";
     String CONTENT_SUBFORMAT = "content_subformat";
     String CONTENT_SUBFORMAT_TYPE = "content_subformat_type";
     String METADATA = "metadata";
     String TAXONOMY = "taxonomy";
     String DEPTH_OF_KNOWLEDGE = "depth_of_knowledge";
     String COURSE_ID = "course_id";
     String UNIT_ID = "unit_id";
     String LESSON_ID = "lesson_id";
     String COLLECTION_ID = "collection_id";
     String SEQUENCE_ID = "sequence_id"; 
     String IS_COPYRIGHT_OWNER = "is_copyright_owner";
     String COPYRIGHT_OWNER = "copyright_owner";
     String VISIBLE_ON_PROFILE = "visible_on_profile";
     String IS_FRAME_BREAKER = "is_frame_breaker";
     String IS_BROKEN = "is_broken";
     String IS_DELETED = "is_deleted";
    
     final String VALID_CONTENT_FORMAT_FOR_RESOURCE = "resource";
     final String JSONB_FORMAT = "jsonb";
    
    // jsonb fields relevant to resource
     static final String[] JSONB_FIELDS = { METADATA, TAXONOMY, DEPTH_OF_KNOWLEDGE, COPYRIGHT_OWNER };

    // not null fields in db
     static final String[] NOTNULL_FIELDS =
          { RESOURCE_ID, RESOURCE_TITLE, CREATOR_ID, ORIGINAL_CREATOR_ID, CONTENT_FORMAT, CONTENT_SUBFORMAT, VISIBLE_ON_PROFILE, IS_DELETED };

    // <TBD> - Need to decide
    // only owner (original creator of the resource) can change, which will have
    // to update all the copied records of the resource
     static final String[] OWNER_SPECIFIC_FIELDS = 
          { RESOURCE_TITLE, RESOURCE_URL, DESCRIPTION, DEPTH_OF_KNOWLEDGE, CONTENT_FORMAT, CONTENT_SUBFORMAT };

    final String [] attributes = {ResourceRepo.RESOURCE_ID, 
                                  ResourceRepo.RESOURCE_TITLE, 
                                  ResourceRepo.RESOURCE_URL, 
                                  ResourceRepo.CREATOR_ID,
                                  ResourceRepo.NARRATION,
                                  ResourceRepo.DESCRIPTION,
                                  ResourceRepo.CONTENT_FORMAT,
                                  ResourceRepo.CONTENT_SUBFORMAT,
                                  ResourceRepo.METADATA,
                                  ResourceRepo.TAXONOMY,
                                  ResourceRepo.DEPTH_OF_KNOWLEDGE,
                                  ResourceRepo.ORIGINAL_CONTENT_ID,
                                  ResourceRepo.ORIGINAL_CREATOR_ID,
                                  ResourceRepo.IS_FRAME_BREAKER,
                                  ResourceRepo.IS_BROKEN,
                                  ResourceRepo.IS_DELETED,
                                  ResourceRepo.IS_COPYRIGHT_OWNER,
                                  ResourceRepo.COPYRIGHT_OWNER,
                                  ResourceRepo.VISIBLE_ON_PROFILE};

    MessageResponse createResource();
    MessageResponse updateResource();
    MessageResponse fetchResource();

}
