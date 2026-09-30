package sipSe.testData.auth;

import org.jarfinApiBackendAutomation.configuration.ConfigLoader;

public class TestDataAuth {
    public static final String TEST_PHONE_NUMBER_SIPSE =
            ConfigLoader.get("test.phone.number.sipse");
    public static final String TEST_COUNTRY_CODE = ConfigLoader.get("test.country.code");
    public static final String TEST_ADMIN_TOKEN = ConfigLoader.get("test.admin.token");

    // Fallback values used when ENTER_PAN_MANUALLY status is returned from lookup
    public static final String MANUAL_PAN = ConfigLoader.get("test.manual.pan");
    public static final String MANUAL_NAME = ConfigLoader.get("test.manual.name");
    public static final String MANUAL_DOB = ConfigLoader.get("test.manual.dob");
}
