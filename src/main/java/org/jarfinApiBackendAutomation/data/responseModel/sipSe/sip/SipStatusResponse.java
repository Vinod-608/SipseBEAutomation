package org.jarfinApiBackendAutomation.data.responseModel.sipSe.sip;

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
public class SipStatusResponse extends CommonResultModel {
    private boolean success;
    private SipStatusData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SipStatusData {
        private String purchasePlanId;
        private String schemeId;
        private String schemeName;
        private String schemeLogoUrl;
        private String schemeType;
        private String frequency;
        private Double amount;
        private String folioNumber;
        private Double investedValue;
        private Double currentValue;
        private Double availableUnits;
        private Double returns;
        private Double returnsPercentage;
        private PaymentStatus status;
        private Long startedAt;
        private String bankAccountNumber;
        private String ifscCode;
        private PaymentStatus action;

    }
}
