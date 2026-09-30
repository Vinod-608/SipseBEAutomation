package org.jarfinApiBackendAutomation.dbConfiguration;

import org.jarfinApiBackendAutomation.configuration.ConfigLoader;

public class DBConstants {
    // SIPse Constants
    public static final String DB_JARFIN = "jarfin";
    public static final String JARFIN_MONGO_DB_URL = ConfigLoader.get("db.jarfin.mongo.url");
    public static final String OTP_DELIVERY_REPORTS_COLLECTION = "otpDeliveryReports";
    public static final String SIPSE_USERS_COLLECTION = "users";
    public static final String SIPSE_MOCK_RESPONSES_COLLECTION = "mockResponses";
    public static final String SIPSE_FILTER_KEY = "phoneNumber";
    public static final String SIPSE_OTP_FIELD = "otp";
    public static final String SIPSE_USER_ID_FIELD = "_id";
    public static final String SORT_FIELD = "updatedAt";
    public static final String UPDATED_AT_FIELD = "updatedAt";
    public static final String SOURCE_REF_ID_FIELD = "sourceRefId";
    public static final String OTP_TYPE_FIELD = "otpType";
    public static final String DAILY_SIP_SETUP_CONSENT_OTP_TYPE = "DAILY_SIP_SETUP_CONSENT";
}
