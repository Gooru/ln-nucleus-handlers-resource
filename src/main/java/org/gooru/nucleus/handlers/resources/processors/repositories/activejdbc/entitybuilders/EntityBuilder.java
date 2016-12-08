package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entitybuilders;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.converters.CustomTypeConverter;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.converters.CustomTypeConverterBuilder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.converters.FieldConverter;
import org.javalite.activejdbc.Model;
import org.postgresql.util.PGobject;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 17/10/16.
 */
public interface EntityBuilder<T extends Model> {

    /*
     * Populate the model from JSON object provided. Note that it does not
     * validate the input with respect to whether the field is allowed, or a
     * field is mandatory to be populated in model but it is not. For that have
     * a look at validators package
     */
    default void build(T model, JsonObject input, ConverterRegistry registry) {
        if (model == null || input == null || input.isEmpty()) {
            return;
        }
        input.forEach(entry -> {
            if (registry != null) {
                FieldConverter converter = registry.lookupConverter(entry.getKey());
                Object value = entry.getValue();
                if (converter != null) {
                    PGobject interimValue = converter.convertField(entry.getValue());
                    CustomTypeConverter customTypeConverter = CustomTypeConverterBuilder.build(interimValue);
                    if (customTypeConverter.isCustomType()) {
                        value = customTypeConverter.convertBasedOnType(value);
                    } else {
                        value = interimValue;
                    }
                }
                model.set(entry.getKey(), value);
            }
        });
    }

}
