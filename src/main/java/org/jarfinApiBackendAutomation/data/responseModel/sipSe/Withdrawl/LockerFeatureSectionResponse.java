package org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockerFeatureSectionResponse extends CommonResultModel {

    private boolean success;
    private DataResponse data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataResponse {
        private String featureSection;
        private LockerFeatureSectionDetails lockerFeatureSectionDetails;
        private Object lockerSipOrderSectionDetails;
        private Object sipOrderFeatureSectionDetails;
        private Object retryFlowFeatureSectionDetails;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LockerFeatureSectionDetails {
        @JsonProperty("isManuallyCalculated")
        private boolean manuallyCalculated;
        private Double investedValue;
        private Double currentValue;
        private Double returnsPercentage;
        private Double returns;
    }
}