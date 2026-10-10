package org.jarfinApiBackendAutomation.endPoints;

public class SipSeEndPoints {

    public static final String SUBMIT_EMAIL = "/v1/api/onb/email";
    public static final String onBoardingVideo = "/v1/api/onb/asset";
    public static final String userDetails = "/v1/api/user/details";
    public static final String KYC_PAN_LOOKUP = "/v1/api/kyc/pan/lookup";
    public static final String KYC_PAN_CONFIRM = "/v1/api/kyc/pan/confirm";
    public static final String KYC_PAN_STATUS = "/v1/api/kyc/pan/confirm/status";
    public static final String BANK_ACCOUNT_LOOKUP = "/v1/api/bankAccount/lookup/fetch";
    public static final String BANK_VERIFICATION_INITIATE =
            "/v1/api/bankAccount/verification/initiate";
    public static final String BANK_VERIFICATION_FETCH =
            "/v1/api/bankAccount/verification/fetch";
    public static final String KYC_PREFILL_DETAILS = "/v1/api/kyc/prefill/details";
    public static final String KYC_PROFILE_SUBMIT = "/v1/api/kyc/profile/submit";
    public static final String KYC_PRE_VERIFICATION_STATUS = "/v1/api/kyc/preVerification/status";
    public static final String BANK_VERIFICATION_INITIATE_V2 =
            "/v1/api/bankAccount/verification/initiate";
    public static final String SETU_MOCK_PAYMENT = "/api/verify/ban/reverse/mock_payment/{verificationId}";
    public static final String BANK_PRE_VERIFICATION_INITIATE = "/v1/api/bankAccount/preVerification/initiate";
    public static final String BANK_PRE_VERIFICATION_INITIATE_V2 = "/finretail/v1/api/bankAccount/preVerification/initiate";
    public static final String BANK_PRE_VERIFICATION_STATUS = "/v1/api/bankAccount/preVerification/status";
    public static final String MANDATE_SETUP = "/finretail/v1/api/sip/setup";
    public static final String BILLDESK_ENACH_CALLBACK_SIMULATOR = "https://uat1.billdesk.com/u2/websimulator/enach/callbackSimulator";
    public static final String BILLDESK_ENACH_CALLBACK_RESPONSE = "https://uat1.billdesk.com/u2/websimulator/enach/callbackResponse";
    public static final String BILLDESK_MANDATE_PROCESS_NPCI_RESP = "https://uat1.billdesk.com/u2/web/v1_2/mandates/processnpciresp";
    public static final String FINPRIM_BILLDESK_CALLBACK = "https://api-staging.sipse.in/finprim/v1/billdesk/callback";
    public static final String FINPRIM_ONDC_CALLBACK = "https://api-staging.sipse.in/finprim/v1/billdesk/callback/ondc";
    public static final String NOMINEE_ADD = "/v1/api/nominee/add";
    public static final String CONSENT_SUBMIT = "/v1/api/consent/submit";
    public static final String MANDATE_VERIFY = "/finretail/v1/api/sip/verify";
    public static final String SIP_SUBMIT = "/finretail/v1/api/sip/submit";
    public static final String SIP_STATUS = "/finretail/v1/api/sip";
    public static final String HOMEFEED_METADATA = "/finretail/v1/api/homefeed/metadata";
    //lumsum
    public static final String LUMPSUM_INITIATE = "/finretail/v1/api/lumpsum/initiate";
    public static final String LUMPSUM_VERIFY = "/finretail/v1/api/lumpsum/verify";
    public static final String LUMPSUM_SUBMIT = "/finretail/v1/api/lumpsum/submit";
    public static final String LUMPSUM_STATUS = "/finretail/v1/api/lumpsum/status";
    // Billdesk Netbanking simulator (lumpsum mock payment)
    public static final String BILLDESK_NETBANKING_CALLBACK_SIMULATOR = "https://uat1.billdesk.com/u2/websimulator/netbanking/callbackSimulator";
    public static final String BILLDESK_NETBANKING_CALLBACK_RESPONSE = "https://uat1.billdesk.com/u2/websimulator/netbanking/callbackResponse";

}
