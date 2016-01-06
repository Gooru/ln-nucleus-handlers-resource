package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.gooru.nucleus.handlers.resources.app.components.DataSourceRegistry;
import org.gooru.nucleus.handlers.resources.processors.exceptions.InvalidUserException;
import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.ResourceReference;
import org.gooru.nucleus.handlers.resources.processors.responses.transformers.ResponseTransformerBuilder;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PGobject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Created by ashish on 29/12/15.
 */
public class AJResourceRepo implements ResourceRepo {

  private static final Logger LOGGER = LoggerFactory.getLogger(AJResourceRepo.class);
  private static final String [] JSONB_FIELDS = {"metadata", "taxonomy", "depth_of_knowledge", "copyright_owner"};
  

  // <TBD>Need to create a list of owner specific editable fields, non-owner
  // non-editable and common fields

  /*
   * owner specific editable fields are - only the owner will be able to change.
   * non-owner (common) editable fields are the fields which belong to owner and
   * are editable by the non-owner if non-owner tries to edit a resource, owner
   * specific fields should not be updated whereas the common fields which can
   * be editable by both owner and non-owner, need to be updated locally. If
   * owner tries to change owner specific fields, then those changes need to be
   * propagated where the original_content_id is matching this content id but
   * the common editable fields should be locally updated and not propagated
   */

  @Override
  public JsonObject getResourceById(String resourceId) {
    String sql =
            "SELECT content_id, title, url, creator_id, narration, description, content_subformat, metadata, taxonomy, depth_of_knowledge, thumbnail, is_frame_breaker, is_broken, is_deleted, is_copyright_owner, copyright_owner, visible_on_profile FROM CONTENT WHERE content_id = '"
                    + resourceId + "' AND content_format ='resource'";

    Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
    LazyList<ResourceReference> result = ResourceReference.findBySQL(sql);
    JsonObject returnValue = new JsonObject();
    if (result.get(0) != null) {
      /*
       * JsonObject jsonObject = new JsonObject(result.get(0).toJson(false,
       * "content_id", "title", "url", "creator_id", "narration", "description",
       * "content_subformat", "metadata", "taxonomy", "depth_of_knowledge",
       * "thumbnail", "is_frame_breaker", "is_broken", "is_deleted",
       * "is_copyright_owner", "copyright_owner", "visible_on_profile"));
       * JsonObject object = jsonObject.getJsonObject("metadata"); String
       * stringMetadata = jsonObject.getString("metadata"); LOGGER.debug(
       * "Object is : {} and string is : {}", object, stringMetadata);
       */
      returnValue = new AJResponseJsonTransformer().transform(result.get(0).toJson(false, "content_id", "title", "url", "creator_id", "narration",
              "description", "content_subformat", "metadata", "taxonomy", "depth_of_knowledge", "thumbnail", "is_frame_breaker", "is_broken",
              "is_deleted", "is_copyright_owner", "copyright_owner", "visible_on_profile"));
    }
    Base.close();
    return returnValue;
  }

  @Override
  public String createResource(JsonObject resourceData) {
    
    
    Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
    LOGGER.debug("Created resource  " + resourceData);
    String resourceId = resourceData.getString("content_id");
    try {
      ResourceReference createRes = new ResourceReference();
      for (Map.Entry<String, Object> entry : resourceData) {
        if (entry.getKey() == "content_format") {
          PGobject contentFormat = new PGobject();
          contentFormat.setType("content_format");
          contentFormat.setValue(entry.getValue().toString());
          createRes.set(entry.getKey(), contentFormat);
        } else if (entry.getKey() == "content_subformat") {
          PGobject contentSubformat = new PGobject();
          contentSubformat.setType("content_subformat");
          contentSubformat.setValue(entry.getValue().toString());
          createRes.set(entry.getKey(), contentSubformat);
        } else if (Arrays.asList(JSONB_FIELDS).contains(entry.getKey())) {
          PGobject jsonbFields = new PGobject();
          jsonbFields.setType("jsonb");
          jsonbFields.setValue(entry.getValue().toString());
          createRes.set(entry.getKey(), jsonbFields);
        } else {
          createRes.set(entry.getKey(), entry.getValue());
        }
      }

      createRes.setId(resourceId);
      LOGGER.debug("Creating resource From MAP  : {}", createRes.toInsert());

      if (createRes.insert()) {
        String retId = createRes.getString("content_id");
        LOGGER.debug("Created resource ID: " + retId);
        return retId;
      } else {
        LOGGER.error("Failed to create <TBD> get cause from error object!");
      }

      return null;
    } catch (SQLException e) {
      LOGGER.warn("Caught sqlExceptions", e);
      return null;
    } catch (Throwable throwable) {
      LOGGER.warn("Caught unexpected exception here", throwable);
      return null;
    } finally {
      Base.close();
    }
  }
}
