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

package eu.eidas.auth.engine.configuration.dom;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;

public class ListKeystoreContent implements KeyContainer {

    private final List<KeyStoreContent> keystoreContentList;

    public ListKeystoreContent(List<KeyStoreContent> keystoreContentList) throws ProtocolEngineConfigurationException {
        if (keystoreContentList.isEmpty()) {
            throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE, "At least one keystore is required");
        }
        this.keystoreContentList = keystoreContentList;
    }

    @Override
    public ImmutableSet<X509Certificate> getCertificates() {
        return ImmutableSet.<X509Certificate>builder()
                .addAll(keystoreContentList.stream()
                        .map(KeyStoreContent::getCertificates)
                        .flatMap(Set::stream)
                        .iterator())
                .build();
    }

    @Override
    public KeyStore.PrivateKeyEntry getMatchingPrivateKeyEntry(String serialNumber, String issuer) throws ProtocolEngineConfigurationException {
        for(KeyStoreContent keyStoreContent : keystoreContentList) {
            try {
                return keyStoreContent.getMatchingPrivateKeyEntry(serialNumber, issuer);
            } catch (ProtocolEngineConfigurationException ignored) {
                // No luck, check next keystore
            }
        }
        throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR,
                "No private key entry matching serialNumber=" + serialNumber + " and issuer=" + issuer
                        + " found in configured keyStores");
    }

    @Override
    public KeyContainerEntry getMatchingKeyEntry(final String serialNumber, final String issuer) throws ProtocolEngineConfigurationException {
        for(KeyStoreContent keyStoreContent : keystoreContentList) {
            try {
                return keyStoreContent.getMatchingKeyEntry(serialNumber, issuer);
            } catch (ProtocolEngineConfigurationException ignored) {
                // No luck, check next keystore
            }
        }
        throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR,
                "No private key entry matching serialNumber=" + serialNumber + " and issuer=" + issuer
                        + " found in configured keyStores");
    }

    @Override
    public ImmutableSet<KeyStore.PrivateKeyEntry> getPrivateKeyEntries() {
        return ImmutableSet.<KeyStore.PrivateKeyEntry>builder()
                .addAll(keystoreContentList.stream()
                        .map(KeyStoreContent::getPrivateKeyEntries)
                        .flatMap(Set::stream)
                        .iterator())
                .build();
    }

    /**
     * Only uses keystores that have a given purpose.
     */
    public ListKeystoreContent subset(KeyStoreContent.KeystorePurpose purpose) throws ProtocolEngineConfigurationException {
        try {
            return new ListKeystoreContent(this.keystoreContentList.stream()
                    .filter(keyStoreContent -> keyStoreContent.hasPurpose(purpose))
                    .collect(Collectors.toList())
            );
        } catch (ProtocolEngineConfigurationException pece) {
            throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE,
                    "At least one keystore is required with purpose: " + purpose.name() , pece);
        }
    }
}
