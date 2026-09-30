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
public class PrefillKycDetailsResponse extends CommonResultModel {

    private boolean success;
    private PrefillData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrefillData {
        private String name;
        private String dob;
        private String gender;
        private String incomeSlab;
        private String occupationType;
        private String fatherName;
        private Address address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String pincode;
        private String country;
    }
}
