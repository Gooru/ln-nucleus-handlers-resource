package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbutils.LicenseUtil;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityMetadataReference;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityResource;

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

    public static void populateLicense(AJEntityOriginalResource resource) {

        Integer licenseFromRequest = resource.getInteger(AJEntityResource.LICENSE);
        if (licenseFromRequest == null || !isValidLicense(licenseFromRequest)) {
            resource.setInteger(AJEntityOriginalResource.LICENSE, LicenseHelper.getDafaultLicense());
        } else {
            resource.setInteger(AJEntityOriginalResource.LICENSE, licenseFromRequest);
        }

    }
}