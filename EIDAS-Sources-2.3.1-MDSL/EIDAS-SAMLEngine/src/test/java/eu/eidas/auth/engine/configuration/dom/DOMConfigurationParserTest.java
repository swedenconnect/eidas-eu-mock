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

package eu.eidas.auth.engine.configuration.dom;

import com.google.common.collect.ImmutableMap;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.core.SAMLCore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class for {@link DOMConfigurationParser}
 */
public class DOMConfigurationParserTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link DOMConfigurationParser#validateParameters(String, String, Map)}
     * validate that when value map contains a legal value, validation is ok and returns correct, immutable result map.
     * Must succeed.
     */
    @Test
    public void testValidateParametersAllOK() throws ProtocolEngineConfigurationException {

        Map<String, String> values = new HashMap<>();
        values.put(SAMLCore.CONSENT_AUTHN_REQ.getValue(), "obtained");

        ImmutableMap<String, String> immutableMap = DOMConfigurationParser.validateParameters("dummyInstanceName", "dummyConfigurationName", values);

        assertEquals("obtained", immutableMap.get(SAMLCore.CONSENT_AUTHN_REQ.getValue()));
    }

    /**
     * Test method for
     * {@link DOMConfigurationParser#validateParameters(String, String, Map)}
     * when the parameter name (key in the map) contains a blank value, a {@link ProtocolEngineConfigurationException} is thrown.
     * <p>
     * Must fail and throw {@link ProtocolEngineConfigurationException}
     */
    @Test
    public void testValidateParametersExceptionWhenParameterNameBlank() throws ProtocolEngineConfigurationException {

        expectedException.expect(ProtocolEngineConfigurationException.class);
        expectedException.expectMessage("Error (no. null) processing request : SAML engine configuration file contains a blank parameter name for configuration name \"dummyConfigurationName\" in instance name \"dummyInstanceName\" - null");

        Map<String, String> values = new HashMap<>();
        values.put("  ", "someValue");

        DOMConfigurationParser.validateParameters("dummyInstanceName", "dummyConfigurationName", values);
    }

    /**
     * Test method for
     * {@link DOMConfigurationParser#validateParameters(String, String, Map)}
     * when the parameter name (key in the map) contains a null value, a {@link ProtocolEngineConfigurationException} is thrown.
     * <p>
     * Must fail and throw {@link ProtocolEngineConfigurationException}
     */
    @Test
    public void testValidateParametersExceptionWhenParameterNameNull() throws ProtocolEngineConfigurationException {

        expectedException.expect(ProtocolEngineConfigurationException.class);
        expectedException.expectMessage("Error (no. null) processing request : SAML engine configuration file contains a blank parameter name for configuration name \"dummyConfigurationName\" in instance name \"dummyInstanceName\" - null");

        Map<String, String> values = new HashMap<>();
        values.put(null, "someValue");

        DOMConfigurationParser.validateParameters("dummyInstanceName", "dummyConfigurationName", values);
    }

    /**
     * Test method for
     * {@link DOMConfigurationParser#validateParameters(String, String, Map)}
     * when the parameter value (value in the map) contains a blank value, a {@link ProtocolEngineConfigurationException} is thrown.
     * <p>
     * Must fail and throw {@link ProtocolEngineConfigurationException}
     */
    @Test
    public void testValidateParametersExceptionWhenParameterValueBlank() throws ProtocolEngineConfigurationException {

        expectedException.expect(ProtocolEngineConfigurationException.class);
        expectedException.expectMessage("Error (no. null) processing request : SAML engine configuration file contains parameter name \"consentAuthnRequest\" with a blank value for configuration name \"dummyConfigurationName\" in instance name \"dummyInstanceName\" - null");

        Map<String, String> values = new HashMap<>();
        values.put(SAMLCore.CONSENT_AUTHN_REQ.getValue(), "  ");

        DOMConfigurationParser.validateParameters("dummyInstanceName", "dummyConfigurationName", values);
    }

    /**
     * Test method for
     * {@link DOMConfigurationParser#validateParameters(String, String, Map)}
     * when the parameter value (value in the map) contains a null value, a {@link ProtocolEngineConfigurationException} is thrown.
     * <p>
     * Must fail and throw {@link ProtocolEngineConfigurationException}
     */
    @Test
    public void testValidateParametersExceptionWhenParameterValueNull() throws ProtocolEngineConfigurationException {

        expectedException.expect(ProtocolEngineConfigurationException.class);
        expectedException.expectMessage("Error (no. null) processing request : SAML engine configuration file contains parameter name \"consentAuthnRequest\" with a blank value for configuration name \"dummyConfigurationName\" in instance name \"dummyInstanceName\" - null");

        Map<String, String> values = new HashMap<>();
        values.put(SAMLCore.CONSENT_AUTHN_REQ.getValue(), null);

        DOMConfigurationParser.validateParameters("dummyInstanceName", "dummyConfigurationName", values);
    }

}