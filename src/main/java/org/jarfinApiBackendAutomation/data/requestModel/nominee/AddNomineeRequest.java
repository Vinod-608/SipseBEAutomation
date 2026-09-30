package org.jarfinApiBackendAutomation.data.requestModel.nominee;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddNomineeRequest {
    private String name;
    private String dob;
    private String relation;
    private String emailAddress;
    private String line;
    private String pincode;
    private String phoneNumber;
    private String pan;
    private Boolean isMinor;
    private String guardianName;
    private String guardianEmailAddress;
    private String guardianPhoneNumber;
    private String guardianPan;

    public static AddNomineeRequest buildDefault() {
        return AddNomineeRequest.builder()
                .name("Jane Doe")
                .dob("1992-03-21")
                .relation("SPOUSE")
                .emailAddress("jane@example.com")
                .phoneNumber("+919876543210")
                .line("cacncanja")
                .pan("BNZPM2501F")
                .pincode("560010")
                .build();
    }



    public static AddNomineeRequest buildMinor(String guardianName) {
        return AddNomineeRequest.builder()
                .name("Minor Nominee")
                .dob("2018-06-15")
                .relation("CHILD")
                .isMinor(true)
                .guardianName(guardianName)
                .guardianEmailAddress("guardian@example.com")
                .guardianPhoneNumber("+919876543210")
                .build();
    }
}
