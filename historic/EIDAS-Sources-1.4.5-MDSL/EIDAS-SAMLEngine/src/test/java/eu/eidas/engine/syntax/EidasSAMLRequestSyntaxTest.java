/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.engine.syntax;

import org.junit.Before;
import org.junit.Test;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

/**
 * EidasSAMLRequestSyntaxTest
 *
 * @since 1.1
 */
public class EidasSAMLRequestSyntaxTest {

    private byte[] samlToken;

    private String samlTokenString;

    @Before
    public void setUp() throws Exception {
        samlToken = SyntaxTestUtil.createSAMLRequestToken();
        // removing CR/LF of the string
        final String oSIndependetNewlineDelimeter = System.lineSeparator();
        samlTokenString = (EidasStringUtil.toString(samlToken)).replaceAll(oSIndependetNewlineDelimeter, " ");
    }

    @Test
    public void testNormalValidationOnSAMLrequest() throws Exception {
        IAuthenticationRequest parsedRequest = SyntaxTestUtil.getEngine(SyntaxTestUtil.SAMLENGINE_CONF).unmarshallRequestAndValidate(
                samlToken, "BE",Arrays.asList(SyntaxTestUtil.ISSUER_REQUEST));
        assertNotNull(parsedRequest);
        assertFalse(parsedRequest.getRequestedAttributes().isEmpty());
    }

    @Test
    public void checkSaml2pHeader() {

        assertTrue("Request header SAML2 start tag failed: " + samlTokenString, samlTokenString.contains("<saml2p:AuthnRequest"));

        assertTrue("Request header SAML2 protocol failed: " + samlTokenString, samlTokenString.contains("xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\""));

        assertTrue("Request header xmlns:xmldsig failed", samlTokenString.contains("xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\""));

        assertTrue("Request header xmlns:eidas saml-extension failed", samlTokenString.contains("xmlns:eidas=\"http://eidas.europa.eu/saml-extensions"));

        assertTrue("Request header xmlns:eidas saml2-assertion failed", samlTokenString.contains("xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion"));

        assertTrue("Request header consent:unspecified failed", samlTokenString.contains("Consent=\"urn:oasis:names:tc:SAML:2.0:consent:unspecified\""));

        assertTrue("Request header destinationUrl failed", samlTokenString.contains("Destination=\"http://"));

        assertTrue("Request header forceAuthn failed", samlTokenString.contains("ForceAuthn=\"true\""));

        assertTrue("Request header ID failed", samlTokenString.contains("ID=\""));

        assertTrue("Request header isPassive=false failed", samlTokenString.contains("ProviderName=\""));

        assertTrue("Request header providerName failed", samlTokenString.contains("IsPassive=\"false\""));
    }

    @Test
    public void checkNameIdentifier() {
        assertTrue("Name identifier persistent or transient or unspecified not found", samlTokenString.contains("urn:oasis:names:tc:SAML:2.0:nameid-format:transient"));
    }

    @Test
    public void checkMetadataIssuer() {
        assertTrue("Request metadata issuer failed", samlTokenString.contains("<saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">http"));
    }

    @Test
    public void checkDsSignaturePresent() {
        assertTrue("Request signature block present: " + samlTokenString, samlTokenString.contains("<ds:Signature") && samlTokenString.contains("xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\""));
    }

    @Test
    public void checkSaml2pExtension() {
        assertTrue("Request saml2pExtension present", samlTokenString.contains("<saml2p:Extensions>"));
    }

    @Test
    public void checkRequestedAuthnContextMinimum() {
        assertTrue("Request saml2pExtension present", samlTokenString.contains("<saml2p:RequestedAuthnContext Comparison=\"minimum\">"));
    }

    @Test
    public void checkAuthnContextClassRefLoaLow() {
        assertTrue("Request AuthnContextClassRef Loa Low present", samlTokenString.contains("<saml2:AuthnContextClassRef>http://eidas.europa.eu/LoA/low</saml2:AuthnContextClassRef>"));
    }

    @Test
    public void checkSPType() {
        assertTrue("Request SPType present", samlTokenString.contains("<eidas:SPType>public</eidas:SPType>"));
    }

    @Test
    public void checkAttributes() {
        assertTrue("Request RequestedAttributes begin tag present", samlTokenString.contains("<eidas:RequestedAttributes>"));

        assertTrue("Request RequestedAttributes dateOfBirth attribute present", samlTokenString.matches(".*<eidas:RequestedAttribute FriendlyName=\"DateOfBirth\".*" +
                "Name=\"http://eidas.europa.eu/attributes/naturalperson/DateOfBirth\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\" isRequired=\"true\"/>.*"));

        assertTrue("Request RequestedAttributes PersonIdentifier attribute present", samlTokenString.matches(".*<eidas:RequestedAttribute FriendlyName=\"PersonIdentifier\".*" +
                "Name=\"http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\" isRequired=\"true\"/>.*"));

        assertTrue("Request RequestedAttributes FamilyName attribute present", samlTokenString.matches(".*<eidas:RequestedAttribute FriendlyName=\"FamilyName\".*" +
                "Name=\"http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\" isRequired=\"true\"/>.*"));

        assertTrue("Request RequestedAttributes Gender attribute present", samlTokenString.matches(".*<eidas:RequestedAttribute FriendlyName=\"Gender\".*" +
                "Name=\"http://eidas.europa.eu/attributes/naturalperson/Gender\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\" isRequired=\"false\"/>.*"));

        assertTrue("Request RequestedAttributes LegalPersonIdentifier attribute present", samlTokenString.matches(".*<eidas:RequestedAttribute FriendlyName=\"LegalPersonIdentifier\".*" +
                "Name=\"http://eidas.europa.eu/attributes/legalperson/LegalPersonIdentifier\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\" isRequired=\"true\"/>.*"));

        assertTrue("Request RequestedAttributes LegalName attribute present", samlTokenString.matches(".*<eidas:RequestedAttribute FriendlyName=\"LegalName\".*" +
                "Name=\"http://eidas.europa.eu/attributes/legalperson/LegalName\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\" isRequired=\"true\"/>.*"));

        assertTrue("Request RequestedAttributes EORI attribute present", samlTokenString.matches(".*<eidas:RequestedAttribute FriendlyName=\"EORI\".*" +
                "Name=\"http://eidas.europa.eu/attributes/legalperson/EORI\".*" +
                "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\" isRequired=\"false\"/>.*"));

        assertTrue("Request RequestedAttributes end tag present", samlTokenString.contains("</eidas:RequestedAttributes>"));
    }

}
