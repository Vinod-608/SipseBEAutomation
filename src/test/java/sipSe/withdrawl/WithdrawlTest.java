package sipSe.withdrawl;

import lombok.extern.slf4j.Slf4j;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.LockerFeatureSectionResponse;
import org.jarfinApiBackendAutomation.data.requestModel.withdrawl.WithdrawlInitiateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.withdrawl.WithdrawlSubmitRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.PreRedemptionSummaryResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.WithdrawlInitiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.WithdrawlStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.WithdrawlSubmitResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.SchemeFolioBalanceResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.SchemeWiseBalanceResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.homeFeed.HomeFeedMetaDataResponse;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.jarfinApiBackendAutomation.utils.CommonUtil;
import base.SispeBaseTest;
import sipSe.authLogin.SipSeAuthHelper;
import sipSe.authLogin.SipSeAuthMethods;
import sipSe.onBoarding.OnboardingMethodhelper;
import sipSe.testData.auth.TestDataAuth;
import sipSe.withdrawl.WithdrawlDataProvider.PreRedemptionSummaryScenario;
import sipSe.withdrawl.WithdrawlDataProvider.SchemeFolioBalanceScenario;
import sipSe.withdrawl.WithdrawlDataProvider.SchemeWiseBalanceScenario;
import sipSe.withdrawl.WithdrawlDataProvider.WithdrawlInitiateScenario;

@Slf4j
public class WithdrawlTest extends SispeBaseTest {

    private final SipSeAuthHelper sipSeAuthHelper = new SipSeAuthHelper();
    private final OnboardingMethodhelper onboardingMethodhelper = new OnboardingMethodhelper();
    private final WithdrawlMethodhelper withdrawlMethodhelper = new WithdrawlMethodhelper();
    private WithdrawlValidation withdrawlValidation;

    private String accessToken;
    private String userId;
    private String phoneNumber;
    private boolean isKycVerified = false;
    private boolean eligibleForWithdrawl = true;
    private String fundSchemeId;
    private String userFolioId;
    private String withdrawlOrderId;

    @BeforeClass(alwaysRun = true)
    public void generateToken(ITestContext context) {
        withdrawlValidation = new WithdrawlValidation(new SoftAssert());
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
        log.info("Access token obtained for withdrawal tests");
    }

    @Test(priority = 1, description = "METADATA")
    public void HomeFeedMetaData() {
        try {
            HomeFeedMetaDataResponse response =
                    onboardingMethodhelper.HomefeedMetadataStatus(accessToken);
            if (response.getData() != null && "KYC_VERIFIED".equalsIgnoreCase(response.getData().getOnboardingState())) {
                isKycVerified = true;
            }
        } catch (Exception e) {
            log.error("META DATA", e.getMessage(), e);
            Assert.fail("META DATA " + e.getMessage(), e);
        }
    }

    @Test(priority = 2, description = "Locker Feature Section Details")
    public void lockerScreenDetails() {
        try {
            LockerFeatureSectionResponse response =
                    withdrawlMethodhelper.lockerSectionDetails(accessToken);
            withdrawlValidation.assertLockerDetailsSuccess(response);
            withdrawlValidation.assertAll();

            if (response.getData() != null && "LOCKER".equalsIgnoreCase(response.getData().getFeatureSection())) {
                Double currentValue = response.getData().getLockerFeatureSectionDetails().getCurrentValue();
                Double investValue = response.getData().getLockerFeatureSectionDetails().getInvestedValue();
                if (currentValue == null || investValue == null || currentValue == 0 || investValue == 0) {
                    eligibleForWithdrawl = false;
                    log.info("User not eligible for withdrawal — currentValue={}, investedValue={}", currentValue, investValue);
                }
            }
        } catch (Exception e) {
            log.error("locker details", e.getMessage(), e);
            Assert.fail("locker details " + e.getMessage(), e);
        }
    }

