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
 */

package eu.eidas.encryption.config;

import org.opensaml.xmlsec.impl.BasicDecryptionConfiguration;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Eidas Decryption Configuration class to
 * add {@link KeyInfoProvider} capability.
 */
public class EidasDecryptionConfiguration extends BasicDecryptionConfiguration {

    @Nullable
    private List<KeyInfoProvider> keyInfoProviders;

    public List<KeyInfoProvider> getKeyInfoProviders() {
        return keyInfoProviders;
    }

    /**
     * Sets the KeyInfoProviders that can be used to resolve the KeyInfo from an EncryptedKey
     * @param keyInfoProviders the list of KeyInfoProviders
     */
    public void setKeyInfoProviders(@Nullable List<KeyInfoProvider> keyInfoProviders) {
        this.keyInfoProviders = keyInfoProviders;
    }
}
