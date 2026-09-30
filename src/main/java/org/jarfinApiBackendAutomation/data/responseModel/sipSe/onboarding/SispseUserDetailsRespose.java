package org.jarfinApiBackendAutomation.data.responseModel.sipSe.onboarding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SispseUserDetailsRespose extends CommonResultModel {
    private boolean success;
    private UserData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserData {
        private String id;
        private String name;
        private String phoneNumber;
        private String email;
        private String dob;
        private String fatherName;
        private String gender;
        private Address address;
        private boolean isKycVerified;
        private String kycStatus;
        private BankDetails bankDetails;
        private String incomeSlab;
        private String occupationType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String pincode;
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankDetails {
        private String bankName;
        private String bankAccountNumber;
        private String bankVerificationStatus;
    }
}
