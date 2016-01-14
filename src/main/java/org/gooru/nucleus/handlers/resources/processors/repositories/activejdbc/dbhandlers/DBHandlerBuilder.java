package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;

/**
 * Created by ashish on 11/1/16.
 */
public class DBHandlerBuilder {

  public DBHandler buildFetchResourceHandler(ProcessorContext context) {
    return new FetchResourceHandler(context);
  }

  public DBHandler buildUpdateResourceHandler(ProcessorContext context) {
    return new UpdateResourceHandler(context);
  }

  public DBHandler buildCreateResourceHandler(ProcessorContext context) {
    return new CreateResourceHandler(context);

  }
}
