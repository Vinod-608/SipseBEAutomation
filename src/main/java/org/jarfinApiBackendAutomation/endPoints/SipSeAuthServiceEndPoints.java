package org.jarfinApiBackendAutomation.endPoints;

public class
SipSeAuthServiceEndPoints {

    // Versioning
    public static final String V1 = "v1";
    public static final String V2 = "v2";
    public static final String V3 = "v3";

    // Authorization
    public static final String initiate_OTP = "/v1/api/auth/login/initiate";
    public static final String DECRYPT_OTP = "/v1/api/auth/decryptOtp";
    public static final String VERIFY_OTP = "/v1/api/auth/login/verify";
    public static final String RESET_OTP_LIMIT = "/v1/api/auth/resetOtpLimit";
}
