package se.swedenconnect.eidas.test.cef20demohub.process;

import eu.eidas.SimpleProtocol.AuthenticationRequest;
import eu.eidas.SimpleProtocol.RequestedAuthenticationContext;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import org.springframework.stereotype.Component;
import se.swedenconnect.eidas.test.cef20demohub.data.RequestData;
import se.swedenconnect.eidas.test.cef20demohub.data.RequestModel;

import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

@Component
public class RequestGenerator {

    public RequestData getRequest(RequestModel requestModel) throws JAXBException {
        final AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setId(UUID.randomUUID().toString());
        authenticationRequest.setServiceUrl(requestModel.getReturnUrl());
        authenticationRequest.setProviderName(requestModel.getProviderName());
        authenticationRequest.setCitizenCountry(requestModel.getCitizenCountry());
        String eidasNameIdentifier = requestModel.getEidasNameIdentifier();
        if (RequestModel.PERSISTENT.equalsIgnoreCase(eidasNameIdentifier)
                || RequestModel.TRANSIENT.equalsIgnoreCase(eidasNameIdentifier)
                || RequestModel.UNSPECIFIED.equalsIgnoreCase(eidasNameIdentifier))
            authenticationRequest.setNameIdPolicy(eidasNameIdentifier);
        authenticationRequest.setSpType(requestModel.getEidasSPType().getValue());

        //Set LoA
        final RequestedAuthenticationContext requestedAuthenticationContext = new RequestedAuthenticationContext();
        final ArrayList<String> levelOfAssurances = new ArrayList<>();
        levelOfAssurances.add(requestModel.getEidasloa());
        requestedAuthenticationContext.setContextClass(levelOfAssurances);
        requestedAuthenticationContext.setComparison(requestModel.getEidasloaCompareType().getValue());
        authenticationRequest.setAuthContext(requestedAuthenticationContext);

        //Set attributes
        authenticationRequest.setAttributes(requestModel.getSimpleAttributes());
        String jsonRequest = new SimpleProtocolProcess().convert2Json(authenticationRequest);
        String b64Request = new String(Base64.getEncoder().encode(jsonRequest.getBytes(StandardCharsets.UTF_8)));

        //Create return object
        RequestData requestData = new RequestData();
        requestData.setAuthnRequest(authenticationRequest);
        requestData.setBase64Request(b64Request);
        return requestData;
    }

}
