package sipSe.onBoarding;

import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.jarfinApiBackendAutomation.data.requestModel.onBoarding.EmailOnboardingRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.BankLookupStatus;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.BankVerificationInitiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.BankVerificationResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.BankVerificationStatus;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PanLookupResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PanLookupStatus;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PanPreVerificationStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PreVerificationAction;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding.PanLookupScenario;
import java.util.List;
import org.jarfinApiBackendAutomation.data.requestModel.consent.SubmitUserConsentRequest;
import org.jarfinApiBackendAutomation.data.requestModel.nominee.AddNomineeRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.VerifyMandateResponse;
import org.testng.annotations.DataProvider;
import sipSe.testData.auth.TestDataAuth;

@Slf4j
public class OnBoardingdataProvider {

    public static final int DEFAULT_MAX_RETRIES = 5;
    public static final long DEFAULT_DELAY_MS = 3000;

    public record PanConfirmData(String pan, String name, String dob, boolean skipConfirm) {}

    public enum PanConfirmAssertion {
        SUCCESS,
        UNAUTHORIZED,
        BAD_REQUEST
    }

    public record PanConfirmScenario(
            String description,
            String tokenOverride,
            String panOverride,
            PanConfirmAssertion assertion) {}

    @DataProvider(name = "panConfirmScenarios")
    public static Object[][] panConfirmScenarios() {
        return new Object[][] {
            {
                new PanConfirmScenario(
                        "Negative Scenario: Invalid Token",
                        "invalid_token",
                        null,
                        PanConfirmAssertion.UNAUTHORIZED)
            },
            {
                new PanConfirmScenario(
                        "Negative Scenario: Invalid PAN Format",
                        null,
                        "INVALID123",
                        PanConfirmAssertion.BAD_REQUEST)
            },
                {
                        new PanConfirmScenario(
                                "Positive Scenario: Valid Token + Valid PAN",
                                null,
                                null,
                                PanConfirmAssertion.SUCCESS)
                },
        };
    }

    @DataProvider(name = "submitEmailScenarios")
    public static Object[][] submitEmailScenarios() {
        return new Object[][] {
            {"Negative Scenario: Empty Email", EmailOnboardingRequest.buildEmailPayload(""), false},
            {
                "Negative Scenario: Null Email",
                EmailOnboardingRequest.buildEmailPayload(null),
                false
            },
            {
                "Positive Scenario: Valid Email",
                EmailOnboardingRequest.buildEmailPayload("qa@jarfinretail.com"),
                true
            },
        };
    }

    public static final Predicate<PanLookupResponse> PAN_LOOKUP_SHOULD_RETRY =
            response -> {
                PanLookupResponse.PanData data = response.getData();
                PanLookupStatus status = (data != null) ? data.getStatus() : null;
                return status == PanLookupStatus.LOOKUP_PENDING;
            };

    @DataProvider(name = "panLookupScenarios")
    public static Object[][] panLookupScenarios() {
        return new Object[][] {
//            {new PanLookupScenario("Negative Scenario: Invalid Token", false)},
            {new PanLookupScenario("Positive Scenario: Valid Token", true)},
        };
    }

    public static PanConfirmData resolvePanLookupStatus(
            PanLookupResponse response, OnboardingValidation validation) {

        PanLookupResponse.PanData data = response.getData();
        PanLookupStatus status =
                (data != null && data.getStatus() != null)
                        ? data.getStatus()
                        : PanLookupStatus.ENTER_PAN_MANUALLY;

        log.info("PAN Lookup resolved status={}", status);

        return switch (status) {
            case PAN_DATA_AVAILABLE -> {
                validation.assertPanLookupSuccess(response);
                log.info(
                        "PAN_DATA_AVAILABLE — pan={}, name={}, dob={}",
                        data.getPan(),
                        data.getName(),
                        data.getDob());
                yield new PanConfirmData(data.getPan(), data.getName(), data.getDob(), false);
            }
            case ENTER_PAN_MANUALLY -> {
                validation.assertPanLookupEnterManually(response);
                log.info(
                        "ENTER_PAN_MANUALLY — using manual values pan={}, name={}, dob={}",
                        TestDataAuth.MANUAL_PAN,
                        TestDataAuth.MANUAL_NAME,
                        TestDataAuth.MANUAL_DOB);
                yield new PanConfirmData(
                        TestDataAuth.MANUAL_PAN,
                        TestDataAuth.MANUAL_NAME,
                        TestDataAuth.MANUAL_DOB,
                        false);
            }
            case LOOKUP_PENDING -> {
                validation.assertPanLookupPending(response);
                log.warn("PAN Lookup still LOOKUP_PENDING after all retries");
                yield new PanConfirmData(null, null, null, false);
            }
            case KYC_VERIFIED -> {
                validation.assertPanLookupKycVerified(response);
                log.info("KYC_VERIFIED — PAN confirm flow will be skipped");
                yield new PanConfirmData(null, null, null, true);
            }
        };
    }

