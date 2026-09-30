package org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PanPreVerificationStatusResponse extends CommonResultModel {

    private boolean success;
    private DataResponse data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataResponse {
        private PreVerificationContext currentContext;
        private String providerStatus;
        private PreVerificationAction action;
        private boolean kycVerified;
        private String message;
        private String bankAccountNumber;
        private String preVerificationId;
        private PreVerificationResponse preVerificationResponse;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PreVerificationResponse {
        private String object;
        private String id;
        private String status;
        private VerifiedField readiness;
        private VerifiedField name;
        private VerifiedField pan;

        @JsonProperty("investor_identifier")
        private String investorIdentifier;

        @JsonProperty("date_of_birth")
        private VerifiedField dateOfBirth;

        @JsonProperty("bank_accounts")
        private List<BankAccountField> bankAccounts;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("completed_at")
        private String completedAt;

        @JsonProperty("updated_at")
        private String updatedAt;
    }

    /** Used for readiness, name, pan, date_of_birth — all share {status, code, reason, value: String} */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifiedField {
        private String status;
        private String code;
        private String reason;
        private String value;
    }

    /** One entry inside bank_accounts array: {status, code, reason, value: BankAccountValue} */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankAccountField {
        private String status;
        private String code;
        private String reason;
        private BankAccountValue value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankAccountValue {

        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("ifsc_code")
        private String ifscCode;

        @JsonProperty("account_type")
        private String accountType;

        @JsonProperty("bank_account_proof")
        private Object bankAccountProof;
    }
}
