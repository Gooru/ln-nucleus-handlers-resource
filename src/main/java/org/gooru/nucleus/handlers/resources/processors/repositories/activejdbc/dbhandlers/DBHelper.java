package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.ResourceEntityConstants;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DBHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(DBHelper.class);
  
  public static final int NUM_RETRIES = 2;

  static protected AJEntityResource getResourceById(String resourceId) {
    try {
      String sql = " SELECT " + String.join(", ", ResourceEntityConstants.attributes) + 
                   " FROM content WHERE id = ?  AND content_format = ? AND is_deleted = false";
  
      PGobject contentFormat = new PGobject();
      contentFormat.setType(ResourceEntityConstants.CONTENT_FORMAT_TYPE);
      contentFormat.setValue(ResourceEntityConstants.VALID_CONTENT_FORMAT_FOR_RESOURCE);
      
      LazyList<AJEntityResource> result = AJEntityResource.findBySQL(sql, resourceId, contentFormat);
      LOGGER.debug("getResourceById : {} ", result.toString());
  
      if (result.size() > 0) {
        LOGGER.debug("getResourceById : Return Value : {} ", result.toString());
        return result.get(0);
      }
  
      LOGGER.warn("getResourceById : Resource with id : {} : not found", resourceId);
    } catch (SQLException se) {
      LOGGER.error("getResourceById : SQL Exception caught ! : {} ", se);
    }
    return null;
  }

  /*
   * getDuplicateResourcesByURL: returns NULL if no duplicates
   */
  static protected JsonObject getDuplicateResourcesByURL(String inputURL) {
    JsonObject returnValue = null;
    
    try {
      String sql = "SELECT id FROM content"
                 + " WHERE url = ? AND content_format = ? AND original_content_id is null AND is_deleted = false";
  
      PGobject contentFormat = new PGobject();
      contentFormat.setType(ResourceEntityConstants.CONTENT_FORMAT_TYPE);
      contentFormat.setValue(ResourceEntityConstants.VALID_CONTENT_FORMAT_FOR_RESOURCE);
        
      LazyList<AJEntityResource> result = AJEntityResource.findBySQL(sql, inputURL, contentFormat);
      LOGGER.debug("getDuplicateResourcesByURL ! : {} ", result.toString());
  
      if (result.size() > 0) {
        returnValue = new JsonObject().put("duplicate_ids", new JsonArray(result.collect(ResourceEntityConstants.RESOURCE_ID)));
      }
    } catch (SQLException se) {
      LOGGER.error("getDuplicateResourcesByURL ! : {} ", se);
    }
    return returnValue;
  }

  /*
   * populateEntityFromJson : throws exceptions
   */
  static protected void populateEntityFromJson(JsonObject inputJson, AJEntityResource resource) throws SQLException, IllegalArgumentException {
    String mapValue;

    for (Map.Entry<String, Object> entry : inputJson) {
      mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;

      if (ResourceEntityConstants.NOTNULL_FIELDS.contains(entry.getKey())) {
        if (mapValue == null || mapValue.isEmpty()) {
          throw new IllegalArgumentException("Null value input for : " + entry.getKey());
        }
      }

      if (ResourceEntityConstants.CONTENT_FORMAT.equalsIgnoreCase(entry.getKey())) {
        if (!ResourceEntityConstants.VALID_CONTENT_FORMAT_FOR_RESOURCE.equalsIgnoreCase(mapValue)) {
          throw new IllegalArgumentException("content format should always be a 'resource' but {} has been sent: " + mapValue);
        } else {
          PGobject contentFormat = new PGobject();
          contentFormat.setType(ResourceEntityConstants.CONTENT_FORMAT_TYPE);
          contentFormat.setValue(mapValue);
          resource.set(entry.getKey(), contentFormat);
        }
      } else if (ResourceEntityConstants.CONTENT_SUBFORMAT.equalsIgnoreCase(entry.getKey())) {
        if (mapValue == null || mapValue.isEmpty() || !mapValue.endsWith(ResourceEntityConstants.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
          throw new IllegalArgumentException("content sub format is not a valid resource format ; {} has been sent: " + mapValue);
        } else {
          PGobject contentSubformat = new PGobject();
          contentSubformat.setType(ResourceEntityConstants.CONTENT_SUBFORMAT_TYPE);
          contentSubformat.setValue(mapValue);
          resource.set(entry.getKey(), contentSubformat);
        }
      } else {
        if (ResourceEntityConstants.JSONB_FIELDS.contains(entry.getKey())) {
          PGobject jsonbFields = new PGobject();
          jsonbFields.setType(ResourceEntityConstants.JSONB_FORMAT);
          jsonbFields.setValue(mapValue);
          resource.set(entry.getKey(), jsonbFields);
        } else {
          resource.set(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  /*
   * updateOwnerDataToCopies: as a consequence of primary resource update, we
   * need to update the copies of this resource - but ONLY owner specific data
   * items.
   *
   * NOTE: This method does not do a lot of null checks etc; as all checks are
   * already done by UpdateResource() method.
   */
  static protected int updateOwnerDataToCopies(String ownerResourceId, JsonObject dataToBePropogated, String originalCreator)
    throws SQLException, IllegalArgumentException {
    LOGGER.debug("updateOwnerDataToCopies: OwnerResourceID {}", ownerResourceId);
    int numRecsUpdated = 0;
    String mapValue;
    List<Object> params = new ArrayList<>();
    String updateStmt = null;
    for (Map.Entry<String, Object> entry : dataToBePropogated) {
      mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
      
      if (Arrays.asList(ResourceEntityConstants.NOTNULL_FIELDS).contains(entry.getKey())) {
        if (mapValue == null || mapValue.isEmpty()) {
          throw new IllegalArgumentException("Null value input for : " + entry.getKey());
        }
      }

      LOGGER.debug("updateOwnerDataToCopies: OwnerResourceID {}", entry.getKey());

      updateStmt = (updateStmt == null) ? entry.getKey() + " = ?" : updateStmt + ", " + entry.getKey() + " = ?";

      if (ResourceEntityConstants.CONTENT_FORMAT.equalsIgnoreCase(entry.getKey())) {
        if (!ResourceEntityConstants.VALID_CONTENT_FORMAT_FOR_RESOURCE.equalsIgnoreCase(mapValue)) {
          throw new IllegalArgumentException("content format should always be a 'resource' but {} has been sent: " + mapValue);
        } else {
          PGobject contentFormat = new PGobject();
          contentFormat.setType(ResourceEntityConstants.CONTENT_FORMAT_TYPE);
          contentFormat.setValue(entry.getValue().toString());
          params.add(contentFormat);
        }
      } else if (ResourceEntityConstants.CONTENT_SUBFORMAT.equalsIgnoreCase(entry.getKey())) {
        if (mapValue == null || mapValue.isEmpty() || !mapValue.endsWith(ResourceEntityConstants.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
          throw new IllegalArgumentException("content sub format is not a valid resource format ; {} has been sent: " + mapValue);
        } else {
          PGobject contentSubformat = new PGobject();
          contentSubformat.setType(ResourceEntityConstants.CONTENT_SUBFORMAT_TYPE);
          contentSubformat.setValue(entry.getValue().toString());
          params.add(contentSubformat);
        }
      } else if (Arrays.asList(ResourceEntityConstants.JSONB_FIELDS).contains(entry.getKey())) {
        PGobject jsonbFields = new PGobject();
        jsonbFields.setType(ResourceEntityConstants.JSONB_FORMAT);
        jsonbFields.setValue(entry.getValue().toString());
        params.add(jsonbFields);
      } else {
        params.add(entry.getValue());
      }
    }

    LOGGER.debug("updateOwnerDataToCopies: Statement {}", updateStmt);

    if (updateStmt != null) {
      params.add(ownerResourceId);
      params.add(originalCreator);
      numRecsUpdated = AJEntityResource.update(updateStmt, "original_content_id = ? AND original_creator_id = ? AND is_deleted = false", params.toArray());
      LOGGER.debug("updateOwnerDataToCopies : Update successful. Number of records updated: {}", numRecsUpdated);
    }

    return numRecsUpdated;
  }
  
}
