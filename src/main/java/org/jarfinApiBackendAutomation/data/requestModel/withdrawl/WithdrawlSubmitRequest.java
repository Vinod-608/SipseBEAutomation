package org.jarfinApiBackendAutomation.data.requestModel.withdrawl;

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
public class WithdrawlSubmitRequest {

    private String orderId;
    private String otp;

    public static WithdrawlSubmitRequest build(String orderId, String otp) {
        return WithdrawlSubmitRequest.builder()
                .orderId(orderId)
                .otp(otp)
                .build();
    }
}
