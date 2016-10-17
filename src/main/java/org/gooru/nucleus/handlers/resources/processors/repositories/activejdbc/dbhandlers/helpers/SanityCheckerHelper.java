package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import org.gooru.nucleus.handlers.resources.constants.MessageConstants;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SanityCheckerHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SanityCheckerHelper.class);

    private SanityCheckerHelper() {
        throw new AssertionError();
    }

    public static ExecutionResult<MessageResponse> verifyUserExcludeAnonymous(ProcessorContext context) {
        if (context.userId() == null || context.userId().isEmpty() || context.userId()
            .equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse("Anonymous user denied this action"),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    public static ExecutionResult<MessageResponse> verifyUserAllowAnonymous(ProcessorContext context) {
        if (context.userId() == null || context.userId().isEmpty()) {
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Invalid user context"),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    public static ExecutionResult<MessageResponse> verifyResourceId(ProcessorContext context) {
        if (context.resourceId() == null) {
            LOGGER.error("checkSanity() failed. ResourceID is null!");
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        } else if (context.resourceId().isEmpty()) {
            LOGGER.error("checkSanity() failed. ResourceID is empty!");
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

}
