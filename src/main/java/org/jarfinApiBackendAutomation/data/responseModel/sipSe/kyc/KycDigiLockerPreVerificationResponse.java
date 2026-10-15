package org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KycDigiLockerPreVerificationResponse extends CommonResultModel {

    private boolean success;
    private DataResponse data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataResponse {
        private String currentContext;
        private String action;
        private String providerIdentityDocId;
        private String redirectUrl;
        private boolean kycVerified;
        private String message;
        private String providerStatus;
    }
}
