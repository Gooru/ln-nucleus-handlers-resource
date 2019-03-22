package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.transactions.exceptionhandlers;

import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;

public interface ExceptionHandler {
  
  ExecutionResult<MessageResponse> handleError(Throwable e);

}
