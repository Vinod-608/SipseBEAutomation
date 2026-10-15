package org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KycStatusResponse extends CommonResultModel {

    private boolean success;
    private DataResponse data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataResponse {
        private String kycStatus;
        private String status;
        private String message;
        private String currentContext;
        private boolean kycVerified;
    }
}
