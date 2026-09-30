package org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnboardingVideoResponse extends CommonResultModel {
    private boolean success;
    private ResponseData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseData {
        private String assetUrl;
        private String assetType;
    }
}
