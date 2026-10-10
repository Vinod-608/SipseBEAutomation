package sipSe.onBoarding;

import org.jarfinApiBackendAutomation.data.responseModel.sipSe.homeFeed.HomeFeedMetaDataResponse;
import org.jarfinApiBackendAutomation.utils.MockDataSeeder;
import static org.jarfinApiBackendAutomation.utils.CommonUtil.sipSegetApiEndPoint;
import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.*;

import io.restassured.response.Response;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.jarfinApiBackendAutomation.configuration.BaseUri;
import org.jarfinApiBackendAutomation.data.common.RestRequest;
import org.jarfinApiBackendAutomation.data.requestModel.kyc.BankVerificationInitiateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.mandate.SetupMandateRequest;
import org.jarfinApiBackendAutomation.dbConfiguration.DataBaseFactory;
import org.jarfinApiBackendAutomation.data.requestModel.mandate.VerifyMandateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.consent.SubmitUserConsentRequest;
import org.jarfinApiBackendAutomation.data.requestModel.nominee.AddNomineeRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.BillDeskMandateMockResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.SetupMandateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.VerifyMandateResponse;
import org.jarfinApiBackendAutomation.data.requestModel.sip.SubmitSipRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.sip.SipStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.sip.SubmitSipResponse;
import sipSe.authLogin.SipSeAuthMethods;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.consent.SubmitUserConsentResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.nominee.AddNomineeResponse;
import org.jarfinApiBackendAutomation.data.requestModel.kyc.PanConfirmRequest;
import org.jarfinApiBackendAutomation.data.requestModel.kyc.SetuMockPaymentRequest;
import org.jarfinApiBackendAutomation.data.requestModel.kyc.SubmitProfileRequest;
import org.jarfinApiBackendAutomation.data.requestModel.onBoarding.EmailOnboardingRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.BankVerificationInitiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.BankVerificationInitiateV2Response;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PrefillKycDetailsResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.BankVerificationResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.SetuMockPaymentResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.SubmitProfileResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PanConfirmResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PanLookupResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PanPreVerificationStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.EmailOnboardingResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.OnboardingVideoResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.SispseUserDetailsRespose;
import org.jarfinApiBackendAutomation.endPoints.SipSeEndPoints;
import org.jarfinApiBackendAutomation.utils.ApiRequests;
import org.jarfinApiBackendAutomation.utils.CommonSerializationUtil;

@Slf4j
public class OnboardingMethodhelper {

    private final ApiRequests apiRequests = new ApiRequests();

