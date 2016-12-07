package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.sql.SQLException;

import org.javalite.activejdbc.Model;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TypeHelper {

    private TypeHelper() {
        throw new AssertionError();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeHelper.class);

    public static void setPGObject(Model model, String field, String type, String value) {
        PGobject pgObject = new PGobject();
        pgObject.setType(type);
        try {
            pgObject.setValue(value);
            model.set(field, pgObject);
        } catch (SQLException e) {
            LOGGER.error("Not able to set value for field: {}, type: {}, value: {}", field, type, value);
            model.errors().put(field, value);
        }
    }
}