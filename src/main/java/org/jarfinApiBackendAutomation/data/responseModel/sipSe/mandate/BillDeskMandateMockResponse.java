package org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillDeskMandateMockResponse {
    private int statusCode;
    private String responseBody;

    private int callbackSimulatorStatus;
    private int callbackResponseStatus;
    private int processNpciRespStatus;
    private int finprimBillDeskCallbackStatus;
    private int finprimOndcCallbackStatus;

    private String uniqueID;
    private String mandateRespDoc;
    private String mandateResponse;
}
