package org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WithdrawlSubmitResponse extends CommonResultModel {

    private boolean success;
    private DataResponse data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataResponse {
        private String orderId;
        private String status;
        private String message;
    }
}