    public static final Predicate<PanPreVerificationStatusResponse> PAN_STATUS_SHOULD_RETRY =
            response -> {
                PanPreVerificationStatusResponse.DataResponse data = response.getData();
                return data != null && data.getAction() == PreVerificationAction.IN_PROGRESS;
            };

    public static final Predicate<BankVerificationResponse> BANK_VERIFICATION_SHOULD_RETRY =
            response -> {
                BankVerificationResponse.BankData data = response.getData();
                return data == null || data.getLookupStatus() != BankLookupStatus.FETCHED;
            };

    @DataProvider(name = "bankVerificationScenarios")
    public static Object[][] bankVerificationScenarios() {
        return new Object[][] {
            {new PanLookupScenario("Negative Scenario: Invalid Token", false)},
            {new PanLookupScenario("Positive Scenario: Valid Token", true)},
        };
    }

    public record BankVerificationInitiateScenario(String description, String tokenOverride) {}

    @DataProvider(name = "bankVerificationInitiateScenarios")
    public static Object[][] bankVerificationInitiateScenarios() {
        return new Object[][] {
            {new BankVerificationInitiateScenario("Negative Scenario: Invalid Token", "invalid_token")},
            {new BankVerificationInitiateScenario("Positive Scenario: Valid Token", null)},
        };
    }

    public static final Predicate<BankVerificationInitiateResponse> BANK_INITIATE_SHOULD_RETRY =
            response -> {
                BankVerificationInitiateResponse.VerificationData data = response.getData();
                return data == null || data.getVerificationId() == null;
            };

    @DataProvider(name = "panStatusScenarios")
    public static Object[][] panStatusScenarios() {
        return new Object[][] {
            {new PanLookupScenario("Negative Scenario: Invalid Token", false)},
            {new PanLookupScenario("Positive Scenario: Valid Token", true)},
        };
    }

    @DataProvider(name = "kycPrefillDetailsScenarios")
    public static Object[][] kycPrefillDetailsScenarios() {
        return new Object[][] {
            {new BankVerificationInitiateScenario("Negative Scenario: Invalid Token", "invalid_token")},
            {new BankVerificationInitiateScenario("Positive Scenario: Valid Token", null)},
        };
    }

    @DataProvider(name = "bankVerificationFetchScenarios")
    public static Object[][] bankVerificationFetchScenarios() {
        return new Object[][] {
            {new BankVerificationInitiateScenario("Negative Scenario: Invalid Token", "invalid_token")},
            {new BankVerificationInitiateScenario("Positive Scenario: Valid Token", null)},
        };
    }

    @DataProvider(name = "bankVerificationInitiateV2Scenarios")
    public static Object[][] bankVerificationInitiateV2Scenarios() {
        return new Object[][] {
            {new BankVerificationInitiateScenario("Negative Scenario: Invalid Token", "invalid_token")},
            {new BankVerificationInitiateScenario("Positive Scenario: Valid Token", null)},
        };
    }

    public record BankPreVerificationInitiateScenario(
            String description, String tokenOverride, String bankVerificationContext) {}

    public static final String DEFAULT_PRE_VERIFICATION = "DEFAULT_PRE_VERIFICATION";
    public static final String POST_RPD_PRE_VERIFICATION = "POST_RPD_PRE_VERIFICATION";

