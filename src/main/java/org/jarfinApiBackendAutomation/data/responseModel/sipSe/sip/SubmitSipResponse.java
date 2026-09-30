package org.jarfinApiBackendAutomation.data.responseModel.sipSe.sip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitSipResponse extends CommonResultModel {
    private boolean success;
    private String message;
    private SipSubmitData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SipSubmitData {
        private String purchasePlanId;
        private String status;
    }
}
