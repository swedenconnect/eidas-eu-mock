package eu.eidas.auth.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.signature.SignatureException;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.BinaryRequestMessage;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

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
                .attributes(ImmutableAttributeMap.of(EidasSpec.Definitions.PERSON_IDENTIFIER,
                                                     new StringAttributeValue("LU/BE/1", false)))
                .build();

        IResponseMessage responseMessage = protocolEngine.generateResponseMessage(request, response, true, "127.0.0.1");

        System.out.println("responseMessage = " + EidasStringUtil.toString(responseMessage.getMessageBytes()));
        // hack to look inside what was really generated:
        Response samlResponse = (Response) OpenSamlHelper.unmarshall(responseMessage.getMessageBytes());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());

        Correlated correlated = protocolEngine.unmarshallResponse(responseMessage.getMessageBytes(),Arrays.asList("https://destination.europa.eu/metadata"), true);

        IAuthenticationResponse authenticationResponse =
                protocolEngine.validateUnmarshalledResponse(correlated, "127.0.0.1", 0L, 0L, null);

        assertFalse(authenticationResponse.getStatus().isFailure());
    }
}
