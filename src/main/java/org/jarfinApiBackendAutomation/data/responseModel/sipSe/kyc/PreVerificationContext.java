package org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PreVerificationContext {
    PAN_VALIDATION,
    BANK_VALIDATION,
    KYC_READINESS;

    @JsonCreator
    public static PreVerificationContext fromValue(String value) {
        if (value == null) return null;
        for (PreVerificationContext c : values()) {
            if (c.name().equalsIgnoreCase(value)) return c;
        }
        return null;
    }
}
