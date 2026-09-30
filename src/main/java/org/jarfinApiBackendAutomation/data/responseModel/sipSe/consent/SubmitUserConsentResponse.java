package org.jarfinApiBackendAutomation.data.responseModel.sipSe.consent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitUserConsentResponse extends CommonResultModel {
    private boolean success;
    private ConsentData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConsentData {
        private String id;
        private String consentId;
        private boolean consentGiven;
        private Long consentGivenAt;
    }
}
