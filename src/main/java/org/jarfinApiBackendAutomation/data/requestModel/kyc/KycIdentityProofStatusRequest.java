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
public class KycIdentityProofStatusRequest {
    private String providerIdentityDocId;
    private String providerIdentityDocStatus;

    public static KycIdentityProofStatusRequest build(String providerIdentityDocId, String status) {
        return KycIdentityProofStatusRequest.builder()
                .providerIdentityDocId(providerIdentityDocId)
                .providerIdentityDocStatus(status)
                .build();
    }
}
