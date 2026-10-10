package org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LumpSumSubmitResponse extends CommonResultModel {
    private boolean success;
    private String message;
    private LumpSumSubmitData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LumpSumSubmitData {
        private String orderId;
        private String status;
        private String paymentIntentUrl;
    }
}
