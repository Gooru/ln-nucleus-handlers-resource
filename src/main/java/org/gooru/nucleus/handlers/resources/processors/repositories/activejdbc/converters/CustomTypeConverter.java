package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.converters;

/**
 * @author ashish on 3/11/16.
 */
public interface CustomTypeConverter {

    boolean isCustomType();

    Object convertBasedOnType(Object value);
}
