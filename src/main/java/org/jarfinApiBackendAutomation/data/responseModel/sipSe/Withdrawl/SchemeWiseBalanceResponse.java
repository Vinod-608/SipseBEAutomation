package org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchemeWiseBalanceResponse extends CommonResultModel {

    private boolean success;
    private DataResponse data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataResponse {
        private List<SchemeBalance> schemeWiseBalances;
        private String nextId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SchemeBalance {
        @JsonProperty("isManuallyCalculated")
        private boolean manuallyCalculated;
        private Double investedValue;
        private Double currentValue;
        private Double returns;
        private Double returnsPercentage;
        private Double xirr;
        private String fundSchemeId;
        private String fundSchemeName;
        private String fundSchemeLogoUrl;
    }
}
