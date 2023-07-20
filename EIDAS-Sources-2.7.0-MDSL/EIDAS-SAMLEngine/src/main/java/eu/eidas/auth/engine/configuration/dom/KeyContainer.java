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

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 *
 */
public interface KeyContainer {
    ImmutableSet<X509Certificate> getCertificates();

    KeyStore.PrivateKeyEntry getMatchingPrivateKeyEntry(String serialNumber, String issuer)
            throws ProtocolEngineConfigurationException;

    ImmutableSet<KeyStore.PrivateKeyEntry> getPrivateKeyEntries();
}
