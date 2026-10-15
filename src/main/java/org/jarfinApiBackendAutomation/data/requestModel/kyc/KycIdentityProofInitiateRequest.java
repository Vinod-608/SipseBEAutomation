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
public class KycIdentityProofInitiateRequest {
    private String identityDocPostbackUrl;
    private String esignPostbackUrl;

    public static KycIdentityProofInitiateRequest build(String identityDocPostbackUrl, String esignPostbackUrl) {
        return KycIdentityProofInitiateRequest.builder()
                .identityDocPostbackUrl(identityDocPostbackUrl)
                .esignPostbackUrl(esignPostbackUrl)
                .build();
    }
}
