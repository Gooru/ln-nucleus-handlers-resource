package org.gooru.nucleus.handlers.resources.processors.utils;

import java.util.UUID;

import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 2/1/17.
 */
public class ValidationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtils.class);

    private ValidationUtils() {
        throw new AssertionError();
    }

    public static boolean validateUser(String userId) {
        return !(userId == null || userId.isEmpty()) && (userId.equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)
            || validateUuid(userId));
    }

    public static boolean validateUuid(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid request, id is not a valid uuid. Aborting");
            return false;
        } catch (Exception e) {
            LOGGER.error("Invalid request, id is not a valid uuid. Aborting");
            return false;
        }
    }

    public static boolean isIdInvalid(ProcessorContext context) {
        if (context.resourceId() == null || context.resourceId().isEmpty()) {
            LOGGER.error("Invalid request, resource id not available. Aborting");
            return true;
        }
        return !validateUuid(context.resourceId());
    }

}
