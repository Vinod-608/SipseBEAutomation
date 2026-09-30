package org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginInitiateResponse extends CommonResultModel {
    private boolean success;
    private String message;
    private String errorMessage;
    private ResponseData data;

    public String getErrorDetail() {
        if (errorMessage != null && !errorMessage.isBlank()) return errorMessage;
        if (message != null && !message.isBlank()) return message;
        return "no error detail";
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseData {
        @JsonProperty("isOtpSent")
        private boolean otpSent;

        private Integer otpLength;
    }
}
