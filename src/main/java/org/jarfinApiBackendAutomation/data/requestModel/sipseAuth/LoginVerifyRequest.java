package org.jarfinApiBackendAutomation.data.requestModel.sipseAuth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVerifyRequest {
    private String countryCode;
    private String phoneNumber;
    private String otp;

    public static LoginVerifyRequest buildLoginVerifyPayload(
            String countryCode, String phoneNumber, String otp) {

        return LoginVerifyRequest.builder()
                .countryCode(countryCode)
                .phoneNumber(phoneNumber)
                .otp(otp)
                .build();
    }
}
