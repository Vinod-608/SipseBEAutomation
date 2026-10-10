package org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PreRedemptionSummaryResponse extends CommonResultModel {

    private boolean success;
    private DataResponse data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataResponse {
        private String fundSchemeId;
        private String userFolioId;
        private String schemeName;
        private String folioNumber;
        private Double redeemableAmount;
        private Double minRedeemableAmount;
    }
}
