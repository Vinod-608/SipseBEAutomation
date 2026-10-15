package sipSe.kyc;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.jarfinApiBackendAutomation.configuration.BaseUri;
import org.jarfinApiBackendAutomation.data.common.RestRequest;
import org.jarfinApiBackendAutomation.data.requestModel.kyc.KycEsignInitiateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.kyc.KycIdentityProofInitiateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.kyc.KycIdentityProofStatusRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.*;
import org.jarfinApiBackendAutomation.endPoints.SipSeEndPoints;
import org.jarfinApiBackendAutomation.utils.ApiRequests;
import org.jarfinApiBackendAutomation.utils.CommonSerializationUtil;

import java.util.Base64;
import java.util.Map;

import static org.jarfinApiBackendAutomation.utils.CommonUtil.sipSegetApiEndPoint;

@Slf4j
public class KycDigiLockerMethodhelper {

    private final ApiRequests apiRequests = new ApiRequests();

    public KycDigiLockerPreVerificationResponse kycPreVerificationStatus(String accessToken) {
        RestRequest req = RestRequest.builder()
                .headers(Map.of(
                        "accept", "application/json, text/plain, */*",
                        "Authorization", "Bearer " + accessToken))
                .url(sipSegetApiEndPoint(BaseUri.SIPSE_STAGING_ROOT_URI, SipSeEndPoints.KYC_DIGILOCKER_PRE_VERIFICATION_STATUS))
                .build();
        Response response = apiRequests.get(req);
        KycDigiLockerPreVerificationResponse result = CommonSerializationUtil.readObject(
                response.getBody().asString(), KycDigiLockerPreVerificationResponse.class);
        result.setStatusCode(response.getStatusCode());
        log.info("kycPreVerificationStatus status={}, success={}, providerIdentityDocId={}",
                response.getStatusCode(), result.isSuccess(),
                result.getData() != null ? result.getData().getProviderIdentityDocId() : null);
        return result;
    }

    public int finprimIdentityDocForwardTo(String iddocId, String token) {
        String url = SipSeEndPoints.FINPRIM_BASE_URL
                + "/v2/identity_documents/" + iddocId + "/forward_to?token=" + token;
        RestRequest req = RestRequest.builder()
                .headers(Map.of("accept", "text/html,application/xhtml+xml,*/*"))
                .url(url)
                .build();
        Response response = apiRequests.get(req);
        log.info("finprimIdentityDocForwardTo iddocId={} status={}", iddocId, response.getStatusCode());
        return response.getStatusCode();
    }

    public int finprimIdentityDocSimulate(String iddocId, String token) {
        String url = SipSeEndPoints.FINPRIM_BASE_URL
                + "/v2/identity_documents/" + iddocId + "/simulate?token=" + token;
        RestRequest req = RestRequest.builder()
                .headers(Map.of("accept", "text/html,application/xhtml+xml,*/*"))
                .url(url)
                .build();
        Response response = apiRequests.get(req);
        log.info("finprimIdentityDocSimulate iddocId={} status={}", iddocId, response.getStatusCode());
        return response.getStatusCode();
    }

    public KycIdentityProofInitiateResponse kycIdentityProofInitiate(String accessToken,
                                                                      String identityDocPostbackUrl,
                                                                      String esignPostbackUrl) {
        KycIdentityProofInitiateRequest request =
                KycIdentityProofInitiateRequest.build(identityDocPostbackUrl, esignPostbackUrl);
        RestRequest req = RestRequest.builder()
                .headers(Map.of(
                        "accept", "application/json, text/plain, */*",
                        "content-type", "application/json",
                        "Authorization", "Bearer " + accessToken))
                .url(sipSegetApiEndPoint(BaseUri.SIPSE_STAGING_ROOT_URI, SipSeEndPoints.KYC_IDENTITY_PROOF_INITIATE))
                .body(request)
                .build();
        Response response = apiRequests.post(req);
        KycIdentityProofInitiateResponse result = CommonSerializationUtil.readObject(
                response.getBody().asString(), KycIdentityProofInitiateResponse.class);
        result.setStatusCode(response.getStatusCode());
        log.info("kycIdentityProofInitiate status={}, success={}, providerIdentityDocId={}",
                response.getStatusCode(), result.isSuccess(),
                result.getData() != null ? result.getData().getProviderIdentityDocId() : null);
        return result;
    }

    public KycIdentityProofStatusResponse kycIdentityProofStatus(
            String accessToken, String providerIdentityDocId, String providerStatus) {
        KycIdentityProofStatusRequest request =
                KycIdentityProofStatusRequest.build(providerIdentityDocId, providerStatus);
        RestRequest req = RestRequest.builder()
                .headers(Map.of(
                        "accept", "application/json, text/plain, */*",
                        "content-type", "application/json",
                        "Authorization", "Bearer " + accessToken))
                .url(sipSegetApiEndPoint(BaseUri.SIPSE_STAGING_ROOT_URI, SipSeEndPoints.KYC_IDENTITY_PROOF_STATUS))
                .body(request)
                .build();
        Response response = apiRequests.post(req);
        KycIdentityProofStatusResponse result = CommonSerializationUtil.readObject(
                response.getBody().asString(), KycIdentityProofStatusResponse.class);
        result.setStatusCode(response.getStatusCode());
        log.info("kycIdentityProofStatus status={}, success={}", response.getStatusCode(), result.isSuccess());
        return result;
    }