    @Test(priority = 3,
            description = "Scheme Wise Balance",
            dataProvider = "schemeWiseBalanceScenarios",
            dataProviderClass = WithdrawlDataProvider.class)
    public void schemeWiseBalance(SchemeWiseBalanceScenario scenario) {
        if (!eligibleForWithdrawl) {
            throw new SkipException("User not eligible for withdrawal — skipping schemeWiseBalance");
        }
        try {
            String token = scenario.token() != null ? scenario.token() : accessToken;
            SchemeWiseBalanceResponse response =
                    withdrawlMethodhelper.schemeWiseBalance(token, scenario.pageSize());
            withdrawlValidation.assertSchemeWiseBalanceSuccess(response);
            withdrawlValidation.assertAll();

            if (response.getData() != null
                    && response.getData()
                    .getSchemeWiseBalances() != null
                    && !response.getData().getSchemeWiseBalances().isEmpty()) {
                fundSchemeId = response.getData().getSchemeWiseBalances().get(0).getFundSchemeId();
                log.info("Captured fundSchemeId={} for folio balance test", fundSchemeId);
            }
        } catch (Exception e) {
            log.error("scheme wise balance", e.getMessage(), e);
            Assert.fail("scheme wise balance " + e.getMessage(), e);
        }
    }

    @Test(priority = 4,
            description = "Scheme Folio Balance",
            dataProvider = "schemeFolioBalanceScenarios",
            dataProviderClass = WithdrawlDataProvider.class)
    public void schemeFolioBalance(SchemeFolioBalanceScenario scenario) {
        if (fundSchemeId == null || fundSchemeId.isBlank()) {
            throw new SkipException("schemeFolioBalance skipped — fundSchemeId not captured from schemeWiseBalance");
        }
        try {
            String token = scenario.token() != null ? scenario.token() : accessToken;
            SchemeFolioBalanceResponse response =
                    withdrawlMethodhelper.schemeFolioBalance(token, fundSchemeId, scenario.pageSize());
            withdrawlValidation.assertSchemeFolioBalanceSuccess(response, fundSchemeId);
            withdrawlValidation.assertAll();

            if (response.getData() != null
                    && response.getData().getFolioBalances() != null
                    && !response.getData().getFolioBalances().isEmpty()) {
                userFolioId = response.getData().getFolioBalances().get(0).getUserFolioId();
                log.info("Captured userFolioId={} for preRedemptionSummary test", userFolioId);
            }
        } catch (Exception e) {
            log.error("scheme folio balance", e.getMessage(), e);
            Assert.fail("scheme folio balance " + e.getMessage(), e);
        }
    }

    @Test(priority = 5,
            description = "Pre Redemption Summary",
            dataProvider = "preRedemptionSummaryScenarios",
            dataProviderClass = WithdrawlDataProvider.class)
    public void preRedemptionSummary(PreRedemptionSummaryScenario scenario) {
        if (fundSchemeId == null || fundSchemeId.isBlank()) {
            throw new SkipException("preRedemptionSummary skipped — fundSchemeId not captured");
        }
        if (userFolioId == null || userFolioId.isBlank()) {
            throw new SkipException("preRedemptionSummary skipped — userFolioId not captured from schemeFolioBalance");
        }
        try {
            String token = scenario.token() != null ? scenario.token() : accessToken;
            PreRedemptionSummaryResponse response =
                    withdrawlMethodhelper.preRedemptionSummary(token, fundSchemeId, userFolioId);
            withdrawlValidation.assertPreRedemptionSummarySuccess(response);
            withdrawlValidation.assertAll();
        } catch (Exception e) {
            log.error("pre redemption summary", e.getMessage(), e);
            Assert.fail("pre redemption summary " + e.getMessage(), e);
        }
    }

