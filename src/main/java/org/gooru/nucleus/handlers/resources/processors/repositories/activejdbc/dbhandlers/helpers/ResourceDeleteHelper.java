package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.util.ArrayList;
import java.util.List;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceDeleteHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceDeleteHelper.class);

    private ResourceDeleteHelper() {
        throw new AssertionError();
    }

    public static int deleteResourceCopies(AJEntityResource resource, String originalResourceId)
        throws IllegalArgumentException {
        // update content set is_deleted=true where content_format='resource;
        // and original_content_id=Argument and is_deleted=false
        LOGGER.debug("deleteResourceCopies: originalResourceId {}", originalResourceId);
        int numRecsUpdated;
        List<Object> params = new ArrayList<Object>();
        String updateStmt = AJEntityResource.IS_DELETED + "= ? ";
        TypeHelper.setPGObject(resource, AJEntityResource.CONTENT_FORMAT, AJEntityResource.CONTENT_FORMAT_TYPE,
            AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);
        params.add(true);
        params.add(AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);
        params.add(originalResourceId);

        numRecsUpdated = AJEntityResource
            .update(updateStmt, AJEntityResource.SQL_DELETERESOURCECOPIES_WHERECLAUSE, params.toArray());
        LOGGER.debug(
            "deleteResourceCopies : Update successful and is_deleted set to true for all copies of the resource {} . "
                + "Number of records updated: {}", originalResourceId, numRecsUpdated);
        return numRecsUpdated;

    }
}