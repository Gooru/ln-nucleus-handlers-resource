package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.sql.SQLException;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TypeHelper {

    private TypeHelper() {
        throw new AssertionError();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeHelper.class);

    public static void setPGObject(AJEntityResource resource, String field, String type, String value) {
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