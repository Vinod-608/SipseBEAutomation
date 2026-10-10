package org.jarfinApiBackendAutomation.data.requestModel.lumSum;

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
public class LumpSumSubmitRequest {
    private String orderId;
    private UpiApp upiApp;

    public static LumpSumSubmitRequest build(String orderId, UpiApp upiApp) {
        return LumpSumSubmitRequest.builder()
                .orderId(orderId)
                .upiApp(upiApp)
                .build();
    }
}
