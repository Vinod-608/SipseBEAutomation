package org.jarfinApiBackendAutomation.data.requestModel.consent;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmitUserConsentRequest {
    private String consentId;
    private List<String> consentParams;

    public static SubmitUserConsentRequest build(String consentId, List<String> consentParams) {
        return SubmitUserConsentRequest.builder()
                .consentId(consentId)
                .consentParams(consentParams)
                .build();
    }

    public static SubmitUserConsentRequest build(String consentId) {
        return SubmitUserConsentRequest.builder()
                .consentId(consentId)
                .build();
    }
}
