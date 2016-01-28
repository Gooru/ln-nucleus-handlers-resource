package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;


public class AJResourceRepo implements ResourceRepo {

  private final ProcessorContext context;

  public AJResourceRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse createResource() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildCreateResourceHandler(context));
  }

  @Override
  public MessageResponse updateResource() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildUpdateResourceHandler(context));
  }

  @Override
  public MessageResponse fetchResource() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchResourceHandler(context));

  }

  @Override
  public MessageResponse deleteResource() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildDeleteResourceHandler(context));
  }
}