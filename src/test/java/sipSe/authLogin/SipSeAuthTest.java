package sipSe.authLogin;

import base.SispeBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.jarfinApiBackendAutomation.data.requestModel.sipseAuth.LoginInitiateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.sipseAuth.LoginVerifyRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse.DecryptOtpResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse.LoginInitiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse.LoginVerifyResponse;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import sipSe.testData.auth.TestDataAuth;

@Slf4j
public class SipSeAuthTest extends SispeBaseTest.BaseTest {

    SipSeAuthMethods sipSeAuthMethodsHelper = new SipSeAuthMethods();
    SipSeAuthValidation sipSeAuthValidation;
    private String decryptedOtp;
    private long otpInitiatedAt;

    @BeforeClass(alwaysRun = true)
    public void resetOtpLimit() {
        sipSeAuthMethodsHelper.resetOtpLimit("+91", TestDataAuth.TEST_PHONE_NUMBER_SIPSE);
    }

    @BeforeMethod
    public void init() {
        sipSeAuthValidation = new SipSeAuthValidation(new SoftAssert());
    }

    @Test(
            priority = 1,
            description = "Request OTP — covers invalid inputs and valid phone number",
            dataProvider = "requestOtpScenarios",
            dataProviderClass = SipSeAuthDataProvider.class)
    public void requestOtp(
            String scenario,
            LoginInitiateRequest request,
            SipSeAuthDataProvider.ExpectedError expectedError) {
        try {
            if (expectedError == null) {
                otpInitiatedAt = System.currentTimeMillis();
            }
            LoginInitiateResponse response = sipSeAuthMethodsHelper.initiateOTPHelper(request);
            if (expectedError != null) {
                sipSeAuthValidation.assertRequestOtpFailure(response, scenario);
            } else {
                sipSeAuthValidation.assertRequestOtp(response);
            }
        } catch (Exception e) {
            log.error("Exception during Request OTP [{}]: {}", scenario, e.getMessage());
            throw new RuntimeException("Request OTP [" + scenario + "] failed: " + e.getMessage(), e);
        }
    }

    @Test(
            priority = 2,
            description = "Fetch OTP from jarfin DB and decrypt it",
            dependsOnMethods = "requestOtp")
    public void fetchOtpFromDb() {
        try {
            String encryptedOtp =
                    sipSeAuthMethodsHelper.fetchOtpFromDb(
                            TestDataAuth.TEST_PHONE_NUMBER_SIPSE, otpInitiatedAt);
            DecryptOtpResponse decryptOtpResponse = sipSeAuthMethodsHelper.decryptOtp(encryptedOtp);
            decryptedOtp = decryptOtpResponse.getData();
            sipSeAuthValidation.assertFetchOtpFromDb(
                    encryptedOtp, TestDataAuth.TEST_PHONE_NUMBER_SIPSE);
            sipSeAuthValidation.assertDecryptOtp(decryptOtpResponse);
        } catch (Exception e) {
            log.error("Exception during Fetch/Decrypt OTP: {}", e.getMessage());
            throw new RuntimeException("fetchOtpFromDb failed: " + e.getMessage(), e);
        }
    }

    @Test(
            priority = 3,
            description = "Verify OTP — covers invalid inputs and valid decrypted OTP",
            dataProvider = "verifyOtpScenarios",
            dataProviderClass = SipSeAuthDataProvider.class,
            dependsOnMethods = "fetchOtpFromDb")
    public void verifyOtp(
            String scenario,
            LoginVerifyRequest request,
            boolean isHappyCase,
            ITestContext context) {
        try {
            LoginVerifyRequest reqToUse =
                    isHappyCase
                            ? LoginVerifyRequest.buildLoginVerifyPayload(
                                    request.getCountryCode(),
                                    request.getPhoneNumber(),
                                    decryptedOtp)
                            : request;
            LoginVerifyResponse verifyOtpResponse =
                    sipSeAuthMethodsHelper.verfiyOtpRequestHelper(reqToUse);
            if (isHappyCase) {
                if (verifyOtpResponse.isSuccess() && verifyOtpResponse.getData() != null) {
                    context.getSuite()
                            .setAttribute(
                                    "SIPSE_ACCESS_TOKEN",
                                    verifyOtpResponse.getData().getAccessToken());
                    context.getSuite()
                            .setAttribute(
                                    "SIPSE_REFRESH_TOKEN",
                                    verifyOtpResponse.getData().getRefreshToken());
                    context.getSuite()
                            .setAttribute(
                                    "SIPSE_NEW_USER", verifyOtpResponse.getData().isNewUser());
                    log.info(
                            "Stored SIPSE_ACCESS_TOKEN and SIPSE_REFRESH_TOKEN in ITestContext"
                                    + " suite");
                }
                sipSeAuthValidation.assertVerifyOtp(verifyOtpResponse);
            } else {
                sipSeAuthValidation.assertVerifyOtpFailure(verifyOtpResponse);
            }
        } catch (Exception e) {
            log.error("Exception during Verify OTP [{}]: {}", scenario, e.getMessage());
            throw new RuntimeException("Verify OTP [" + scenario + "] failed: " + e.getMessage(), e);
        }
    }
}
