package org.jarfinApiBackendAutomation.data.requestModel.kyc;

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
public class BankVerificationInitiateRequest {
    private String packageName;
    private String redirectUrl;
    private Integer timeout;
    private String upiApp;

    public static BankVerificationInitiateRequest build(
            String packageName, String redirectUrl, Integer timeout, String upiApp) {
        return BankVerificationInitiateRequest.builder()
                .packageName(packageName)
                .redirectUrl(redirectUrl)
                .timeout(timeout)
                .upiApp(upiApp)
                .build();
    }
}
