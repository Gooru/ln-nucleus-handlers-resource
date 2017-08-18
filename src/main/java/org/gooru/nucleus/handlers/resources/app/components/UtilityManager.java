package org.gooru.nucleus.handlers.resources.app.components;

import org.gooru.nucleus.handlers.resources.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.handlers.resources.bootstrap.startup.Initializer;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbutils.LicenseUtil;
import org.gooru.nucleus.libs.tenant.bootstrap.TenantInitializer;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public final class UtilityManager implements Initializer, Finalizer {
    private static final UtilityManager ourInstance = new UtilityManager();

    public static UtilityManager getInstance() {
        return ourInstance;
    }

    private UtilityManager() {
    }

    @Override
    public void finalizeComponent() {

    }

    @Override
    public void initializeComponent(Vertx vertx, JsonObject config) {
        TenantInitializer.initialize(DataSourceRegistry.getInstance().getDefaultDataSource());
        LicenseUtil.initialize();
    }

}
