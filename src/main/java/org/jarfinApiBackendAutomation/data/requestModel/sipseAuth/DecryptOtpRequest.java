package org.jarfinApiBackendAutomation.data.requestModel.sipseAuth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecryptOtpRequest {
    private String otp;
}
