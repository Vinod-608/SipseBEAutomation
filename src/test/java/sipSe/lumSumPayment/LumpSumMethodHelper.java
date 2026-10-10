package sipSe.lumSumPayment;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.jarfinApiBackendAutomation.configuration.BaseUri;
import org.jarfinApiBackendAutomation.data.common.RestRequest;
import org.jarfinApiBackendAutomation.data.requestModel.lumSum.LumpSumSchemeRequest;
import org.jarfinApiBackendAutomation.data.requestModel.lumSum.LumpSumSubmitRequest;
import org.jarfinApiBackendAutomation.data.requestModel.lumSum.LumpSumVerifyRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumIntiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumSubmitResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumVerifyResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.BillDeskMandateMockResponse;
import org.jarfinApiBackendAutomation.endPoints.SipSeEndPoints;
import org.jarfinApiBackendAutomation.utils.ApiRequests;
import org.jarfinApiBackendAutomation.utils.CommonSerializationUtil;
import org.jarfinApiBackendAutomation.utils.MockDataSeeder;

import java.util.Map;

import static org.jarfinApiBackendAutomation.utils.CommonUtil.sipSegetApiEndPoint;

@Slf4j
public class LumpSumMethodHelper {

    private final ApiRequests apiRequests = new ApiRequests();


    public LumpSumIntiateResponse lumpSumInitiate(String accessToken, LumpSumSchemeRequest request) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Authorization", "Bearer " + accessToken,
                                        "Content-Type", "application/json"))
                        .body(request)
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.LUMPSUM_INITIATE))
                        .build();
        Response response = apiRequests.post(req);
        LumpSumIntiateResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), LumpSumIntiateResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "lumpSum initiate— status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public LumpSumVerifyResponse lumpSumVerify(String accessToken, LumpSumVerifyRequest request) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Authorization", "Bearer " + accessToken,
                                        "Content-Type", "application/json"))
                        .body(request)
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.LUMPSUM_VERIFY))
                        .build();
        Response response = apiRequests.post(req);
        LumpSumVerifyResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), LumpSumVerifyResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "lumpSum verify — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public LumpSumSubmitResponse lumpSumSubmit(String accessToken, LumpSumSubmitRequest request) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Authorization", "Bearer " + accessToken,
                                        "Content-Type", "application/json"))
                        .body(request)
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.LUMPSUM_SUBMIT))
                        .build();
        Response response = apiRequests.post(req);
        LumpSumSubmitResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), LumpSumSubmitResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "lumpSum submit — status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public BillDeskMandateMockResponse lumpSumMockPayment(String redirectUrl) {
        log.info("lumpSumMockPayment — triggering Billdesk Netbanking mock for redirectUrl={}", redirectUrl);
        return MockDataSeeder.billdeskNetbankingMockPayment(redirectUrl);
    }

    public LumpSumStatusResponse lumpSumStatus(String accessToken, String orderId) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of("Authorization", "Bearer " + accessToken))
                        .queryParams(Map.of("orderId", orderId))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.LUMPSUM_STATUS))
                        .build();
        Response response = apiRequests.get(req);
        LumpSumStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), LumpSumStatusResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "lumpSum status — status={}, orderStatus={}",
                response.getStatusCode(),
                deserializedResponse.getData() != null ? deserializedResponse.getData().getStatus() : null);
        return deserializedResponse;
    }
}