    @DataProvider(name = "bankPreVerificationInitiateScenarios")
    public static Object[][] bankPreVerificationInitiateScenarios() {
        return new Object[][] {
            {new BankPreVerificationInitiateScenario("Negative Scenario: Invalid Token", "invalid_token", POST_RPD_PRE_VERIFICATION)},
            {new BankPreVerificationInitiateScenario("Positive Scenario: Valid Token", null, POST_RPD_PRE_VERIFICATION)},
        };
    }

    public static final Predicate<PanPreVerificationStatusResponse> BANK_PRE_VERIFICATION_SHOULD_RETRY =
            response -> {
                PanPreVerificationStatusResponse.DataResponse data = response.getData();
                return data != null && data.getAction() == PreVerificationAction.IN_PROGRESS;
            };

    public static final Predicate<BankVerificationInitiateResponse> BANK_FETCH_SHOULD_RETRY =
            response -> {
                BankVerificationInitiateResponse.VerificationData data = response.getData();
                return data == null || data.getBankVerificationStatus() == BankVerificationStatus.PENDING;
            };

    public static final Predicate<VerifyMandateResponse> MANDATE_VERIFY_SHOULD_RETRY =
            response -> {
                VerifyMandateResponse.MandateVerifyData data = response.getData();
                return data == null || "PENDING".equalsIgnoreCase(data.getMandateStatus());
            };

    @DataProvider(name = "bankPreVerificationStatusScenarios")
    public static Object[][] bankPreVerificationStatusScenarios() {
        return new Object[][] {
            {new PanLookupScenario("Negative Scenario: Invalid Token", false)},
            {new PanLookupScenario("Positive Scenario: Valid Token", true)},
        };
    }

    public record MandateSetupScenario(String description, String tokenOverride) {}

    @DataProvider(name = "mandateSetupScenarios")
    public static Object[][] mandateSetupScenarios() {
        return new Object[][] {
            {new MandateSetupScenario("Negative Scenario: Invalid Token", "invalid_token")},
            {new MandateSetupScenario("Positive Scenario: Valid Token", null)},
        };
    }

    public enum ConsentAssertion {
        SUCCESS,
        UNAUTHORIZED,
        BAD_REQUEST
    }

    public record ConsentScenario(
            String description,
            String tokenOverride,
            SubmitUserConsentRequest request,
            ConsentAssertion assertion) {}

    @DataProvider(name = "submitConsentScenarios")
    public static Object[][] submitConsentScenarios() {
        return new Object[][] {
            {new ConsentScenario("Negative Scenario: Invalid Token", "invalid_token", SubmitUserConsentRequest.build("consent_abc123", List.of("+91 9876543210")), ConsentAssertion.UNAUTHORIZED)},
            {new ConsentScenario("Negative Scenario: Missing consentId", null, SubmitUserConsentRequest.build(null), ConsentAssertion.BAD_REQUEST)},
            {new ConsentScenario("Positive Scenario: Valid Consent", null, SubmitUserConsentRequest.build("6a0c16ec38d726313b5d8fe7", List.of("+91 9876543210")), ConsentAssertion.SUCCESS)},
        };
    }

    public enum NomineeAssertion {
        SUCCESS,
        UNAUTHORIZED,
        BAD_REQUEST
    }

    public record NomineeScenario(
            String description, String tokenOverride, AddNomineeRequest request, NomineeAssertion assertion) {}

    @DataProvider(name = "addNomineeScenarios")
    public static Object[][] addNomineeScenarios() {
        return new Object[][] {
            {new NomineeScenario("Negative Scenario: Invalid Token", "invalid_token", AddNomineeRequest.buildDefault(), NomineeAssertion.UNAUTHORIZED)},
            {new NomineeScenario("Negative Scenario: Missing Required Field", null, AddNomineeRequest.builder().dob("1992-03-21").relation("SPOUSE").build(), NomineeAssertion.BAD_REQUEST)},
            {new NomineeScenario("Positive Scenario: Valid Nominee", null, AddNomineeRequest.buildDefault(), NomineeAssertion.SUCCESS)},
        };
    }
}