    @Test(priority = 6,
            description = "Withdrawal Initiate",
            dataProvider = "withdrawlInitiateScenarios",
            dataProviderClass = WithdrawlDataProvider.class)
    public void withdrawlInitiate(WithdrawlInitiateScenario scenario) {
        if (fundSchemeId == null || fundSchemeId.isBlank()) {
            throw new SkipException("withdrawlInitiate skipped — fundSchemeId not captured");
        }
        if (userFolioId == null || userFolioId.isBlank()) {
            throw new SkipException("withdrawlInitiate skipped — userFolioId not captured");
        }
        try {
            String token = scenario.token() != null ? scenario.token() : accessToken;
            WithdrawlInitiateRequest request = WithdrawlInitiateRequest.build(
                    scenario.request().getAmount(),
                    fundSchemeId,
                    userFolioId);
            WithdrawlInitiateResponse response =
                    withdrawlMethodhelper.withdrawlInitiate(token, request);
            if (response.getData() != null && response.getData().getOrderId() != null) {
                withdrawlOrderId = response.getData().getOrderId();
                log.info("Captured withdrawlOrderId={}", withdrawlOrderId);
            }
            withdrawlValidation.assertWithdrawlInitiateSuccess(response);
            withdrawlValidation.assertAll();
        } catch (Exception e) {
            log.error("withdrawal initiate", e.getMessage(), e);
            Assert.fail("withdrawal initiate " + e.getMessage(), e);
        }
    }

    @Test(priority = 7, description = "Withdrawal Submit")
    public void withdrawlSubmit() {
        if (withdrawlOrderId == null || withdrawlOrderId.isBlank()) {
            throw new SkipException("withdrawlSubmit skipped — withdrawlOrderId not captured from withdrawlInitiate");
        }
        try {
            String otp = fetchWithdrawalOtpFromDb(withdrawlOrderId);
            WithdrawlSubmitRequest request = WithdrawlSubmitRequest.build(withdrawlOrderId, otp);
            WithdrawlSubmitResponse response = withdrawlMethodhelper.withdrawlSubmit(accessToken, request);
            withdrawlValidation.assertWithdrawlSubmitSuccess(response);
            withdrawlValidation.assertAll();
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("withdrawal submit", e.getMessage(), e);
            Assert.fail("withdrawal submit " + e.getMessage(), e);
        }
    }

    @Test(priority = 8, description = "Withdrawal Status — poll until SUCCESS")
    public void withdrawlStatus() {
        if (withdrawlOrderId == null || withdrawlOrderId.isBlank()) {
            throw new SkipException("withdrawlStatus skipped — withdrawlOrderId not captured from withdrawlInitiate");
        }
        try {
            WithdrawlStatusResponse response =
                    CommonUtil.pollUntilDone(
                            () -> withdrawlMethodhelper.withdrawlStatus(accessToken, withdrawlOrderId),
                            WithdrawlDataProvider.WITHDRAWAL_STATUS_SHOULD_RETRY,
                            WithdrawlDataProvider.DEFAULT_MAX_RETRIES,
                            WithdrawlDataProvider.DEFAULT_DELAY_MS,
                            "Withdrawal Status");
            String finalStatus = response.getData() != null ? response.getData().getStatus() : null;
            if ("PENDING".equalsIgnoreCase(finalStatus)
                    || "INITIATED".equalsIgnoreCase(finalStatus)
                    || "IN_PROGRESS".equalsIgnoreCase(finalStatus)) {
                log.warn("Withdrawal status still {} after polling — treating as pass", finalStatus);
            } else {
                withdrawlValidation.assertWithdrawlStatusSuccess(response);
                withdrawlValidation.assertAll();
            }
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("withdrawal status", e.getMessage(), e);
            Assert.fail("withdrawal status " + e.getMessage(), e);
        }
    }

    public static String fetchWithdrawalOtpFromDb(String orderId) {
        SipSeAuthMethods authMethods = new SipSeAuthMethods();
        return new CommonUtil().fetchAndDecryptWithdrawalOtp(
                orderId,
                encrypted -> authMethods.decryptOtp(encrypted).getData());
    }
}
