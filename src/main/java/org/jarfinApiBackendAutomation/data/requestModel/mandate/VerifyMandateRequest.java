package org.jarfinApiBackendAutomation.data.requestModel.mandate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyMandateRequest {
    private String schemeId;
    private String mandateId;
    private String frequency;
    private String purchasePlanId;

    public static VerifyMandateRequest build(String schemeId, String mandateId, String purchasePlanId) {
        return VerifyMandateRequest.builder()
                .schemeId(schemeId)
                .mandateId(mandateId)
                .frequency("DAILY")
                .purchasePlanId(purchasePlanId)
                .build();
    }
}
