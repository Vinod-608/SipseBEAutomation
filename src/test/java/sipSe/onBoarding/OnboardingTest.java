package sipSe.onBoarding;

import base.SispeBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.jarfinApiBackendAutomation.data.requestModel.onBoarding.EmailOnboardingRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.homeFeed.HomeFeedMetaDataResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.*;
import org.jarfinApiBackendAutomation.data.requestModel.kyc.SubmitProfileRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.EmailOnboardingResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.OnboardingVideoResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.PanLookupScenario;
import org.jarfinApiBackendAutomation.utils.CommonUtil;
import org.jarfinApiBackendAutomation.utils.MockDataSeeder;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import sipSe.authLogin.SipSeAuthHelper;
import sipSe.testData.auth.TestDataAuth;
import sipSe.onBoarding.OnBoardingdataProvider.BankVerificationInitiateScenario;
import sipSe.onBoarding.OnBoardingdataProvider.MandateSetupScenario;
import sipSe.onBoarding.OnBoardingdataProvider.PanConfirmData;
import sipSe.onBoarding.OnBoardingdataProvider.PanConfirmScenario;
import org.jarfinApiBackendAutomation.data.requestModel.mandate.SetupMandateRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.UpiApp;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.BillDeskMandateMockResponse;
import org.jarfinApiBackendAutomation.data.requestModel.mandate.VerifyMandateRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.SetupMandateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.VerifyMandateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.sip.SubmitSipResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.consent.SubmitUserConsentResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.nominee.AddNomineeResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.sip.SipStatusResponse;
import sipSe.onBoarding.OnBoardingdataProvider.ConsentScenario;
import sipSe.onBoarding.OnBoardingdataProvider.NomineeScenario;

@Slf4j
public class OnboardingTest extends SispeBaseTest.BaseTest {

    private final OnboardingMethodhelper onboardingMethodhelper = new OnboardingMethodhelper();
    private final SipSeAuthHelper sipSeAuthHelper = new SipSeAuthHelper();
    private OnboardingValidation onboardingValidation;
    private String accessToken;
    private String userId;
    private String phoneNumber;
    private PrefillKycDetailsResponse.PrefillData prefillData;
    private String mandateId;
    private String purchasePlanId;
    private String mandateRedirectUrl;
    private static final String MANDATE_SCHEME_ID = "6a270c20bd140f0050a265cd";

    // Set by panLookup happy-path, consumed by panConfirm* tests
    private PanConfirmData panConfirmData;

