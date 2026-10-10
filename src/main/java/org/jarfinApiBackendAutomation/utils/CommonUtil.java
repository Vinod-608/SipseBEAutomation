package org.jarfinApiBackendAutomation.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jarfinApiBackendAutomation.dbConfiguration.DataBaseFactory;

import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.*;
import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.DAILY_SIP_SETUP_CONSENT_OTP_TYPE;
import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.OTP_TYPE_FIELD;
import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.SIPSE_OTP_FIELD;
import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.SORT_FIELD;

@Slf4j
public class CommonUtil {

    public static void executeShellCmd(String shellCmd) {
        try {
            Process process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", shellCmd});
            process.waitFor();
        } catch (Exception e) {
            log.error("Error executing command: {}", shellCmd, e);
        }
    }

    public static String getApiEndPoint(String baseUri, String version, String endpoint) {
        return MessageFormat.format("{0}{1}{2}", baseUri, version, endpoint);
    }

    public static String generateMongoId() {
        return new ObjectId().toHexString();
    }

    public static String getValueFromDocument(Document doc, String key) {
        Object value = doc != null ? doc.get(key) : null;
        return value != null ? value.toString() : null;
    }

    public static double getDoubleValueFromDocument(Document doc, String key) {
        if (doc == null || key == null) return 0.0;

        Object value = doc.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        return value != null ? Double.parseDouble(value.toString()) : 0.0;
    }

    public static Map<String, String> buildQueryParams(Object... keyValues) {
        Map<String, String> params = new HashMap<>();

        for (int i = 0; i < keyValues.length; i += 2) {
            String key = String.valueOf(keyValues[i]);
            Object value = keyValues[i + 1];

            if (value != null) {
                params.put(key, String.valueOf(value));
            }
        }
        return params.isEmpty() ? null : params;
    }

    public static String generateMerchantOrderId() {
        return "MO-" + UUID.randomUUID();
    }

    public static BigDecimal toOneDecimal(BigDecimal value) {
        return value == null ? null : value.setScale(1, RoundingMode.HALF_UP);
    }

    public static String sipSegetApiEndPoint(String baseUri, String endpoint) {
        return MessageFormat.format("{0}{1}", baseUri, endpoint);
    }

    /**
     * Polls an API call until the retry condition is no longer met or maxRetries is exhausted.
     *
     * @param apiCall supplier that executes the API call and returns the response
     * @param shouldRetry predicate returning true when the response is still pending (keep
     *     retrying)
     * @param maxRetries maximum number of attempts
     * @param delayMs wait time in milliseconds between retries
     * @return the last response received
     */
    public static <T> T pollUntilDone(
            Supplier<T> apiCall,
            Predicate<T> shouldRetry,
            int maxRetries,
            long delayMs,
            String operationName) {

        T response = apiCall.get();

        for (int attempt = 1; attempt < maxRetries; attempt++) {

            boolean retryRequired = shouldRetry.test(response);

            if (!retryRequired) {
                return response;
            }

            log.info(
                    "{} still pending - attempt {}/{}, retrying in {} ms",
                    operationName,
                    attempt,
                    maxRetries,
                    delayMs);

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            response = apiCall.get();
        }

        return response;
    }



    public String fetchEncryptedOtpFromDb(String sourceRefId, String otpType) {
        int maxRetries = 10;
        int delayMs = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            Document doc = DataBaseFactory.jarFinMongo()
                    .fetchDataMultiFilter(
                            DB_JARFIN,
                            OTP_DELIVERY_REPORTS_COLLECTION,
                            Map.of(
                                    SOURCE_REF_ID_FIELD, sourceRefId,
                                    OTP_TYPE_FIELD, otpType),
                            SORT_FIELD);

            if (doc == null) {
                log.warn("Attempt {}/{}: No OTP document found for sourceRefId={}, otpType={}",
                        attempt, maxRetries, sourceRefId, otpType);
                continue;
            }

            String encryptedOtp = doc.getString(SIPSE_OTP_FIELD);
            if (encryptedOtp != null && !encryptedOtp.isBlank()) {
                log.info("Fetched encrypted OTP for sourceRefId={}, otpType={} on attempt {}",
                        sourceRefId, otpType, attempt);
                return encryptedOtp;
            }
        }

        log.error("Failed to fetch OTP from DB for sourceRefId={}, otpType={} after {} attempts",
                sourceRefId, otpType, maxRetries);
        return null;
    }


    public String fetchAndDecryptOtp(String sourceRefId, String otpType, Function<String, String> decryptFn) {
        String encryptedOtp = fetchEncryptedOtpFromDb(sourceRefId, otpType);
        if (encryptedOtp == null) return null;
        String plainOtp = decryptFn.apply(encryptedOtp);
        log.info("Decrypted OTP={} for sourceRefId={}, otpType={}", plainOtp, sourceRefId, otpType);
        return plainOtp;
    }

    public String fetchAndDecryptSipConsentOtp(String purchasePlanId, Function<String, String> decryptFn) {
        return fetchAndDecryptOtp(purchasePlanId, DAILY_SIP_SETUP_CONSENT_OTP_TYPE, decryptFn);
    }

    public String fetchAndDecryptLumpSumOtp(String purchasePlanId, Function<String, String> decryptFn) {
        return fetchAndDecryptOtp(purchasePlanId, LUMPSUM_PURCHASE_CONSENT_OTP_TYPE, decryptFn);
    }


}
