package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public final class ResourceUpdateHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUpdateHelper.class);

    private ResourceUpdateHelper() {
        throw new AssertionError();
    }

    /*
     * updateOwnerDataToCopies: as a consequence of primary resource update, we
     * need to update the copies of this resource - but ONLY owner specific data
     * items.
     *
     * NOTE: This method does not do a lot of null checks etc; as all checks are
     * already done by UpdateResource() method.
     */

    public static int updateOwnerDataToCopies(AJEntityResource resource, String ownerResourceId,
        JsonObject dataToBePropogated, String originalCreator) throws SQLException, IllegalArgumentException {
        LOGGER.debug("updateOwnerDataToCopies: OwnerResourceID {}", ownerResourceId);
        int numRecsUpdated = 0;
        String mapValue;
        List<Object> params = new ArrayList<Object>();
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

                updateStmt =
                    (updateStmt == null) ? entry.getKey() + " = ?" : updateStmt + ", " + entry.getKey() + " = ?";

                if (AJEntityResource.CONTENT_FORMAT.equalsIgnoreCase(entry.getKey())) {
                    if (!AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE.equalsIgnoreCase(mapValue)) {
                        throw new IllegalArgumentException(
                            "content format should always be a 'resource' but {} has been sent: " + mapValue);
                    } else {
                        PGobject contentformat = new PGobject();
                        contentformat.setType(AJEntityResource.CONTENT_FORMAT_TYPE);
                        contentformat.setValue(entry.getValue().toString());
                        params.add(contentformat);
                    }
                } else if (AJEntityResource.CONTENT_SUBFORMAT.equalsIgnoreCase(entry.getKey())) {
                    if (mapValue == null || mapValue.isEmpty() || !mapValue
                        .endsWith(AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
                        throw new IllegalArgumentException(
                            "content sub format is not a valid resource format ; {} has been sent: " + mapValue);
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
                numRecsUpdated = AJEntityResource
                    .update(updateStmt, AJEntityResource.SQL_UPDATEOWNERDATATOCOPIES_WHERECLAUSE, params.toArray());
                LOGGER.debug("updateOwnerDataToCopies : Update successful. Number of records updated: {}",
                    numRecsUpdated);
            }
        }
        return numRecsUpdated;
    }
}