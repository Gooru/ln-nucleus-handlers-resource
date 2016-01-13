package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.resources.processors.ProcessorContext;
import org.gooru.nucleus.handlers.resources.processors.repositories.ResourceRepo;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;


/**
 * Created by ashish on 29/12/15.
 */
public class AJResourceRepo implements ResourceRepo {
 
  private final ProcessorContext context;
  
  /*
   * <TBD> Need to decide on owner specific editable fields, non-owner
   * non-editable and common fields UUID generation In update, Check if the user
   * is the owner of the resource, if so then allow editing
   * OWNER_SPECIFIC_FIELDS & COMMON_EDITABLE_FIELDS and update all
   * COMMON_EDITABLE_FIELDS in the copied records and keep OWNER_SPECIFIC_FIELDS
   * changes locally If the user is not owner, then allow editing the
   * COMMON_EDITABLE_FIELDS but keep changes locally 
   * Taxonomy changes - which I am not clear - something to do with user preference while updating
   * Exception handling
   */
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
  
  
}