    // Set by callPPreVerificationStatus(), available to all tests that follow
    private PanPreVerificationStatusResponse.PreVerificationResponse preVerificationResponse;
    private boolean isPanVerified = false;
    private  boolean isbankVerified = false;
    private boolean isProfileVerified= false;
    private boolean isKycVerified= false;

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
        onboardingValidation = new OnboardingValidation(new SoftAssert());
    }

    @Test(
            priority = 1,
            description = "Submit Email — covers valid and invalid email inputs",
            dataProvider = "submitEmailScenarios",
            dataProviderClass = OnBoardingdataProvider.class)
    public void submitEmail(String scenario, EmailOnboardingRequest request, boolean isHappyCase) {
        if ( isKycVerified) {
            throw new SkipException("KYC is already verified skipping email");
        }
        try {
            EmailOnboardingResponse response =
                    onboardingMethodhelper.submitEmail(request, accessToken);
            if (isHappyCase) {
                onboardingValidation.assertSubmitEmailSuccess(response);
            } else {
                onboardingValidation.assertSubmitEmailFailure(response, scenario);
            }
        } catch (Exception e) {
            log.error("Exception during Submit Email [{}]: {}", scenario, e.getMessage());
            onboardingValidation.assertFail("Submit Email [" + scenario + "] failed: " + e.getMessage() );
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 2, description = "Fetch onboarding video asset")
    public void onboardingVideoAsset() {
        try {
            OnboardingVideoResponse response =
                    onboardingMethodhelper.onboardingVideoAsset(accessToken);
            onboardingValidation.assertOnboardingVideoAsset(response);
        } catch (Exception e) {
            log.error("Exception during Onboarding Video Asset: {}", e.getMessage());
            onboardingValidation.assertFail ("Onboarding Video Asset failed: " + e.getMessage());
        }
        finally {
            onboardingValidation.assertAll();
        }
    }



    @Test(priority = 2, description = "KYC preverification api to check the status")
    public void preVerificationStatusPan() {
        if ( isKycVerified) {
            throw new SkipException("KYC is already verified skipping prefill");
        }
        callPPreVerificationStatus();
    }

    private void callPPreVerificationStatus() {
        try {
            PanPreVerificationStatusResponse response =
                    CommonUtil.pollUntilDone(
                            () -> onboardingMethodhelper.panPreverificationStatus(accessToken),
                            OnBoardingdataProvider.PAN_STATUS_SHOULD_RETRY,
                            OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                            OnBoardingdataProvider.DEFAULT_DELAY_MS,
                            "PAN Pre-Verification Status");

            if (response.getData() != null && response.getData().getPreVerificationResponse() != null) {
                preVerificationResponse = response.getData().getPreVerificationResponse();
                log.info("PAN Pre-Verification — id={}, status={}, pan={}, name={}, dob={}",
                        preVerificationResponse.getId(),
                        preVerificationResponse.getStatus(),
                        preVerificationResponse.getPan() != null ? preVerificationResponse.getPan().getValue() : null,
                        preVerificationResponse.getName() != null ? preVerificationResponse.getName().getValue() : null,
                        preVerificationResponse.getDateOfBirth() != null ? preVerificationResponse.getDateOfBirth().getValue() : null);

                if (preVerificationResponse.getPan() != null
                        && "verified".equalsIgnoreCase(preVerificationResponse.getPan().getStatus())) {
                    isPanVerified = true;
                    log.info("PAN already verified — panLookup, panConfirm and panPreVerificationStatus will be skipped");
                }

                if (preVerificationResponse.getDateOfBirth() != null
                        && "verified".equalsIgnoreCase(preVerificationResponse.getDateOfBirth().getStatus())) {
                    isProfileVerified = true;
                    log.info("Profile verified ");
                }
            }
            onboardingValidation.assertPanStatusSuccess(response);


        } catch (Exception e) {
            log.error("Exception during PAN Pre-Verification Status: {}", e.getMessage());
            Assert.fail("PAN Pre-Verification Status failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(
            priority = 4,
            description = "PAN Lookup — valid and invalid token, status-driven confirm data",
            dataProvider = "panLookupScenarios",
            dataProviderClass = OnBoardingdataProvider.class)
    public void panLookup(PanLookupScenario scenario) {
        if (isPanVerified || isKycVerified) {
            throw new SkipException("PAN already verified — skipping panLookup");
        }
        try {
            if (scenario.isValidToken()) {
                PanLookupResponse response =
                        CommonUtil.pollUntilDone(
                                () -> onboardingMethodhelper.panLookup(accessToken),
                                OnBoardingdataProvider.PAN_LOOKUP_SHOULD_RETRY,
                                OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                                OnBoardingdataProvider.DEFAULT_DELAY_MS,
                                "PAN Lookup");
                panConfirmData =
                        OnBoardingdataProvider.resolvePanLookupStatus(
                                response, onboardingValidation);
            } else {
                PanLookupResponse response = onboardingMethodhelper.panLookup("invalid_token");
                onboardingValidation.assertPanLookupUnauthorized(response);
            }
        } catch (Exception e) {
            log.error(
                    "Exception during PAN Lookup [{}]: {}",
                    scenario.getDescription(),
                    e.getMessage());
            Assert.fail("PAN Lookup [" + scenario.getDescription() + "] failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(
            priority = 5,
            description = "PAN Confirm — valid, invalid token, and invalid PAN scenarios",
            dataProvider = "panConfirmScenarios",
            dataProviderClass = OnBoardingdataProvider.class)
    public void panConfirm(PanConfirmScenario scenario) {
        if (isPanVerified || isKycVerified) {
            throw new SkipException("PAN already verified — skipping panConfirm");
        }
        if (panConfirmData == null || panConfirmData.skipConfirm()) {
            throw new SkipException("KYC_VERIFIED — PAN confirm flow skipped");
        }
        try {
            String token =
                    scenario.tokenOverride() != null ? scenario.tokenOverride() : accessToken;
            String pan =
                    scenario.panOverride() != null ? scenario.panOverride() : panConfirmData.pan();

            PanConfirmResponse response =
                    onboardingMethodhelper.panConfirm(
                            token, pan, panConfirmData.name(), panConfirmData.dob());

            switch (scenario.assertion()) {
                case SUCCESS -> onboardingValidation.assertPanConfirmSuccess(response);
                case UNAUTHORIZED -> onboardingValidation.assertPanConfirmUnauthorized(response);
                case BAD_REQUEST -> onboardingValidation.assertPanConfirmBadRequest(response);
            }
        } catch (Exception e) {
            log.error(
                    "Exception during PAN Confirm [{}]: {}",
                    scenario.description(),
                    e.getMessage());
            Assert.fail("PAN Confirm [" + scenario.description() + "] failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 6,
            description = "PAN Status check to check the status from  cybrila",
            dataProvider = "panStatusScenarios",
            dataProviderClass = OnBoardingdataProvider.class)
    public void panPreVerificationStatus(PanLookupScenario scenario) {
        if (isKycVerified ||isPanVerified && scenario.isValidToken()) {
            throw new SkipException("PAN already verified — skipping panPreVerificationStatus");
        }
        try {
            if (scenario.isValidToken()) {
                PanPreVerificationStatusResponse response =
                        CommonUtil.pollUntilDone(
                                () -> onboardingMethodhelper.panPreverificationStatus(accessToken),
                                OnBoardingdataProvider.PAN_STATUS_SHOULD_RETRY,
                                OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                                OnBoardingdataProvider.DEFAULT_DELAY_MS,
                                "PAN Status");
                if (response.getData() != null && response.getData().getPreVerificationResponse() != null) {
                    PanPreVerificationStatusResponse.PreVerificationResponse pvr =
                            response.getData().getPreVerificationResponse();
                    if (pvr.getPan() != null && "verified".equalsIgnoreCase(pvr.getPan().getStatus())) {
                        isPanVerified = true;
                        log.info("PAN already verified (detected in panPreVerificationStatus) — panLookup and panConfirm will be skipped");
                    }
                    if (pvr.getBankAccounts() != null && !pvr.getBankAccounts().isEmpty()
                            && "verified".equalsIgnoreCase(pvr.getBankAccounts().getFirst().getStatus())) {
                        isbankVerified = true;
                        log.info("Bank already verified (detected in panPreVerificationStatus) — bankVerificationLookUp and bankVerificationInitiate will be skipped");
                    }
                }
                onboardingValidation.assertPanStatusSuccess(response);
            } else {
                PanPreVerificationStatusResponse response =
                        onboardingMethodhelper.panPreverificationStatus("invalid_token");
                onboardingValidation.assertPanStatusUnauthorized(response);
            }
        } catch (Exception e) {
            log.error(
                    "Exception during PAN Status [{}]: {}",
                    scenario.getDescription(),
                    e.getMessage());
            Assert.fail("PAN Status [" + scenario.getDescription() + "] failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }



    @Test(priority = 7, description = "KYC pre-verification status check before bank tests")
    public void BankVerificationStatusPan() {
        if ( isKycVerified) {
            throw new SkipException("KYC is already verified skipping prefill");
        }
        callKycPreVerificationStatus();
    }

    private void callKycPreVerificationStatus() {
        try {
            PanPreVerificationStatusResponse response =
                    CommonUtil.pollUntilDone(
                            () -> onboardingMethodhelper.kycPreVerificationBankStatus(accessToken),
                            OnBoardingdataProvider.PAN_STATUS_SHOULD_RETRY,
                            OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                            OnBoardingdataProvider.DEFAULT_DELAY_MS,
                            "KYC Pre-Verification Status");

            if (response.getData() != null && response.getData().getPreVerificationResponse() != null) {
                PanPreVerificationStatusResponse.PreVerificationResponse pvr =
                        response.getData().getPreVerificationResponse();
                log.info("KYC Pre-Verification (bank check) — status={}, bank_accounts={}",
                        pvr.getStatus(), pvr.getBankAccounts());

                if (pvr.getBankAccounts() != null && !pvr.getBankAccounts().isEmpty()
                        && "verified".equalsIgnoreCase(pvr.getBankAccounts().getFirst().getStatus())) {
                    isbankVerified = true;
                    log.info("Bank already verified — bankVerificationLookUp and bankVerificationInitiate will be skipped");
                }
            }
            onboardingValidation.assertPanStatusSuccess(response);
        } catch (Exception e) {
            log.error("Exception during KYC Pre-Verification Status (bank check): {}", e.getMessage());
            throw new RuntimeException("KYC Pre-Verification Status (bank check) failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(
            priority = 8,
            description = "Bank Verification Lookup — valid and invalid token",
            dataProvider = "bankVerificationScenarios",
            dataProviderClass = OnBoardingdataProvider.class)
    public void bankVerificationLookUp(PanLookupScenario scenario) {
        if (isKycVerified || isbankVerified && scenario.isValidToken()) {
            throw new SkipException("bank already verified — skipping BankVerificationStatus");
        }
        try {
            if (scenario.isValidToken()) {
                BankVerificationResponse response =
                        CommonUtil.pollUntilDone(
                                () -> onboardingMethodhelper.bankVerificationLookup(accessToken),
                                OnBoardingdataProvider.BANK_VERIFICATION_SHOULD_RETRY,
                                OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                                OnBoardingdataProvider.DEFAULT_DELAY_MS,
                                "Bank Verification Lookup");
                onboardingValidation.assertBankVerificationSuccess(response);
            } else {
                BankVerificationResponse response =
                        onboardingMethodhelper.bankVerificationLookup("invalid_token");
                onboardingValidation.assertBankVerificationUnauthorized(response);
            }
        } catch (Exception e) {
            log.error(
                    "Exception during Bank Verification Lookup [{}]: {}",
                    scenario.getDescription(),
                    e.getMessage());
            Assert.fail("Bank Verification Lookup [" + scenario.getDescription() + "] failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(
            priority = 9,
            description = "Bank Verification Initiate — valid and invalid token",
            dataProvider = "bankVerificationInitiateScenarios",
            dataProviderClass = OnBoardingdataProvider.class)
    public void bankVerificationInitiate(BankVerificationInitiateScenario scenario) {
        if (isKycVerified || isbankVerified) {
            throw new SkipException("bank already verified — skipping bankVerificationInitiate");
        }
        try {
            String token =
                    scenario.tokenOverride() != null ? scenario.tokenOverride() : accessToken;
            PanPreVerificationStatusResponse response =
                    onboardingMethodhelper.bankPreVerificationInitiate(token,"DEFAULT_PRE_VERIFICATION");
            if (scenario.tokenOverride() != null) {
                onboardingValidation.assertBankVerificationInitiateUnauthorized(response);
            } else {
                if (response.getData() != null) {

                }
                onboardingValidation.assertBankVerificationInitiateSuccess(response);
            }
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "Exception during Bank Verification Initiate [{}]: {}",
                    scenario.description(),
                    e.getMessage());
            Assert.fail("Bank Verification Initiate [" + scenario.description() + "] failed: " + e.getMessage(), e);
        }
    }



    @Test(
            priority = 11,
            description = "Bank Pre-Verification Status — poll until terminal action",
            dataProvider = "bankPreVerificationStatusScenarios",
            dataProviderClass = OnBoardingdataProvider.class)
    public void bankPreVerificationStatus(PanLookupScenario scenario) {
        if (isKycVerified || isbankVerified) {

            throw new SkipException("bank already verified — skipping bankVerificationInitiate");
        }
        try {

            if (scenario.isValidToken()) {
                PanPreVerificationStatusResponse response =
                        CommonUtil.pollUntilDone(
                                () -> onboardingMethodhelper.bankPreVerificationStatus(accessToken),
                                OnBoardingdataProvider.BANK_PRE_VERIFICATION_SHOULD_RETRY,
                                OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                                OnBoardingdataProvider.DEFAULT_DELAY_MS,
                                "Bank Pre-Verification Status");
                onboardingValidation.assertBankPreVerificationStatusSuccess(response);
            } else {
                PanPreVerificationStatusResponse response =
                        onboardingMethodhelper.bankPreVerificationStatus("invalid_token");
                onboardingValidation.assertBankPreVerificationStatusUnauthorized(response);
            }
        } catch (Exception e) {
            log.error(
                    "Exception during Bank Pre-Verification Status [{}]: {}",
                    scenario.getDescription(),
                    e.getMessage());
            Assert.fail("Bank Pre-Verification Status [" + scenario.getDescription() + "] failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }

}


    @Test(priority = 12, description = " Preverification  status api before calling prefill api verification")
    public void KycPreVerificationStatus() {
        if ( isKycVerified) {
            throw new SkipException("KYC is already verified skipping email");
        }
        callPPreVerificationStatus();

    }


    @Test(
            priority = 13,
            description = "KYC Prefill Details — valid and invalid token",
            dataProvider = "kycPrefillDetailsScenarios",
            dataProviderClass = OnBoardingdataProvider.class)
    public void kycPrefillDetails(BankVerificationInitiateScenario scenario) {
        if ( isKycVerified) {
            throw new SkipException("KYC is already verified skipping prefill");
        }
        try {
            String token =
                    scenario.tokenOverride() != null ? scenario.tokenOverride() : accessToken;
            PrefillKycDetailsResponse response =
                    onboardingMethodhelper.kycPrefillDetails(token);
            if (scenario.tokenOverride() != null) {
                onboardingValidation.assertKycPrefillDetailsUnauthorized(response);
            } else {
                if (response.getData() != null) {
                    prefillData = response.getData();
                }
                onboardingValidation.assertKycPrefillDetailsSuccess(response);
            }
        } catch (Exception e) {
            log.error(
                    "Exception during KYC Prefill Details [{}]: {}",
                    scenario.description(),
                    e.getMessage());
            Assert.fail("KYC Prefill Details [" + scenario.description() + "] failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 14, description = "KYC Profile Submit — using prefill data or defaults")
    public void kycProfileSubmit() {
        if ( isKycVerified) {
            throw new SkipException("Kyc is already verified skipping submit");
        }
        try {
            SubmitProfileRequest request = SubmitProfileRequest.build(prefillData);
            SubmitProfileResponse response =
                    onboardingMethodhelper.kycProfileSubmit(accessToken, request);
            onboardingValidation.assertKycProfileSubmitSuccess(response);
        } catch (Exception e) {
            log.error("Exception during KYC Profile Submit: {}", e.getMessage());
            Assert.fail("KYC Profile Submit failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 15, description = "KYC Pre-Verification Status — validate overall KYC state after profile submit")
    public void kycPreVerificationStatus() {
        if ( isKycVerified) {
            throw new SkipException("Skipping the pre-verification status");
        }
        try {
            PanPreVerificationStatusResponse response =
                    CommonUtil.pollUntilDone(
                            () -> onboardingMethodhelper.kycPreVerificationStatus(accessToken),
                            OnBoardingdataProvider.PAN_STATUS_SHOULD_RETRY,
                            OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                            OnBoardingdataProvider.DEFAULT_DELAY_MS,
                            "KYC Pre-Verification Status");
            if (response.getData() != null && response.getData().getPreVerificationResponse() != null) {
                PanPreVerificationStatusResponse.PreVerificationResponse pvr =
                        response.getData().getPreVerificationResponse();
                log.info("KYC Pre-Verification — context={}, action={}, pan={}, bank_accounts={}",
                        response.getData().getCurrentContext(),
                        response.getData().getAction(),
                        pvr.getPan() != null ? pvr.getPan().getValue() : null,
                        pvr.getBankAccounts());
            }
            onboardingValidation.assertPanStatusSuccess(response);
        } catch (Exception e) {
            log.error("Exception during KYC Pre-Verification Status: {}", e.getMessage());
            throw new RuntimeException("KYC Pre-Verification Status failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(
            priority = 16,
            description = "Submit Consent — user consents after KYC profile submit",
            dataProvider = "submitConsentScenarios",
            dataProviderClass = OnBoardingdataProvider.class)
    public void submitConsent(ConsentScenario scenario) {
        try {
            String token = scenario.tokenOverride() != null ? scenario.tokenOverride() : accessToken;
            SubmitUserConsentResponse response =
                    onboardingMethodhelper.submitConsent(token, scenario.request());
            switch (scenario.assertion()) {
                case SUCCESS -> onboardingValidation.assertSubmitConsentSuccess(response);
                case UNAUTHORIZED -> onboardingValidation.assertSubmitConsentUnauthorized(response);
                case BAD_REQUEST -> onboardingValidation.assertSubmitConsentBadRequest(response);
            }
        } catch (Exception e) {
            log.error(
                    "Exception during Submit Consent [{}]: {}",
                    scenario.description(),
                    e.getMessage());
            Assert.fail("Submit Consent [" + scenario.description() + "] failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(
            priority = 17,
            description = "Mandate Setup — set up UPI mandate for daily SIP",
            dataProvider = "mandateSetupScenarios",
            dataProviderClass = OnBoardingdataProvider.class)
    public void mandateSetup(MandateSetupScenario scenario) {
        try {
            String token = scenario.tokenOverride() != null ? scenario.tokenOverride() : accessToken;
            SetupMandateRequest request = SetupMandateRequest.build(100, MANDATE_SCHEME_ID, UpiApp.PHONEPE);
            SetupMandateResponse response = onboardingMethodhelper.setupMandate(token, request);
            if (scenario.tokenOverride() != null) {
                onboardingValidation.assertSetupMandateUnauthorized(response);
            } else {
                if (response.getData() != null) {
                    mandateId = response.getData().getMandateId();
                    purchasePlanId = response.getData().getPurchasePlanId();
                    mandateRedirectUrl = response.getData().getRedirectUrl();
                }
                onboardingValidation.assertSetupMandateSuccess(response);
            }
        } catch (Exception e) {
            log.error("Exception during Mandate Setup [{}]: {}", scenario.description(), e.getMessage());
            Assert.fail("Mandate Setup [" + scenario.description() + "] failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }



    @Test(
            priority = 18,
            description = "Add Nominee — add nominee after KYC submit and mandate setup",
            dataProvider = "addNomineeScenarios",
            dataProviderClass = OnBoardingdataProvider.class)
    public void addNominee(NomineeScenario scenario) {
        try {
            String token = scenario.tokenOverride() != null ? scenario.tokenOverride() : accessToken;
            AddNomineeResponse response = onboardingMethodhelper.addNominee(token, scenario.request());
            switch (scenario.assertion()) {
                case SUCCESS -> onboardingValidation.assertAddNomineeSuccess(response);
                case UNAUTHORIZED -> onboardingValidation.assertAddNomineeUnauthorized(response);
                case BAD_REQUEST -> onboardingValidation.assertAddNomineeBadRequest(response);
            }
        } catch (Exception e) {
            log.error("Exception during Add Nominee [{}]: {}", scenario.description(), e.getMessage());
            Assert.fail("Add Nominee [" + scenario.description() + "] failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 19, description = "BillDesk eNACH Callback Simulator — trigger successful mandate approval")
    public void billdeskMandateMockPayment() {
        try {
            BillDeskMandateMockResponse response =
                    MockDataSeeder.billdeskMandateMockPayment(mandateRedirectUrl);
            onboardingValidation.assertBillDeskMandateMockSuccess(response);
        } catch (Exception e) {
            log.error("Exception during BillDesk Mandate Mock Payment: {}", e.getMessage());
            Assert.fail("BillDesk Mandate Mock Payment failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 20, description = "Mandate Verify — verify mandate and create purchase plan after BillDesk callback")
    public void mandateVerify() {
        if (mandateId == null || mandateId.isBlank()) {
            throw new SkipException("mandateId not available — skipping Mandate Verify");
        }
        try {
            VerifyMandateRequest request = VerifyMandateRequest.build(MANDATE_SCHEME_ID, mandateId, purchasePlanId);
            VerifyMandateResponse response = onboardingMethodhelper.verifyMandate(accessToken, request);
            if (response.getData() != null && response.getData().getPurchasePlanId() != null) {
                purchasePlanId = response.getData().getPurchasePlanId();
            }
            onboardingValidation.assertVerifyMandateSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during Mandate Verify: {}", e.getMessage());
            Assert.fail("Mandate Verify failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 21, description = "SIP Submit — submit OTP consent to activate daily SIP")
    public void sipSubmit() {
        if (purchasePlanId == null || purchasePlanId.isBlank()) {
            throw new SkipException("purchasePlanId not available — skipping SIP Submit");
        }
        try {
            SubmitSipResponse response = onboardingMethodhelper.sipSubmit(accessToken, purchasePlanId, true);
            onboardingValidation.assertSipSubmitSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during SIP Submit: {}", e.getMessage());
            Assert.fail("SIP Submit failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }
    }





    @Test(priority = 22, description = "SIP Status Screen — fetch SIP details by purchasePlanId")
    public void sipStatusScreen() {
        if (purchasePlanId == null || purchasePlanId.isBlank()) {
            throw new SkipException("purchasePlanId not available — skipping SIP Status Screen");
        }
        try {
            SipStatusResponse response =
                        CommonUtil.pollUntilDone(
                                () -> onboardingMethodhelper.sipStatusScreen(accessToken, purchasePlanId),
                                OnBoardingdataProvider.SIP_STATUS_SHOULD_RETRY,
                                OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                                OnBoardingdataProvider.DEFAULT_DELAY_MS,
                                "payment Pre-Verification Status");
            onboardingValidation.assertSipStatusSuccess(response);


        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during SIP Status Screen: {}", e.getMessage());
            Assert.fail("SIP Status Screen failed: " + e.getMessage(), e);
        }
        finally {
            onboardingValidation.assertAll();
        }

    }
        @Test(priority = 1, description = "METADATA")
        public void HomeFeedMetaData() {

            try {
                HomeFeedMetaDataResponse response =
                        onboardingMethodhelper.HomefeedMetadataStatus(accessToken);

                if (response.getData() != null   && "KYC_VERIFIED".equalsIgnoreCase(response.getData().getOnboardingState())) {
                    isKycVerified=true;
                }
//                onboardingValidation.assertSipStatusSuccess(response);
            } catch (SkipException e) {
                throw e;
            } catch (Exception e) {
                log.error("META DATA ", e.getMessage());
                Assert.fail("META DATA " + e.getMessage(), e);
            }

    }
}
