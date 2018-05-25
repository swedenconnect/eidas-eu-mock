package se.swedenconnect.eidas.test.cef20demohub.data;

import eu.eidas.SimpleProtocol.Response;
import lombok.Data;

import java.util.Map;

@Data
public class ResponseData {
    private Response response;
    private String b64Response;
    private Map<String,Boolean> requestedAttributesMap;
    private boolean error;
    private String statusCode;
    private String errorMessage;
    private String errorMessageTitle;

    public ResponseData() {
        this.response = new Response();
    }
}
