package sipSe.authLogin;

import org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse.DecryptOtpResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse.LoginInitiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse.LoginVerifyResponse;
import org.jarfinApiBackendAutomation.utils.ApiAssertions;
import org.testng.asserts.SoftAssert;

public class SipSeAuthValidation extends ApiAssertions {

    public SipSeAuthValidation(SoftAssert softAssert) {
        super(softAssert);
    }

    public void assertRequestOtp(LoginInitiateResponse response) {
        if (!assertStatusCode(response.getStatusCode(), 200, "Request OTP")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.getData().isOtpSent(), "isOtpSent must be true");
        softAssert.assertNotNull(response.getData().getOtpLength(), "otpLength must not be null");
        softAssert.assertAll();
    }

    public void assertRequestOtpFailure(LoginInitiateResponse response, String scenario) {
        if (!assertStatusCode(response.getStatusCode(), 400, "Request OTP [" + scenario + "]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(
                response.isSuccess(), "[" + scenario + "] success must be false for invalid input");
        softAssert.assertNull(
                response.getData(), "[" + scenario + "] data must be null on failure");
        softAssert.assertAll();
    }

    public void assertFetchOtpFromDb(String otp, String phoneNumber) {
        softAssert.assertNotNull(
                otp, "Encrypted OTP must not be null for phoneNumber=" + phoneNumber);
        softAssert.assertFalse(
                otp == null || otp.isBlank(),
                "Encrypted OTP must not be blank for phoneNumber=" + phoneNumber);
        softAssert.assertAll();
    }

    public void assertDecryptOtp(DecryptOtpResponse response) {
        if (!assertStatusCode(response.getStatusCode(), 200, "Decrypt OTP")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "Decrypt OTP success must be true");
        softAssert.assertNotNull(response.getData(), "Decrypted OTP must not be null");
        softAssert.assertFalse(
                response.getData() == null || response.getData().isBlank(),
                "Decrypted OTP must not be blank");
        softAssert.assertAll();
    }

    public void assertVerifyOtp(LoginVerifyResponse response) {
        if (!assertStatusCode(response.getStatusCode(), 200, "Verify OTP")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "Verify OTP success must be true");
        softAssert.assertNotNull(response.getData(), "Verify OTP response data must not be null");
        softAssert.assertNotNull(
                response.getData().getAccessToken(), "accessToken must not be null");
        softAssert.assertFalse(
                response.getData().getAccessToken().isBlank(), "accessToken must not be blank");
        softAssert.assertNotNull(
                response.getData().getRefreshToken(), "refreshToken must not be null");
        softAssert.assertAll();
    }

    public void assertVerifyOtpFailure(LoginVerifyResponse response) {
        if (!assertStatusCode(response.getStatusCode(), 400, "Verify OTP")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(
                response.isSuccess(), "Verify OTP success must be false for invalid inputs");
        softAssert.assertNull(response.getData(), "Data must be null when verify OTP fails");
        softAssert.assertAll();
    }
}
