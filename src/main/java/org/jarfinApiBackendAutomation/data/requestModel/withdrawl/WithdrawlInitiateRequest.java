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
public class WithdrawlInitiateRequest {

    private double amount;
    private String schemeId;
    private String userFolioId;

    public static WithdrawlInitiateRequest build(double amount, String schemeId, String userFolioId) {
        return WithdrawlInitiateRequest.builder()
                .amount(amount)
                .schemeId(schemeId)
                .userFolioId(userFolioId)
                .build();
    }
}
