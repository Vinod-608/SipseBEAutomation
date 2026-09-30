package org.jarfinApiBackendAutomation.data.requestModel.onBoarding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailOnboardingRequest {

    private String email;

    public static EmailOnboardingRequest buildEmailPayload(String email) {
        return EmailOnboardingRequest.builder().email(email).build();
    }
}