    public EmailOnboardingResponse submitEmail(EmailOnboardingRequest request, String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Content-Type",
                                        "application/json",
                                        "Authorization",
                                        "Bearer " + accessToken))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI,
                                        SipSeEndPoints.SUBMIT_EMAIL))
                        .body(request)
                        .build();
        Response response = apiRequests.post(req);
        EmailOnboardingResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), EmailOnboardingResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Submit Email — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public OnboardingVideoResponse onboardingVideoAsset(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Content-Type",
                                        "application/json",
                                        "Authorization",
                                        "Bearer " + accessToken))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI,
                                        SipSeEndPoints.onBoardingVideo))
                        .build();
        Response response = apiRequests.get(req);
        OnboardingVideoResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), OnboardingVideoResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Onboarding Video Asset — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public SispseUserDetailsRespose userDetails(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Content-Type",
                                        "application/json",
                                        "Authorization",
                                        "Bearer " + accessToken))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI, SipSeEndPoints.userDetails))
                        .build();
        Response response = apiRequests.get(req);
        SispseUserDetailsRespose deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SispseUserDetailsRespose.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "User Details — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public void insertMockBankLookupFp(String phoneNumber) {
        MockDataSeeder.insertMockBankLookupFp(phoneNumber);
    }

    public void insertMockProfileLookupFp(String phoneNumber) {
        MockDataSeeder.insertMockProfileLookupFp(phoneNumber);
    }

    public void insertMockPanLookupOnGrid(String phoneNumber) {
        MockDataSeeder.insertMockPanLookupOnGrid(phoneNumber);
    }

    public PanLookupResponse panLookup(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "accept", "application/json, text/plain, */*",
                                        "Authorization", "Bearer " + accessToken,
                                        "os", "android"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI, SipSeEndPoints.KYC_PAN_LOOKUP))
                        .build();
        Response response = apiRequests.get(req);
        PanLookupResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), PanLookupResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "PAN Lookup — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public PanConfirmResponse panConfirm(String accessToken, String pan, String name, String dob) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "accept", "application/json, text/plain, */*",
                                        "Authorization", "Bearer " + accessToken,
                                        "os", "android"))
                        .body(PanConfirmRequest.build(pan, name, dob))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI, SipSeEndPoints.KYC_PAN_CONFIRM))
                        .build();
        Response response = apiRequests.post(req);
        PanConfirmResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), PanConfirmResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "PAN Confirm — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public PanPreVerificationStatusResponse panPreverificationStatus(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "accept", "application/json, text/plain, */*",
                                        "Authorization", "Bearer " + accessToken,
                                        "os", "android"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI, SipSeEndPoints.KYC_PAN_STATUS))
                        .build();
        Response response = apiRequests.get(req);
        PanPreVerificationStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), PanPreVerificationStatusResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "PAN Lookup — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public BankVerificationResponse bankVerificationLookup(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "accept", "application/json, text/plain, */*",
                                        "Authorization", "Bearer " + accessToken,
                                        "x-requested-with", "com.sipse.app.staging"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI
                                        , SipSeEndPoints.BANK_ACCOUNT_LOOKUP))
                        .build();
        Response response = apiRequests.get(req);
        BankVerificationResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), BankVerificationResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Bank Verification Lookup — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public BankVerificationInitiateResponse bankVerificationInitiate(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Authorization", "Bearer " + accessToken,
                                        "Content-Type", "application/json"))
                        .queryParams(Map.of("bankVerificationContext","DEFAULT_PRE_VERIFICATION"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI,
                                        SipSeEndPoints.BANK_VERIFICATION_INITIATE))
                        .build();
        Response response = apiRequests.post(req);
        BankVerificationInitiateResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), BankVerificationInitiateResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Bank Verification Initiate — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public BankVerificationInitiateResponse bankVerificationFetch(
            String accessToken, String verificationId) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Authorization", "Bearer " + accessToken))
                        .queryParams(Map.of("verificationId", verificationId))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI,
                                        SipSeEndPoints.BANK_VERIFICATION_FETCH))
                        .build();
        Response response = apiRequests.get(req);
        BankVerificationInitiateResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), BankVerificationInitiateResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Bank Verification Fetch — status={}, verificationId={}",
                response.getStatusCode(),
                verificationId);
        return deserializedResponse;
    }

    public PrefillKycDetailsResponse kycPrefillDetails(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(Map.of("Authorization", "Bearer " + accessToken))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI,
                                        SipSeEndPoints.KYC_PREFILL_DETAILS))
                        .build();
        Response response = apiRequests.get(req);
        PrefillKycDetailsResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), PrefillKycDetailsResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "KYC Prefill Details — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public SubmitProfileResponse kycProfileSubmit(String accessToken, SubmitProfileRequest request) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Authorization", "Bearer " + accessToken,
                                        "Content-Type", "application/json"))
                        .body(request)
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI,
                                        SipSeEndPoints.KYC_PROFILE_SUBMIT))
                        .build();
        Response response = apiRequests.post(req);
        SubmitProfileResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SubmitProfileResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "KYC Profile Submit — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public PanPreVerificationStatusResponse kycPreVerificationStatus(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "accept", "application/json, text/plain, */*",
                                        "Authorization", "Bearer " + accessToken,
                                        "os", "android"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI,
                                        SipSeEndPoints.KYC_PRE_VERIFICATION_STATUS))
                        .build();
        Response response = apiRequests.get(req);
        PanPreVerificationStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), PanPreVerificationStatusResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "KYC Pre-Verification Status — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }



    public PanPreVerificationStatusResponse kycPreVerificationBankStatus(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "accept", "application/json, text/plain, */*",
                                        "Authorization", "Bearer " + accessToken,
                                        "os", "android"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI,
                                        SipSeEndPoints.BANK_PRE_VERIFICATION_STATUS))
                        .build();
        Response response = apiRequests.get(req);
        PanPreVerificationStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), PanPreVerificationStatusResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "KYC Pre-Verification Status — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }


    public BankVerificationInitiateV2Response bankVerificationInitiateV2(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "accept", "application/json, text/plain, */*",
                                        "content-type", "application/json",
                                        "Authorization", "Bearer " + accessToken,
                                        "x-requested-with", "com.sipse.app.staging"))
                        .body(
                                BankVerificationInitiateRequest.build(
                                        "",
                                        "https://ui-staging.sipse.in/onboarding/bank-verification-status?source=add-new&context=POST_RPD_PRE_VERIFICATION",
                                        300,
                                        "PHONEPE"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.BANK_VERIFICATION_INITIATE_V2))
                        .build();
        Response response = apiRequests.post(req);
        BankVerificationInitiateV2Response deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), BankVerificationInitiateV2Response.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Bank Verification Initiate V2 — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public PanPreVerificationStatusResponse bankPreVerificationInitiate(String accessToken, String bankVerificationContext) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Authorization", "Bearer " + accessToken,
                                        "accept", "application/json, text/plain, */*",
                                        "os", "android",
                                        "x-requested-with", "com.sipse.app.staging"))
                        .queryParams(Map.of("bankVerificationContext", bankVerificationContext))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.BANK_PRE_VERIFICATION_INITIATE_V2))
                        .build();
        Response response = apiRequests.post(req);
        PanPreVerificationStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), PanPreVerificationStatusResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Bank Pre-Verification Initiate — status={}, success={}, context={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess(),
                bankVerificationContext);
        return deserializedResponse;
    }

    public PanPreVerificationStatusResponse bankPreVerificationStatus(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Authorization", "Bearer " + accessToken,
                                        "accept", "application/json, text/plain, */*"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI,
                                        SipSeEndPoints.BANK_PRE_VERIFICATION_STATUS))
                        .build();
        Response response = apiRequests.get(req);
        PanPreVerificationStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), PanPreVerificationStatusResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Bank Pre-Verification Status — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public SetupMandateResponse setupMandate(String accessToken, SetupMandateRequest request) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "accept", "application/json, text/plain, */*",
                                        "Authorization", "Bearer " + accessToken,
                                        "content-type", "application/json",
                                        "os", "android",
                                        "x-requested-with", "com.sipse.app.staging"))
                        .body(request)
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.MANDATE_SETUP))
                        .build();
        Response response = apiRequests.post(req);
        SetupMandateResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SetupMandateResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Mandate Setup — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public SubmitUserConsentResponse submitConsent(String accessToken, SubmitUserConsentRequest request) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Authorization", "Bearer " + accessToken,
                                        "Content-Type", "application/json"))
                        .body(request)
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI,
                                        SipSeEndPoints.CONSENT_SUBMIT))
                        .build();
        Response response = apiRequests.post(req);
        SubmitUserConsentResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SubmitUserConsentResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Submit Consent — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public AddNomineeResponse addNominee(String accessToken, AddNomineeRequest request) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Authorization", "Bearer " + accessToken,
                                        "Content-Type", "application/json"))
                        .body(request)
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_STAGING_BASE_URI,
                                        SipSeEndPoints.NOMINEE_ADD))
                        .build();
        Response response = apiRequests.post(req);
        AddNomineeResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), AddNomineeResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Add Nominee — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public VerifyMandateResponse verifyMandate(String accessToken, VerifyMandateRequest request) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "accept", "application/json, text/plain, */*",
                                        "Authorization", "Bearer " + accessToken,
                                        "content-type", "application/json",
                                        "os", "android",
                                        "x-requested-with", "com.sipse.app.staging"))
                        .body(request)
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.MANDATE_VERIFY))
                        .build();
        Response response = apiRequests.post(req);
        VerifyMandateResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), VerifyMandateResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Mandate Verify — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }





    public SubmitSipResponse sipSubmit(String accessToken, String purchasePlanId, boolean isNewPurchasePlan, String plainOtp ) {

        RestRequest req = RestRequest.builder()
                .headers(Map.of(
                        "accept", "application/json, text/plain, */*",
                        "Authorization", "Bearer " + accessToken,
                        "content-type", "application/json",
                        "os", "android",
                        "x-requested-with", "com.sipse.app.staging"))
                .body(SubmitSipRequest.build(plainOtp, purchasePlanId, isNewPurchasePlan))
                .url(sipSegetApiEndPoint(BaseUri.SIPSE_BASE_URI, SipSeEndPoints.SIP_SUBMIT))
                .build();
        Response response = apiRequests.post(req);
        SubmitSipResponse deserializedResponse =
                CommonSerializationUtil.readObject(response.getBody().asString(), SubmitSipResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info("SIP Submit — status={}, success={}", response.getStatusCode(), deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public SetuMockPaymentResponse setuMockPayment(String verificationId) {
        String url =
                BaseUri.SETU_SANDBOX_BASE_URI
                        + SipSeEndPoints.SETU_MOCK_PAYMENT.replace(
                                "{verificationId}", verificationId);
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "x-client-id", "15906645-897a-476a-9560-f1e2831724d1",
                                        "x-client-secret", "ieX7F4YI8UONKgtW63C4O67Xx1QASPXR",
                                        "x-product-instance-id", "575d3b78-ebf9-40a4-b8da-96b720dc5265",
                                        "Content-Type", "application/json"))
                        .body(SetuMockPaymentRequest.build("successful"))
                        .url(url)
                        .build();
        Response response = apiRequests.post(req);
        SetuMockPaymentResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SetuMockPaymentResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "Setu Mock Payment — status={}, verificationId={}",
                response.getStatusCode(),
                verificationId);
        return deserializedResponse;
    }



    public SipStatusResponse sipStatusScreen(String accessToken, String purchasePlanId) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "accept", "application/json, text/plain, */*",
                                        "Authorization", "Bearer " + accessToken,
                                        "os", "android",
                                        "x-requested-with", "com.sipse.app.staging"))
                        .queryParams(Map.of("id", purchasePlanId))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.SIP_STATUS))
                        .build();
        Response response = apiRequests.get(req);
        int statusCode = response.getStatusCode();
        if (statusCode >= 500) {
            // Gateway errors return HTML, not JSON — return a retryable empty response
            SipStatusResponse errorResponse = new SipStatusResponse();
            errorResponse.setStatusCode(statusCode);
            log.warn("SIP Status Screen — {} gateway error for purchasePlanId={}, will retry", statusCode, purchasePlanId);
            return errorResponse;
        }
        SipStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SipStatusResponse.class);
        deserializedResponse.setStatusCode(statusCode);
        log.info(
                "SIP Status Screen — status={}, purchasePlanId={}",
                statusCode,
                purchasePlanId);
        return deserializedResponse;
    }


    public HomeFeedMetaDataResponse HomefeedMetadataStatus(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "accept", "application/json, text/plain, */*",
                                        "Authorization", "Bearer " + accessToken,
                                        "os", "android"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.HOMEFEED_METADATA))
                        .build();
        Response response = apiRequests.get(req);
        HomeFeedMetaDataResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), HomeFeedMetaDataResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "SIP Status Screen — status={}",
                response.getStatusCode());
        return deserializedResponse;
    }
}
