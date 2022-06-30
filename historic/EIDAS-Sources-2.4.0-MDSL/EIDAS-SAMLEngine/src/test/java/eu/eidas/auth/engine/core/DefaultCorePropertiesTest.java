/*
 * Copyright (c) 2019 by European Commission
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
 *
 */

package eu.eidas.auth.engine.core;

import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Test class for {@link DefaultCoreProperties}
 */
public class DefaultCorePropertiesTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Properties)}
     * when the properties are null an {@link IllegalArgumentException} is thrown.
     * <p>
     * Must fail and throw {@link IllegalArgumentException}
     */
    @Test
    public void testIllegalArgumentExceptionWhenNoProperties() {
        expectedException.expect(IllegalArgumentException.class);

        new DefaultCoreProperties((Properties) null);
    }

    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Properties)}
     * when the properties contain a blank value for {@link SAMLCore#PROT_BINDING_TAG} a {@link EIDASSAMLEngineRuntimeException} is thrown.
     * <p>
     * Must fail and throw {@link EIDASSAMLEngineRuntimeException}
     */
    @Test
    public void testEIDASSAMLEngineRuntimeExceptionWhenProtBindingTagBlank() {

        expectedException.expect(EIDASSAMLEngineRuntimeException.class);
        expectedException.expectMessage("Error (no. null) processing request : SamlEngine.xml - PROT_BINDING_TAG is mandatory. - null");

        new DefaultCoreProperties(new Properties());
    }

    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Properties)}
     * when the properties contain an unknown value for {@link SAMLCore#PROT_BINDING_TAG} a {@link EIDASSAMLEngineRuntimeException} is thrown.
     * <p>
     * Must fail and throw {@link EIDASSAMLEngineRuntimeException}
     */
    @Test
    public void testEIDASSAMLEngineRuntimeExceptionWhenProtBindingTagUnknown() {

        expectedException.expect(EIDASSAMLEngineRuntimeException.class);
        expectedException.expectMessage("Error (no. null) processing request : SamlEngine.xml - PROT_BINDING_TAG is not supported (unknown). - null");

        Properties instance = new Properties();
        instance.put(SAMLCore.PROT_BINDING_TAG.getValue(), "unknown");
        new DefaultCoreProperties(instance);
    }

    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Properties)}
     * when the properties contain an unknown value for {@link SAMLCore#CONSENT_AUTHN_RES} a {@link EIDASSAMLEngineRuntimeException} is thrown.
     * <p>
     * Must fail and throw {@link EIDASSAMLEngineRuntimeException}
     */
    @Test
    public void testEIDASSAMLEngineRuntimeExceptionWhenConsentAuthnResponseUnknown() {

        expectedException.expect(EIDASSAMLEngineRuntimeException.class);
        expectedException.expectMessage("Error (no. null) processing request : SamlEngine.xml - consentAuthnResponse is not supported (unknown). - null");

        Properties instance = new Properties();
        instance.put(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        instance.put(SAMLCore.CONSENT_AUTHN_RES.getValue(), "unknown");

        new DefaultCoreProperties(instance);
    }

    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Properties)}
     * validate that when consent authentication response is read, and contains only whitespaces, the value is
     * set to null.
     * Must succeed.
     */
    @Test
    public void testConsentAuthResSetToNullWhenBlank() {
        Properties instance = new Properties();
        instance.put(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        instance.put(SAMLCore.CONSENT_AUTHN_REQ.getValue(), "obtained");
        instance.put("timeNotOnOrAfter", "5");
        instance.put(SAMLCore.CONSENT_AUTHN_RES.getValue(), "   ");

        DefaultCoreProperties defaultCoreProperties = new DefaultCoreProperties(instance);

        assertNull(defaultCoreProperties.getConsentAuthnResponse());
    }

    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Properties)}
     * validate all ok when properties contain a valid value for {@link SAMLCore#CONSENT_AUTHN_REQ}
     * Valid values are:
     * <ul>
     * <li>obtained</li>
     * <li>prior</li>
     * <li>current-implicit</li>
     * <li>current-explicit</li>
     * <li>unspecified</li>
     * <li>unavailable</li>
     * <li>inapplicable</li>
     * </ul>
     * <p>
     * Must succeed.
     */
    @Test
    public void testConsentAuthReqValid() {
        Map<String, String> validConsentValues = new HashMap<>();
        validConsentValues.put("obtained", "urn:oasis:names:tc:SAML:2.0:consent:obtained");
        validConsentValues.put("prior", "urn:oasis:names:tc:SAML:2.0:consent:prior");
        validConsentValues.put("current-implicit", "urn:oasis:names:tc:SAML:2.0:consent:current-implicit");
        validConsentValues.put("current-explicit", "urn:oasis:names:tc:SAML:2.0:consent:current-explicit");
        validConsentValues.put("unspecified", "urn:oasis:names:tc:SAML:2.0:consent:unspecified");
        validConsentValues.put("unavailable", "urn:oasis:names:tc:SAML:2.0:consent:unavailable");
        validConsentValues.put("inapplicable", "urn:oasis:names:tc:SAML:2.0:consent:inapplicable");

        DefaultCoreProperties defaultCoreProperties;

        Properties instance = new Properties();
        instance.put(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        instance.put(SAMLCore.CONSENT_AUTHN_RES.getValue(), "obtained");
        instance.put("timeNotOnOrAfter", "5");

        for (Map.Entry<String, String> entry : validConsentValues.entrySet()) {
            instance.put(SAMLCore.CONSENT_AUTHN_REQ.getValue(), entry.getKey());
            defaultCoreProperties = new DefaultCoreProperties(instance);
            assertEquals(entry.getValue(), defaultCoreProperties.getConsentAuthnRequest());
        }
    }

    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Properties)}
     * validate that when consent authentication request is read, and contains only whitespaces, the value is
     * set to null.
     * Must succeed.
     */
    @Test
    public void testConsentAuthReqSetToNullWhenBlank() {
        Properties instance = new Properties();
        instance.put(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        instance.put(SAMLCore.CONSENT_AUTHN_RES.getValue(), "obtained");
        instance.put("timeNotOnOrAfter", "5");
        instance.put(SAMLCore.CONSENT_AUTHN_REQ.getValue(), "   ");

        DefaultCoreProperties defaultCoreProperties = new DefaultCoreProperties(instance);

        assertNull(defaultCoreProperties.getConsentAuthnRequest());
    }


    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Properties)}
     * validate all valid when the properties contain a valid value for {@link SAMLCore#CONSENT_AUTHN_RES}
     * Valid values are:
     * <ul>
     * <li>obtained</li>
     * <li>prior</li>
     * <li>current-implicit</li>
     * <li>current-explicit</li>
     * <li>unspecified</li>
     * <li>unavailable</li>
     * <li>inapplicable</li>
     * </ul>
     * <p>
     * Must succeed.
     */
    @Test
    public void testConsentAuthRespValid() {
        Map<String, String> validConsentValues = new HashMap<>();
        validConsentValues.put("obtained", "urn:oasis:names:tc:SAML:2.0:consent:obtained");
        validConsentValues.put("prior", "urn:oasis:names:tc:SAML:2.0:consent:prior");
        validConsentValues.put("current-implicit", "urn:oasis:names:tc:SAML:2.0:consent:current-implicit");
        validConsentValues.put("current-explicit", "urn:oasis:names:tc:SAML:2.0:consent:current-explicit");
        validConsentValues.put("unspecified", "urn:oasis:names:tc:SAML:2.0:consent:unspecified");
        validConsentValues.put("unavailable", "urn:oasis:names:tc:SAML:2.0:consent:unavailable");
        validConsentValues.put("inapplicable", "urn:oasis:names:tc:SAML:2.0:consent:inapplicable");

        DefaultCoreProperties defaultCoreProperties;

        Properties instance = new Properties();
        instance.put(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        instance.put("timeNotOnOrAfter", "5");

        for (Map.Entry<String, String> entry : validConsentValues.entrySet()) {
            instance.put(SAMLCore.CONSENT_AUTHN_RES.getValue(), entry.getKey());
            defaultCoreProperties = new DefaultCoreProperties(instance);
            assertEquals(entry.getValue(), defaultCoreProperties.getConsentAuthnResp());
        }

    }

    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Properties)}
     * when the properties contain a null value for "timeNotOnOrAfter" a {@link EIDASSAMLEngineRuntimeException} is thrown.
     * <p>
     * Must fail and throw {@link EIDASSAMLEngineRuntimeException}
     */
    @Test
    public void testEIDASSAMLEngineRuntimeExceptionWhenTimeNotOnOrAfterNotPresent() {

        expectedException.expect(EIDASSAMLEngineRuntimeException.class);
        expectedException.expectMessage("java.lang.NumberFormatException: null");

        Properties instance = new Properties();
        instance.put(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        instance.put(SAMLCore.CONSENT_AUTHN_RES.getValue(), "obtained");

        new DefaultCoreProperties(instance);
    }

    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Properties)}
     * when the properties contain a negative value for "timeNotOnOrAfter" a {@link EIDASSAMLEngineRuntimeException} is thrown.
     * <p>
     * Must fail and throw {@link EIDASSAMLEngineRuntimeException}
     */
    @Test
    public void testEIDASSAMLEngineRuntimeExceptionWhenTimeNotOnOrAfterNegative() {

        expectedException.expect(EIDASSAMLEngineRuntimeException.class);
        expectedException.expectMessage("eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException: Error (no. null) processing request : SamlEngine.xml - timeNotOnOrAfter cannot be negative. - null");

        Properties instance = new Properties();
        instance.put(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        instance.put(SAMLCore.CONSENT_AUTHN_RES.getValue(), "obtained");
        instance.put("timeNotOnOrAfter", "-1");

        new DefaultCoreProperties(instance);
    }

    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Map)}
     * when the properties map contains valid values, no exceptions should be thrown
     * Must succeed.
     */
    @Test
    public void testValid() {

        Map<String, String> instance = new HashMap<>();
        instance.put(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        instance.put(SAMLCore.CONSENT_AUTHN_RES.getValue(), "obtained");
        instance.put(SAMLCore.FORMAT_ENTITY.getValue(), "entity");
        instance.put(SAMLCore.ONE_TIME_USE.getValue(), "true");
        instance.put(SAMLCore.CONSENT_AUTHN_REQ.getValue(), "unspecified");
        instance.put(SAMLCore.VALIDATE_SIGNATURE_TAG.getValue(), "true");
        instance.put(SAMLCore.REQUESTER_TAG.getValue(), "requester");
        instance.put(SAMLCore.RESPONDER_TAG.getValue(), "responder");
        instance.put("timeNotOnOrAfter", "5");
        instance.put("ipAddrValidation", "true");
        instance.put("eIDSectorShare", "sectorShare");
        instance.put("eIDCrossSectorShare", "crossSectorShare");
        instance.put("eIDCrossBorderShare", "crossBorderShare");

        DefaultCoreProperties defaultCoreProperties = new DefaultCoreProperties(instance);

        assertEquals("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST", defaultCoreProperties.getProtocolBinding());
        assertEquals("urn:oasis:names:tc:SAML:2.0:consent:obtained", defaultCoreProperties.getConsentAuthnResp());
        assertEquals("urn:oasis:names:tc:SAML:2.0:consent:obtained", defaultCoreProperties.getConsentAuthnResponse());
        assertEquals("urn:oasis:names:tc:SAML:2.0:nameid-format:entity", defaultCoreProperties.getFormatEntity());
        assertEquals("urn:oasis:names:tc:SAML:2.0:consent:unspecified", defaultCoreProperties.getConsentAuthnRequest());
        assertEquals(new Integer(5), defaultCoreProperties.getTimeNotOnOrAfter());
        assertEquals("requester", defaultCoreProperties.getRequester());
        assertEquals("responder", defaultCoreProperties.getResponder());
        assertEquals("crossBorderShare", defaultCoreProperties.isEidCrossBordShare());
        assertEquals("crossBorderShare", defaultCoreProperties.isEidCrossBorderShare());
        assertEquals("crossSectorShare", defaultCoreProperties.isEidCrossSectShare());
        assertEquals("crossSectorShare", defaultCoreProperties.isEidCrossSectorShare());
        assertEquals("sectorShare", defaultCoreProperties.isEidSectorShare());
        assertEquals("responder", defaultCoreProperties.getProperty(SAMLCore.RESPONDER_TAG.getValue()));
        assertArrayEquals(new String[]{"eidas"}, defaultCoreProperties.getSupportedMessageFormatNames().toArray());

        assertTrue(defaultCoreProperties.isOneTimeUse());
        assertTrue(defaultCoreProperties.isValidateSignature());
        assertTrue(defaultCoreProperties.isIpValidation());
    }

    /**
     * Test method for
     * {@link DefaultCoreProperties#DefaultCoreProperties(Properties)}
     * when the, current single message format, property is set to false, it should not be present in the supported message format names.
     * Must succeed.
     */
    @Test
    public void testSupportedFormatNotAddedWhenAlreadyPresentAndValueFalse() {
        Properties instance = new Properties();
        instance.put(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        instance.put(SAMLCore.CONSENT_AUTHN_RES.getValue(), "obtained");
        instance.put("timeNotOnOrAfter", "5");

        instance.put("messageFormat.eidas", "false");

        DefaultCoreProperties defaultCoreProperties = new DefaultCoreProperties(instance);

        assertNotNull(defaultCoreProperties.getSupportedMessageFormatNames());
        assertTrue(defaultCoreProperties.getSupportedMessageFormatNames().isEmpty());
    }

}
