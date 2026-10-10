package sipSe.lumSumPayment;

import base.SispeBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.jarfinApiBackendAutomation.data.requestModel.lumSum.LumpSumSubmitRequest;
import org.jarfinApiBackendAutomation.data.requestModel.lumSum.LumpSumVerifyRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.homeFeed.HomeFeedMetaDataResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumIntiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumSubmitResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumVerifyResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.UpiApp;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.BillDeskMandateMockResponse;
import org.jarfinApiBackendAutomation.utils.CommonUtil;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import sipSe.authLogin.SipSeAuthHelper;
import sipSe.authLogin.SipSeAuthMethods;
import sipSe.lumSumPayment.LumpSumPaymentDataProvider.LumpSumInitiateScenario;
import sipSe.onBoarding.OnboardingMethodhelper;
import sipSe.testData.auth.TestDataAuth;

@Slf4j
public class LumpSumPaymentTest extends SispeBaseTest {

    private final SipSeAuthHelper sipSeAuthHelper = new SipSeAuthHelper();
    private final OnboardingMethodhelper onboardingMethodhelper = new OnboardingMethodhelper();
    private final LumpSumMethodHelper lumpSumMethodHelper = new LumpSumMethodHelper();
    private  LumpSumValidation lumpSumValidation;

    private String accessToken;
    private String userId;
    private String phoneNumber;
    private String orderId;
    private boolean isKycVerified=false;
    private  String redirectUrl;

    @BeforeClass(alwaysRun = true)
    public void generateToken(ITestContext context) {
        String existingToken = (String) context.getSuite().getAttribute("SIPSE_ACCESS_TOKEN");
        if (existingToken != null && !existingToken.isBlank()) {
            accessToken = existingToken;
            userId = (String) context.getSuite().getAttribute("SIPSE_USER_ID");
            phoneNumber = "+" + TestDataAuth.TEST_COUNTRY_CODE + TestDataAuth.TEST_PHONE_NUMBER_SIPSE;
            log.info("Reusing existing access token from suite context");
            return;
        }
        sipSeAuthHelper.generateToken(context);
        accessToken = sipSeAuthHelper.getAccessToken();
        userId = sipSeAuthHelper.getUserId();
        phoneNumber = "+" + TestDataAuth.TEST_COUNTRY_CODE + TestDataAuth.TEST_PHONE_NUMBER_SIPSE;
        log.info("Access token obtained for onboarding tests");
    }


    @BeforeMethod
    public void init() {
         lumpSumValidation = new LumpSumValidation(new SoftAssert());
    }

        @Test(priority = 1, description = "METADATA")
        public void HomeFeedMetaData() {

            try {
                HomeFeedMetaDataResponse response =
                        onboardingMethodhelper.HomefeedMetadataStatus(accessToken);

                if (response.getData() != null   && "KYC_VERIFIED".equalsIgnoreCase(response.getData().getOnboardingState())) {
                    isKycVerified=true;
                }
            } catch (Exception e) {
                log.error("META DATA ", e.getMessage());
                Assert.fail("META DATA " + e.getMessage(), e);


        }
    }


    @Test(priority = 02, description = "LumSum Intiate ",
            dataProvider = "lumpSumPaymentInitiateScenarios",
    dataProviderClass = LumpSumPaymentDataProvider.class)
    public void LumSumIntiate(LumpSumInitiateScenario scenario) {
        if (isKycVerified) {
            try {
                String token = scenario.token() != null ? scenario.token() : accessToken;
                LumpSumIntiateResponse response =
                        lumpSumMethodHelper.lumpSumInitiate(token, scenario.request());
                if (response.getData() != null) {
                    orderId = response.getData().getOrderId();
                }
                lumpSumValidation.assertLumpSumInitiateSuccess(response);

            } catch (Exception e) {
                log.error("Exception during KYC Profile Submit: {}", e.getMessage());
                Assert.fail("KYC Profile Submit failed: " + e.getMessage(), e);
            }
            finally {
                lumpSumValidation.assertAll();
            }
        }
        else {
            throw new SkipException("Kyc is not verified lumpsum can not  be initiated");
        }
    }

