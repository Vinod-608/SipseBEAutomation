package org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PreVerificationAction {
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    VERIFIED,
    RETRY;

    @JsonCreator
    public static PreVerificationAction fromValue(String value) {
        if (value == null) return null;
        for (PreVerificationAction a : values()) {
            if (a.name().equalsIgnoreCase(value)) return a;
        }
        return null;
    }
}
