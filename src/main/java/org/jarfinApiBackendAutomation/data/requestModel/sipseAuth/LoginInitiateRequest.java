package org.jarfinApiBackendAutomation.data.requestModel.sipseAuth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginInitiateRequest {
    private String countryCode;
    private String phoneNumber;

    public static LoginInitiateRequest buildLoginInitiatePayload(
            String countryCode, String phoneNumber) {

        return LoginInitiateRequest.builder()
                .countryCode(countryCode)
                .phoneNumber(phoneNumber)
                .build();
    }
}
