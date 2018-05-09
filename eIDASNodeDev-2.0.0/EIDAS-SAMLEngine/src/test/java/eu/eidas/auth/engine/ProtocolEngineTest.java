/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 *  EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the
 * "NOTICE" text file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution.
 * Any derivative works that you distribute must include a readable
 * copy of the "NOTICE" text file.
 */
package eu.eidas.auth.engine;

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

import org.junit.Test;
import org.opensaml.saml.saml2.core.Response;

import static org.junit.Assert.assertFalse;

/**
 * ProtocolEngineTest
 *
 * @since 1.1
 */
public final class ProtocolEngineTest {

    @Test
    public void unmarshallResponseAndValidate() throws Exception {

        ProtocolEngineI protocolEngine = DefaultProtocolEngineFactory.getInstance().getProtocolEngine("METADATATEST");

        EidasAuthenticationRequest request = EidasAuthenticationRequest.builder()
                .id("_1")
                .issuer("https://source.europa.eu/metadata")
                .destination("https://destination.europa.eu")
                .citizenCountryCode("BE")
                .originCountryCode("BE")
                .providerName("Prov")
                .assertionConsumerServiceURL("https://source.europa.eu/metadata")
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
