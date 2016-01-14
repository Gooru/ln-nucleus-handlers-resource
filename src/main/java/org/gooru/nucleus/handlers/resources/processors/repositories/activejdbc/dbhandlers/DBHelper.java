package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
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

  private DBHelper() {
    // TODO Auto-generated constructor stub
  }

  public static DBHelper getInstance() {
    return Holder.INSTANCE;
  }

  public AJEntityResource getResourceById(String resourceId) {
    String sql = "SELECT " + String.join(", ", ResourceRepo.attributes) + " FROM CONTENT WHERE id = '" + resourceId + "' AND content_format ='"
      + ResourceRepo.VALID_CONTENT_FORMAT_FOR_RESOURCE + "'";

    LazyList<AJEntityResource> result = AJEntityResource.findBySQL(sql);
    LOGGER.debug("getResourceById : {} ", result.toString());

    if (result.size() > 0) {
      LOGGER.debug("getResourceById : Return Value : {} ", result.toString());
      return result.get(0);
      //return new AJResponseJsonTransformer().transform(result.get(0).toJson(false, ResourceRepo.attributes));
    }

    LOGGER.warn("getResourceById : Resource with id : {} : not found", resourceId);

    return null;
  }

  /*
   * getDuplicateResourcesByURL: returns NULL if no duplicates
   */
  protected JsonObject getDuplicateResourcesByURL(String inputURL) {
    String sql = "SELECT id FROM content WHERE url = '" + inputURL + "' AND content_format = '" + ResourceRepo.VALID_CONTENT_FORMAT_FOR_RESOURCE
      + "' AND original_content_id is null";

    LazyList<AJEntityResource> result = AJEntityResource.findBySQL(sql);
    LOGGER.debug("getDuplicateResourcesByURL ! : {} ", result.toString());

    JsonObject returnValue = null;
    if (result.size() > 0) {
      returnValue = new JsonObject().put("duplicate_ids", new JsonArray(result.collect("id")));
    }
    return returnValue;
  }

  /*
   * populateEntityFromJson : throws exceptions
   */
  protected void populateEntityFromJson(JsonObject inputJson, AJEntityResource resource) throws SQLException, IllegalArgumentException {
    String mapValue;
    for (Map.Entry<String, Object> entry : inputJson) {
      mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;

      if (Arrays.asList(ResourceRepo.NOTNULL_FIELDS).contains(entry.getKey())) {
        if (mapValue == null || mapValue.isEmpty()) {
          throw new IllegalArgumentException("Null value input for : " + entry.getKey());
        }
      }

      if (entry.getKey().equalsIgnoreCase(ResourceRepo.CONTENT_FORMAT)) {
        if (!mapValue.equalsIgnoreCase(ResourceRepo.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
          throw new IllegalArgumentException("content format should always be a 'resource' but {} has been sent: " + mapValue);
        } else {
          PGobject contentFormat = new PGobject();
          contentFormat.setType(ResourceRepo.CONTENT_FORMAT_TYPE);
          contentFormat.setValue(mapValue);
          resource.set(entry.getKey(), contentFormat);
        }
      } else if (entry.getKey().equalsIgnoreCase(ResourceRepo.CONTENT_SUBFORMAT)) {
        if (!mapValue.contains(ResourceRepo.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
          throw new IllegalArgumentException("content sub format is not a valid resource format ; {} has been sent: " + mapValue);
        } else {
          PGobject contentSubformat = new PGobject();
          contentSubformat.setType(ResourceRepo.CONTENT_SUBFORMAT_TYPE);
          contentSubformat.setValue(mapValue);
          resource.set(entry.getKey(), contentSubformat);
        }
      } else if (Arrays.asList(ResourceRepo.JSONB_FIELDS).contains(entry.getKey())) {
        PGobject jsonbFields = new PGobject();
        jsonbFields.setType(ResourceRepo.JSONB_FORMAT);
        jsonbFields.setValue(mapValue);
        resource.set(entry.getKey(), jsonbFields);
      } else {
        resource.set(entry.getKey(), entry.getValue());
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
  protected int updateOwnerDataToCopies(String ownerResourceId, JsonObject dataToBePropogated, String originalCreator)
    throws SQLException, IllegalArgumentException {
    LOGGER.debug("updateOwnerDataToCopies: OwnerResourceID {}", ownerResourceId);
    int numRecsUpdated = 0;
    String mapValue;
    List<Object> params = new ArrayList<>();
    String updateStmt = null;
    for (Map.Entry<String, Object> entry : dataToBePropogated) {
      mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
      LOGGER.debug("updateOwnerDataToCopies: OwnerResourceID {}", entry.getKey());

      updateStmt = (updateStmt == null) ? entry.getKey() + " = ?" : updateStmt + ", " + entry.getKey() + " = ?";

      if (entry.getKey().equalsIgnoreCase(ResourceRepo.CONTENT_FORMAT)) {
        if (!mapValue.equalsIgnoreCase(ResourceRepo.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
          throw new IllegalArgumentException("content format should always be a 'resource' but {} has been sent: " + mapValue);
        } else {
          PGobject contentFormat = new PGobject();
          contentFormat.setType(ResourceRepo.CONTENT_FORMAT_TYPE);
          contentFormat.setValue(entry.getValue().toString());
          params.add(contentFormat);
        }
      } else if (entry.getKey().equalsIgnoreCase(ResourceRepo.CONTENT_SUBFORMAT)) {
        if (!mapValue.contains(ResourceRepo.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
          throw new IllegalArgumentException("content sub format is not a valid resource format ; {} has been sent: " + mapValue);
        } else {
          PGobject contentSubformat = new PGobject();
          contentSubformat.setType(ResourceRepo.CONTENT_SUBFORMAT_TYPE);
          contentSubformat.setValue(entry.getValue().toString());
          params.add(contentSubformat);
        }
      } else if (Arrays.asList(ResourceRepo.JSONB_FIELDS).contains(entry.getKey())) {
        PGobject jsonbFields = new PGobject();
        jsonbFields.setType(ResourceRepo.JSONB_FORMAT);
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
      numRecsUpdated = AJEntityResource.update(updateStmt, "original_content_id = ? AND original_creator_id = ? ", params.toArray());
      LOGGER.debug("updateOwnerDataToCopies : Update successful. Number of records updated: {}", numRecsUpdated);
    }

    return numRecsUpdated;
  }

  private static class Holder {
    private static final DBHelper INSTANCE = new DBHelper();
  }

}
