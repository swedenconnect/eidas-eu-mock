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
package eu.eidas.auth.engine.core.impl;

import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.EncryptionKey;
import eu.eidas.auth.engine.configuration.dom.KeyStoreContent;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

import javax.annotation.Nonnull;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * * This class it is used to load encrypt configuration for the engine. Specify to use encryption and decryption
 *
 * @since 1.1
 */
public class EncryptionDecryptionSW extends EncryptionActivationSW {

    private final X509Certificate decryptionCertificate;

    public EncryptionDecryptionSW(Map<String, String> properties, String defaultPath) throws EIDASSAMLEngineException {
        super(properties, defaultPath);
        String serialNumber = EncryptionKey.SERIAL_NUMBER.getAsString(properties);
        String issuer = EncryptionKey.RESPONSE_DECRYPTION_ISSUER.getAsString(properties);
        KeyStore.PrivateKeyEntry privateKeyEntry = KeyStoreContent.getMatchingPrivateKeyEntry(
                getDecryptionKeyAndCertificates(),
                serialNumber,
                issuer
        );
        decryptionCertificate = (X509Certificate) privateKeyEntry.getCertificate();
    }

    @Nonnull
    @Override
    public X509Certificate getDecryptionCertificate() throws ProtocolEngineConfigurationException {
        return decryptionCertificate;
    }
}
