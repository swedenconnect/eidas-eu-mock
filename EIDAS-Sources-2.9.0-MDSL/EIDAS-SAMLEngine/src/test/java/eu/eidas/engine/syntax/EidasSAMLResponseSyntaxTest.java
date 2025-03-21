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

import eu.eidas.RecommendedSecurityProviders;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.xml.opensaml.SAMLConsent;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * EidasSAMLResponseSyntaxTest
 *
 * @since 1.1
 */
public class EidasSAMLResponseSyntaxTest {

    private byte[] samlRequestToken;

    private byte[] samlResponseToken;

    private String samlResponseTokenString;

    @BeforeClass
    public static void setupClass() {
        RecommendedSecurityProviders.setupRecommendedSecurityProviders();
    }



    @Before
    public void setUp() throws Exception {
        samlRequestToken = SyntaxTestUtil.createSAMLRequestToken();

        samlResponseToken = SyntaxTestUtil.createSAMLResponseToken(samlRequestToken);
        // removing CR/LF of the string
        final String oSIndependetNewlineDelimeter = System.lineSeparator();
        samlResponseTokenString = (EidasStringUtil.toString(samlResponseToken)).replaceAll(oSIndependetNewlineDelimeter, " ");
    }

    @Test
    public void testNormalValidationOnSAMLrequest() throws Exception {
        assertNotNull(samlResponseToken);
        IAuthenticationResponse response = SyntaxTestUtil.getEngine(SyntaxTestUtil.SAMLENGINE_CONF).unmarshallResponseAndValidate(samlResponseToken, null, 0, 0, null);
        assertNotNull(response);
    }

