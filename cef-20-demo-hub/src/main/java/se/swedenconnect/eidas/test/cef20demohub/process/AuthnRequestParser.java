package se.swedenconnect.eidas.test.cef20demohub.process;

import eu.eidas.SimpleProtocol.AuthenticationRequest;
import eu.eidas.SimpleProtocol.Response;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

@Component
public class AuthnRequestParser {

    public AuthenticationRequest parseAuthnRequest(String b64Request) throws JAXBException {
        String jsonRequest = new String(Base64.getDecoder().decode(b64Request), StandardCharsets.UTF_8);
        AuthenticationRequest authenticationRequest = null;
        SimpleProtocolProcess simpleProtocolProcess = new SimpleProtocolProcess();
        StringReader stringReaderJson = new StringReader(jsonRequest);
        authenticationRequest = simpleProtocolProcess.convertFromJson(stringReaderJson, AuthenticationRequest.class);
        return authenticationRequest;
    }

}
