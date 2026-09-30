package org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyMandateResponse extends CommonResultModel {
    private boolean success;
    private MandateVerifyData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MandateVerifyData {
        private String purchasePlanId;
        private boolean isOTPSent;
        private Integer otpLength;
        private String mandateStatus;
        private Integer amount;
    }
}
