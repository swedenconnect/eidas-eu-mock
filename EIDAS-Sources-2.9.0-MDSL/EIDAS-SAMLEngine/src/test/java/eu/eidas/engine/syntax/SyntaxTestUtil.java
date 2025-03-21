/*
 * Copyright (c) 2024 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package eu.eidas.engine.syntax;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.auth.engine.metadata.FakeMetadata;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.Issuer;

import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;

/**
 * SyntaxTestUtil
 *
 * @since 1.1
 */
public class SyntaxTestUtil {

    public static final String SAMLENGINE_CONF = "CONF1";

    private static ImmutableAttributeMap newRequestImmutableAttributeMap() throws EIDASSAMLEngineException {

        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();

        builder.put(EidasSpec.Definitions.DATE_OF_BIRTH);

        builder.put(EidasSpec.Definitions.PERSON_IDENTIFIER);

        builder.put(EidasSpec.Definitions.CURRENT_FAMILY_NAME);

        builder.put(EidasSpec.Definitions.GENDER);

        builder.put(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER);

        builder.put(EidasSpec.Definitions.LEGAL_NAME);

        builder.put(EidasSpec.Definitions.EORI);

        return builder.build();
    }

    private static ImmutableAttributeMap newResponseImmutableAttributeMap() throws EIDASSAMLEngineException {

        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();

        builder.put(EidasSpec.Definitions.DATE_OF_BIRTH, "1899-01-25");

        builder.put(EidasSpec.Definitions.PERSON_IDENTIFIER, "BE123456");

        builder.put(EidasSpec.Definitions.CURRENT_FAMILY_NAME, "Paul Henri Spaak");

        builder.put(EidasSpec.Definitions.GENDER, "Unspecified");

        builder.put(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER, "LE132456BE");

        builder.put(EidasSpec.Definitions.LEGAL_NAME, "EuropeFunder");

        builder.put(EidasSpec.Definitions.EORI, "EORI1235648");

        return builder.build();
    }

    public static ProtocolEngineI getEngine(String conf) {
        final MetadataFetcherI metadataFetcher = Mockito.mock(MetadataFetcherI.class);
        ProtocolEngineI engine = null;
        try {
            Mockito.when(metadataFetcher.getEidasMetadata(any(Issuer.class), any(), any(), any())).then(invocationOnMock -> {
                final Issuer issuer = invocationOnMock.getArgument(0);
                final String metadataUrl = issuer.getValue();
                if (List.of(ISSUER_RESPONSE).contains(metadataUrl)) return FakeMetadata.proxyService();
                else return FakeMetadata.connector();
            });

            engine = ProtocolEngineFactory.createProtocolEngine(conf, new EidasProtocolProcessor(
                    "saml-engine-eidas-attributes-" + conf + ".xml",
                    "saml-engine-additional-attributes-" + conf + ".xml",
                    null,
                    metadataFetcher,
                    null,
                    null
            ));

        } catch (EIDASSAMLEngineException | EIDASMetadataException exc) {
            fail("Failed to initialize SAMLEngine");
        }
        return engine;
    }

    static final String ISSUER_REQUEST="http://localhost:7001/SP/metadata".toLowerCase();
    public static byte[] createSAMLRequestToken() throws EIDASSAMLEngineException {
        EidasAuthenticationRequest.Builder builder =
                new EidasAuthenticationRequest.Builder().id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                        .issuer(ISSUER_REQUEST)
                        .destination("http://proxyservice.gov.xx/EidasNode/ColleagueRequest")
                        .assertionConsumerServiceURL("http://connector.gov.xx/EidasNode/ColleagueResponse")
                        .providerName("my Service Provider")
                        .requestedAttributes(newRequestImmutableAttributeMap())
                        .citizenCountryCode("BE")
                        .spType("public")
                        .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                        .nameIdFormat(SamlNameIdFormat.TRANSIENT.getNameIdFormat());
        IAuthenticationRequest request = builder.build();

        return getEngine(SAMLENGINE_CONF).generateRequestMessage(request, ISSUER_RESPONSE).getMessageBytes();
    }

    public static AuthenticationResponse.Builder getPrefilledAuthenticationResponseBuilder() throws EIDASSAMLEngineException {
        return new AuthenticationResponse.Builder().id("123")
                .issuer(ISSUER_RESPONSE)
                .inResponseTo("456")
                .ipAddress("111.222.333.4444")
                .subject("UK/BE/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .attributes(newResponseImmutableAttributeMap());
    }

    static final String ISSUER_RESPONSE="http://C-PEPS.gov.xx".toLowerCase();
    public static byte[] createSAMLResponseToken(final byte[] requestToken) throws EIDASSAMLEngineException {

        AuthenticationResponse response = getPrefilledAuthenticationResponseBuilder()
                .build();

        IAuthenticationRequest request = getEidasAuthnRequestAndValidateFromToken(requestToken);

        IResponseMessage responseMessage =
                getEngine(SAMLENGINE_CONF).generateResponseMessage(request, response, "111.222.333.4444");

        return responseMessage.getMessageBytes();
    }

    public static byte[] createSAMLResponseToken(AuthenticationResponse response, final byte[] requestToken)
            throws EIDASSAMLEngineException {
        IAuthenticationRequest request = getEidasAuthnRequestAndValidateFromToken(requestToken);

        IResponseMessage responseMessage =
                getEngine(SAMLENGINE_CONF).generateResponseMessage(request, response, "111.222.333.4444");

        return responseMessage.getMessageBytes();
    }

    public static IAuthenticationRequest getEidasAuthnRequestAndValidateFromToken(final byte[] tokenSaml)
            throws EIDASSAMLEngineException {
        return getEngine(SAMLENGINE_CONF).unmarshallRequestAndValidate(tokenSaml, "BE");
    }
}
