package sipSe.kyc;

import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.*;
import org.jarfinApiBackendAutomation.utils.ApiAssertions;
import org.testng.asserts.SoftAssert;

import static org.apache.http.HttpStatus.SC_OK;

public class KycDigiLockerValidation extends ApiAssertions {

    public KycDigiLockerValidation(SoftAssert softAssert) {
        super(softAssert);
    }

    public void assertPreVerificationStatusSuccess(KycDigiLockerPreVerificationResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "KYC PreVerification Status")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "KYC PreVerification — expected success=true");
        softAssert.assertNotNull(response.getData(), "KYC PreVerification — data must not be null");
    }

    public void assertIdentityProofInitiateSuccess(KycIdentityProofInitiateResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "KYC Identity Proof Initiate")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "KYC Identity Proof Initiate — expected success=true");
        softAssert.assertNotNull(response.getData(), "KYC Identity Proof Initiate — data must not be null");
    }

    public void assertIdentityProofStatusSuccess(KycIdentityProofStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "KYC Identity Proof Status")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "KYC Identity Proof — expected success=true");
        softAssert.assertNotNull(response.getData(), "KYC Identity Proof — data must not be null");
    }

    public void assertPrefillDetailsSuccess(PrefillKycDetailsResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "KYC Prefill Details")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "KYC Prefill Details — expected success=true");
        softAssert.assertNotNull(response.getData(), "KYC Prefill Details — data must not be null");
    }

    public void assertPreSignedUrlSuccess(KycPreSignedUrlResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "KYC PreSigned URL")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "KYC PreSigned URL — expected success=true");
        softAssert.assertNotNull(response.getData(), "KYC PreSigned URL — data must not be null");
    }

    public void assertSignatureSubmitSuccess(KycSignatureSubmitResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "KYC Signature Submit")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "KYC Signature Submit — expected success=true");
    }

    public void assertEsignInitiateSuccess(KycEsignInitiateResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "KYC eSign Initiate")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "KYC eSign Initiate — expected success=true");
        softAssert.assertNotNull(response.getData(), "KYC eSign Initiate — data must not be null");
        if (response.getData() != null) {
            softAssert.assertNotNull(
                    response.getData().getProviderEsignId(),
                    "KYC eSign Initiate — providerEsignId must not be null");
        }
    }

    public void assertEsignStatusSuccess(KycEsignStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "KYC eSign Status")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "KYC eSign Status — expected success=true");
        softAssert.assertNotNull(response.getData(), "KYC eSign Status — data must not be null");
    }

    public void assertKycStatusSuccess(KycStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "KYC Status")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "KYC Status — expected success=true");
        softAssert.assertNotNull(response.getData(), "KYC Status — data must not be null");
    }
}
