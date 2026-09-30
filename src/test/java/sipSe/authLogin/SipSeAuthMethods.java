package sipSe.authLogin;

import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.*;
import static org.jarfinApiBackendAutomation.utils.CommonUtil.sipSegetApiEndPoint;

import io.restassured.response.Response;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.jarfinApiBackendAutomation.configuration.BaseUri;
import org.jarfinApiBackendAutomation.data.common.RestRequest;
import org.jarfinApiBackendAutomation.data.requestModel.sipseAuth.DecryptOtpRequest;
import org.jarfinApiBackendAutomation.data.requestModel.sipseAuth.LoginInitiateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.sipseAuth.LoginVerifyRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse.DecryptOtpResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse.LoginInitiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.authResonse.LoginVerifyResponse;
import org.jarfinApiBackendAutomation.dbConfiguration.DataBaseFactory;
import org.jarfinApiBackendAutomation.endPoints.SipSeAuthServiceEndPoints;
import org.jarfinApiBackendAutomation.utils.ApiRequests;
import org.jarfinApiBackendAutomation.utils.CommonSerializationUtil;

@Slf4j
public class SipSeAuthMethods {

    private final ApiRequests apiRequests = new ApiRequests();

    public LoginInitiateResponse initiateOTPHelper(LoginInitiateRequest loginRequest) {
        RestRequest req =
                RestRequest.builder()
                        .headers(Map.of("content-type", "application/json"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_PUBLIC_BASE_URI,
                                        SipSeAuthServiceEndPoints.initiate_OTP))
                        .body(loginRequest)
                        .build();
        Response response = apiRequests.post(req);
        LoginInitiateResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), LoginInitiateResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public String fetchOtpFromDb(String mobileNumber) {
        return fetchOtpFromDb(mobileNumber, System.currentTimeMillis());
    }

    public String fetchOtpFromDb(String mobileNumber, long otpInitiatedAt) {
        String phoneWithCountryCode = "+91" + mobileNumber;
        int maxRetries = 10;
        int delayMs = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            Document doc =
                    DataBaseFactory.jarFinMongo()
                            .fetchData(
                                    DB_JARFIN,
                                    OTP_DELIVERY_REPORTS_COLLECTION,
                                    SIPSE_FILTER_KEY,
                                    phoneWithCountryCode,
                                    SORT_FIELD);

            if (doc == null) {
                log.warn(
                        "Attempt {}/{}: No OTP document found for mobileNumber={}",
                        attempt,
                        maxRetries,
                        mobileNumber);
                continue;
            }

            // Backend may update the existing document rather than insert a new one,
            // so check updatedAt (not createdAt) to confirm this is the fresh OTP.
            Object updatedAtObj = doc.get(UPDATED_AT_FIELD);
            if (updatedAtObj instanceof java.util.Date updatedAt
                    && updatedAt.getTime() >= otpInitiatedAt) {
                String encryptedOtp = doc.getString(SIPSE_OTP_FIELD);
                log.info(
                        "Fetched fresh encrypted OTP={} for mobileNumber={} on attempt {}",
                        encryptedOtp,
                        mobileNumber,
                        attempt);
                return encryptedOtp;
            } else {
                log.warn(
                        "Attempt {}/{}: OTP document is stale (updatedAt={}), retrying...",
                        attempt,
                        maxRetries,
                        updatedAtObj);
            }
        }

        log.error(
                "Failed to fetch a fresh OTP from DB for mobileNumber={} after {} attempts",
                mobileNumber,
                maxRetries);
        return null;
    }

    public DecryptOtpResponse decryptOtp(String encryptedOtp) {
        RestRequest req =
                RestRequest.builder()
                        .headers(Map.of("Content-Type", "application/json"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_PUBLIC_BASE_URI,
                                        SipSeAuthServiceEndPoints.DECRYPT_OTP))
                        .body(new DecryptOtpRequest(encryptedOtp))
                        .build();
        Response response = apiRequests.post(req);

        DecryptOtpResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), DecryptOtpResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info("Decrypted OTP={}", deserializedResponse.getData());
        return deserializedResponse;
    }

    public void resetOtpLimit(String countryCode, String phoneNumber) {
        RestRequest req =
                RestRequest.builder()
                        .headers(Map.of("Content-Type", "application/json"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_PUBLIC_BASE_URI,
                                        SipSeAuthServiceEndPoints.RESET_OTP_LIMIT))
                        .body(
                                LoginInitiateRequest.buildLoginInitiatePayload(
                                        countryCode, phoneNumber))
                        .build();
        Response response = apiRequests.post(req);
        log.info(
                "Reset OTP limit for {}{} — status={}",
                countryCode,
                phoneNumber,
                response.getStatusCode());
    }

    public String fetchUserIdFromDb(String mobileNumber) {
        String phoneWithCountryCode = "+91" + mobileNumber;
        Document doc =
                DataBaseFactory.jarFinMongo()
                        .fetchData(
                                DB_JARFIN,
                                SIPSE_USERS_COLLECTION,
                                SIPSE_FILTER_KEY,
                                phoneWithCountryCode,
                                SORT_FIELD);
        if (doc == null) {
            log.error("No user document found for mobileNumber={}", mobileNumber);
            return null;
        }
        String userId = doc.getObjectId(SIPSE_USER_ID_FIELD).toHexString();
        log.info("Fetched userId={} for mobileNumber={}", userId, mobileNumber);
        return userId;
    }

    public LoginVerifyResponse verfiyOtpRequestHelper(LoginVerifyRequest verifyRequest) {
        RestRequest req =
                RestRequest.builder()
                        .headers(Map.of("content-type", "application/json"))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_PUBLIC_BASE_URI,
                                        SipSeAuthServiceEndPoints.VERIFY_OTP))
                        .body(verifyRequest)
                        .build();

        Response response = apiRequests.post(req);

        LoginVerifyResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), LoginVerifyResponse.class);

        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }
}
