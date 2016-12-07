package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.converters;

import java.sql.Connection;
import java.sql.SQLException;

import org.javalite.activejdbc.Base;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;

/**
 * @author ashish on 3/11/16.
 */
class CustomTypeConverterImpl implements CustomTypeConverter {

    private final PGobject pgObject;
    private static final String CUSTOM_TYPE_PREFIX = "custom-";
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomTypeConverter.class);

    public CustomTypeConverterImpl(PGobject pgObject) {
        this.pgObject = pgObject;
    }

    @Override
    public boolean isCustomType() {
        String type = this.pgObject.getType();
        return (type != null && !type.isEmpty() && type.startsWith(CUSTOM_TYPE_PREFIX));

    }

    @Override
    public Object convertBasedOnType(Object value) {
        String type = this.pgObject.getType();
        if (type != null && !type.isEmpty()) {
            return doConversion(type, value);
        }
        return value;
    }

    private Object doConversion(String type, Object value) {
        switch (type) {
        case "custom-int-array-from-jsonarray":
            return convertIntArrayFromJsonArray(value);
        case "custom-text-array-from-jsonarray":
            return convertTextArrayFromJsonArray(value);
        default:
            return value;
        }
    }

    private Object convertTextArrayFromJsonArray(Object value) {
        if (value instanceof JsonArray) {
            if (!((JsonArray) value).isEmpty()) {
                Connection connection = Base.connection();
                try {
                    return connection.createArrayOf("VARCHAR", ((JsonArray) value).getList().toArray());
                } catch (SQLException e) {
                    LOGGER.warn("Not able to convert JsonArray to JDBC VARCHAR Array");
                }
            }
        }
        return value;
    }

    private Object convertIntArrayFromJsonArray(Object value) {
        if (value instanceof JsonArray) {
            if (!((JsonArray) value).isEmpty()) {
                Connection connection = Base.connection();
                try {
                    return connection.createArrayOf("Integer", ((JsonArray) value).getList().toArray());
                } catch (SQLException e) {
                    LOGGER.warn("Not able to convert JsonArray to JDBC Integer Array");
                }
            }
        }
        return value;
    }
}
