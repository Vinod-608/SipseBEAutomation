package sipSe.withdrawl;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.jarfinApiBackendAutomation.configuration.BaseUri;
import org.jarfinApiBackendAutomation.data.common.RestRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.LockerFeatureSectionResponse;
import org.jarfinApiBackendAutomation.data.requestModel.withdrawl.WithdrawlInitiateRequest;
import org.jarfinApiBackendAutomation.data.requestModel.withdrawl.WithdrawlSubmitRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.PreRedemptionSummaryResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.WithdrawlInitiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.WithdrawlStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.WithdrawlSubmitResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.SchemeFolioBalanceResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.SchemeWiseBalanceResponse;
import org.jarfinApiBackendAutomation.endPoints.SipSeEndPoints;
import org.jarfinApiBackendAutomation.utils.ApiRequests;
import org.jarfinApiBackendAutomation.utils.CommonSerializationUtil;

import java.util.Map;

import static org.jarfinApiBackendAutomation.utils.CommonUtil.sipSegetApiEndPoint;

@Slf4j
public class WithdrawlMethodhelper {


    private final ApiRequests apiRequests = new ApiRequests();

    public LockerFeatureSectionResponse lockerSectionDetails(String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "Authorization", "Bearer " + accessToken))
                        .url(
                                sipSegetApiEndPoint(
                                        BaseUri.SIPSE_BASE_URI,
                                        SipSeEndPoints.LOCKER_DETAILS))
                        .build();
        Response response = apiRequests.get(req);
        LockerFeatureSectionResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), LockerFeatureSectionResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "locker details  status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public SchemeWiseBalanceResponse schemeWiseBalance(String accessToken, int pageSize) {
        RestRequest req =
                RestRequest.builder()
                        .headers(Map.of("Authorization", "Bearer " + accessToken))
                        .url(sipSegetApiEndPoint(BaseUri.SIPSE_BASE_URI,
                                SipSeEndPoints.SCHEME_WISE_BALANCE + "?pageSize=" + pageSize))
                        .build();
        Response response = apiRequests.get(req);
        SchemeWiseBalanceResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SchemeWiseBalanceResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "schemeWiseBalance status={}, success={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess());
        return deserializedResponse;
    }

    public SchemeFolioBalanceResponse schemeFolioBalance(String accessToken, String fundSchemeId, int pageSize) {
        RestRequest req =
                RestRequest.builder()
                        .headers(Map.of("Authorization", "Bearer " + accessToken))
                        .url(sipSegetApiEndPoint(BaseUri.SIPSE_BASE_URI,
                                SipSeEndPoints.SCHEME_FOLIO_BALANCE
                                        + "?fundSchemeId=" + fundSchemeId
                                        + "&pageSize=" + pageSize))
                        .build();
        Response response = apiRequests.get(req);
        SchemeFolioBalanceResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SchemeFolioBalanceResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "schemeFolioBalance status={}, success={}, fundSchemeId={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess(),
                fundSchemeId);
        return deserializedResponse;
    }

    public PreRedemptionSummaryResponse preRedemptionSummary(String accessToken, String fundSchemeId, String userFolioId) {
        RestRequest req =
                RestRequest.builder()
                        .headers(Map.of("Authorization", "Bearer " + accessToken))
                        .url(sipSegetApiEndPoint(BaseUri.SIPSE_BASE_URI,
                                SipSeEndPoints.PRE_REDEMPTION_SUMMARY
                                        + "?fundSchemeId=" + fundSchemeId
                                        + "&userFolioId=" + userFolioId))
                        .build();
        Response response = apiRequests.get(req);
        PreRedemptionSummaryResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), PreRedemptionSummaryResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "preRedemptionSummary status={}, success={}, fundSchemeId={}, userFolioId={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess(),
                fundSchemeId,
                userFolioId);
        return deserializedResponse;
    }

    public WithdrawlInitiateResponse withdrawlInitiate(String accessToken, WithdrawlInitiateRequest request) {
        RestRequest req =
                RestRequest.builder()
                        .headers(Map.of(
                                "Authorization", "Bearer " + accessToken,
                                "Content-Type", "application/json"))
                        .url(sipSegetApiEndPoint(BaseUri.SIPSE_BASE_URI, SipSeEndPoints.WITHDRAWAL_INITIATE))
                        .body(request)
                        .build();
        Response response = apiRequests.post(req);
        WithdrawlInitiateResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), WithdrawlInitiateResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "withdrawlInitiate status={}, success={}, orderId={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess(),
                deserializedResponse.getData() != null ? deserializedResponse.getData().getOrderId() : null);
        return deserializedResponse;
    }

    public WithdrawlSubmitResponse withdrawlSubmit(String accessToken, WithdrawlSubmitRequest request) {
        RestRequest req =
                RestRequest.builder()
                        .headers(Map.of(
                                "Authorization", "Bearer " + accessToken,
                                "Content-Type", "application/json"))
                        .url(sipSegetApiEndPoint(BaseUri.SIPSE_BASE_URI, SipSeEndPoints.WITHDRAWAL_SUBMIT))
                        .body(request)
                        .build();
        Response response = apiRequests.post(req);
        WithdrawlSubmitResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), WithdrawlSubmitResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "withdrawlSubmit status={}, success={}, orderStatus={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess(),
                deserializedResponse.getData() != null ? deserializedResponse.getData().getStatus() : null);
        return deserializedResponse;
    }

    public WithdrawlStatusResponse withdrawlStatus(String accessToken, String orderId) {
        RestRequest req =
                RestRequest.builder()
                        .headers(Map.of("Authorization", "Bearer " + accessToken))
                        .url(sipSegetApiEndPoint(BaseUri.SIPSE_BASE_URI,
                                SipSeEndPoints.WITHDRAWAL_STATUS + "?orderId=" + orderId))
                        .build();
        Response response = apiRequests.get(req);
        WithdrawlStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), WithdrawlStatusResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        log.info(
                "withdrawlStatus status={}, success={}, orderStatus={}",
                response.getStatusCode(),
                deserializedResponse.isSuccess(),
                deserializedResponse.getData() != null ? deserializedResponse.getData().getStatus() : null);
        return deserializedResponse;
    }
}
