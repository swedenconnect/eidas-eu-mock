/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.auth.engine.configuration.dom;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.configuration.KeyStoreType;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static eu.eidas.auth.engine.configuration.dom.KeyStoreConfigurator.DEFAULT_KEYSTORE_CONFIGURATION_KEYS;

/**
 * Test class for {@link KeyStoreConfigurator}.
 */
public class KeyStoreConfiguratorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link KeyStoreConfigurator#getKeyStoreConfiguration(Map, KeyStoreConfigurator.KeyStoreConfigurationKeys, String)}
     * when {@link KeyStoreKey#KEYSTORE_TYPE} is {@link KeyStoreType#PKCS11}, an empty {@link KeyStoreKey#KEYSTORE_PATH} is allowed
     * <p>
     * Must succeed.
     */
    @Test
    public void getKeyStoreConfigurationNoPathPKCS11() throws ProtocolEngineConfigurationException {
        final Map<String, String> properties = new HashMap<>();
        properties.put(KeyStoreKey.KEYSTORE_PATH.getKey(), "  ");
        properties.put(KeyStoreKey.KEYSTORE_TYPE.getKey(), KeyStoreType.PKCS11.toString());

        KeyStoreConfigurator.getKeyStoreConfiguration(properties, DEFAULT_KEYSTORE_CONFIGURATION_KEYS, null);
    }

    /**
     * Test method for
     * {@link KeyStoreConfigurator#getKeyStoreConfiguration(Map, KeyStoreConfigurator.KeyStoreConfigurationKeys, String)}
     * when {@link KeyStoreKey#KEYSTORE_TYPE} is {@link KeyStoreType#JKS}, {@link KeyStoreKey#KEYSTORE_PATH} cannot be empty
     * <p>
     * Must fail.
     */
    @Test
    public void getKeyStoreConfigurationNoPathJKS() throws ProtocolEngineConfigurationException {
        expectedException.expect(ProtocolEngineConfigurationException.class);
        expectedException.expectMessage(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorMessage());

        final Map<String, String> properties = new HashMap<>();
        properties.put(KeyStoreKey.KEYSTORE_PATH.getKey(), "  ");
        properties.put(KeyStoreKey.KEYSTORE_TYPE.getKey(), KeyStoreType.JKS.toString());

        KeyStoreConfigurator.getKeyStoreConfiguration(properties, DEFAULT_KEYSTORE_CONFIGURATION_KEYS, null);
    }
}