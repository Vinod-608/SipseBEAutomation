package sipSe.authLogin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jarfinApiBackendAutomation.data.requestModel.sipseAuth.LoginInitiateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.sipseAuth.LoginVerifyRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse.DecryptOtpResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse.LoginVerifyResponse;
import org.jarfinApiBackendAutomation.utils.MockDataSeeder;
import org.testng.ITestContext;
import sipSe.testData.auth.TestDataAuth;

@Slf4j
public class SipSeAuthHelper {

    private final SipSeAuthMethods sipSeAuthMethods = new SipSeAuthMethods();

    @Getter private String accessToken;
    @Getter private String refreshToken;
    @Getter private boolean newUser;
    @Getter private String userId;

    public void generateToken(ITestContext context) {
        sipSeAuthMethods.resetOtpLimit("+91", TestDataAuth.TEST_PHONE_NUMBER_SIPSE);

        long otpInitiatedAt = System.currentTimeMillis();
        var initiateResponse =
                sipSeAuthMethods.initiateOTPHelper(
                        LoginInitiateRequest.buildLoginInitiatePayload(
                                "+91", TestDataAuth.TEST_PHONE_NUMBER_SIPSE));
        if (!initiateResponse.isSuccess()
                || initiateResponse.getData() == null
                || !initiateResponse.getData().isOtpSent()) {
            throw new RuntimeException(
                    "OTP initiation failed for phoneNumber="
                            + TestDataAuth.TEST_PHONE_NUMBER_SIPSE
                            + " — response: "
                            + initiateResponse.getErrorDetail());
        }
        log.info("OTP initiated for phoneNumber={}", TestDataAuth.TEST_PHONE_NUMBER_SIPSE);

        String encryptedOtp =
                sipSeAuthMethods.fetchOtpFromDb(
                        TestDataAuth.TEST_PHONE_NUMBER_SIPSE, otpInitiatedAt);
        if (encryptedOtp == null) {
            throw new RuntimeException(
                    "Failed to fetch OTP from DB for phoneNumber="
                            + TestDataAuth.TEST_PHONE_NUMBER_SIPSE);
        }

        DecryptOtpResponse decryptOtpResponse = sipSeAuthMethods.decryptOtp(encryptedOtp);
        if (!decryptOtpResponse.isSuccess() || decryptOtpResponse.getData() == null) {
            throw new RuntimeException("Failed to decrypt OTP");
        }
        String decryptedOtp = decryptOtpResponse.getData();
        log.info("OTP decrypted successfully");

        String phoneNumber = "+" + TestDataAuth.TEST_COUNTRY_CODE + TestDataAuth.TEST_PHONE_NUMBER_SIPSE;
        MockDataSeeder.insertMockPanLookupOnGrid(phoneNumber);
        MockDataSeeder.insertMockBankLookupFp(phoneNumber);
        MockDataSeeder.insertMockProfileLookupFp(phoneNumber);
        log.info("Mock responses seeded for phoneNumber={}", phoneNumber);

        LoginVerifyResponse verifyResponse =
                sipSeAuthMethods.verfiyOtpRequestHelper(
                        LoginVerifyRequest.buildLoginVerifyPayload(
                                "+91", TestDataAuth.TEST_PHONE_NUMBER_SIPSE, decryptedOtp));

        if (!verifyResponse.isSuccess() || verifyResponse.getData() == null) {
            throw new RuntimeException("OTP verification failed: " + verifyResponse.getMessage());
        }

        accessToken = verifyResponse.getData().getAccessToken();
        refreshToken = verifyResponse.getData().getRefreshToken();
        newUser = verifyResponse.getData().isNewUser();

        userId = sipSeAuthMethods.fetchUserIdFromDb(TestDataAuth.TEST_PHONE_NUMBER_SIPSE);

        context.getSuite().setAttribute("SIPSE_ACCESS_TOKEN", accessToken);
        context.getSuite().setAttribute("SIPSE_REFRESH_TOKEN", refreshToken);
        context.getSuite().setAttribute("SIPSE_NEW_USER", newUser);
        if (userId != null) {
            context.getSuite().setAttribute("SIPSE_USER_ID", userId);
        }

        log.info(
                "Auth token generated — accessToken={}, newUser={}, userId={}",
                accessToken,
                newUser,
                userId);
    }
}
