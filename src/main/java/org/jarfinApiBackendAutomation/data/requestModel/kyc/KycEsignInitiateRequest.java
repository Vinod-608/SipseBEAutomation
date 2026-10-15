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
public class KycEsignInitiateRequest {
    private String esignPostbackUrl;

    public static KycEsignInitiateRequest build(String postbackUrl) {
        return KycEsignInitiateRequest.builder()
                .esignPostbackUrl(postbackUrl)
                .build();
    }
}
