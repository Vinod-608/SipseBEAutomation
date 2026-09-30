package org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginVerifyResponse extends CommonResultModel {

    private boolean success;
    private String message;
    private ResponseData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseData {
        private String accessToken;
        private String refreshToken;
        private boolean newUser;
    }
}
