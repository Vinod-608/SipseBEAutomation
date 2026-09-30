package org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PanLookupStatus {

    /**
     * No customer data lookup has been initiated, or the lookup completed without PAN data, or the
     * lookup failed. The user must enter PAN, name, and date of birth manually.
     */
    ENTER_PAN_MANUALLY,

    /**
     * The customer data lookup is still being processed by the partner. The client should poll
     * again after a short delay.
     */
    LOOKUP_PENDING,

    /**
     * The customer data lookup completed successfully and the investor's PAN, name, and date of
     * birth are available for pre-filling.
     */
    PAN_DATA_AVAILABLE,

    /** The investor's KYC is already fully verified — no PAN confirmation flow is needed. */
    KYC_VERIFIED;

    @JsonCreator
    public static PanLookupStatus fromValue(String value) {
        if (value == null) return null;
        for (PanLookupStatus s : values()) {
            if (s.name().equalsIgnoreCase(value)) return s;
        }
        return null;
    }
}
