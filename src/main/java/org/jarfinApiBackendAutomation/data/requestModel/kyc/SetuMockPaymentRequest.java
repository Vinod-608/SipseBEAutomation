package org.jarfinApiBackendAutomation.data.requestModel.kyc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetuMockPaymentRequest {
    private String paymentStatus;

    public static SetuMockPaymentRequest build(String paymentStatus) {
        return SetuMockPaymentRequest.builder().paymentStatus(paymentStatus).build();
    }
}
