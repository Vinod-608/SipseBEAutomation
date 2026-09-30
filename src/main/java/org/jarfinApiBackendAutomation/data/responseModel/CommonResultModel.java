package org.jarfinApiBackendAutomation.data.responseModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonResultModel {
    private int statusCode;
}
