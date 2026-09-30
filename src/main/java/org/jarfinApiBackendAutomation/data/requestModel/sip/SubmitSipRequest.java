package org.jarfinApiBackendAutomation.data.requestModel.sip;

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
public class SubmitSipRequest {
    private String otp;
    private String purchasePlanId;
    private boolean isNewPurchasePlan;

    public static SubmitSipRequest build(String otp, String purchasePlanId, boolean isNewPurchasePlan) {
        return SubmitSipRequest.builder()
                .otp(otp)
                .purchasePlanId(purchasePlanId)
                .isNewPurchasePlan(isNewPurchasePlan)
                .build();
    }
}
