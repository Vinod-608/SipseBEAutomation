package org.jarfinApiBackendAutomation.data.requestModel.kyc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.kyc.PrefillKycDetailsResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmitProfileRequest {
    private String maritalStatus;
    private String gender;
    private String incomeSlab;
    private String occupationType;
    private String nationalityCountry;
    private String fatherName;
    private String spouseName;
    private String email;
    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
    private Geolocation geolocation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Geolocation {
        private Double latitude;
        private Double longitude;
    }

    public static SubmitProfileRequest build(PrefillKycDetailsResponse.PrefillData prefill) {
        return SubmitProfileRequest.builder()
                .gender(prefill != null && prefill.getGender() != null
                        ? prefill.getGender() : "MALE")
                .incomeSlab(prefill != null && prefill.getIncomeSlab() != null
                        ? prefill.getIncomeSlab() : "ABOVE_5LAKH_UPTO_10LAKH")
                .occupationType(prefill != null && prefill.getOccupationType() != null
                        ? prefill.getOccupationType() : "PRIVATE_SECTOR")
                .fatherName(prefill != null && prefill.getFatherName() != null
                        ? prefill.getFatherName() : "Test")
                .nationalityCountry("in")
                .maritalStatus("UNMARRIED")
                .addressLine1("Ggdggg")
                .state("Arunachal Pradesh")
                .pincode("560010")
                .geolocation(Geolocation.builder()
                        .latitude(12.9353207)
                        .longitude(77.6149771)
                        .build())
                .build();
    }
}
