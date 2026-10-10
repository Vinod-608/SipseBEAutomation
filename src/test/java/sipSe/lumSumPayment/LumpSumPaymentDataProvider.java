package sipSe.lumSumPayment;

import org.jarfinApiBackendAutomation.data.requestModel.lumSum.LumpSumSchemeRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumStatusResponse;
import org.testng.annotations.DataProvider;

import java.util.function.Predicate;

public class LumpSumPaymentDataProvider {

    public static final int DEFAULT_MAX_RETRIES = 5;
    public static final long DEFAULT_DELAY_MS = 3000;

    static final String LUMPSUM_SCHEME_ID = "6a270c20bd140f0050a265cd";

    public enum LumpSumAssertion {
        SUCCESS,
        UNAUTHORIZED,
        BAD_REQUEST
    }

    public record
    LumpSumInitiateScenario(
            String description,
            String token,
            LumpSumSchemeRequest request,
            LumpSumAssertion assertion) {}

    @DataProvider(name = "lumpSumPaymentInitiateScenarios")
    public static Object[][] lumpSumPaymentInitiateScenarios() {
        return new Object[][] {
            {new LumpSumInitiateScenario(
                "Positive Scenario: Valid Token",
                null,
                LumpSumSchemeRequest.build(100, LUMPSUM_SCHEME_ID),
                LumpSumAssertion.SUCCESS)},
        };
    }



    public static final Predicate<LumpSumStatusResponse> LumpSum_STATUS_SHOULD_RETRY =
            response -> {
                if (response.getStatusCode() >= 500) return true;
                LumpSumStatusResponse.LumpSumStatusData data = response.getData();
                if (data == null || data.getStatus() == null) return true;
                String status = data.getStatus();
                return "PAYMENT_PENDING".equalsIgnoreCase(status)
                        || "PAYMENT_INITIATED".equalsIgnoreCase(status);
            };
}
