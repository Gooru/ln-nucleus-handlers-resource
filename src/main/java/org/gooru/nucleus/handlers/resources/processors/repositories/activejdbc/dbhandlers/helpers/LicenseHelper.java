package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbutils.LicenseUtil;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityMetadataReference;

public final class LicenseHelper {

    private LicenseHelper() {
        throw new AssertionError();
    }

    public static Integer getDafaultLicense() {
        return LicenseUtil.getDefaultLicenseCode();
    }

    public static boolean isValidLicense(int licenseId) {
        Long count = AJEntityMetadataReference.count(AJEntityMetadataReference.VALIDATE_LICENSE, licenseId);
        return count == 1;
    }
}