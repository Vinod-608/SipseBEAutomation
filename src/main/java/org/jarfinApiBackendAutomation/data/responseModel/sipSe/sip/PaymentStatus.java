package org.jarfinApiBackendAutomation.data.responseModel.sipSe.sip;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentStatus {

    SUCCESS,
    PENDING,
    FAILED;

    @JsonCreator
    public static PaymentStatus fromValue(String value) {
        if (value == null) return null;
        for (PaymentStatus a : values()) {
            if (a.name().equalsIgnoreCase(value)) return a;
        }
        return null;
    }
}
