package com.taskadapter.license;

import com.taskadapter.util.MyIOUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Calendar;

public class LicenseTest {
    @Test(expected = LicenseExpiredException.class)
    public void expiredLicenseThrowsException() throws LicenseException {
        Calendar calendar = Calendar.getInstance();
        // 1 month in the past
        calendar.add(Calendar.MONTH, -1);
        License expiredLicense = new License();
        expiredLicense.setExpiresOn(calendar.getTime());
        expiredLicense.validate();
    }

    @Test(expected = LicenseParseException.class)
    public void invalidLicenseThrowsException() throws IOException, LicenseException {
        String invalidLicenseString = MyIOUtils.getResourceAsString("taskadapter.license.invalid");
        License invalidLicense = new LicenseParser().parseLicense(invalidLicenseString);
        invalidLicense.validate();
    }
}
