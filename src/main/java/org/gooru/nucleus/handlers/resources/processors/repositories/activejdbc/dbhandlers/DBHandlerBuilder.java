package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;

/**
 * Created by ashish on 11/1/16.
 */
public final class DBHandlerBuilder {

  public static DBHandler buildFetchResourceHandler(ProcessorContext context) {
    return new FetchResourceHandler(context);
  }

  public static DBHandler buildUpdateResourceHandler(ProcessorContext context) {
    return new UpdateResourceHandler(context);
  }

  public static DBHandler buildCreateResourceHandler(ProcessorContext context) {
    return new CreateResourceHandler(context);
  }

  public static DBHandler buildDeleteResourceHandler(ProcessorContext context) {
    return new DeleteResourceHandler(context);
  }

  private DBHandlerBuilder() {
    throw new AssertionError();
  }
}
