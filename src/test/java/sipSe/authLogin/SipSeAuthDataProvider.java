package sipSe.authLogin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jarfinApiBackendAutomation.data.requestModel.sipseAuth.LoginInitiateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.sipseAuth.LoginVerifyRequest;
import org.testng.annotations.DataProvider;
import sipSe.testData.auth.TestDataAuth;

public class SipSeAuthDataProvider {

    @AllArgsConstructor
    @Getter
    public enum ExpectedError {
        INVALID_PHONE("Invalid or unsupported phone number format"),
        INVALID_OTP("Invalid or expired OTP"),
        MISSING_FIELD("Required field is missing or empty");

        private final String description;
    }

    @DataProvider(name = "requestOtpScenarios", parallel = true)
    public static Object[][] requestOtpScenarios() {
        return new Object[][] {
            {
                "Negative Scenario: Too Short Phone Number",
                LoginInitiateRequest.buildLoginInitiatePayload("+91", "123456"),
                ExpectedError.INVALID_PHONE
            },
            {
                "Negative Scenario: Too Long Phone Number",
                LoginInitiateRequest.buildLoginInitiatePayload("+91", "987654321098"),
                ExpectedError.INVALID_PHONE
            },
            {
                "Negative Scenario: Alphabetic Phone Number",
                LoginInitiateRequest.buildLoginInitiatePayload("+91", "abcdefghij"),
                ExpectedError.INVALID_PHONE
            },
            {
                "Negative Scenario: Special Characters in Phone Number",
                LoginInitiateRequest.buildLoginInitiatePayload("+91", "98@76#54"),
                ExpectedError.INVALID_PHONE
            },
            {
                "Negative Scenario: All Zeros Phone Number",
                LoginInitiateRequest.buildLoginInitiatePayload("+91", "0000000000"),
                ExpectedError.INVALID_PHONE
            },
            {
                "Negative Scenario: Empty Phone Number",
                LoginInitiateRequest.buildLoginInitiatePayload("+91", ""),
                ExpectedError.MISSING_FIELD
            },
            {
                "Negative Scenario: Whitespace Phone Number",
                LoginInitiateRequest.buildLoginInitiatePayload("+91", " "),
                ExpectedError.INVALID_PHONE
            },
            {
                "Negative Scenario: Country Code as Phone Number",
                LoginInitiateRequest.buildLoginInitiatePayload("+91", "+91"),
                ExpectedError.INVALID_PHONE
            },
            {
                "Negative Scenario: 5-Digit Phone Number",
                LoginInitiateRequest.buildLoginInitiatePayload("+91", "98765"),
                ExpectedError.INVALID_PHONE
            },
            {
                "Negative Scenario: Alphanumeric Phone Number",
                LoginInitiateRequest.buildLoginInitiatePayload("+91", "12345abcde"),
                ExpectedError.INVALID_PHONE
            },
            {
                "Positive Scenario: With Valid Phone Number",
                LoginInitiateRequest.buildLoginInitiatePayload(
                        "+91", TestDataAuth.TEST_PHONE_NUMBER_SIPSE),
                null
            },
        };
    }

    @DataProvider(name = "verifyOtpScenarios", parallel = true)
    public static Object[][] verifyOtpScenarios() {
        return new Object[][] {
            {
                "Negative Scenario: Empty OTP",
                LoginVerifyRequest.buildLoginVerifyPayload(
                        "+91", TestDataAuth.TEST_PHONE_NUMBER_SIPSE, ""),
                false
            },
            {
                "Negative Scenario: Short OTP",
                LoginVerifyRequest.buildLoginVerifyPayload(
                        "+91", TestDataAuth.TEST_PHONE_NUMBER_SIPSE, "123"),
                false
            },
            {
                "Negative Scenario: Non-Numeric OTP",
                LoginVerifyRequest.buildLoginVerifyPayload(
                        "+91", TestDataAuth.TEST_PHONE_NUMBER_SIPSE, "abcdef"),
                false
            },
            {
                "Negative Scenario: Invalid OTP",
                LoginVerifyRequest.buildLoginVerifyPayload(
                        "+91", TestDataAuth.TEST_PHONE_NUMBER_SIPSE, "000000"),
                false
            },
            {
                "Negative Scenario: Empty Phone Number",
                LoginVerifyRequest.buildLoginVerifyPayload("+91", "", "123456"),
                false
            },
            {
                "Negative Scenario: Empty Country Code",
                LoginVerifyRequest.buildLoginVerifyPayload(
                        "", TestDataAuth.TEST_PHONE_NUMBER_SIPSE, "123456"),
                false
            },
            {
                "Positive Scenario: With Valid Inputs",
                LoginVerifyRequest.buildLoginVerifyPayload(
                        "+91", TestDataAuth.TEST_PHONE_NUMBER_SIPSE, null),
                true // OTP injected at runtime from fetchOtpFromDb
            },
        };
    }
}
