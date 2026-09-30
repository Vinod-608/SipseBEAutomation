package org.jarfinApiBackendAutomation.data.responseModel.sipSe.nominee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddNomineeResponse extends CommonResultModel {
    private boolean success;
    private NomineeData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NomineeData {
        private String nomineeId;
        private NomineeStatus status;
    }
}
