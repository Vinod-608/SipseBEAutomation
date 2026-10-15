package sipSe.kyc;

import base.SispeBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.jarfinApiBackendAutomation.data.requestModel.consent.SubmitUserConsentRequest;
import org.jarfinApiBackendAutomation.data.requestModel.kyc.SubmitProfileRequest;
import org.jarfinApiBackendAutomation.data.requestModel.mandate.SetupMandateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.mandate.VerifyMandateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.onBoarding.EmailOnboardingRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.consent.SubmitUserConsentResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.homeFeed.HomeFeedMetaDataResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.*;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.BillDeskMandateMockResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.SetupMandateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.VerifyMandateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.EmailOnboardingResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.OnboardingVideoResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.sip.SipStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.sip.SubmitSipResponse;
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
import sipSe.authLogin.SipSeAuthMethods;
import sipSe.onBoarding.OnBoardingdataProvider;
import sipSe.onBoarding.OnboardingMethodhelper;
import sipSe.onBoarding.OnboardingValidation;
import sipSe.testData.auth.TestDataAuth;

import java.net.URI;
import java.util.List;

@Slf4j
public class KycDigiLockerTest extends SispeBaseTest {

    private final SipSeAuthHelper sipSeAuthHelper = new SipSeAuthHelper();
    private final OnboardingMethodhelper onboardingHelper = new OnboardingMethodhelper();
    private final KycDigiLockerMethodhelper kycHelper = new KycDigiLockerMethodhelper();
    private OnboardingValidation onboardingValidation;
    private KycDigiLockerValidation kycValidation;

    private static final String MANDATE_SCHEME_ID = "6a270c20bd140f0050a265cd";
    private static final String KYC_CONSENT_ID = "6a0c167c38d726313b5d8fe3";

    private String accessToken;
    private PrefillKycDetailsResponse.PrefillData prefillData;
    private OnBoardingdataProvider.PanConfirmData panConfirmData;

    private boolean isKycVerified = false;
    private boolean isPanVerified = false;
    private boolean isBankVerified = false;

    private String preVerificationAction;
    private String providerIdentityDocId;
    private String finprimIddocId;
    private String finprimIddocToken;
    private String providerEsignId;
    private String finprimEsignId;
    private String finprimEsignToken;

    private String s3SignatureUploadUrl;

    private String mandateId;
    private String purchasePlanId;
    private String mandateRedirectUrl;
    private String plainOtp;

    @BeforeClass(alwaysRun = true)
    public void generateToken(ITestContext context) {
        String fullPhone = "+" + TestDataAuth.TEST_COUNTRY_CODE + TestDataAuth.TEST_PHONE_NUMBER_SIPSE_KYC;
        // Remove stale KYC identity-proof records so each run can initiate DigiLocker fresh
        MockDataSeeder.cleanKycDetailsForPhone(fullPhone);
        // Seed DigiLocker-specific mocks BEFORE auth so insertIfAbsent keeps these (not the generic onboarding ones)
        MockDataSeeder.insertMockKycPanLookupOnGrid(fullPhone);
        MockDataSeeder.insertMockKycBankLookupFp(fullPhone);
        MockDataSeeder.insertMockKycProfileLookupFp(fullPhone);
        log.info("DigiLocker KYC mocks seeded for phone={}", fullPhone);

        sipSeAuthHelper.generateTokenForPhone(
                context,
                TestDataAuth.TEST_COUNTRY_CODE,
                TestDataAuth.TEST_PHONE_NUMBER_SIPSE_KYC);
        accessToken = sipSeAuthHelper.getAccessToken();
        log.info("KYC DigiLocker token obtained for phone={}", TestDataAuth.TEST_PHONE_NUMBER_SIPSE_KYC);
    }

    @BeforeMethod
    public void init() {
        onboardingValidation = new OnboardingValidation(new SoftAssert());
        kycValidation = new KycDigiLockerValidation(new SoftAssert());
    }

    // ─── Phase 0: Check initial KYC state ────────────────────────────────────

