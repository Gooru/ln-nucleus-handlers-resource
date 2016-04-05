package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class DBHelper {
  public static final int NUM_RETRIES = 2;
  private static final Logger LOGGER = LoggerFactory.getLogger(DBHelper.class);

  private DBHelper() {
    throw new AssertionError();
  }

  static AJEntityResource getResourceById(String resourceId) {
    try {
      PGobject contentFormat = new PGobject();
      contentFormat.setType(AJEntityResource.CONTENT_FORMAT_TYPE);
      contentFormat.setValue(AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);

      LazyList<AJEntityResource> result = AJEntityResource.findBySQL(AJEntityResource.SQL_GETRESOURCEBYID, resourceId, contentFormat);
      LOGGER.debug("getResourceById : {} ", result.toString());

      if (result.size() > 0) {
        if (result.size() > 1) {
          LOGGER.error("getResourceById : {} GOT MORE RESULTS FOR THE SAME ID", result.toString());
        }
        return result.get(0);
      }

      LOGGER.warn("getResourceById : Resource with id : {} : not found", resourceId);
    } catch (SQLException se) {
      LOGGER.error("getResourceById : SQL Exception caught ! : {} ", se);
    }
    return null;
  }

  static AJEntityResource getResourceDetailUpForDeletion(String resourceId) {
    LazyList<AJEntityResource> result =
      AJEntityResource.findBySQL(AJEntityResource.SQL_GETRESOURCEDETAILUPFORDELETION, resourceId, AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);
    LOGGER.debug("getResourceById : {} ", result.toString());

    if (result.size() > 0) {
      if (result.size() > 1) {
        LOGGER.error("getResourceById : {} GOT MORE RESULTS FOR THE SAME ID", result.toString());
      }
      return result.get(0);
    }

    LOGGER.warn("getResourceById : Resource with id : {} : not found", resourceId);
    return null;
  }

  /*
   * getDuplicateResourcesByURL: returns NULL if no duplicates
   */
  static JsonObject getDuplicateResourcesByURL(String inputURL) {
    JsonObject returnValue = null;

    try {
      PGobject contentFormat = new PGobject();
      contentFormat.setType(AJEntityResource.CONTENT_FORMAT_TYPE);
      contentFormat.setValue(AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);

      LazyList<AJEntityResource> result = AJEntityResource.findBySQL(AJEntityResource.SQL_GETDUPLICATERESOURCESBYURL, inputURL, contentFormat);
      LOGGER.debug("getDuplicateResourcesByURL ! : {} ", result.toString());

      if (result.size() > 0) {
        JsonArray retArray = new JsonArray();
        for (AJEntityResource model : result) {
          retArray.add(model.get(AJEntityResource.RESOURCE_ID).toString());
        }
        returnValue = new JsonObject().put("duplicate_ids", retArray);
      }
    } catch (SQLException se) {
      LOGGER.error("getDuplicateResourcesByURL ! : {} ", se);
    }
    return returnValue;
  }

  /*
   * populateEntityFromJson : throws exceptions
   */
  static void populateEntityFromJson(JsonObject inputJson, AJEntityResource resource) throws IllegalArgumentException {
    String mapValue;

    for (Map.Entry<String, Object> entry : inputJson) {
      mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;

      if (AJEntityResource.NOTNULL_FIELDS.contains(entry.getKey())) {
        if (mapValue == null || mapValue.isEmpty()) {
          throw new IllegalArgumentException("Null value input for : " + entry.getKey());
        }
      }

      if (AJEntityResource.CONTENT_FORMAT.equalsIgnoreCase(entry.getKey())) {
        if (!AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE.equalsIgnoreCase(mapValue)) {
          throw new IllegalArgumentException("content format should always be a 'resource' but {} has been sent: " + mapValue);
        } else {
          setPGObject(resource, AJEntityResource.CONTENT_FORMAT, AJEntityResource.CONTENT_FORMAT_TYPE, mapValue);
        }
      } else if (AJEntityResource.CONTENT_SUBFORMAT.equalsIgnoreCase(entry.getKey())) {
        if (mapValue == null || mapValue.isEmpty() || !mapValue.endsWith(AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
          throw new IllegalArgumentException("content sub format is not a valid resource format ; {} has been sent: " + mapValue);
        } else {
          setPGObject(resource, AJEntityResource.CONTENT_SUBFORMAT, AJEntityResource.CONTENT_SUBFORMAT_TYPE, mapValue);
        }
      } else {
        if (AJEntityResource.JSONB_FIELDS.contains(entry.getKey())) {
          setPGObject(resource, entry.getKey(), AJEntityResource.JSONB_FORMAT, mapValue);
        } else if (AJEntityResource.UUID_FIELDS.contains(entry.getKey())) {
          setPGObject(resource, entry.getKey(), AJEntityResource.UUID_TYPE, mapValue);
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
  static int updateOwnerDataToCopies(AJEntityResource resource, String ownerResourceId, JsonObject dataToBePropogated, String originalCreator)
    throws SQLException, IllegalArgumentException {
    LOGGER.debug("updateOwnerDataToCopies: OwnerResourceID {}", ownerResourceId);
    int numRecsUpdated = 0;
    String mapValue;
    List<Object> params = new ArrayList<>();
    String updateStmt = null;
    if (!dataToBePropogated.isEmpty()) {
      for (Map.Entry<String, Object> entry : dataToBePropogated) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;

        if (AJEntityResource.NOTNULL_FIELDS.contains(entry.getKey())) {
          if (mapValue == null || mapValue.isEmpty()) {
            throw new IllegalArgumentException("Null value input for : " + entry.getKey());
          }
        }

        LOGGER.debug("updateOwnerDataToCopies: OwnerResourceID {}", entry.getKey());

        updateStmt = (updateStmt == null) ? entry.getKey() + " = ?" : updateStmt + ", " + entry.getKey() + " = ?";

        if (AJEntityResource.CONTENT_FORMAT.equalsIgnoreCase(entry.getKey())) {
          if (!AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE.equalsIgnoreCase(mapValue)) {
            throw new IllegalArgumentException("content format should always be a 'resource' but {} has been sent: " + mapValue);
          } else {
            PGobject contentformat = new PGobject();
            contentformat.setType(AJEntityResource.CONTENT_FORMAT_TYPE);
            contentformat.setValue(entry.getValue().toString());
            params.add(contentformat);
          }
        } else if (AJEntityResource.CONTENT_SUBFORMAT.equalsIgnoreCase(entry.getKey())) {
          if (mapValue == null || mapValue.isEmpty() || !mapValue.endsWith(AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
            throw new IllegalArgumentException("content sub format is not a valid resource format ; {} has been sent: " + mapValue);
          } else {
            PGobject contentSubformat = new PGobject();
            contentSubformat.setType(AJEntityResource.CONTENT_SUBFORMAT_TYPE);
            contentSubformat.setValue(entry.getValue().toString());
            params.add(contentSubformat);
          }
        } else if (AJEntityResource.JSONB_FIELDS.contains(entry.getKey())) {
          PGobject jsonbFields = new PGobject();
          jsonbFields.setType(AJEntityResource.JSONB_FORMAT);
          jsonbFields.setValue(entry.getValue().toString());
          params.add(jsonbFields);
        } else if (AJEntityResource.UUID_FIELDS.contains(entry.getKey())) {
          PGobject uuidFields = new PGobject();
          uuidFields.setType(AJEntityResource.UUID_TYPE);
          uuidFields.setValue(mapValue);
          params.add(uuidFields);
        } else {
          params.add(entry.getValue());
        }
      }


      LOGGER.debug("updateOwnerDataToCopies: Statement {}", updateStmt);

      if (updateStmt != null) {
        params.add(ownerResourceId);
        params.add(originalCreator);
        numRecsUpdated = AJEntityResource.update(updateStmt, AJEntityResource.SQL_UPDATEOWNERDATATOCOPIES_WHERECLAUSE, params.toArray());
        LOGGER.debug("updateOwnerDataToCopies : Update successful. Number of records updated: {}", numRecsUpdated);
      }
    }
    return numRecsUpdated;
  }

  static JsonObject getCopiesOfAResource(AJEntityResource resource, String originalResourceId) {
    JsonObject returnValue = null;

    setPGObject(resource, AJEntityResource.CONTENT_FORMAT, AJEntityResource.CONTENT_FORMAT_TYPE, AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);

    LazyList<AJEntityResource> result =
      AJEntityResource.findBySQL(AJEntityResource.SQL_GETCOPIESOFARESOURCE, AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE, originalResourceId);
    if (result.size() > 0) {
      JsonArray retArray = new JsonArray();
      for (AJEntityResource model : result) {
        retArray.add(model.get(AJEntityResource.RESOURCE_ID).toString());
      }
      returnValue = new JsonObject().put("resource_copy_ids", retArray);
      LOGGER.debug("getCopiesOfAResource ! : {} ", returnValue.toString());
    }
    return returnValue;
  }

  static int deleteResourceCopies(AJEntityResource resource, String originalResourceId) throws IllegalArgumentException {
    // update content set is_deleted=true where content_format='resource; and original_content_id=Argument and is_deleted=false
    LOGGER.debug("deleteResourceCopies: originalResourceId {}", originalResourceId);
    int numRecsUpdated;
    List<Object> params = new ArrayList<>();
    String updateStmt = AJEntityResource.IS_DELETED + "= ? ";
    setPGObject(resource, AJEntityResource.CONTENT_FORMAT, AJEntityResource.CONTENT_FORMAT_TYPE, AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);
    params.add(true);
    params.add(AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);
    params.add(originalResourceId);

    numRecsUpdated = AJEntityResource.update(updateStmt, AJEntityResource.SQL_DELETERESOURCECOPIES_WHERECLAUSE, params.toArray());
    LOGGER
      .debug("deleteResourceCopies : Update successful and is_deleted set to true for all copies of the resource {} . Number of records updated: {}",
        originalResourceId, numRecsUpdated);
    return numRecsUpdated;

  }

  static void setPGObject(AJEntityResource resource, String field, String type, String value) {
    PGobject pgObject = new PGobject();
    pgObject.setType(type);
    try {
      pgObject.setValue(value);
      resource.set(field, pgObject);
    } catch (SQLException e) {
      LOGGER.error("Not able to set value for field: {}, type: {}, value: {}", field, type, value);
      resource.errors().put(field, value);
    }
  }


}
