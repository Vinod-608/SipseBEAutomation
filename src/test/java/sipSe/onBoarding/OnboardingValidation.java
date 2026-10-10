package sipSe.onBoarding;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.homeFeed.HomeFeedMetaDataResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.BankVerificationInitiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.BankVerificationInitiateV2Response;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.BankVerificationStatus;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PrefillKycDetailsResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.SubmitProfileResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.SetuMockPaymentResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.BankVerificationResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PanConfirmResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PanLookupResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PanPreVerificationStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PreVerificationContext;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.consent.SubmitUserConsentResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.BillDeskMandateMockResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.nominee.AddNomineeResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.nominee.NomineeStatus;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.SetupMandateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.VerifyMandateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.sip.SipStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.sip.SubmitSipResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.EmailOnboardingResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.OnboardingVideoResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.SispseUserDetailsRespose;
import org.jarfinApiBackendAutomation.utils.ApiAssertions;
import org.testng.asserts.SoftAssert;

import static org.apache.http.HttpStatus.*;

public class OnboardingValidation extends ApiAssertions {

    public OnboardingValidation(SoftAssert softAssert) {
        super(softAssert);
    }

    public void assertSubmitEmailSuccess(EmailOnboardingResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Submit Email")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true for valid email");
        softAssert.assertAll();
    }

    public void assertSubmitEmailFailure(EmailOnboardingResponse response, String scenario) {
        if (!assertStatusCode(response.getStatusCode(), SC_BAD_REQUEST, "Submit Email [" + scenario + "]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(
                response.isSuccess(), "[" + scenario + "] success must be false for invalid input");
        softAssert.assertAll();
    }

    public void assertOnboardingVideoAsset(OnboardingVideoResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Onboarding Video Asset")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        softAssert.assertNotNull(response.getData().getAssetUrl(), "assetUrl must not be null");
        softAssert.assertFalse(
                response.getData().getAssetUrl().isBlank(), "assetUrl must not be blank");
        softAssert.assertAll();
    }

    public void assertUserDetails(SispseUserDetailsRespose response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "User Details")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        SispseUserDetailsRespose.UserData data = response.getData();
        softAssert.assertNotNull(data.getId(), "id must not be null");
        softAssert.assertNotNull(data.getPhoneNumber(), "phoneNumber must not be null");
        softAssert.assertFalse(
                data.getPhoneNumber() == null || data.getPhoneNumber().isBlank(),
                "phoneNumber must not be blank");
        softAssert.assertAll();
    }

    public void assertPanLookupSuccess(PanLookupResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "PAN Lookup")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        PanLookupResponse.PanData data = response.getData();
        softAssert.assertNotNull(data.getStatus(), "status must not be null");
        if (data.getPan() != null) {
            softAssert.assertFalse(data.getPan().isBlank(), "pan must not be blank");
        }
        softAssert.assertAll();
    }

    public void assertPanLookupPending(PanLookupResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "PAN Lookup [LOOKUP_PENDING]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true for LOOKUP_PENDING");
        softAssert.assertAll();
    }

    public void assertPanLookupKycVerified(PanLookupResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "PAN Lookup [KYC_VERIFIED]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true for KYC_VERIFIED");
        softAssert.assertAll();
    }

    public void assertPanLookupEnterManually(PanLookupResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "PAN Lookup [ENTER_PAN_MANUALLY]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true for ENTER_PAN_MANUALLY");
        softAssert.assertAll();
    }

    public void assertPanLookupUnauthorized(PanLookupResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_UNAUTHORIZED, "PAN Lookup [Unauthorized]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid token");
        softAssert.assertAll();
    }

    public void assertPanConfirmSuccess(PanConfirmResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "PAN Confirm")) {
            softAssert.assertAll();
            return;
        }

        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        softAssert.assertAll();
    }

    public void assertPanConfirmUnauthorized(PanConfirmResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_UNAUTHORIZED, "PAN Confirm [Unauthorized]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid token");
        softAssert.assertAll();
    }

    public void assertPanStatusSuccess(PanPreVerificationStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "PAN Status")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        softAssert.assertAll();
    }

    public void assertPanStatusUnauthorized(PanPreVerificationStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_UNAUTHORIZED, "PAN Status [Unauthorized]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid token");
        softAssert.assertAll();
    }

    public void assertBankVerificationSuccess(BankVerificationResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Bank Verification Lookup")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        BankVerificationResponse.BankData data = response.getData();
        if (data != null) {
            softAssert.assertNotNull(data.getLookupStatus(), "lookupStatus must not be null");
        }
        softAssert.assertAll();
    }

    public void assertBankVerificationUnauthorized(BankVerificationResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_UNAUTHORIZED, "Bank Verification Lookup [Unauthorized]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid token");
        softAssert.assertAll();
    }

    public void assertBankVerificationInitiateSuccess(@MonotonicNonNull PanPreVerificationStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Bank Verification Initiate")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
    }

    public void assertKycPrefillDetailsSuccess(PrefillKycDetailsResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "KYC Prefill Details")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        softAssert.assertAll();
    }

    public void assertKycPrefillDetailsUnauthorized(PrefillKycDetailsResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_UNAUTHORIZED, "KYC Prefill Details [Unauthorized]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid token");
        softAssert.assertAll();
    }

    public void assertKycProfileSubmitSuccess(SubmitProfileResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "KYC Profile Submit")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertAll();
    }


    public void assertBankVerificationInitiateUnauthorized(
            @MonotonicNonNull PanPreVerificationStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_UNAUTHORIZED, "Bank Verification Initiate [Unauthorized]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid token");
        softAssert.assertAll();
    }






    public void assertBankPreVerificationStatusSuccess(PanPreVerificationStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Bank Pre-Verification Status")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        PanPreVerificationStatusResponse.DataResponse data = response.getData();
        if (data != null) {
            softAssert.assertNotNull(data.getCurrentContext(), "currentContext must not be null");
            softAssert.assertNotNull(data.getAction(), "action must not be null");
        }
        softAssert.assertAll();
    }

    public void assertBankPreVerificationStatusUnauthorized(PanPreVerificationStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_UNAUTHORIZED, "Bank Pre-Verification Status [Unauthorized]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid token");
        softAssert.assertAll();
    }

    public void assertSetupMandateSuccess(SetupMandateResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Mandate Setup")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        SetupMandateResponse.MandateData data = response.getData();
        if (data != null) {
            softAssert.assertNotNull(data.getMandateId(), "mandateId must not be null");
            if (data.isNewMandateCreated()) {
                softAssert.assertNotNull(data.getRedirectUrl(), "redirectUrl must not be null for new mandate");
                softAssert.assertFalse(
                        data.getRedirectUrl() == null || data.getRedirectUrl().isBlank(),
                        "redirectUrl must not be blank for new mandate");
            }
        }
        softAssert.assertAll();
    }

    public void assertSubmitConsentSuccess(SubmitUserConsentResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Submit Consent")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        SubmitUserConsentResponse.ConsentData data = response.getData();
        if (data != null) {
            softAssert.assertNotNull(data.getId(), "id must not be null");
            softAssert.assertNotNull(data.getConsentId(), "consentId must not be null");
            softAssert.assertTrue(data.isConsentGiven(), "consentGiven must be true");
            softAssert.assertTrue(data.getConsentGivenAt() != null && data.getConsentGivenAt() > 0,
                    "consentGivenAt must be a positive timestamp");
        }
        softAssert.assertAll();
    }

    public void assertSubmitConsentUnauthorized(SubmitUserConsentResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_INTERNAL_SERVER_ERROR, "Submit Consent [Unauthorized]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid token");
        softAssert.assertAll();
    }

    public void assertSubmitConsentBadRequest(SubmitUserConsentResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_BAD_REQUEST, "Submit Consent [Bad Request]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid input");
        softAssert.assertAll();
    }

    public void assertAddNomineeSuccess(AddNomineeResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Add Nominee")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        AddNomineeResponse.NomineeData data = response.getData();
        if (data != null) {
            softAssert.assertNotNull(data.getNomineeId(), "nomineeId must not be null");
            softAssert.assertFalse(
                    data.getNomineeId() == null || data.getNomineeId().isBlank(),
                    "nomineeId must not be blank");
            softAssert.assertNotNull(data.getStatus(), "status must not be null");
            softAssert.assertEquals(
                    data.getStatus(), NomineeStatus.ACTIVE, "nominee status must be ACTIVE");
        }
        softAssert.assertAll();
    }

    public void assertAddNomineeUnauthorized(AddNomineeResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_UNAUTHORIZED, "Add Nominee [Unauthorized]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid token");
        softAssert.assertAll();
    }

    public void assertAddNomineeBadRequest(AddNomineeResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_BAD_REQUEST, "Add Nominee [Bad Request]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid input");
        softAssert.assertAll();
    }

    public void assertVerifyMandateSuccess(VerifyMandateResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Mandate Verify")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        softAssert.assertAll();
    }

    public void assertVerifyMandateUnauthorized(VerifyMandateResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_UNAUTHORIZED, "Mandate Verify [Unauthorized]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid token");
        softAssert.assertAll();
    }

    public void assertSipSubmitSuccess(SubmitSipResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "SIP Submit")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertAll();
    }

    public void assertBillDeskMandateMockSuccess(BillDeskMandateMockResponse response) {
        assertStatusCode(response.getCallbackSimulatorStatus(), SC_OK, "Step 1: BillDesk callbackSimulator");
        assertStatusCode(response.getCallbackResponseStatus(), SC_OK, "Step 2: BillDesk callbackResponse");
        assertStatusCode(response.getProcessNpciRespStatus(), SC_OK, "Step 3: BillDesk processNpciResp");
        assertStatusCode(response.getFinprimBillDeskCallbackStatus(), SC_OK, "Step 4: Finprim BillDesk callback");
        assertStatusCode(response.getFinprimOndcCallbackStatus(), SC_OK, "Step 5: Finprim ONDC callback");
        softAssert.assertNotNull(response.getUniqueID(), "uniqueID must not be null after Step 1");
        softAssert.assertAll();
    }

    public void assertSetupMandateUnauthorized(SetupMandateResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_INTERNAL_SERVER_ERROR, "Mandate Setup [Unauthorized]")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertFalse(response.isSuccess(), "success must be false for invalid token");
        softAssert.assertAll();
    }

    public void assertPanConfirmBadRequest(PanConfirmResponse response) {
        assertStatusCode(response.getStatusCode(), SC_BAD_REQUEST, "PAN Confirm [Bad Request]");
        softAssert.assertAll();
    }

    public void assertSipStatusSuccess(SipStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "SIP Status Screen")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "success must be true");
        softAssert.assertNotNull(response.getData(), "data must not be null");
        SipStatusResponse.SipStatusData data = response.getData();
        if (data != null) {
            softAssert.assertNotNull(data.getPurchasePlanId(), "purchasePlanId must not be null");
            softAssert.assertNotNull(data.getSchemeId(), "schemeId must not be null");
            softAssert.assertNotNull(data.getSchemeName(), "schemeName must not be null");
            softAssert.assertNotNull(data.getStatus(), "status must not be null");
            softAssert.assertNotNull(data.getFrequency(), "frequency must not be null");
            softAssert.assertNotNull(data.getAmount(), "amount must not be null");
            softAssert.assertNotNull(data.getBankAccountNumber(), "bankAccountNumber must not be null");
        }
        softAssert.assertAll();
    }


    public void assertHomeFeedMetaData(HomeFeedMetaDataResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Home Feed MetaData")) {
            softAssert.assertAll();
            return;
        }

        softAssert.assertAll();
    }

}
