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
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildCreateResourceHandler(context));
    }

    @Override
    public MessageResponse updateResource() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildUpdateResourceHandler(context));
    }

    @Override
    public MessageResponse fetchResource() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildFetchResourceHandler(context));

    }

    @Override
    public MessageResponse deleteResource() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildDeleteResourceHandler(context));
    }
}