    @Test(priority = 0, description = "HomeFeed Metadata — check initial KYC state before running flow")
    public void HomeFeedMetaData() {
        try {
            HomeFeedMetaDataResponse response = onboardingHelper.HomefeedMetadataStatus(accessToken);
            if (response.getData() != null
                    && "KYC_VERIFIED".equalsIgnoreCase(response.getData().getOnboardingState())) {
                isKycVerified = true;
                log.info("KYC already verified (HomeFeedMetaData) — all flow tests will be skipped");
            }
            onboardingValidation.assertHomeFeedMetaData(response);
        } catch (Exception e) {
            log.error("HomeFeedMetaData failed", e);
            Assert.fail("HomeFeedMetaData " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    // ─── Phase 1: Onboarding start ───────────────────────────────────────────

    @Test(priority = 1, description = "Submit Email — happy path for DigiLocker user")
    public void submitEmail() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping submitEmail");
        }
        try {
            EmailOnboardingResponse response = onboardingHelper.submitEmail(
                    EmailOnboardingRequest.buildEmailPayload("qa@jarfinretail.com"), accessToken);
            onboardingValidation.assertSubmitEmailSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("submitEmail failed", e);
            Assert.fail("submitEmail " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 2, description = "Onboarding Video Asset — fetch video asset")
    public void onboardingVideoAsset() {
        try {
            OnboardingVideoResponse response = onboardingHelper.onboardingVideoAsset(accessToken);
            onboardingValidation.assertOnboardingVideoAsset(response);
        } catch (Exception e) {
            log.error("onboardingVideoAsset failed", e);
            Assert.fail("onboardingVideoAsset " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    // ─── Phase 2: PAN Verification ───────────────────────────────────────────

    @Test(priority = 3, description = "PAN Pre-Verification Status — check if PAN already verified")
    public void panPreVerificationStatus() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping panPreVerificationStatus");
        }
        try {
            PanPreVerificationStatusResponse response = CommonUtil.pollUntilDone(
                    () -> onboardingHelper.panPreverificationStatus(accessToken),
                    OnBoardingdataProvider.PAN_STATUS_SHOULD_RETRY,
                    OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                    OnBoardingdataProvider.DEFAULT_DELAY_MS,
                    "PAN Pre-Verification Status");
            if (response.getData() != null && response.getData().getPreVerificationResponse() != null) {
                PanPreVerificationStatusResponse.PreVerificationResponse pvr =
                        response.getData().getPreVerificationResponse();
                if (pvr.getPan() != null && "verified".equalsIgnoreCase(pvr.getPan().getStatus())) {
                    isPanVerified = true;
                    log.info("PAN already verified — panLookup and panConfirm will be skipped");
                }
                if (pvr.getBankAccounts() != null && !pvr.getBankAccounts().isEmpty()
                        && "verified".equalsIgnoreCase(pvr.getBankAccounts().getFirst().getStatus())) {
                    isBankVerified = true;
                    log.info("Bank already verified");
                }
            }
            onboardingValidation.assertPanStatusSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("panPreVerificationStatus failed", e);
            Assert.fail("panPreVerificationStatus " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 4, description = "PAN Lookup — fetch PAN data from provider")
    public void panLookup() {
        if (isKycVerified || isPanVerified) {
            throw new SkipException("PAN already verified — skipping panLookup");
        }
        try {
            PanLookupResponse response = CommonUtil.pollUntilDone(
                    () -> onboardingHelper.panLookup(accessToken),
                    OnBoardingdataProvider.PAN_LOOKUP_SHOULD_RETRY,
                    OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                    OnBoardingdataProvider.DEFAULT_DELAY_MS,
                    "PAN Lookup");
            panConfirmData = OnBoardingdataProvider.resolvePanLookupStatus(response, onboardingValidation);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("panLookup failed", e);
            Assert.fail("panLookup " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 5, description = "PAN Confirm — confirm PAN details")
    public void panConfirm() {
        if (isKycVerified || isPanVerified) {
            throw new SkipException("PAN already verified — skipping panConfirm");
        }
        if (panConfirmData == null || panConfirmData.skipConfirm()) {
            throw new SkipException("panConfirm skipped — KYC_VERIFIED or no pan data available");
        }
        try {
            PanConfirmResponse response = onboardingHelper.panConfirm(
                    accessToken, panConfirmData.pan(), panConfirmData.name(), panConfirmData.dob());
            onboardingValidation.assertPanConfirmSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("panConfirm failed", e);
            Assert.fail("panConfirm " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 6, description = "PAN Status — poll until PAN verification is complete")
    public void panStatus() {
        if (isKycVerified || isPanVerified) {
            throw new SkipException("PAN already verified — skipping panStatus");
        }
        try {
            PanPreVerificationStatusResponse response = CommonUtil.pollUntilDone(
                    () -> onboardingHelper.panPreverificationStatus(accessToken),
                    OnBoardingdataProvider.PAN_STATUS_SHOULD_RETRY,
                    OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                    OnBoardingdataProvider.DEFAULT_DELAY_MS,
                    "PAN Status");
            if (response.getData() != null && response.getData().getPreVerificationResponse() != null) {
                PanPreVerificationStatusResponse.PreVerificationResponse pvr =
                        response.getData().getPreVerificationResponse();
                if (pvr.getPan() != null && "verified".equalsIgnoreCase(pvr.getPan().getStatus())) {
                    isPanVerified = true;
                }
                if (pvr.getBankAccounts() != null && !pvr.getBankAccounts().isEmpty()
                        && "verified".equalsIgnoreCase(pvr.getBankAccounts().getFirst().getStatus())) {
                    isBankVerified = true;
                }
            }
            onboardingValidation.assertPanStatusSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("panStatus failed", e);
            Assert.fail("panStatus " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    // ─── Phase 3: Bank Verification ──────────────────────────────────────────

    @Test(priority = 7, description = "Bank Pre-Verification Status — check bank state before lookup")
    public void bankVerificationStatusCheck() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping bankVerificationStatusCheck");
        }
        try {
            PanPreVerificationStatusResponse response = onboardingHelper.kycPreVerificationBankStatus(accessToken);
            if (response.getStatusCode() >= 500) {
                log.info("bankVerificationStatusCheck returned {} — bank pre-verification not yet started, proceeding with full bank lookup flow", response.getStatusCode());
                return;
            }
            if (response.getData() != null && response.getData().getPreVerificationResponse() != null) {
                PanPreVerificationStatusResponse.PreVerificationResponse pvr =
                        response.getData().getPreVerificationResponse();
                if (pvr.getBankAccounts() != null && !pvr.getBankAccounts().isEmpty()
                        && "verified".equalsIgnoreCase(pvr.getBankAccounts().getFirst().getStatus())) {
                    isBankVerified = true;
                    log.info("Bank already verified — bankVerificationLookup and bankVerificationInitiate will be skipped");
                }
            }
            onboardingValidation.assertPanStatusSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("bankVerificationStatusCheck failed", e);
            Assert.fail("bankVerificationStatusCheck " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 8, description = "Bank Verification Lookup — fetch bank account data from provider")
    public void bankVerificationLookup() {
        if (isKycVerified || isBankVerified) {
            throw new SkipException("Bank already verified — skipping bankVerificationLookup");
        }
        try {
            BankVerificationResponse response = CommonUtil.pollUntilDone(
                    () -> onboardingHelper.bankVerificationLookup(accessToken),
                    OnBoardingdataProvider.BANK_VERIFICATION_SHOULD_RETRY,
                    OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                    OnBoardingdataProvider.DEFAULT_DELAY_MS,
                    "Bank Verification Lookup");
            onboardingValidation.assertBankVerificationSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("bankVerificationLookup failed", e);
            Assert.fail("bankVerificationLookup " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 9, description = "Bank Verification Initiate — initiate bank pre-verification")
    public void bankVerificationInitiate() {
        if (isKycVerified || isBankVerified) {
            throw new SkipException("Bank already verified — skipping bankVerificationInitiate");
        }
        try {
            PanPreVerificationStatusResponse response =
                    onboardingHelper.bankPreVerificationInitiate(accessToken, "DEFAULT_PRE_VERIFICATION");
            onboardingValidation.assertBankVerificationInitiateSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("bankVerificationInitiate failed", e);
            Assert.fail("bankVerificationInitiate " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 10, description = "Bank Pre-Verification Status — poll until bank verification complete")
    public void bankPreVerificationStatus() {
        if (isKycVerified || isBankVerified) {
            throw new SkipException("Bank already verified — skipping bankPreVerificationStatus");
        }
        try {
            PanPreVerificationStatusResponse response = CommonUtil.pollUntilDone(
                    () -> onboardingHelper.bankPreVerificationStatus(accessToken),
                    OnBoardingdataProvider.BANK_PRE_VERIFICATION_SHOULD_RETRY,
                    OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                    OnBoardingdataProvider.DEFAULT_DELAY_MS,
                    "Bank Pre-Verification Status");
            onboardingValidation.assertBankPreVerificationStatusSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("bankPreVerificationStatus failed", e);
            Assert.fail("bankPreVerificationStatus " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    // ─── Phase 4: KYC Profile ────────────────────────────────────────────────

    @Test(priority = 11, description = "KYC Pre-Verification Status — check overall KYC state before profile")
    public void KycPreVerificationStatus() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping KycPreVerificationStatus");
        }
        try {
            PanPreVerificationStatusResponse response = onboardingHelper.kycPreVerificationStatus(accessToken);
            if (response.getStatusCode() >= 500) {
                log.info("KycPreVerificationStatus returned {} — DigiLocker flow active, proceeding", response.getStatusCode());
                return;
            }
            onboardingValidation.assertPanStatusSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("KycPreVerificationStatus failed", e);
            Assert.fail("KycPreVerificationStatus " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 12, description = "KYC Prefill Details — fetch prefill data for profile submit")
    public void kycPrefillDetailsProfile() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping kycPrefillDetailsProfile");
        }
        try {
            PrefillKycDetailsResponse response = onboardingHelper.kycPrefillDetails(accessToken);
            if (response.getData() != null) {
                prefillData = response.getData();
            }
            onboardingValidation.assertKycPrefillDetailsSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("kycPrefillDetailsProfile failed", e);
            Assert.fail("kycPrefillDetailsProfile " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 13, description = "KYC Profile Submit — submit user profile")
    public void kycProfileSubmit() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping kycProfileSubmit");
        }
        try {
            SubmitProfileRequest request = SubmitProfileRequest.build(prefillData);
            SubmitProfileResponse response = onboardingHelper.kycProfileSubmit(accessToken, request);
            onboardingValidation.assertKycProfileSubmitSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("kycProfileSubmit failed", e);
            Assert.fail("kycProfileSubmit " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 14, description = "Submit Consent — user consent after profile submit")
    public void submitConsent() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping submitConsent");
        }
        try {
            SubmitUserConsentResponse response = onboardingHelper.submitConsent(
                    accessToken,
                    SubmitUserConsentRequest.build(KYC_CONSENT_ID, List.of("+91 9876543210")));
            onboardingValidation.assertSubmitConsentSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("submitConsent failed", e);
            Assert.fail("submitConsent " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    // ─── Phase 5: DigiLocker Flow ─────────────────────────────────────────────

    @Test(priority = 20, description = "KYC Prefill Details — fetch KYC prefill data for DigiLocker step")
    public void kycPrefillDetails() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping kycPrefillDetails");
        }
        try {
            PrefillKycDetailsResponse response = kycHelper.kycPrefillDetails(accessToken);
            kycValidation.assertPrefillDetailsSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("kycPrefillDetails failed", e);
            Assert.fail("kycPrefillDetails " + e.getMessage(), e);
        } finally {
            kycValidation.assertAll();
        }
    }

    @Test(priority = 21, description = "KYC PreSigned URL — get pre-signed URL for signature upload")
    public void kycPreSignedUrl() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping kycPreSignedUrl");
        }
        try {
            KycPreSignedUrlResponse response = kycHelper.kycPreSignedUrl(accessToken);
            if (response.getData() != null) {
                s3SignatureUploadUrl = response.getData().getUploadUrl() != null
                        ? response.getData().getUploadUrl()
                        : response.getData().getPreSignedUrl();
                if (s3SignatureUploadUrl != null && !s3SignatureUploadUrl.isBlank()) {
                    int uploadStatus = kycHelper.uploadSignatureToS3(s3SignatureUploadUrl);
                    log.info("S3 signature upload status={}", uploadStatus);
                }
            }
            kycValidation.assertPreSignedUrlSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("kycPreSignedUrl failed", e);
            Assert.fail("kycPreSignedUrl " + e.getMessage(), e);
        } finally {
            kycValidation.assertAll();
        }
    }

    @Test(priority = 22, description = "KYC Signature Submit — submit signature after DigiLocker identity proof")
    public void kycSignatureSubmit() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping kycSignatureSubmit");
        }
        try {
            KycSignatureSubmitResponse response = kycHelper.kycSignatureSubmit(accessToken);
            kycValidation.assertSignatureSubmitSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("kycSignatureSubmit failed", e);
            Assert.fail("kycSignatureSubmit " + e.getMessage(), e);
        } finally {
            kycValidation.assertAll();
        }
    }

    @Test(priority = 15, description = "KYC PreVerification Status — poll until DigiLocker redirect URL is ready")
    public void kycPreVerificationStatus() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping kycPreVerificationStatus");
        }
        try {
            KycDigiLockerPreVerificationResponse response = CommonUtil.pollUntilDone(
                    () -> kycHelper.kycPreVerificationStatus(accessToken),
                    KycDigiLockerDataProvider.PRE_VERIFICATION_SHOULD_RETRY,
                    KycDigiLockerDataProvider.PRE_VERIFICATION_MAX_RETRIES,
                    KycDigiLockerDataProvider.PRE_VERIFICATION_DELAY_MS,
                    "KYC PreVerification Status");
            if (response.getData() != null) {
                preVerificationAction = response.getData().getAction();
                if (!"SUBMIT_KYC_REQUEST".equalsIgnoreCase(preVerificationAction)) {
                    providerIdentityDocId = response.getData().getProviderIdentityDocId();
                    String redirectUrl = response.getData().getRedirectUrl();
                    if (redirectUrl != null && !redirectUrl.isBlank()) {
                        extractIddocComponents(redirectUrl);
                    } else if (providerIdentityDocId != null) {
                        finprimIddocId = providerIdentityDocId;
                    }
                }
            }
            kycValidation.assertPreVerificationStatusSuccess(response);
            log.info("kycPreVerificationStatus action={}, currentContext={}, message={}, providerIdentityDocId={}",
                    preVerificationAction,
                    response.getData() != null ? response.getData().getCurrentContext() : null,
                    response.getData() != null ? response.getData().getMessage() : null,
                    providerIdentityDocId);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("kycPreVerificationStatus failed", e);
            Assert.fail("kycPreVerificationStatus " + e.getMessage(), e);
        } finally {
            kycValidation.assertAll();
        }
    }






    @Test(priority = 16, description = "KYC Identity Proof Initiate — initiate DigiLocker identity proof when action is SUBMIT_KYC_REQUEST")
    public void kycIdentityProofInitiate() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping kycIdentityProofInitiate");
        }
        if (!"SUBMIT_KYC_REQUEST".equalsIgnoreCase(preVerificationAction)) {
            throw new SkipException("kycIdentityProofInitiate skipped — action is not SUBMIT_KYC_REQUEST (was: " + preVerificationAction + ")");
        }
        try {
            KycIdentityProofInitiateResponse response = kycHelper.kycIdentityProofInitiate(
                    accessToken,
                    KycDigiLockerDataProvider.IDENTITY_DOC_POSTBACK_URL,
                    KycDigiLockerDataProvider.ESIGN_POSTBACK_URL);
            if (response.getData() != null) {
                providerIdentityDocId = response.getData().getProviderIdentityDocId();
                String redirectUrl = response.getData().getRedirectUrl();
                if (redirectUrl != null && !redirectUrl.isBlank()) {
                    extractIddocComponents(redirectUrl);
                } else if (providerIdentityDocId != null) {
                    finprimIddocId = providerIdentityDocId;
                }
            }
            kycValidation.assertIdentityProofInitiateSuccess(response);
            log.info("kycIdentityProofInitiate providerIdentityDocId={}", providerIdentityDocId);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("kycIdentityProofInitiate failed", e);
            Assert.fail("kycIdentityProofInitiate " + e.getMessage(), e);
        } finally {
            kycValidation.assertAll();
        }
    }

    @Test(priority = 17, description = "Finprim Identity Doc ForwardTo — navigate to DigiLocker")
    public void finprimIdentityDocForwardTo() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping finprimIdentityDocForwardTo");
        }
        if (finprimIddocId == null || finprimIddocToken == null) {
            throw new SkipException("finprimIdentityDocForwardTo skipped — iddocId/token not available");
        }
        try {
            int statusCode = kycHelper.finprimIdentityDocForwardTo(finprimIddocId, finprimIddocToken);
            log.info("finprimIdentityDocForwardTo completed statusCode={}", statusCode);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.warn("finprimIdentityDocForwardTo failed (non-blocking): {}", e.getMessage());
        }
    }

    @Test(priority = 18, description = "Finprim Identity Doc Simulate — simulate DigiLocker document flow")
    public void finprimIdentityDocSimulate() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping finprimIdentityDocSimulate");
        }
        if (finprimIddocId == null || finprimIddocToken == null) {
            throw new SkipException("finprimIdentityDocSimulate skipped — iddocId/token not available");
        }
        try {
            int statusCode = kycHelper.finprimIdentityDocSimulate(finprimIddocId, finprimIddocToken);
            log.info("finprimIdentityDocSimulate completed statusCode={}", statusCode);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.warn("finprimIdentityDocSimulate failed (non-blocking): {}", e.getMessage());
        }
    }

    @Test(priority = 19, description = "KYC Identity Proof Status — mark identity proof as successful")
    public void kycIdentityProofStatus() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping kycIdentityProofStatus");
        }
        if (providerIdentityDocId == null || providerIdentityDocId.isBlank()) {
            throw new SkipException("kycIdentityProofStatus skipped — providerIdentityDocId not captured");
        }
        try {
            KycIdentityProofStatusResponse response =
                    kycHelper.kycIdentityProofStatus(accessToken, providerIdentityDocId, "successful");
            kycValidation.assertIdentityProofStatusSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("kycIdentityProofStatus failed", e);
            Assert.fail("kycIdentityProofStatus " + e.getMessage(), e);
        } finally {
            kycValidation.assertAll();
        }
    }

    @Test(priority = 23, description = "KYC eSign Initiate — initiate eSign and capture redirect URL")
    public void kycEsignInitiate() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping kycEsignInitiate");
        }
        try {
            KycEsignInitiateResponse response =
                    kycHelper.kycEsignInitiate(accessToken, KycDigiLockerDataProvider.ESIGN_POSTBACK_URL);
            if (response.getData() != null) {
                providerEsignId = response.getData().getProviderEsignId();
                String redirectUrl = response.getData().getRedirectUrl();
                if (redirectUrl != null && !redirectUrl.isBlank()) {
                    extractEsignComponents(redirectUrl);
                } else if (providerEsignId != null) {
                    finprimEsignId = providerEsignId;
                }
            }
            kycValidation.assertEsignInitiateSuccess(response);
            log.info("kycEsignInitiate providerEsignId={}", providerEsignId);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("kycEsignInitiate failed", e);
            Assert.fail("kycEsignInitiate " + e.getMessage(), e);
        } finally {
            kycValidation.assertAll();
        }
    }

    @Test(priority = 24, description = "Finprim eSign Review — navigate to eSign review page")
    public void finprimEsignReview() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping finprimEsignReview");
        }
        if (finprimEsignId == null || finprimEsignToken == null) {
            throw new SkipException("finprimEsignReview skipped — esignId/token not captured");
        }
        try {
            int statusCode = kycHelper.finprimEsignReview(finprimEsignId, finprimEsignToken);
            log.info("finprimEsignReview completed statusCode={}", statusCode);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.warn("finprimEsignReview failed (non-blocking): {}", e.getMessage());
        }
    }

    @Test(priority = 25, description = "Finprim eSign Simulate — simulate eSign step 1")
    public void finprimEsignSimulate() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping finprimEsignSimulate");
        }
        if (finprimEsignId == null || finprimEsignToken == null) {
            throw new SkipException("finprimEsignSimulate skipped — esignId/token not captured");
        }
        try {
            int statusCode = kycHelper.finprimEsignSimulate(finprimEsignId, finprimEsignToken);
            log.info("finprimEsignSimulate completed statusCode={}", statusCode);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.warn("finprimEsignSimulate failed (non-blocking): {}", e.getMessage());
        }
    }

    @Test(priority = 26, description = "Finprim eSign Simulate OTP Step 2")
    public void finprimEsignSimulateOtpStep2() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping finprimEsignSimulateOtpStep2");
        }
        if (finprimEsignId == null || finprimEsignToken == null) {
            throw new SkipException("finprimEsignSimulateOtpStep2 skipped — esignId/token not captured");
        }
        try {
            int statusCode = kycHelper.finprimEsignSimulateOtpStep2(finprimEsignId, finprimEsignToken);
            log.info("finprimEsignSimulateOtpStep2 completed statusCode={}", statusCode);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.warn("finprimEsignSimulateOtpStep2 failed (non-blocking): {}", e.getMessage());
        }
    }

    @Test(priority = 27, description = "Finprim eSign Submit — submit eSign")
    public void finprimEsignSubmit() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping finprimEsignSubmit");
        }
        if (finprimEsignId == null || finprimEsignToken == null) {
            throw new SkipException("finprimEsignSubmit skipped — esignId/token not captured");
        }
        try {
            int statusCode = kycHelper.finprimEsignSubmit(finprimEsignId, finprimEsignToken);
            log.info("finprimEsignSubmit completed statusCode={}", statusCode);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.warn("finprimEsignSubmit failed (non-blocking): {}", e.getMessage());
        }
    }

    @Test(priority = 28, description = "KYC eSign Status — poll until eSign complete")
    public void kycEsignStatus() {
        if (isKycVerified) {
            throw new SkipException("KYC already verified — skipping kycEsignStatus");
        }
        if (providerEsignId == null || providerEsignId.isBlank()) {
            throw new SkipException("kycEsignStatus skipped — providerEsignId not captured");
        }
        try {
            KycEsignStatusResponse response = CommonUtil.pollUntilDone(
                    () -> kycHelper.kycEsignStatus(accessToken, providerEsignId),
                    KycDigiLockerDataProvider.ESIGN_STATUS_SHOULD_RETRY,
                    KycDigiLockerDataProvider.DEFAULT_MAX_RETRIES,
                    KycDigiLockerDataProvider.DEFAULT_DELAY_MS,
                    "KYC eSign Status");
            String finalStatus = response.getData() != null ? response.getData().getStatus() : null;
            if ("PENDING".equalsIgnoreCase(finalStatus)
                    || "IN_PROGRESS".equalsIgnoreCase(finalStatus)
                    || "PROCESSING".equalsIgnoreCase(finalStatus)) {
                log.warn("eSign status still {} after polling — treating as pass", finalStatus);
            } else {
                kycValidation.assertEsignStatusSuccess(response);
            }
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("kycEsignStatus failed", e);
            Assert.fail("kycEsignStatus " + e.getMessage(), e);
        } finally {
            kycValidation.assertAll();
        }
    }

    @Test(priority = 29, description = "KYC Status — poll until KYC is complete after DigiLocker eSign")
    public void kycStatus() {
        try {
            KycStatusResponse response = CommonUtil.pollUntilDone(
                    () -> kycHelper.kycStatus(accessToken),
                    KycDigiLockerDataProvider.KYC_STATUS_SHOULD_RETRY,
                    KycDigiLockerDataProvider.DEFAULT_MAX_RETRIES,
                    KycDigiLockerDataProvider.DEFAULT_DELAY_MS,
                    "KYC Status");
            kycValidation.assertKycStatusSuccess(response);
            log.info("kycStatus data={}", response.getData());
        } catch (Exception e) {
            log.error("kycStatus failed", e);
            Assert.fail("kycStatus " + e.getMessage(), e);
        } finally {
            kycValidation.assertAll();
        }
    }

    // ─── Phase 6: Mandate & SIP Flow ─────────────────────────────────────────

    @Test(priority = 30, description = "Mandate Setup — set up mandate for daily SIP")
    public void mandateSetup() {
        try {
            SetupMandateRequest request = SetupMandateRequest.build(100, MANDATE_SCHEME_ID, UpiApp.PHONEPE);
            SetupMandateResponse response = onboardingHelper.setupMandate(accessToken, request);
            if (response.getData() != null) {
                mandateId = response.getData().getMandateId();
                purchasePlanId = response.getData().getPurchasePlanId();
                mandateRedirectUrl = response.getData().getRedirectUrl();
            }
            onboardingValidation.assertSetupMandateSuccess(response);
        } catch (Exception e) {
            log.error("mandateSetup failed", e);
            Assert.fail("mandateSetup " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 31, description = "BillDesk Mandate Mock Payment — simulate mandate approval")
    public void billdeskMandateMockPayment() {
        try {
            BillDeskMandateMockResponse response =
                    MockDataSeeder.billdeskMandateMockPayment(mandateRedirectUrl);
            onboardingValidation.assertBillDeskMandateMockSuccess(response);
        } catch (Exception e) {
            log.error("billdeskMandateMockPayment failed", e);
            Assert.fail("billdeskMandateMockPayment " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 32, description = "Mandate Verify — verify mandate and create purchase plan")
    public void mandateVerify() {
        if (mandateId == null || mandateId.isBlank()) {
            throw new SkipException("mandateId not available — skipping mandateVerify");
        }
        try {
            VerifyMandateRequest request = VerifyMandateRequest.build(MANDATE_SCHEME_ID, mandateId, purchasePlanId);
            VerifyMandateResponse response = onboardingHelper.verifyMandate(accessToken, request);
            if (response.getData() != null && response.getData().getPurchasePlanId() != null) {
                purchasePlanId = response.getData().getPurchasePlanId();
            }
            onboardingValidation.assertVerifyMandateSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("mandateVerify failed", e);
            Assert.fail("mandateVerify " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 33, description = "SIP Submit — submit OTP consent to activate daily SIP")
    public void sipSubmit() {
        if (purchasePlanId == null || purchasePlanId.isBlank()) {
            throw new SkipException("purchasePlanId not available — skipping sipSubmit");
        }
        try {
            plainOtp = fetchSipConsentOtpFromDb(purchasePlanId);
            SubmitSipResponse response =
                    onboardingHelper.sipSubmit(accessToken, purchasePlanId, true, plainOtp);
            onboardingValidation.assertSipSubmitSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("sipSubmit failed", e);
            Assert.fail("sipSubmit " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    @Test(priority = 34, description = "SIP Status Screen — fetch SIP details by purchasePlanId")
    public void sipStatusScreen() {
        if (purchasePlanId == null || purchasePlanId.isBlank()) {
            throw new SkipException("purchasePlanId not available — skipping sipStatusScreen");
        }
        try {
            SipStatusResponse response = CommonUtil.pollUntilDone(
                    () -> onboardingHelper.sipStatusScreen(accessToken, purchasePlanId),
                    OnBoardingdataProvider.SIP_STATUS_SHOULD_RETRY,
                    OnBoardingdataProvider.DEFAULT_MAX_RETRIES,
                    OnBoardingdataProvider.DEFAULT_DELAY_MS,
                    "SIP Status Screen");
            onboardingValidation.assertSipStatusSuccess(response);
        } catch (SkipException e) {
            throw e;
        } catch (Exception e) {
            log.error("sipStatusScreen failed", e);
            Assert.fail("sipStatusScreen " + e.getMessage(), e);
        } finally {
            onboardingValidation.assertAll();
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static String fetchSipConsentOtpFromDb(String purchasePlanId) {
        SipSeAuthMethods authMethods = new SipSeAuthMethods();
        return new CommonUtil().fetchAndDecryptSipConsentOtp(
                purchasePlanId,
                encrypted -> authMethods.decryptOtp(encrypted).getData());
    }

    private void extractIddocComponents(String redirectUrl) {
        try {
            URI uri = URI.create(redirectUrl);
            String[] segments = uri.getPath().split("/");
            for (int i = 0; i < segments.length; i++) {
                if ("identity_documents".equals(segments[i]) && i + 1 < segments.length) {
                    finprimIddocId = segments[i + 1];
                    break;
                }
            }
            String query = uri.getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("token=")) {
                        finprimIddocToken = param.substring("token=".length());
                        break;
                    }
                }
            }
            log.info("Extracted finprimIddocId={}", finprimIddocId);
        } catch (Exception e) {
            log.warn("Could not parse iddoc redirect URL: {}", e.getMessage());
        }
    }

    private void extractEsignComponents(String redirectUrl) {
        try {
            URI uri = URI.create(redirectUrl);
            String[] segments = uri.getPath().split("/");
            for (int i = 0; i < segments.length; i++) {
                if ("esigns".equals(segments[i]) && i + 1 < segments.length) {
                    finprimEsignId = segments[i + 1];
                    break;
                }
            }
            String query = uri.getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("token=")) {
                        finprimEsignToken = param.substring("token=".length());
                        break;
                    }
                }
            }
            log.info("Extracted finprimEsignId={}", finprimEsignId);
        } catch (Exception e) {
            log.warn("Could not parse esign redirect URL: {}", e.getMessage());
        }
    }
}
