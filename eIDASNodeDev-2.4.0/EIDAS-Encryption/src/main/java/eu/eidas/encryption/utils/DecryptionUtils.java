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

package eu.eidas.encryption.utils;

import eu.eidas.encryption.config.EidasDecryptionConfiguration;
import eu.eidas.encryption.support.FirstInlineEncryptedKeyResolver;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.DecryptionConfiguration;
import org.opensaml.xmlsec.DecryptionParameters;
import org.opensaml.xmlsec.encryption.support.ChainingEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.EncryptedKeyResolver;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.LocalKeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.provider.InlineX509DataProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.RSAKeyValueProvider;
import se.swedenconnect.opensaml.xmlsec.keyinfo.provider.KeyAgreementMethodKeyInfoProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DecryptionUtils {

    /**
     * Create DecryptionParameters based on the configuration and the given credentials
     * @param credentials the credentials to be use by the decryption parameters
     * @return the DecryptionParameters
     */
    public static DecryptionParameters createDecryptionParameters(List<Credential> credentials) {
        final DecryptionConfiguration decryptionConfiguration = getDecryptionConfiguration(credentials);
        final DecryptionParameters decryptionParameters = createDecryptionParameters(decryptionConfiguration);

        return decryptionParameters;
    }

    private static DecryptionParameters createDecryptionParameters(final DecryptionConfiguration config) {
        final DecryptionParameters decryptionParameters = new DecryptionParameters();

        Collection<String> blacklistedAlgorithms = config.getBlacklistedAlgorithms();
        decryptionParameters.setBlacklistedAlgorithms(blacklistedAlgorithms);

        Collection<String> whitelistedAlgorithms = config.getWhitelistedAlgorithms();
        decryptionParameters.setWhitelistedAlgorithms(whitelistedAlgorithms);

        KeyInfoCredentialResolver dataKeyInfoCredentialResolver = config.getDataKeyInfoCredentialResolver();
        decryptionParameters.setDataKeyInfoCredentialResolver(dataKeyInfoCredentialResolver);

        EncryptedKeyResolver encryptedKeyResolver = config.getEncryptedKeyResolver();
        decryptionParameters.setEncryptedKeyResolver(encryptedKeyResolver);

        KeyInfoCredentialResolver kekKeyInfoCredentialResolver = config.getKEKKeyInfoCredentialResolver();
        decryptionParameters.setKEKKeyInfoCredentialResolver(kekKeyInfoCredentialResolver);
        return decryptionParameters;
    }

    private static DecryptionConfiguration getDecryptionConfiguration(List<Credential> credentials) {
        DecryptionConfiguration decryptionConfiguration = ConfigurationService.get(DecryptionConfiguration.class);
        if (decryptionConfiguration == null) {
            decryptionConfiguration = buildDefaultDecryptionConfiguration(credentials);
        }
        if (decryptionConfiguration instanceof EidasDecryptionConfiguration) {
            EidasDecryptionConfiguration eidasDecryptionConf = (EidasDecryptionConfiguration) decryptionConfiguration;
            KeyInfoCredentialResolver keyInfoCredentialResolver = buildDefaultKeyInfoCredentialResolver(credentials);
            eidasDecryptionConf.setKEKKeyInfoCredentialResolver(keyInfoCredentialResolver);
        }
        return decryptionConfiguration;
    }

    /**
     * Create a default decryption configuration.
     * @return the default decryption configuration.
     * @param credentials the credentials to be use to build the default key info providers
     */
    public static EidasDecryptionConfiguration buildDefaultDecryptionConfiguration(List<Credential> credentials) {
        EidasDecryptionConfiguration eidasDecryptionConfiguration = new EidasDecryptionConfiguration();

        List<String> defaultBlacklistedAlgorithms = getDefaultBlacklistedAlgorithms();
        eidasDecryptionConfiguration.setBlacklistedAlgorithms(defaultBlacklistedAlgorithms);

        EncryptedKeyResolver resolver = buildDefaultEncryptedKeyResolver();
        eidasDecryptionConfiguration.setEncryptedKeyResolver(resolver);

        List<KeyInfoProvider> keyInfoProviders = buildDefaultKeyInfoProviders(credentials);
        eidasDecryptionConfiguration.setKeyInfoProviders(keyInfoProviders);

        return eidasDecryptionConfiguration;
    }

    static List<String> getDefaultBlacklistedAlgorithms() {
        List<String> blacklistedAlgorithms = Collections.singletonList("http://www.w3.org/2001/04/xmlenc#rsa-1_5");
        return blacklistedAlgorithms;
    }

    static EncryptedKeyResolver buildDefaultEncryptedKeyResolver() {
        List<EncryptedKeyResolver> keyResolvers = new ArrayList<>();

        FirstInlineEncryptedKeyResolver e = new FirstInlineEncryptedKeyResolver();
        keyResolvers.add(e);

        ChainingEncryptedKeyResolver chainingEncryptedKeyResolver = new ChainingEncryptedKeyResolver(keyResolvers);
        return chainingEncryptedKeyResolver;
    }

    static List<KeyInfoProvider> buildDefaultKeyInfoProviders(List<Credential> credentials) {
        ArrayList<KeyInfoProvider> keyInfoProviders = new ArrayList<>();

        KeyAgreementMethodKeyInfoProvider keyAgreementMethodKeyInfoProvider = new KeyAgreementMethodKeyInfoProvider(credentials);
        keyInfoProviders.add(keyAgreementMethodKeyInfoProvider);

        RSAKeyValueProvider rsaKeyValueProvider = new RSAKeyValueProvider();
        keyInfoProviders.add(rsaKeyValueProvider);

        InlineX509DataProvider inlineX509DataProvider = new InlineX509DataProvider();
        keyInfoProviders.add(inlineX509DataProvider);

        return keyInfoProviders;
    }

    /**
     * Build a LocalKeyInfoCredentialResolver with the given KeyInfo Providers and the local credentials
     * @param credentials an array of local credentials
     * @return a KeyInfoCredentialResolver
     */
    public static KeyInfoCredentialResolver buildDefaultKeyInfoCredentialResolver(List<Credential> credentials) {
        List<KeyInfoProvider> keyInfoProviders = buildDefaultKeyInfoProviders(credentials);
        StaticKeyInfoCredentialResolver staticKeyInfoCredentialResolver =
                new StaticKeyInfoCredentialResolver(credentials);

        LocalKeyInfoCredentialResolver localKeyInfoCredentialResolver = new LocalKeyInfoCredentialResolver(keyInfoProviders, staticKeyInfoCredentialResolver);
        return localKeyInfoCredentialResolver;
    }
}
