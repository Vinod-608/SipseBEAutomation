package sipSe.withdrawl;

import org.jarfinApiBackendAutomation.data.requestModel.withdrawl.WithdrawlInitiateRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.SchemeFolioBalanceResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.SchemeWiseBalanceResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.WithdrawlStatusResponse;
import org.testng.annotations.DataProvider;

import java.util.function.Predicate;

public class WithdrawlDataProvider {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_MAX_RETRIES = 5;
    public static final long DEFAULT_DELAY_MS = 3000;

    public enum WithdrawlAssertion {
        SUCCESS,
        UNAUTHORIZED,
        BAD_REQUEST
    }

    public record SchemeWiseBalanceScenario(
            String description,
            String token,
            int pageSize,
            WithdrawlAssertion assertion) {}

    public record SchemeFolioBalanceScenario(
            String description,
            String token,
            int pageSize,
            WithdrawlAssertion assertion) {}

    public record PreRedemptionSummaryScenario(
            String description,
            String token,
            WithdrawlAssertion assertion) {}

    public record WithdrawlInitiateScenario(
            String description,
            String token,
            WithdrawlInitiateRequest request,
            WithdrawlAssertion assertion) {}

    @DataProvider(name = "schemeWiseBalanceScenarios")
    public static Object[][] schemeWiseBalanceScenarios() {
        return new Object[][] {
            {new SchemeWiseBalanceScenario(
                "Positive Scenario: Valid Token",
                null,
                DEFAULT_PAGE_SIZE,
                WithdrawlAssertion.SUCCESS)},
        };
    }

    @DataProvider(name = "schemeFolioBalanceScenarios")
    public static Object[][] schemeFolioBalanceScenarios() {
        return new Object[][] {
            {new SchemeFolioBalanceScenario(
                "Positive Scenario: Valid Token",
                null,
                DEFAULT_PAGE_SIZE,
                WithdrawlAssertion.SUCCESS)},
        };
    }

    @DataProvider(name = "preRedemptionSummaryScenarios")
    public static Object[][] preRedemptionSummaryScenarios() {
        return new Object[][] {
            {new PreRedemptionSummaryScenario(
                "Positive Scenario: Valid Token",
                null,
                WithdrawlAssertion.SUCCESS)},
        };
    }

    // schemeId and userFolioId are null here — they are injected at runtime from prior test responses
    @DataProvider(name = "withdrawlInitiateScenarios")
    public static Object[][] withdrawlInitiateScenarios() {
        return new Object[][] {
            {new WithdrawlInitiateScenario(
                "Positive Scenario: Valid Token",
                null,
                WithdrawlInitiateRequest.build(25, null, null),
                WithdrawlAssertion.SUCCESS)},
        };
    }

    public static final Predicate<SchemeWiseBalanceResponse> SCHEME_WISE_BALANCE_SHOULD_RETRY =
            response -> {
                if (response.getStatusCode() >= 500) return true;
                return response.getData() == null;
            };

    public static final Predicate<SchemeFolioBalanceResponse> SCHEME_FOLIO_BALANCE_SHOULD_RETRY =
            response -> {
                if (response.getStatusCode() >= 500) return true;
                return response.getData() == null;
            };

    public static final Predicate<WithdrawlStatusResponse> WITHDRAWAL_STATUS_SHOULD_RETRY =
            response -> {
                if (response.getStatusCode() >= 500) return true;
                WithdrawlStatusResponse.DataResponse data = response.getData();
                if (data == null || data.getStatus() == null) return true;
                String status = data.getStatus();
                return "PENDING".equalsIgnoreCase(status)
                        || "INITIATED".equalsIgnoreCase(status)
                        || "IN_PROGRESS".equalsIgnoreCase(status);
            };
}
