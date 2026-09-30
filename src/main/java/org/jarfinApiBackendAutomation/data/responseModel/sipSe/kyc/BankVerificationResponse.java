package org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankVerificationResponse extends CommonResultModel {

    private boolean success;
    private BankData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankData {
        private String bankName;
        private String accountNumber;
        private String accountIfsc;
        private BankLookupStatus lookupStatus;
        private String accountName;
        private BankAccountType accountType;
        private String verificationProvider;
        private BankAccountStatus accountStatus;
    }
}