    @Test
    public void checkSaml2pHeader() {
        assertTrue("Response header SAML2 start tag failed", samlResponseTokenString.contains("<saml2p:Response"));

        assertTrue("Response header SAML2 protocol failed", samlResponseTokenString.contains("xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\""));

        assertTrue("Response header xmlns:xmldsig failed", samlResponseTokenString.contains("xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\""));

        assertTrue("Response header natural person failed", samlResponseTokenString.contains("xmlns:eidas-natural=\"http://eidas.europa.eu/attributes/naturalperson\""));

        assertTrue("Response header issuer info failed: " + samlResponseTokenString, samlResponseTokenString.contains("<saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">http://c-peps.gov.xx</saml2:Issuer>"));
    }

    @Test
    public void checkInResponseToRequestId() throws Exception {
        IAuthenticationRequest request = SyntaxTestUtil.getEidasAuthnRequestAndValidateFromToken(samlRequestToken);
        assertTrue("Response InResponseTo " + request.getId() + " valid", samlResponseTokenString.contains("InResponseTo=\"" + request.getId() + "\""));
    }

    @Test
    public void checkStatusSuccess() {
        assertTrue("Response Success response", samlResponseTokenString.matches(".*<saml2p:Status>.*<saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>.*<saml2p:StatusMessage>urn:oasis:names:tc:SAML:2.0:status:Success</saml2p:StatusMessage>.*</saml2p:Status>.*"));
    }

    @Test
    public void checkEnveloppedSignature() {
        assertTrue("Response envelopped signature present", samlResponseTokenString.matches(".*<ds:Transforms>.*<ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>.*<ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\">.*"));
    }

    @Test
    public void checkIssuer() throws Exception {
        assertTrue("Response Issuer valid: " + samlResponseTokenString, samlResponseTokenString.contains("<saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">http://c-peps.gov.xx</saml2:Issuer>"));
    }

    @Test
    public void checkNameId() throws Exception {
        assertTrue("Response NameID transient present", samlResponseTokenString.matches(".*<saml2:NameID Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:transient\".*" +
                "NameQualifier=\"http://C-PEPS.gov.xx\">UK/BE/Banks.*</saml2:NameID>.*"));
    }

    @Test
    @Ignore
    public void checkSubjectConfirmation() throws Exception {
        assertTrue("Response SubjectConfirmation present", samlResponseTokenString.matches(".*<saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\">.*" +
                "<saml2:SubjectConfirmationData Address=\"111.222.333.4444\".*" +
                "InResponseTo=.*" +
                "NotOnOrAfter=.*" +
                "Recipient=\"http://connector.gov.xx/EidasNode/ColleagueResponse\"/>.*" +
                "</saml2:SubjectConfirmation>.*"));
    }

    @Test
    public void checkConditions() throws Exception {
        assertTrue("Response Conditions present", samlResponseTokenString.matches(".*<saml2:Conditions NotBefore=\".*" +
                "\" NotOnOrAfter=\".*\">.*" +
                "<saml2:AudienceRestriction>.*<saml2:Audience>http://localhost:7001/sp/metadata</saml2:Audience>.*" +
                "</saml2:AudienceRestriction>.*</saml2:Conditions>.*"));
    }

    @Test
    public void checkAttributes() {
        assertTrue("Response RequestedAttributes begin tag present", samlResponseTokenString.contains("<saml2:AttributeStatement>"));

        assertTrue("Response DateOfBirth AttributeValue present", samlResponseTokenString.matches(".*<saml2:Attribute FriendlyName=\"DateOfBirth\".*" +
                "Name=\"http://eidas.europa.eu/attributes/naturalperson/DateOfBirth\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">.*" +
                "<saml2:AttributeValue.*" +
                "xsi:type=\"eidas-natural:DateOfBirthType\">1899-01-25.*" +
                "</saml2:AttributeValue>.*" +
                "</saml2:Attribute>.*"));


        assertTrue("Response PersonIdentifier AttributeValue present", samlResponseTokenString.matches(".*<saml2:Attribute FriendlyName=\"PersonIdentifier\".*" +
                "Name=\"http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">.*" +
                "<saml2:AttributeValue.*" +
                "xsi:type=\"eidas-natural:PersonIdentifierType\">BE123456.*" +
                "</saml2:AttributeValue>.*" +
                "</saml2:Attribute>.*"));

        assertTrue("Response CurrentFamilyName AttributeValue present", samlResponseTokenString.matches(".*<saml2:Attribute FriendlyName=\"FamilyName\".*" +
                "Name=\"http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">.*" +
                "<saml2:AttributeValue.*" +
                "xsi:type=\"eidas-natural:CurrentFamilyNameType\">Paul Henri Spaak.*" +
                "</saml2:AttributeValue>.*" +
                "</saml2:Attribute>.*"));

        assertTrue("Response Gender AttributeValue present", samlResponseTokenString.matches(".*<saml2:Attribute FriendlyName=\"Gender\".*" +
                "Name=\"http://eidas.europa.eu/attributes/naturalperson/Gender\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">.*" +
                "<saml2:AttributeValue.*" +
                "xsi:type=\"eidas-natural:GenderType\">Unspecified.*" +
                "</saml2:AttributeValue>.*" +
                "</saml2:Attribute>.*"));

        assertTrue("Response LegalPersonIdentifier AttributeValue present", samlResponseTokenString.matches(".*<saml2:Attribute FriendlyName=\"LegalPersonIdentifier\".*" +
                "Name=\"http://eidas.europa.eu/attributes/legalperson/LegalPersonIdentifier\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">.*" +
                "<saml2:AttributeValue.*" +
                "xsi:type=\"eidas-legal:LegalPersonIdentifierType\">LE132456BE.*" +
                "</saml2:AttributeValue>.*" +
                "</saml2:Attribute>.*"));

        assertTrue("Response LegalName AttributeValue present", samlResponseTokenString.matches(".*<saml2:Attribute FriendlyName=\"LegalName\".*" +
                "Name=\"http://eidas.europa.eu/attributes/legalperson/LegalName\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">.*" +
                "<saml2:AttributeValue.*" +
                "xsi:type=\"eidas-legal:LegalNameType\">EuropeFunder.*" +
                "</saml2:AttributeValue>.*" +
                "</saml2:Attribute>.*"));

        assertTrue("Response EORI AttributeValue present", samlResponseTokenString.matches(".*<saml2:Attribute FriendlyName=\"EORI\".*" +
                "Name=\"http://eidas.europa.eu/attributes/legalperson/EORI\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">.*" +
                "<saml2:AttributeValue.*" +
                "xsi:type=\"eidas-legal:EORIType\">EORI1235648.*" +
                "</saml2:AttributeValue>.*" +
                "</saml2:Attribute>.*"));

        assertTrue("Response RequestedAttributes begin tag present", samlResponseTokenString.contains("</saml2:AttributeStatement>"));
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.ProtocolEngineI#generateResponseMessage(IAuthenticationRequest, IAuthenticationResponse, String)}
     * when consent from the LightResponse is "obtained"
     * Then the eIDAS SAMLResponse must have the "obtained" SAML consent value in the header.
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkSAMLResponseWithObtainedConsentValue() throws EIDASSAMLEngineException {
        AuthenticationResponse authenticationResponse = SyntaxTestUtil.getPrefilledAuthenticationResponseBuilder()
                .consent(SAMLConsent.OBTAINED.getValue())
                .build();

        samlResponseToken = SyntaxTestUtil.createSAMLResponseToken(authenticationResponse, samlRequestToken);
        samlResponseTokenString = (EidasStringUtil.toString(samlResponseToken)).replaceAll(System.lineSeparator(), " ");

        assertTrue("Response header consent obtained failed", samlResponseTokenString.contains("Consent=\"urn:oasis:names:tc:SAML:2.0:consent:obtained\""));
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.ProtocolEngineI#generateResponseMessage(IAuthenticationRequest, IAuthenticationResponse, String)}
     * when consent from the LightResponse is not a valid SAML consent value
     * Then the eIDAS SAMLResponse must have the "unspecified" SAML consent value in the header.
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkSAMLResponseWithAnUnknownConsentValue() throws EIDASSAMLEngineException {
        AuthenticationResponse authenticationResponse = SyntaxTestUtil.getPrefilledAuthenticationResponseBuilder()
                .consent("unknownConsentValue")
                .build();

        samlResponseToken = SyntaxTestUtil.createSAMLResponseToken(authenticationResponse, samlRequestToken);
        samlResponseTokenString = (EidasStringUtil.toString(samlResponseToken)).replaceAll(System.lineSeparator(), " ");

        assertTrue("Response header consent unspecified failed", samlResponseTokenString.contains("Consent=\"urn:oasis:names:tc:SAML:2.0:consent:unspecified\""));
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.ProtocolEngineI#generateResponseMessage(IAuthenticationRequest, IAuthenticationResponse, String)}
     * when consent from the LightResponse is not present
     * Then the eIDAS SAMLResponse must have the "unspecified" SAML consent value in the header.
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkSAMLResponseWhenNoConsentValueInLightResponse() throws EIDASSAMLEngineException {
        AuthenticationResponse authenticationResponse = SyntaxTestUtil.getPrefilledAuthenticationResponseBuilder()
                .build();

        samlResponseToken = SyntaxTestUtil.createSAMLResponseToken(authenticationResponse, samlRequestToken);
        samlResponseTokenString = (EidasStringUtil.toString(samlResponseToken)).replaceAll(System.lineSeparator(), " ");

        assertTrue("Response header consent unspecified failed", samlResponseTokenString.contains("Consent=\"urn:oasis:names:tc:SAML:2.0:consent:unspecified\""));
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.ProtocolEngineI#generateResponseMessage(IAuthenticationRequest, IAuthenticationResponse, String)}
     * when consent from the LightResponse is the "unspecified" SAML consent value
     * Then the eIDAS SAMLResponse must have the "unspecified" SAML consent value in the header.
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkSAMLResponseWithUnspecifiedConsentValue() throws EIDASSAMLEngineException {
        AuthenticationResponse authenticationResponse = SyntaxTestUtil.getPrefilledAuthenticationResponseBuilder()
                .consent(SAMLConsent.UNSPECIFIED.getValue())
                .build();

        samlResponseToken = SyntaxTestUtil.createSAMLResponseToken(authenticationResponse, samlRequestToken);
        samlResponseTokenString = (EidasStringUtil.toString(samlResponseToken)).replaceAll(System.lineSeparator(), " ");

        assertTrue("Response header consent unspecified failed", samlResponseTokenString.contains("Consent=\"urn:oasis:names:tc:SAML:2.0:consent:unspecified\""));
    }
}
