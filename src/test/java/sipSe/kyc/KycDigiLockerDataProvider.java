package sipSe.kyc;

import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.KycDigiLockerPreVerificationResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.KycEsignStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.KycStatusResponse;

import java.util.function.Predicate;

public class KycDigiLockerDataProvider {

    public static final int DEFAULT_MAX_RETRIES = 10;
    public static final long DEFAULT_DELAY_MS = 3000;

    // Longer poll for pre-verification: backend may take 60-120s to process profile+consent
    public static final int PRE_VERIFICATION_MAX_RETRIES = 30;
    public static final long PRE_VERIFICATION_DELAY_MS = 5000;

    public static final String IDENTITY_DOC_POSTBACK_URL = "https://ui-static.sipse.in/onboarding/digilocker-callback";
    public static final String ESIGN_POSTBACK_URL = "https://ui-static.sipse.in/onboarding/esign-status";

    public static final Predicate<KycDigiLockerPreVerificationResponse> PRE_VERIFICATION_SHOULD_RETRY =
            response -> {
                if (response.getStatusCode() >= 500) return true;
                KycDigiLockerPreVerificationResponse.DataResponse data = response.getData();
                if (data == null) return true;
                if ("SUBMIT_KYC_REQUEST".equalsIgnoreCase(data.getAction())) return false;
                return data.getProviderIdentityDocId() == null;
            };

    public static final Predicate<KycEsignStatusResponse> ESIGN_STATUS_SHOULD_RETRY =
            response -> {
                if (response.getStatusCode() >= 500) return true;
                KycEsignStatusResponse.DataResponse data = response.getData();
                if (data == null || data.getStatus() == null) return true;
                String status = data.getStatus();
                return "PENDING".equalsIgnoreCase(status)
                        || "INITIATED".equalsIgnoreCase(status)
                        || "IN_PROGRESS".equalsIgnoreCase(status)
                        || "PROCESSING".equalsIgnoreCase(status);
            };

    public static final Predicate<KycStatusResponse> KYC_STATUS_SHOULD_RETRY =
            response -> {
                if (response.getStatusCode() >= 500) return true;
                KycStatusResponse.DataResponse data = response.getData();
                if (data == null) return true;
                String status = data.getKycStatus() != null ? data.getKycStatus() : data.getStatus();
                if (status == null) return true;
                return "PENDING".equalsIgnoreCase(status)
                        || "IN_PROGRESS".equalsIgnoreCase(status)
                        || "PROCESSING".equalsIgnoreCase(status);
            };
}
