package org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LumpSumIntiateResponse extends CommonResultModel {

    private boolean success;
    private LumSumIntiateData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LumSumIntiateData {
        private String orderId;
        private int otpLength;
    }
}

