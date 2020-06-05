package eu.eidas.engine.syntax;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import eu.eidas.engine.Authorization.EidasProtocolProcessorWithAutorization;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

/**
 * EidasSAMLResponseWithAuthorizationSyntaxTest
 *
 * @since 2016-09-05
 */
public class EidasSAMLResponseWithAuthorizationSyntaxTest {

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

    private byte[] samlRequestToken;

    private byte[] samlResponseToken;

    private String samlResponseTokenString;

    public static byte[] createSAMLResponseTokenWithAuthorization(final byte[] requestToken) throws EIDASSAMLEngineException {

        AuthenticationResponse response = new AuthenticationResponse.Builder().id("123")
                .issuer("http://C-PEPS.gov.xx")
                .inResponseTo("456")
                .ipAddress("111.222.333.4444")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .attributes(newResponseImmutableAttributeMap())
                .build();

        IAuthenticationRequest request = getEidasAuthnRequestAndValidateFromToken(requestToken);

        IResponseMessage responseMessage =
                getAuthorizationEngine(SyntaxTestUtil.SAMLENGINE_CONF).generateResponseMessage(request, response, false, "111.222.333.4444");

        return responseMessage.getMessageBytes();
    }

    public static IAuthenticationRequest getEidasAuthnRequestAndValidateFromToken(final byte[] tokenSaml)
            throws EIDASSAMLEngineException {
        return getAuthorizationEngine(SyntaxTestUtil.SAMLENGINE_CONF).unmarshallRequestAndValidate(tokenSaml, "BE",Arrays.asList(SyntaxTestUtil.ISSUER_REQUEST));
    }

    public static ProtocolEngineI getAuthorizationEngine(String conf) {
        ProtocolEngineI engine = null;
        try {
            engine = ProtocolEngineFactory.createProtocolEngine(conf, new EidasProtocolProcessorWithAutorization(
                    "saml-engine-eidas-attributes-" + conf + ".xml",
                    "saml-engine-additional-attributes-" + conf + ".xml", null, null, null));

        } catch (EIDASSAMLEngineException exc) {
            fail("Failed to initialize SAMLEngine");
        }
        return engine;
    }

    @Before
    public void setUp() throws Exception {
        samlRequestToken = SyntaxTestUtil.createSAMLRequestToken();

        samlResponseToken = createSAMLResponseTokenWithAuthorization(samlRequestToken);
        // removing CR/LF of the string
        final String oSIndependetNewlineDelimeter = System.lineSeparator();
        samlResponseTokenString = (EidasStringUtil.toString(samlResponseToken)).replaceAll(oSIndependetNewlineDelimeter, " ");
    }

    @Test
    public void testNormalValidationOnSAMLrequest() throws Exception {
        assertNotNull(samlResponseToken);
        IAuthenticationResponse response = SyntaxTestUtil.getEngine(SyntaxTestUtil.SAMLENGINE_CONF).unmarshallResponseAndValidate(samlResponseToken, null, 0, 0, null,Arrays.asList(SyntaxTestUtil.ISSUER_RESPONSE),false);
        assertNotNull(response);
    }

    @Test
    public void checkAuthroizationStatementPresent() {
        /* Checks the following String can be found
            <saml2:AuthzDecisionStatement Decision="Permit" Resource="testResource">
                <saml2:Action Namespace="urn:testNamespace">testAction</saml2:Action>
            </saml2:AuthzDecisionStatement>
        */
        assertTrue("Response AuthzDecisionStatement decision", samlResponseTokenString.contains("<saml2:AuthzDecisionStatement Decision=\"Permit\" Resource=\""));
        assertTrue("Response header SAML2 protocol failed", samlResponseTokenString.contains("</saml2:Action></saml2:AuthzDecisionStatement>"));
    }

}
