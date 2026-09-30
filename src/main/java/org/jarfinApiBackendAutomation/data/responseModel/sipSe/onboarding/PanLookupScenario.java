package org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PanLookupScenario {
    private final String description;
    private final boolean validToken;
}
