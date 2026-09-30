package org.jarfinApiBackendAutomation.data.common;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
public class RestRequest {
    private String url;
    private Map<String, String> headers;
    private Map<String, ?> queryParams;
    private Object body;
    private String rawFormBody;
}