    public PrefillKycDetailsResponse kycPrefillDetails(String accessToken) {
        RestRequest req = RestRequest.builder()
                .headers(Map.of(
                        "accept", "application/json, text/plain, */*",
                        "Authorization", "Bearer " + accessToken))
                .url(sipSegetApiEndPoint(BaseUri.SIPSE_STAGING_ROOT_URI, SipSeEndPoints.KYC_PREFILL_DETAILS_V2))
                .build();
        Response response = apiRequests.get(req);
        PrefillKycDetailsResponse result = CommonSerializationUtil.readObject(
                response.getBody().asString(), PrefillKycDetailsResponse.class);
        result.setStatusCode(response.getStatusCode());
        log.info("kycPrefillDetails status={}, success={}", response.getStatusCode(), result.isSuccess());
        return result;
    }

    public KycPreSignedUrlResponse kycPreSignedUrl(String accessToken) {
        RestRequest req = RestRequest.builder()
                .headers(Map.of(
                        "accept", "application/json, text/plain, */*",
                        "Authorization", "Bearer " + accessToken))
                .queryParams(Map.of("context", "KYC_UPLOAD_SIGNATURE"))
                .url(sipSegetApiEndPoint(BaseUri.SIPSE_STAGING_ROOT_URI, SipSeEndPoints.KYC_PRE_SIGNED_URL))
                .build();
        Response response = apiRequests.get(req);
        KycPreSignedUrlResponse result = CommonSerializationUtil.readObject(
                response.getBody().asString(), KycPreSignedUrlResponse.class);
        result.setStatusCode(response.getStatusCode());
        log.info("kycPreSignedUrl status={}, success={}", response.getStatusCode(), result.isSuccess());
        return result;
    }

