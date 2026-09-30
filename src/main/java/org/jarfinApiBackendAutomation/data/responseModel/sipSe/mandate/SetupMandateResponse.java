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
public class SetupMandateResponse extends CommonResultModel {
    private boolean success;
    private MandateData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MandateData {
        private String mandateId;
        private String redirectUrl;
        private boolean isNewMandateCreated;
        private boolean isOTPSent;
        private Integer otpLength;
        private String purchasePlanId;
    }
}
