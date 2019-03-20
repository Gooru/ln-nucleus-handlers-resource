package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.exceptionhandlers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author renuka
 * 
 */
public class ExceptionHandlerRegistry {

  private static final List<ExceptionHandler> EXCEPTION_HANDLERS = new ArrayList<>(2);

  public static ExceptionHandlerRegistry getInstance() {
    return Holder.INSTANCE;
  }
  
  static {
    initializeExceptionHandlers();
  }

  //DefaultExceptionHandler should always be the last one
  private static void initializeExceptionHandlers() {
    EXCEPTION_HANDLERS.add(UTF8EncodingExceptionHandler.getInstance());
    EXCEPTION_HANDLERS.add(DefaultExceptionHandler.getInstance());
  }
  
  public List<ExceptionHandler> getHandlers() {
    return EXCEPTION_HANDLERS;
  }
  
  private static final class Holder {
    private static final ExceptionHandlerRegistry INSTANCE = new ExceptionHandlerRegistry();
  }
}
