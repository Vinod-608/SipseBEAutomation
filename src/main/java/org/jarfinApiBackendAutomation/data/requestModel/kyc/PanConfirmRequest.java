package org.jarfinApiBackendAutomation.data.requestModel.kyc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanConfirmRequest {
    private String pan;
    private String name;
    private String dob;

    public static PanConfirmRequest build(String pan, String name, String dob) {
        return PanConfirmRequest.builder().pan(pan).name(name).dob(dob).build();
    }
}
