package org.jarfinApiBackendAutomation.data.requestModel.mandate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.UpiApp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SetupMandateRequest {
    private int amount;
    private String schemeId;
    private UpiApp upiApp;
    private String userBankAccountId;
    private String frequency;
    private String setupType;

    public static SetupMandateRequest build(int amount, String schemeId, UpiApp upiApp) {
        return SetupMandateRequest.builder()
                .amount(amount)
                .schemeId(schemeId)
                .upiApp(upiApp)
                .frequency("DAILY")
                .setupType("SETUP")
                .build();
    }
}
