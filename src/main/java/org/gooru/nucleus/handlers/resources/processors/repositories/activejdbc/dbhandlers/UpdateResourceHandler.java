package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import java.sql.SQLException;
import java.util.Map;
import java.util.StringJoiner;

import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbutils.LicenseUtil;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult.ExecutionStatus;
import org.javalite.activejdbc.Base;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class UpdateResourceHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateResourceHandler.class);
    private boolean isOwner = false;
    private final ProcessorContext context;
    private JsonObject ownerDataToPropogateToCopies;
    private AJEntityResource updateRes;
    private AJEntityResource fetchDBResourceData;

    public UpdateResourceHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        if (context.request() == null || context.request().isEmpty()) {
            LOGGER.error("checkSanity() failed with invalid json. ");
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        if (context.userId() == null || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse("Anonymous user denied this action"),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        if (context.resourceId() == null) {
            LOGGER.error("checkSanity() failed with invalid resourceid. ");
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        } else if (context.resourceId().isEmpty()) {
            LOGGER.error("checkSanity() failed. ResourceID is empty!");
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
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
            } else if (!AJEntityResource.VALID_UPDATE_FIELDS.contains(entry.getKey())) {
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

        LOGGER.debug("checkSanity() passed");
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        // fetch resource from DB based on Id received
        this.fetchDBResourceData = DBHelper.getResourceById(context.resourceId());
        if (this.fetchDBResourceData == null) {
            LOGGER.error(
                "validateRequest : updateResource : Object to update is not found in DB! Input resource ID: {} ",
                context.resourceId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        if (!authorized()) {
            // Update is forbidden
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse("Need to be owner/collaborator on course/collection"),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        LOGGER.debug("validateRequest : updateResource : Ok! So, who is trying to update content? {}.",
            (isOwner) ? "owner" : "someone else");
        
        String mapValue;

        // now mandatory field checks on input resource data and if contains
        // owner-Specific editable fields
        // compare input value and collect only changed attributes in new model
        // that we will use to update
        this.updateRes = new AJEntityResource();
        this.updateRes.setInteger(AJEntityResource.LICENSE, LicenseUtil.getDefaultLicenseCode());
        
        DBHelper.setPGObject(this.updateRes, AJEntityResource.RESOURCE_ID, AJEntityResource.UUID_TYPE,
            context.resourceId());
        if (this.updateRes.hasErrors()) {
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(this.updateRes.errors()),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        LOGGER.debug("validateRequest updateResource : Iterate through the input Json now.");

        for (Map.Entry<String, Object> entry : context.request()) {
            LOGGER.debug("validateRequest updateResource : checking the key & values..before collection. Key: {}",
                entry.getKey());

            mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
            if (AJEntityResource.NOTNULL_FIELDS.contains(entry.getKey())) {
                if (mapValue == null) {
                    LOGGER.error(
                        "validateRequest Failed to update resource. Field : {} : is mandatory field and cannot be null.",
                        entry.getKey());
                    return new ExecutionResult<>(
                        MessageResponseFactory
                            .createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue())),
                        ExecutionResult.ExecutionStatus.FAILED);
                }
            }

            if (AJEntityResource.RESOURCE_URL.equalsIgnoreCase(entry.getKey())) {
                JsonObject resourceIdWithURLDuplicates =
                    DBHelper.getDuplicateResourcesByURL(entry.getValue().toString());
                if (resourceIdWithURLDuplicates != null && !resourceIdWithURLDuplicates.isEmpty()) {
                    LOGGER.error(
                        "validateRequest : Duplicate resource URL found. So cannot go ahead with creating new resource! URL : {}",
                        entry.getKey());
                    LOGGER.error("validateRequest : Duplicate resources : {}", resourceIdWithURLDuplicates);
                    return new ExecutionResult<>(
                        MessageResponseFactory.createValidationErrorResponse(resourceIdWithURLDuplicates),
                        ExecutionResult.ExecutionStatus.FAILED);
                }

            }

            // mandatory and owner specific items may be overlapping...so do a
            // separate check not as ELSE condition
            if (!isOwner && !AJEntityResource.COPY_UPDATE_FIELDS.contains(entry.getKey())) {
                // LOGGER.debug("validateRequest updateResource : Not owner but
                // changing
                // owner specific fields?");
                LOGGER.error("Error updating resource. Field: {} : can not be allowed to update for resource copy.",
                    entry.getKey());
                return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
                    ExecutionResult.ExecutionStatus.FAILED);
            } else if (isOwner && AJEntityResource.OWNER_SPECIFIC_FIELDS.contains(entry.getKey())) {
                // collect the DB fields to update for owner specific fields
                // across
                // all
                // copies of this resource
                LOGGER.debug("updateResource : need to propagate this : {} : to other resources. ", entry.getKey());
                if (ownerDataToPropogateToCopies == null) {
                    ownerDataToPropogateToCopies = new JsonObject();
                }

                ownerDataToPropogateToCopies.put(entry.getKey(), entry.getValue());
            }

            // collect the attributes and values in the model.
            if (AJEntityResource.CONTENT_FORMAT.equalsIgnoreCase(entry.getKey())) {
                if (!AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE.equalsIgnoreCase(mapValue)) {
                    LOGGER.error("updateResource : content format is invalid! : {} ", entry.getKey());
                    return new ExecutionResult<>(
                        MessageResponseFactory
                            .createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue())),
                        ExecutionResult.ExecutionStatus.FAILED);
                }
            } else if (AJEntityResource.CONTENT_SUBFORMAT.equalsIgnoreCase(entry.getKey())) {
                if (mapValue == null || mapValue.isEmpty()
                    || !mapValue.endsWith(AJEntityResource.VALID_CONTENT_FORMAT_FOR_RESOURCE)) {
                    LOGGER.error("updateResource : content subformat is invalid! : {} ", entry.getKey());
                    return new ExecutionResult<>(
                        MessageResponseFactory
                            .createValidationErrorResponse(new JsonObject().put(entry.getKey(), entry.getValue())),
                        ExecutionResult.ExecutionStatus.FAILED);
                } else {
                    DBHelper.setPGObject(this.updateRes, entry.getKey(), AJEntityResource.CONTENT_SUBFORMAT_TYPE,
                        mapValue);
                    if (this.updateRes.hasErrors()) {
                        return new ExecutionResult<>(
                            MessageResponseFactory.createValidationErrorResponse(this.updateRes.errors()),
                            ExecutionResult.ExecutionStatus.FAILED);
                    }
                }
            } else if (AJEntityResource.JSONB_FIELDS.contains(entry.getKey())) {
                DBHelper.setPGObject(this.updateRes, entry.getKey(), AJEntityResource.JSONB_FORMAT, mapValue);
                if (this.updateRes.hasErrors()) {
                    return new ExecutionResult<>(
                        MessageResponseFactory.createValidationErrorResponse(this.updateRes.errors()),
                        ExecutionResult.ExecutionStatus.FAILED);
                }
            } else if (AJEntityResource.UUID_FIELDS.contains(entry.getKey())) {
                DBHelper.setPGObject(this.updateRes, entry.getKey(), AJEntityResource.UUID_TYPE, mapValue);
                if (this.updateRes.hasErrors()) {
                    return new ExecutionResult<>(
                        MessageResponseFactory.createValidationErrorResponse(this.updateRes.errors()),
                        ExecutionResult.ExecutionStatus.FAILED);
                }
            } else {
                this.updateRes.set(entry.getKey(), entry.getValue()); // intentionally
                // kept
                // entry.getValue
                // instead of
                // mapValue as it
                // needs to handle
                // other datatypes
                // like boolean
            }
        }
        
        LOGGER.debug(" \n **** Model to save: {}", this.updateRes);
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        if (this.updateRes == null) {
            LOGGER.debug(
                "executeRequest : We should not end up here...but if we do it is because this object is not updated in validateRequest.");
            LOGGER.error(
                "executeRequest : updateResource : Object to update is not found or NULL! Input resource ID: {} ",
                context.resourceId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        // try to save
        DBHelper.setPGObject(this.updateRes, AJEntityResource.MODIFIER_ID, AJEntityResource.UUID_TYPE,
            context.userId());

        if (!this.updateRes.save()) {
            LOGGER.error("executeRequest : Update resource failed! {} ", this.updateRes.errors());
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(this.updateRes.errors()),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        // save successful...try to propagate ownerData changes to other copies
        if (ownerDataToPropogateToCopies != null) {
            try {
                DBHelper.updateOwnerDataToCopies(this.updateRes, context.resourceId(), ownerDataToPropogateToCopies,
                    context.userId());
                LOGGER.debug(
                    "executeRequest : Updated resource ID: " + this.updateRes.getString(AJEntityResource.RESOURCE_ID));

            } catch (IllegalArgumentException | SQLException e) {
                LOGGER.error("executeRequest : Update resource failed to propagate changes to other copies!", e);
                return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(e.getMessage()),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }

        return new ExecutionResult<>(MessageResponseFactory.createPutSuccessResponse("Location",
            this.updateRes.getString(AJEntityResource.RESOURCE_ID)), ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }
    
    private boolean authorized() {
        String creator = this.fetchDBResourceData.getString(AJEntityResource.CREATOR_ID);
        String course = this.fetchDBResourceData.getString(AJEntityResource.COURSE_ID);
        String collection = this.fetchDBResourceData.getString(AJEntityResource.COLLECTION_ID);
        String originalContentId = this.fetchDBResourceData.getString(AJEntityResource.ORIGINAL_CONTENT_ID);
        String originalCreatorId = this.fetchDBResourceData.getString(AJEntityResource.ORIGINAL_CREATOR_ID);

        if (creator != null && creator.equalsIgnoreCase(context.userId()) && originalContentId == null
            && originalCreatorId == null && collection == null && course == null) {
            // Since the creator is modifying, and it is not part of any
            // collection or course, then owner should be able to modify
            this.isOwner = true;
            return true;
        } else if (creator != null && creator.equalsIgnoreCase(context.userId()) && (originalContentId != null
            || originalCreatorId != null) && collection == null && course == null) {
            LOGGER.error("ACTIONABLE: collection/course is null, but one of original content or creator id NOT null");
            return false;
        } else if (creator != null && creator.equalsIgnoreCase(context.userId()) && originalContentId == null
            && originalCreatorId == null && (collection != null || course != null)) {
            LOGGER.error("ACTIONABLE: original * id is null, but course/collection is NOT null");
            return false;
        } else if (course != null) {
            // Check if user is one of collaborator on course, we do not
            // need to check the owner as course owner should be resource
            // creator
            long authRecordCount = Base.count(AJEntityResource.TABLE_COURSE, AJEntityResource.AUTH_VIA_COURSE_FILTER,
                course, context.userId(), context.userId());
            if (authRecordCount >= 1) {
                // Auth check successful
                LOGGER.debug("Auth check successful based on course: {}", course);
                return true;
            }
        } else if (collection != null) {
            // Check if the user is one of collaborator on collection, we do
            // not need to check about course now
            long authRecordCount = Base.count(AJEntityResource.TABLE_COLLECTION,
                AJEntityResource.AUTH_VIA_COLLECTION_FILTER, collection, context.userId(), context.userId());
            if (authRecordCount >= 1) {
                LOGGER.debug("Auth check successful based on collection: {}", collection);
                return true;
            }
        }

        LOGGER.error("Auth check failed");
        return false;
    }
}
