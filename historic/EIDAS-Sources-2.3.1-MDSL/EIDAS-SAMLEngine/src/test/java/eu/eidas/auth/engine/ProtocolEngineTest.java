/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.engine;


import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;
import org.opensaml.saml.saml2.core.Response;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;


/**
 * ProtocolEngineTest
 *
 * @since 1.1
 */
public final class ProtocolEngineTest {


    @Test
    public void unmarshallResponseAndValidate() throws Exception {

        ProtocolEngineI protocolEngine = DefaultProtocolEngineFactory.getInstance().getProtocolEngine("METADATATEST");

        final String ISSUER = "https://source.europa.eu/metadata";
        
		EidasAuthenticationRequest request = EidasAuthenticationRequest.builder()
                .id("_1")
                .issuer(ISSUER)
                .destination("https://destination.europa.eu")
                .citizenCountryCode("BE")
                .originCountryCode("BE")
                .providerName("Prov")
                .assertionConsumerServiceURL(ISSUER)
                .requestedAttributes(ImmutableAttributeMap.of(EidasSpec.Definitions.PERSON_IDENTIFIER,
                                                              new StringAttributeValue[] {}))
                .build();

        IRequestMessage requestMessage =
                protocolEngine.generateRequestMessage(request, "https://destination.europa.eu/metadata");

        AuthenticationResponse response = AuthenticationResponse.builder()
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .id("_2")
                .inResponseTo(request.getId())
                .issuer("https://destination.europa.eu/metadata")
                .subject("UK/UK/Bankys")
                .subjectNameIdFormat("urn:oasis:names:tc:saml2:2.0:nameid-format:persistent")
                .attributes(ImmutableAttributeMap.of(EidasSpec.Definitions.PERSON_IDENTIFIER,
                        new StringAttributeValue("LU/BE/1", false)))
                .build();

        IResponseMessage responseMessage = protocolEngine.generateResponseMessage(request, response, true, "127.0.0.1");

        System.out.println("responseMessage = " + EidasStringUtil.toString(responseMessage.getMessageBytes()));
        // hack to look inside what was really generated:
        Response samlResponse = (Response) OpenSamlHelper.unmarshall(responseMessage.getMessageBytes());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());

        Correlated correlated = protocolEngine.unmarshallResponse(responseMessage.getMessageBytes());

        IAuthenticationResponse authenticationResponse =
                protocolEngine.validateUnmarshalledResponse(correlated, "127.0.0.1", 0L, 0L, null);

        assertFalse(authenticationResponse.getStatus().isFailure());
    }
}
