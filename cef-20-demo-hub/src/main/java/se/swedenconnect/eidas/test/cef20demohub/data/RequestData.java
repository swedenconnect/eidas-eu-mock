package se.swedenconnect.eidas.test.cef20demohub.data;

import eu.eidas.SimpleProtocol.AuthenticationRequest;
import lombok.Data;

@Data
public class RequestData {
    AuthenticationRequest authnRequest;
    String base64Request;
}
