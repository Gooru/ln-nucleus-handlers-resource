package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.EntityConstants;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

final class ResourceRefUpdateHelper {

    private ResourceRefUpdateHelper() {
        throw new AssertionError();
    }

    private static final Map<String, String> ownerFieldNameTypeMap;

    static {
        ownerFieldNameTypeMap = new HashMap<>(AJEntityResource.OWNER_SPECIFIC_FIELDS.size());
        for (int i = 0; i < AJEntityResource.OWNER_SPECIFIC_FIELDS.size(); i++) {
            ownerFieldNameTypeMap.put(AJEntityResource.OWNER_SPECIFIC_FIELDS.get(i),
                AJEntityResource.OWNER_SPECIFIC_FIELDS_TYPES.get(i));
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRefUpdateHelper.class);

    static ExecutionResult<MessageResponse> updateResourceRef(AJEntityResource resource, ProcessorContext context) {
        new DefaultResourceRefBuilder().build(resource, context.request(), AJEntityResource.getConverterRegistry());

        TypeHelper
            .setPGObject(resource, AJEntityOriginalResource.MODIFIER_ID, EntityConstants.UUID_TYPE, context.userId());

        ExecutionResult<MessageResponse> result = saveResourceRef(resource, context);
        if (result.hasFailed()) {
            return result;
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    static ExecutionResult<MessageResponse> updateResourceRefsForGivenOriginal(AJEntityOriginalResource resource,
        ProcessorContext context) {
        JsonObject refUpdatePayload = getPayloadToUpdateRefs(context.request());
        if (refUpdatePayload == null || refUpdatePayload.isEmpty()) {
            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.SUCCESSFUL);
        }
        PreparedStatement ps = createResourceRefUpdateStatementForGivenOriginal(context, refUpdatePayload);
        try {
            List<Object> params = new ArrayList<>(refUpdatePayload.size());
            Iterator<Map.Entry<String, Object>> it = refUpdatePayload.iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                params.add(String.valueOf(entry.getValue()));
            }
            Base.addBatch(ps, params.toArray());
            Base.executeBatch(ps);
            ps.close();
            LOGGER.info("Resource '{}' updated along with references", context.resourceId());
            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.SUCCESSFUL);
        } catch (DBException | SQLException e) {
            LOGGER.error("Error updating references for resource '{}'", context.resourceId(), e);
            return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Error from store"),
                ExecutionResult.ExecutionStatus.FAILED);
        }
    }

    static ExecutionResult<MessageResponse> validatePayloadForResourceRef(AJEntityResource resource,
        ProcessorContext context) {
        JsonObject errors = new DefaultPayloadValidator()
            .validatePayload(context.request(), AJEntityResource.editFieldSelector(),
                AJEntityResource.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    private static PreparedStatement createResourceRefUpdateStatementForGivenOriginal(ProcessorContext context,
        JsonObject refUpdatePayload) {
        StringBuilder query =
            new StringBuilder().append("UPDATE ").append(AJEntityResource.TABLE_RESOURCE).append(" SET ");
        Iterator<Map.Entry<String, Object>> it = refUpdatePayload.iterator();
        for (; ; ) {
            Map.Entry<String, Object> entry = it.next();
            query.append(entry.getKey()).append(getTypedAttributeForQuery(entry.getKey()));
            if (it.hasNext()) {
                query.append(", ");
            } else {
                break;
            }
        }
        query.append(getWhereClause(context)).toString();
        PreparedStatement ps = Base.startBatch(query.toString());
        LOGGER.debug("query to update resource ref :{}", query.toString());
        return ps;
    }

    private static String getWhereClause(ProcessorContext context) {
        return " where " + AJEntityResource.ORIGINAL_CONTENT_ID + " = '" + context.resourceId() + "'::"
            + EntityConstants.UUID_TYPE;
    }
    
    private static String getTypedAttributeForQuery(String field) {
        String type = ownerFieldNameTypeMap.get(field);
        if (type == null || Objects.equals(type, "boolean")) {
            return " = ?";
        } else {
            return " = ?::" + type;
        }
    }

    private static JsonObject getPayloadToUpdateRefs(JsonObject request) {
        JsonObject result = new JsonObject();
        AJEntityResource.OWNER_SPECIFIC_FIELDS.forEach(field -> {
            Object value = request.getValue(field);
            if (value != null) {
                result.put(field, value);
            }
        });
        return result;
    }

    private static ExecutionResult<MessageResponse> saveResourceRef(AJEntityResource resource,
        ProcessorContext context) {
        if (!resource.save()) {
            LOGGER.error("Update resource reference failed! {} ", resource.errors());
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(resource.errors()),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    private static class DefaultResourceRefBuilder implements EntityBuilder<AJEntityResource> {
    }

}