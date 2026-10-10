package org.jarfinApiBackendAutomation.data.requestModel.lumSum;

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
public class LumpSumVerifyRequest {
    private String orderId;
    private String otp;

    public static LumpSumVerifyRequest build(String orderId, String otp) {
        return LumpSumVerifyRequest.builder()
                .orderId(orderId)
                .otp(otp)
                .build();
    }
}
