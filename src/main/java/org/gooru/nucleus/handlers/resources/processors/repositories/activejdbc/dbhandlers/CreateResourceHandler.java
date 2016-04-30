package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import java.util.Map;
import java.util.StringJoiner;

import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class CreateResourceHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateResourceHandler.class);
    private final ProcessorContext context;
    private AJEntityResource createRes;

    public CreateResourceHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        if (context.request() == null || context.request().isEmpty()) {
            LOGGER.warn("invalid request received to create resource");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to create resource"),
                ExecutionStatus.FAILED);
        }

        if (context.userId() == null || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse("Anonymous user denied this action"),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        JsonObject request = context.request();
        StringJoiner missingFields = new StringJoiner(", ");
        StringJoiner resourceIrrelevantFields = new StringJoiner(", ");
        String mapValue;
        for (Map.Entry<String, Object> entry : request) {
            mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
            if (AJEntityResource.NOTNULL_FIELDS.contains(entry.getKey())) {
                if (mapValue == null || mapValue.isEmpty()) {
                    missingFields.add(entry.getKey());
                }
            } else if (!AJEntityResource.RESOURCE_SPECIFIC_FIELDS.contains(entry.getKey())) {
                resourceIrrelevantFields.add(entry.getKey());
            }
        }

        // TODO: May be need to revisit this logic of validating fields and
        // returning error back for all validation failed in one go
        if (!missingFields.toString().isEmpty()) {
            LOGGER.info("request data validation failed for '{}'", missingFields.toString());
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
                "mandatory field(s) '" + missingFields.toString() + "' missing"), ExecutionStatus.FAILED);
        }

        if (!resourceIrrelevantFields.toString().isEmpty()) {
            LOGGER.info("request data validation failed for '{}'", resourceIrrelevantFields.toString());
            return new ExecutionResult<>(MessageResponseFactory
                .createInvalidRequestResponse("Resource irrelevant fields are being sent in the request '"
                    + resourceIrrelevantFields.toString() + '\''),
                ExecutionStatus.FAILED);
        }

        LOGGER.debug("checkSanity() OK");
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        try {

            this.createRes = new AJEntityResource();
            DBHelper.populateEntityFromJson(context.request(), createRes);
            DBHelper.setPGObject(this.createRes, AJEntityResource.MODIFIER_ID, AJEntityResource.UUID_TYPE,
                context.userId());
            DBHelper.setPGObject(this.createRes, AJEntityResource.CREATOR_ID, AJEntityResource.UUID_TYPE,
                context.userId());
            DBHelper.setPGObject(this.createRes, AJEntityResource.ORIGINAL_CREATOR_ID, AJEntityResource.UUID_TYPE,
                context.userId());
            DBHelper.setPGObject(this.createRes, AJEntityResource.CONTENT_FORMAT, AJEntityResource.CONTENT_FORMAT_TYPE,
                AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE);

            Integer licenseFromRequest = this.createRes.getInteger(AJEntityResource.LICENSE);
            if (licenseFromRequest == null || !DBHelper.isValidLicense(licenseFromRequest)) {
                this.createRes.setInteger(AJEntityResource.LICENSE, DBHelper.getDafaultLicense());
            }
            LOGGER.debug("validateRequest : Creating resource From MAP  : {}", this.createRes.toInsert());

            JsonObject resourceIdWithURLDuplicates =
                DBHelper.getDuplicateResourcesByURL(this.createRes.getString(AJEntityResource.RESOURCE_URL));
            if (resourceIdWithURLDuplicates != null && !resourceIdWithURLDuplicates.isEmpty()) {
                LOGGER.error(
                    "validateRequest : Duplicate resource URL found. So cannot go ahead with creating new resource! URL : {}",
                    createRes.getString(AJEntityResource.RESOURCE_URL));
                LOGGER.error("validateRequest : Duplicate resources : {}", resourceIdWithURLDuplicates);
                return new ExecutionResult<>(
                    MessageResponseFactory.createValidationErrorResponse(resourceIdWithURLDuplicates),
                    ExecutionResult.ExecutionStatus.FAILED);
            }

        } catch (IllegalArgumentException e) {
            LOGGER.error("CheckSanity : {} ", e);
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        if (!this.createRes.insert()) {
            if (this.createRes.hasErrors()) {
                LOGGER.error("executeRequest : Create resource failed for input object. Errors: {}",
                    this.createRes.errors());
                return new ExecutionResult<>(
                    MessageResponseFactory.createValidationErrorResponse(this.createRes.errors()),
                    ExecutionResult.ExecutionStatus.FAILED);
            } else {
                LOGGER.error("executeRequest : Create resource failed for input object: {}", context.request());
                return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }

        // successful...
        LOGGER.debug("executeRequest : Created resource ID: " + this.createRes.getString(AJEntityResource.RESOURCE_ID));
        return new ExecutionResult<>(MessageResponseFactory.createPostSuccessResponse("Location",
            this.createRes.getString(AJEntityResource.RESOURCE_ID)), ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

}