    @Test(priority = 3, description = "LumSum Verify — submit OTP to complete lumpsum purchase")
    public void lumpSumVerify() {
        if (orderId == null || orderId.isBlank()) {
            throw new SkipException("orderId not available — skipping LumpSum Verify");
        }
        try {
            String otp = fetchLumpSumOtpFromDb(orderId);
            LumpSumVerifyRequest request = LumpSumVerifyRequest.build(orderId, otp);
            LumpSumVerifyResponse response = lumpSumMethodHelper.lumpSumVerify(accessToken, request);
            lumpSumValidation.assertLumpSumVerifySuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during LumpSum Verify: {}", e.getMessage());
            Assert.fail("LumpSum Verify failed: " + e.getMessage(), e);
        } finally {
            lumpSumValidation.assertAll();
        }
    }

    @Test(priority = 4, description = "LumSum Submit — initiate UPI payment for lumpsum order")
    public void lumpSumSubmit() {
        if (orderId == null || orderId.isBlank()) {
            throw new SkipException("orderId not available — skipping LumpSum Submit");
        }
        try {
            LumpSumSubmitRequest request = LumpSumSubmitRequest.build(orderId, UpiApp.PHONEPE);
            LumpSumSubmitResponse response = lumpSumMethodHelper.lumpSumSubmit(accessToken, request);
            if (response.getData() != null) {
                redirectUrl = response.getData().getPaymentIntentUrl();
            }
            lumpSumValidation.assertLumpSumSubmitSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during LumpSum Submit: {}", e.getMessage());
            Assert.fail("LumpSum Submit failed: " + e.getMessage(), e);
        } finally {
            lumpSumValidation.assertAll();
        }
    }


    @Test(priority = 5, description = "BillDesk Netbanking Mock Payment — trigger successful lumpsum payment")
    public void billdeskMockPayment(

    ) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            throw new SkipException("redirectUrl not available — skipping BillDesk Mock Payment");
        }
        try {
            BillDeskMandateMockResponse response = lumpSumMethodHelper.lumpSumMockPayment(redirectUrl);
            log.info("BillDesk Netbanking Mock Payment — simulator={}, callbackResponse={}, bankresponse={}, finprimBd={}, ondc={}",
                    response.getCallbackSimulatorStatus(),
                    response.getCallbackResponseStatus(),
                    response.getProcessNpciRespStatus(),
                    response.getFinprimBillDeskCallbackStatus(),
                    response.getFinprimOndcCallbackStatus());
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during BillDesk Netbanking Mock Payment: {}", e.getMessage(), e);
            Assert.fail("BillDesk Netbanking Mock Payment failed: " + e.getMessage(), e);
        }
    }

    @Test(priority = 6, description = "LumSum Status — poll lumpsum order status")
    public void lumpSumStatus() {
        if (orderId == null || orderId.isBlank()) {
            throw new SkipException("orderId not available — skipping LumpSum Status");
        }
        try {
            LumpSumStatusResponse response =
                    CommonUtil.pollUntilDone(
                            () -> lumpSumMethodHelper.lumpSumStatus(accessToken, orderId),
                            LumpSumPaymentDataProvider.LumpSum_STATUS_SHOULD_RETRY,
                            LumpSumPaymentDataProvider.DEFAULT_MAX_RETRIES,
                            LumpSumPaymentDataProvider.DEFAULT_DELAY_MS,
                            "payment Pre-Verification Status");
            lumpSumValidation.assertLumpSumStatusSuccess(response);


        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during LumpSum Status: {}", e.getMessage());
            Assert.fail("LumpSum Status failed: " + e.getMessage(), e);
        } finally {
            lumpSumValidation.assertAll();
        }
    }

    public static String fetchLumpSumOtpFromDb(String orderId) {
        SipSeAuthMethods authMethods = new SipSeAuthMethods();
        return new CommonUtil().fetchAndDecryptLumpSumOtp(
                orderId,
                encrypted -> authMethods.decryptOtp(encrypted).getData());
    }

}