    public int uploadSignatureToS3(String uploadUrl) {
        // Minimal valid JPEG (1×1 pixel) — use java.net.http.HttpClient to avoid RestAssured
        // double-encoding pre-encoded URL query params (e.g. %2F → %252F) which breaks S3 signature
        byte[] minimalJpeg = Base64.getDecoder().decode(
                "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8U" +
                "HRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgN" +
                "DRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIy" +
                "MjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFAABAAAAAAAAAAAAAAAAAAAACf/EABQQAQAA" +
                "AAAAAAAAAAAAAAAAAAAA/8QAFBEBAAAAAAAAAAAAAAAAAAAAAP/EABQRAQAAAAAAAAAAAAAAAAAAAAD/" +
                "2gAMAwEAAhEDEQA/AJVN/9k=");
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                    .build();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(uploadUrl))
                    .header("Content-Type", "image/jpeg")
                    .PUT(java.net.http.HttpRequest.BodyPublishers.ofByteArray(minimalJpeg))
                    .build();
            java.net.http.HttpResponse<String> response =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            log.info("uploadSignatureToS3 status={} body={}", response.statusCode(), response.body());
            return response.statusCode();
        } catch (Exception e) {
            log.error("uploadSignatureToS3 failed: {}", e.getMessage());
            return -1;
        }
    }

    public KycSignatureSubmitResponse kycSignatureSubmit(String accessToken) {
        RestRequest req = RestRequest.builder()
                .headers(Map.of(
                        "accept", "application/json, text/plain, */*",
                        "Authorization", "Bearer " + accessToken))
                .url(sipSegetApiEndPoint(BaseUri.SIPSE_STAGING_ROOT_URI, SipSeEndPoints.KYC_SIGNATURE_SUBMIT))
                .build();
        Response response = apiRequests.post(req);
        KycSignatureSubmitResponse result = CommonSerializationUtil.readObject(
                response.getBody().asString(), KycSignatureSubmitResponse.class);
        result.setStatusCode(response.getStatusCode());
        log.info("kycSignatureSubmit status={}, success={}", response.getStatusCode(), result.isSuccess());
        return result;
    }

    public int finprimEsignReview(String esignId, String token) {
        String url = SipSeEndPoints.FINPRIM_BASE_URL
                + "/v2/esigns/" + esignId + "/review?token=" + token;
        RestRequest req = RestRequest.builder()
                .headers(Map.of("accept", "text/html,application/xhtml+xml,*/*"))
                .url(url)
                .build();
        Response response = apiRequests.get(req);
        log.info("finprimEsignReview esignId={} status={}", esignId, response.getStatusCode());

        String step1Url = SipSeEndPoints.FINPRIM_BASE_URL
                + "/v2/esigns/" + esignId + "/review?step=1&token=" + token;
        RestRequest step1Req = RestRequest.builder()
                .headers(Map.of("accept", "text/html,application/xhtml+xml,*/*"))
                .url(step1Url)
                .build();
        Response step1Response = apiRequests.get(step1Req);
        log.info("finprimEsignReview step=1 esignId={} status={}", esignId, step1Response.getStatusCode());

        return response.getStatusCode();
    }

    public KycEsignInitiateResponse kycEsignInitiate(String accessToken, String postbackUrl) {
        KycEsignInitiateRequest request = KycEsignInitiateRequest.build(postbackUrl);
        RestRequest req = RestRequest.builder()
                .headers(Map.of(
                        "accept", "application/json, text/plain, */*",
                        "content-type", "application/json",
                        "Authorization", "Bearer " + accessToken))
                .url(sipSegetApiEndPoint(BaseUri.SIPSE_STAGING_ROOT_URI, SipSeEndPoints.KYC_ESIGN_INITIATE))
                .body(request)
                .build();
        Response response = apiRequests.post(req);
        KycEsignInitiateResponse result = CommonSerializationUtil.readObject(
                response.getBody().asString(), KycEsignInitiateResponse.class);
        result.setStatusCode(response.getStatusCode());
        log.info("kycEsignInitiate status={}, success={}, providerEsignId={}",
                response.getStatusCode(), result.isSuccess(),
                result.getData() != null ? result.getData().getProviderEsignId() : null);
        return result;
    }

    public int finprimEsignSimulate(String esignId, String token) {
        String url = SipSeEndPoints.FINPRIM_BASE_URL
                + "/v2/esigns/" + esignId + "/simulate_esign?token=" + token;
        RestRequest req = RestRequest.builder()
                .headers(Map.of("accept", "text/html,application/xhtml+xml,*/*"))
                .url(url)
                .build();
        Response response = apiRequests.get(req);
        log.info("finprimEsignSimulate esignId={} status={}", esignId, response.getStatusCode());
        return response.getStatusCode();
    }

    public int finprimEsignSimulateOtpStep2(String esignId, String token) {
        String url = SipSeEndPoints.FINPRIM_BASE_URL
                + "/v2/esigns/" + esignId + "/simulate_esign?otp_step=2&token=" + token;
        RestRequest req = RestRequest.builder()
                .headers(Map.of("accept", "text/html,application/xhtml+xml,*/*"))
                .url(url)
                .build();
        Response response = apiRequests.get(req);
        log.info("finprimEsignSimulateOtpStep2 esignId={} status={}", esignId, response.getStatusCode());
        return response.getStatusCode();
    }

    public int finprimEsignSubmit(String esignId, String token) {
        String url = SipSeEndPoints.FINPRIM_BASE_URL
                + "/v2/esigns/" + esignId + "/submit?otp_step=2&token=" + token;
        RestRequest req = RestRequest.builder()
                .headers(Map.of("accept", "text/html,application/xhtml+xml,*/*"))
                .url(url)
                .build();
        Response response = apiRequests.get(req);
        log.info("finprimEsignSubmit esignId={} status={}", esignId, response.getStatusCode());
        return response.getStatusCode();
    }

    public KycEsignStatusResponse kycEsignStatus(String accessToken, String providerEsignId) {
        RestRequest req = RestRequest.builder()
                .headers(Map.of(
                        "accept", "application/json, text/plain, */*",
                        "Authorization", "Bearer " + accessToken))
                .queryParams(Map.of("providerEsignId", providerEsignId))
                .url(sipSegetApiEndPoint(BaseUri.SIPSE_STAGING_ROOT_URI, SipSeEndPoints.KYC_ESIGN_STATUS))
                .build();
        Response response = apiRequests.get(req);
        KycEsignStatusResponse result = CommonSerializationUtil.readObject(
                response.getBody().asString(), KycEsignStatusResponse.class);
        result.setStatusCode(response.getStatusCode());
        log.info("kycEsignStatus status={}, esignStatus={}",
                response.getStatusCode(),
                result.getData() != null ? result.getData().getStatus() : null);
        return result;
    }

    public KycStatusResponse kycStatus(String accessToken) {
        RestRequest req = RestRequest.builder()
                .headers(Map.of(
                        "accept", "application/json, text/plain, */*",
                        "Authorization", "Bearer " + accessToken))
                .url(sipSegetApiEndPoint(BaseUri.SIPSE_STAGING_ROOT_URI, SipSeEndPoints.KYC_STATUS))
                .build();
        Response response = apiRequests.get(req);
        KycStatusResponse result = CommonSerializationUtil.readObject(
                response.getBody().asString(), KycStatusResponse.class);
        result.setStatusCode(response.getStatusCode());
        log.info("kycStatus status={}, success={}", response.getStatusCode(), result.isSuccess());
        return result;
    }
}
