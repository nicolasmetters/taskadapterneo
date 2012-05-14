package com.taskadapter.license;

import org.apache.commons.codec.binary.Base64;

import java.text.SimpleDateFormat;

import static com.taskadapter.license.LicenseFormatDescriptor.*;

public class LicenseTextGenerator {
    private SimpleDateFormat licenseDateFormatter = new SimpleDateFormat(LICENSE_DATE_FORMAT);

    private static final String LINE_DELIMITER = "\n";
    private static final String KEY_STR = "-------------- Key --------------" + LINE_DELIMITER;
    private License license;

    public LicenseTextGenerator(License license) {
        this.license = license;
    }

    public String generateLicenseText() {
        String expirationDateFormattedString = licenseDateFormatter.format(license.getExpiresOn());
        String text = license.getType().toString() + license.getCustomerName() + license.getEmail() + license.getCreatedOn() + expirationDateFormattedString;
        String key = new LicenseEncryptor(PASSWORD).chiper(text);
        String base64EncodedKey = new String(Base64.encodeBase64(key.getBytes()));

        StringBuilder licenseText = new StringBuilder()
                .append(PREFIX_PRODUCT).append(license.getProduct().toString())
                .append(LINE_DELIMITER).append(PREFIX_LICENSE_TYPE).append(license.getType().toString())
                .append(LINE_DELIMITER).append(PREFIX_REGISTERED_TO).append(license.getCustomerName())
                .append(LINE_DELIMITER).append(PREFIX_EMAIL).append(license.getEmail())
                .append(LINE_DELIMITER).append(PREFIX_CREATED_ON).append(license.getCreatedOn())
                .append(LINE_DELIMITER).append(PREFIX_EXPIRES_ON).append(expirationDateFormattedString)
                .append(LINE_DELIMITER).append(KEY_STR).append(base64EncodedKey);

        return licenseText.toString();
    }
}
