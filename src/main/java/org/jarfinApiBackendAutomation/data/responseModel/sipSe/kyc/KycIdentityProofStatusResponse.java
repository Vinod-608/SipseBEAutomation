package org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KycIdentityProofStatusResponse extends CommonResultModel {

    private boolean success;
    private DataResponse data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataResponse {
        private String status;
        private String message;
        private String currentContext;
        private String action;

        @JsonCreator
        public static DataResponse fromString(String value) {
            DataResponse dr = new DataResponse();
            dr.setStatus(value);
            return dr;
        }
    }
}
