package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.converters;

import org.postgresql.util.PGobject;

/**
 * @author ashish on 3/11/16.
 */
public final class CustomTypeConverterBuilder {
    private CustomTypeConverterBuilder() {
        throw new AssertionError();
    }

    public static CustomTypeConverter build(PGobject pgObject) {
        return new CustomTypeConverterImpl(pgObject);
    }
}
