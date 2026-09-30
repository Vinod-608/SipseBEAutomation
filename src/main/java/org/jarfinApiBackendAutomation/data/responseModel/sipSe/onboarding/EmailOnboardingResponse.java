package org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailOnboardingResponse extends CommonResultModel {

    private boolean success;
    private String message;
    private Object data;
}
