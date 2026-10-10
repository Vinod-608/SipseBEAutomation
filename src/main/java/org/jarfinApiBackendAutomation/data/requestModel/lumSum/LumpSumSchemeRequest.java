package org.jarfinApiBackendAutomation.data.requestModel.lumSum;

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
public  class LumpSumSchemeRequest {
    private double amount;
    private String schemeId;


    public static LumpSumSchemeRequest build(double amount, String schemeId) {
        return LumpSumSchemeRequest.builder()
                .amount(amount)
                .schemeId(schemeId)
                .build();
    }


}