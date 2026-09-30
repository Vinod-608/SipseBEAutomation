package org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankVerificationInitiateV2Response extends CommonResultModel {

    private boolean success;
    private VerificationData data;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerificationData {
        private String upiLink;
        private String validUpto;
        private String verificationId;
        private String bankVerificationStatus;
        private Object bankAccountDetailsDto;
    }
}
