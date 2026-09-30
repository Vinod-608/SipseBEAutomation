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

    public BillDeskMandateMockResponse billdeskMandateMockPayment() {
        BillDeskMandateMockResponse result = new BillDeskMandateMockResponse();

        // Step 1 — callbackSimulator: submit the mandate request XML to get the uniqueID form
        String step1FormBody =
                "MandateReqDoc=%3C%3Fxml+version%3D%221.0%22+encoding%3D%22UTF-8%22%3F%3E%3CDocument+xmlns%3D%22http%3A%2F%2Fnpci.org%2Fonmags%2Fschema%22%3E%3CMndtAuthReq%3E%3CGrpHdr%3E%3CMsgId%3EMA0638010122991785242846482lnEryw%3C%2FMsgId%3E%3CCreDtTm%3E2026-07-28T18%3A17%3A26%3C%2FCreDtTm%3E%3CReqInitPty%3E%3CInfo%3E%3CId%3ECITI00008000001138%3C%2FId%3E%3CCatCode%3ESMBP%3C%2FCatCode%3E%3CUtilCode%3ECITI00008000001138%3C%2FUtilCode%3E%3CCatDesc%3EA+test+Onmags+category%3C%2FCatDesc%3E%3CName%3EBDUATV2K40%3C%2FName%3E%3CSpn_Bnk_Nm%3EYES+Bank+SI%3C%2FSpn_Bnk_Nm%3E%3C%2FInfo%3E%3C%2FReqInitPty%3E%3C%2FGrpHdr%3E%3CMndt%3E%3CMndtReqId%3EMA0638010122991785242846482lnEryw%3C%2FMndtReqId%3E%3CMndt_Type%3EDEBIT%3C%2FMndt_Type%3E%3COcrncs%3E%3CSeqTp%3ERCUR%3C%2FSeqTp%3E%3CFrqcy%3EADHO%3C%2FFrqcy%3E%3CFrstColltnDt%3EU1t9ahgqOm5NOp1g8JxsYYPhGD5JJQgMTS%2FSWcLtudMGz73Tx91OAQ%2B0LBtZ4JyUWsHehxYCVJtcUChmbmVl1FKs6MxwGcibPJRFC0La4wvK7Gxuvo5hbcz972fNr8dxP5zlttyHuXmsbufX2aaU4wb9vLgE400gu%2BQlYw4jGkxNbXtcf1QOhFmlhTCO%2BZ3JkkHoFmYynh0NvwKzXxAhxKOQW4DbHhNa%2FkI3dU0DX7DNqEP03ux%2F6pNUN6ErUwXB1altePar05l5cOacjW63sFCgDzRboiHEN3Y8ySv2wpXdp1Y%2BTcreONojufckhVIi2cNAR8bKm1DFuRHv6rUAwA%3D%3D%3C%2FFrstColltnDt%3E%3CFnlColltnDt%3EhBXJggXc1lRntQz7Rkp0SNYq5HhWN5yTDFkSXJ%2BiKkWW7UfPleZACbWkS5iUmSHx%2BOMdcPzlDJ3cCC3UgkiCaI%2B9V%2BBFu6g4aUBBuZ%2FOVDMdsJJM0wGYLMgJydEAisiqOHgvjnAUn%2BgmaHyJJkn68M%2B7Xvn6lrkqh0deZwbBp5VxYrgK1zRZYGowIzbpGHHd4I8OsZ3Mn0S18HV%2Bov%2FYB0YGko8CVWzEqxVh6edkof3T%2FTaDfuj1EVrtdfXisFuRRf4i3NXAtGzTaMXr%2BQHhOr%2FXaGzcStOUpEy7hEfELJ%2FS7O%2FELAWHfg2FU5aa5SAbEN%2FukmWjczU5sbMWKz1Znw%3D%3D%3C%2FFnlColltnDt%3E%3C%2FOcrncs%3E%3CMaxAmt+Ccy%3D%22INR%22%3EG9tk5JH%2FTt0Q%2F7G1vJNnl5MNPCCx1Z%2BWZ8TQAfcRQliGFy29amekvZW6XmI9HBVq%2FcBAIThNc8LO5Kkx6tDK2RaJSgOBCW7PMATaua4pVkiF%2BsCPQ9uUIyOJzM1xWh%2FEEp%2Fp1dlh6dp1DJ9DBXB4W07cfsrvFvihKxhN9LtaWI%2BXqbRBDG9EW7MVrV5%2FAy%2FJH5AGbYfQZfyK%2F%2FfPuZUQm9fam5XtpdRmJizRRhiZNN3fdzsnYjdOobnfcT5mthGfs2SfDNkMEinOa%2FDTXkrpjxYjyd7HLybxJxhV2%2FBhiAo0vRmpRRF6PeKpRhS98qwgVow%2F0VWGUU1hkUkyDQXEGw%3D%3D%3C%2FMaxAmt%3E%3CDbtr%3E%3CNm%3EMohan+Mock%3C%2FNm%3E%3CAccNo%3EL7hlX5zsdrWa5FQkdrR97M3KjXZqw%2FhcqdG9y67wy5AuFEzTkLoGoY0v3ly65x5rgOpaila472NjYx3QkdWgFQidqjN5ot7wOYFnZdA7cMxZHh40gOd090%2BGP54AI2EW32U%2FQ6i7a33PfTm5lWazwQwKmr8xcQGHfeF5UHsO82VdE49qMmIQv7lW%2BSxkVJw2R5t66csdHjKt53aNT5noubI59fzLylC2brqe5btZMxgFu3g4%2Fyu3GiQi%2BN5f9m8ey5W6xeAZl5fuqXn6UDKR9sVL16js85dvC0pbsr8oHvSvsaiSooQ8HjyXxCLf8pfXhCV%2FASGbGqGx7Ee9hZWcNQ%3D%3D%3C%2FAccNo%3E%3CAcct_Type%3ESAVINGS%3C%2FAcct_Type%3E%3CCons_Ref_No%3Ecybri_17695%3C%2FCons_Ref_No%3E%3C%2FDbtr%3E%3CCrAccDtl%3E%3CNm%3EBDUATV2K40%3C%2FNm%3E%3CAccNo%3ECITI00008000001138%3C%2FAccNo%3E%3CMmbId%3EYESB0000001%3C%2FMmbId%3E%3C%2FCrAccDtl%3E%3C%2FMndt%3E%3C%2FMndtAuthReq%3E%3CSignature+xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2F09%2Fxmldsig%23%22%3E%3CSignedInfo%3E%3CCanonicalizationMethod+Algorithm%3D%22http%3A%2F%2Fwww.w3.org%2FTR%2F2001%2FREC-xml-c14n-20010315%22%2F%3E%3CSignatureMethod+Algorithm%3D%22http%3A%2F%2Fwww.w3.org%2F2001%2F04%2Fxmldsig-more%23rsa-sha256%22%2F%3E%3CReference+URI%3D%22%22%3E%3CTransforms%3E%3CTransform+Algorithm%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2F09%2Fxmldsig%23enveloped-signature%22%2F%3E%3C%2FTransforms%3E%3CDigestMethod+Algorithm%3D%22http%3A%2F%2Fwww.w3.org%2F2001%2F04%2Fxmlenc%23sha256%22%2F%3E%3CDigestValue%3EfUVPpGOYOAA18UsfY1HRiI5foMEOBtYWOacFY35adXw%3D%3C%2FDigestValue%3E%3C%2FReference%3E%3C%2FSignedInfo%3E%3CSignatureValue%3Eweaso3xbrswp%2FMojhi%2FTZPwDqNKE3xaMTeGDMCcOPEb%2FPHR3WTdsflzE4Urrs0Ncw2QlJjBw2Lr6%26%2313%3B3lt2KMmuyxgcmcE8HeceLySm3DANrXv%2FhoI9YuM1GOc4CEZibXDOOJaKa7pVyJBLQRUsujF9Y9IZ%26%2313%3Bc%2Bms1QKRqJmG4q41zNCOBJYeL8u6yFksjTpY7OQfvm8cJocRguyDiDVfe39hCKT6abZi8kv6u6Bv%26%2313%3BXNfO0wtHgVGqrO9%2B3OlcMJTayTfsqRGPlMOCULSM5uTMP%2F3xUqb248uUUbcWZcxpf989btPJo%2BLI%26%2313%3BckUm7HCak6fz9aE8gnF%2BGSIYoyXNAlfPuNgE4w%3D%3D%3C%2FSignatureValue%3E%3CKeyInfo%3E%3CX509Data%3E%3CX509SubjectName%3ECN%3DAPIEMandate%3C%2FX509SubjectName%3E%3CX509Certificate%3EMIICrTCCAZWgAwIBAgIJAMvAkWVdDUl%2FMA0GCSqGSIb3DQEBBQUAMBYxFDASBgNVBAMTC0FQSUVN%26%2313%3BYW5kYXRlMB4XDTE4MDcwNTEyMTg0N1oXDTIxMDMzMTEyMTg0N1owFjEUMBIGA1UEAxMLQVBJRU1h%26%2313%3BbmRhdGUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQD19uluioI%2FY05Si%2FY0PtlZdaN7%26%2313%3B4FmBKHxJKfIJWTobikPCx0L54quvHzMdV3mgoDvuVPgTP0JaoBH9CsBvRtXRC9DcqRQF39YD6I14%26%2313%3BZwd4%2BpYrnq81yodBluN1mfvMR%2FL6ArMYlCS63xI31dN4hY5Xf1uf74Av18sS%2F5BEBJkmzDs0ckPI%26%2313%3BXL0%2BQe4VkyjZ%2B9166QcBp1T%2FFWy85g6UaiHLHWyNZxGT6u6rMYG%2B9vGh9JegqadUs506Fh14oq5d%26%2313%3BpMVjN1c1MJWHYppdn4UzblfYXdQsM2SXFjUkeQg1%2BMVY1HySGA8esC2jLm8Z%2BeqH4GxrJH6aiiVo%26%2313%3B3Wc7%2BNmwd3HxAgMBAAEwDQYJKoZIhvcNAQEFBQADggEBAGNarKWMl%2F8BW6X5LGoMI73IkrQRv6OL%26%2313%3BPHRjJqsJxI3n2x1nmFHh6LMVM1zJidSNK%2FpTcxcBzT7n0%2BuXci7eSbuVRE207BLUPTvaPTAG2%2FdJ%26%2313%3B6X%2F3hGYGbCUfJDY9SyGpB%2BDWeYgEjs9mKH4IVy6JWuxo9i4nmJlGOQ2zcSgLG%2Bl%2BdJSe%2BgsOHGxa%26%2313%3Bh85HpEl6NCKrs3DsX5HSBsUF%2F5UQbfLXJ1gPgc6oabQOLk2KjK2Ry1oJIwTBUgNkXx6pALIqUXtC%26%2313%3BgBZ%2FKgzJX1vh4xD%2FNwTXPTNxDBC2%2B1514AMZo%2BefHa%2FBC6tmL5OI7%2FvFBLZKtFk%2BLIM2tysSGelx%26%2313%3BVlWYgac%3D%3C%2FX509Certificate%3E%3C%2FX509Data%3E%3C%2FKeyInfo%3E%3C%2FSignature%3E%3C%2FDocument%3E"
                + "&MerchantID=CITI00008000001138"
                + "&CheckSumVal=vcKYhXmR%2FyA6LV348tImjJRm5QflAmIpz%2BUx5BNXdXe9d015XiA%2B3B%2BL%2BXj1JgUM0XC3NQ4FoYSOTa8NPhvk%2FIjOTSK3P822fTHMofWmZOKUsLV%2Fo3tZ6KfsM1tr7RMHApwbkMieG81MPsjCwtSuw%2FGijMVKRfgF95BK02gWovlFm%2B5KgsSFdEeHPP8Pxs%2FGVDtYieS3PVE6usSeN%2B%2FabIul67lXOJIiXFPiJHPTEAYTWu9e4I92unJ6L%2FKklR2Du3uUxKFutDT5xhF6Vy1wVDAN2aNiBq8i%2F1JUiTnwSWq52EthO%2B9VvzK0uRKXVKwMdOOIeALaxpwJIuMXx3uwvg%3D%3D"
                + "&BankID=YESB"
                + "&SPID=CITI00008000001138_17"
                + "&AuthMode=NETBANKING"
                + "&uniqueID=MA0638010122991785242846482lnEryw";

        Response step1 = apiRequests.post(RestRequest.builder()
                .headers(Map.of(
                        "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                        "origin", "https://cybrillarta.s.finprim.com",
                        "user-agent", "Mozilla/5.0 (Linux; Android 16; CPH2619 Build/BP2A.250605.015; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/150.0.7871.124 Mobile Safari/537.36",
                        "x-requested-with", "com.sipse.app.staging"))
                .rawFormBody(step1FormBody)
                .url(SipSeEndPoints.BILLDESK_ENACH_CALLBACK_SIMULATOR)
                .build());
        result.setCallbackSimulatorStatus(step1.getStatusCode());
        result.setStatusCode(step1.getStatusCode());
        result.setResponseBody(step1.getBody().asString());

        // Extract uniqueID from hidden input in the returned HTML form
        String step1Body = step1.getBody().asString();
        String uniqueID = extractHiddenInputValue(step1Body, "uniqueID");
        if (uniqueID == null || uniqueID.isBlank()) {
            uniqueID = "MA0638010122991785242846482lnEryw";
            log.warn("uniqueID not found in Step 1 HTML — using fallback: {}", uniqueID);
        } else {
            log.info("Step 1 — extracted uniqueID={}", uniqueID);
        }
        result.setUniqueID(uniqueID);

        // Step 2 — callbackResponse: select "Success" for the extracted uniqueID
        String step2FormBody = "uniqueID=" + uniqueID + "&status=Success";
        Response step2 = apiRequests.post(RestRequest.builder()
                .headers(Map.of("Content-Type", "application/x-www-form-urlencoded"))
                .rawFormBody(step2FormBody)
                .url(SipSeEndPoints.BILLDESK_ENACH_CALLBACK_RESPONSE)
                .build());
        result.setCallbackResponseStatus(step2.getStatusCode());
        log.info("Step 2 callbackResponse — status={}", step2.getStatusCode());

        // Extract MandateRespDoc from Step 2 response (hidden input or form field)
        String step2Body = step2.getBody().asString();
        log.info("Step 2 response body (first 500 chars): {}", step2Body.length() > 500 ? step2Body.substring(0, 500) : step2Body);
        String mandateRespDoc = extractHiddenInputValue(step2Body, "MandateRespDoc");
        if (mandateRespDoc == null || mandateRespDoc.isBlank()) {
            log.warn("MandateRespDoc not found in Step 2 HTML — full body length={}", step2Body.length());
            mandateRespDoc = "";
        } else {
            log.info("Step 2 — extracted MandateRespDoc (length={})", mandateRespDoc.length());
        }
        result.setMandateRespDoc(mandateRespDoc);

        // HTML-decode the value extracted from the HTML attribute before URL-encoding
        String mandateRespDocDecoded = unescapeHtml(mandateRespDoc);
        log.info("Step 2 — MandateRespDoc decoded length={}", mandateRespDocDecoded.length());

        // Step 3 — processnpciresp: forward the mandate response XML to BillDesk NPCI processor
        String step3FormBody = "MandateRespDoc=" + java.net.URLEncoder.encode(mandateRespDocDecoded, java.nio.charset.StandardCharsets.UTF_8)
                + "&respType=RespXML";
        Response step3 = apiRequests.post(RestRequest.builder()
                .headers(Map.of("Content-Type", "application/x-www-form-urlencoded"))
                .rawFormBody(step3FormBody)
                .url(SipSeEndPoints.BILLDESK_MANDATE_PROCESS_NPCI_RESP)
                .build());
        result.setProcessNpciRespStatus(step3.getStatusCode());
        log.info("Step 3 processNpciResp — status={}", step3.getStatusCode());

        // Extract mandate_response token from Step 3 for the finprim callback
        String step3Body = step3.getBody().asString();
        log.info("Step 3 response body (full, length={}): {}", step3Body.length(), step3Body.length() > 1000 ? step3Body.substring(0, 1000) : step3Body);
        String mandateResponse = extractHiddenInputValue(step3Body, "mandate_response");
        if (mandateResponse == null || mandateResponse.isBlank()) {
            log.warn("mandate_response not found in Step 3 HTML — body: {}", step3Body);
            mandateResponse = "";
        } else {
            log.info("Step 3 — extracted mandate_response (length={})", mandateResponse.length());
        }
        result.setMandateResponse(mandateResponse);

        // HTML-decode before URL-encoding
        String mandateResponseDecoded = unescapeHtml(mandateResponse);
        log.info("Step 3 — mandate_response decoded length={}", mandateResponseDecoded.length());

        // Step 4 — finprim BillDesk callback: deliver the mandate token to finprim
        String step4FormBody = "mandate_response=" + java.net.URLEncoder.encode(mandateResponseDecoded, java.nio.charset.StandardCharsets.UTF_8)
                + "&mandate_tokenid=";
        Response step4 = apiRequests.post(RestRequest.builder()
                .headers(Map.of("Content-Type", "application/x-www-form-urlencoded"))
                .rawFormBody(step4FormBody)
                .url(SipSeEndPoints.FINPRIM_BILLDESK_CALLBACK)
                .build());
        result.setFinprimBillDeskCallbackStatus(step4.getStatusCode());
        log.info("Step 4 finprimBillDeskCallback — status={}", step4.getStatusCode());

        // Step 5 — finprim ONDC callback: notify ONDC of the successful mandate
        String step5FormBody = "status=success"
                + "&paymentId=2030"
                + "&failureReason=Mandate+Successful"
                + "&failureCode="
                + "&hash=8658976cb80ab6e1a1714792d892048c21498f4844f30e700388ab7c35b6c703";
        Response step5 = apiRequests.post(RestRequest.builder()
                .headers(Map.of("Content-Type", "application/x-www-form-urlencoded"))
                .rawFormBody(step5FormBody)
                .url(SipSeEndPoints.FINPRIM_ONDC_CALLBACK)
                .build());
        result.setFinprimOndcCallbackStatus(step5.getStatusCode());
        log.info("Step 5 finprimOndcCallback — status={}", step5.getStatusCode());

        log.info("BillDesk Mandate Mock Payment complete — steps: simulator={}, callbackResponse={}, npci={}, finprimBd={}, finprimOndc={}",
                result.getCallbackSimulatorStatus(),
                result.getCallbackResponseStatus(),
                result.getProcessNpciRespStatus(),
                result.getFinprimBillDeskCallbackStatus(),
                result.getFinprimOndcCallbackStatus());
        return result;
    }

    private String unescapeHtml(String s) {
        if (s == null || s.isBlank()) return s == null ? "" : s;
        // Replace named entities first, then strip numeric character references
        String result = s.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
        // Replace decimal numeric references like &#13; &#10;
        java.util.regex.Matcher dm = java.util.regex.Pattern.compile("&#(\\d+);").matcher(result);
        StringBuffer sbDec = new StringBuffer();
        while (dm.find()) {
            dm.appendReplacement(sbDec, String.valueOf((char) Integer.parseInt(dm.group(1))));
        }
        dm.appendTail(sbDec);
        result = sbDec.toString();
        // Replace hex numeric references like &#xD; &#x0A;
        java.util.regex.Matcher hm = java.util.regex.Pattern.compile("&#x([0-9a-fA-F]+);").matcher(result);
        StringBuffer sbHex = new StringBuffer();
        while (hm.find()) {
            hm.appendReplacement(sbHex, String.valueOf((char) Integer.parseInt(hm.group(1), 16)));
        }
        hm.appendTail(sbHex);
        return sbHex.toString();
    }

    private String extractHiddenInputValue(String html, String fieldName) {
        if (html == null || html.isBlank()) return null;
        int flags = java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL;
        String quotedName = java.util.regex.Pattern.quote(fieldName);
        // name before value
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "<input[^>]+name=[\"']" + quotedName + "[\"'][^>]+value=[\"']([^\"']*)[\"']", flags);
        java.util.regex.Matcher m = p.matcher(html);
        if (m.find()) return m.group(1);
        // value before name
        p = java.util.regex.Pattern.compile(
                "<input[^>]+value=[\"']([^\"']*)[\"'][^>]+name=[\"']" + quotedName + "[\"']", flags);
        m = p.matcher(html);
        if (m.find()) return m.group(1);
        // textarea fallback: <textarea name="fieldName">value</textarea>
        p = java.util.regex.Pattern.compile(
                "<textarea[^>]+name=[\"']" + quotedName + "[\"'][^>]*>(.*?)</textarea>", flags);
        m = p.matcher(html);
        if (m.find()) return m.group(1).trim();
        return null;
    }

    public String fetchSipConsentOtpFromDb(String purchasePlanId) {
        int maxRetries = 10;
        int delayMs = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            Document doc = DataBaseFactory.jarFinMongo()
                    .fetchDataMultiFilter(
                            DB_JARFIN,
                            OTP_DELIVERY_REPORTS_COLLECTION,
                            Map.of(
                                    SOURCE_REF_ID_FIELD, purchasePlanId,
                                    OTP_TYPE_FIELD, DAILY_SIP_SETUP_CONSENT_OTP_TYPE),
                            SORT_FIELD);

            if (doc == null) {
                log.warn("Attempt {}/{}: No SIP consent OTP document found for purchasePlanId={}",
                        attempt, maxRetries, purchasePlanId);
                continue;
            }

            String encryptedOtp = doc.getString(SIPSE_OTP_FIELD);
            if (encryptedOtp != null && !encryptedOtp.isBlank()) {
                log.info("Fetched SIP consent encrypted OTP for purchasePlanId={} on attempt {}",
                        purchasePlanId, attempt);
                return encryptedOtp;
            }
        }

        log.error("Failed to fetch SIP consent OTP from DB for purchasePlanId={} after {} attempts",
                purchasePlanId, maxRetries);
        return null;
    }

    public SubmitSipResponse sipSubmit(String accessToken, String purchasePlanId, boolean isNewPurchasePlan) {
        String encryptedOtp = fetchSipConsentOtpFromDb(purchasePlanId);

        SipSeAuthMethods authMethods = new SipSeAuthMethods();
        String plainOtp = authMethods.decryptOtp(encryptedOtp).getData();
        log.info("SIP Submit — decrypted OTP={} for purchasePlanId={}", plainOtp, purchasePlanId);

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
                                        "os", "android"))
                        .queryParams(Map.of("id", purchasePlanId))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.SIP_STATUS))
                        .build();
        Response response = apiRequests.get(req);
        SipStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SipStatusResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "SIP Status Screen — status={}, purchasePlanId={}",
                response.getStatusCode(),
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
