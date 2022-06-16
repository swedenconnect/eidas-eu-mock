/*
 * Copyright (c) 2021 by European Commission
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

package eu.eidas.encryption.support;

import eu.eidas.encryption.DecrypterHelper;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.crypto.JCAConstants;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.agreement.KeyAgreementException;
import org.opensaml.xmlsec.agreement.KeyAgreementParameter;
import org.opensaml.xmlsec.agreement.KeyAgreementParameters;
import org.opensaml.xmlsec.agreement.KeyAgreementProcessor;
import org.opensaml.xmlsec.agreement.KeyAgreementSupport;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.KeyInfoCriterion;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.keyinfo.impl.CollectionKeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.KeyAgreementKeyInfoGeneratorFactory;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoResolutionContext;
import org.opensaml.xmlsec.keyinfo.impl.provider.AgreementMethodKeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.ECKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.InlineX509DataProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.RSAKeyValueProvider;
import org.opensaml.xmlsec.signature.KeyInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *  Test class for {@link EidasKeyInfoCredentialResolver}
 */
public class EidasKeyInfoCredentialResolverTest {

    @Before
    public void setup() throws InitializationException {
        InitializationService.initialize();
    }

    /**
     * Test for  {@link EidasKeyInfoCredentialResolver#postProcess(KeyInfoResolutionContext, CriteriaSet, List)}
     * when keyInfoResolutionContext, criteriaSet, decryptionCredentials parameters are valid ones
     * <p>
     * Must succeed.
     */
    @Test
    public void postProcess() throws Exception {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        List<Credential> decryptionCredentials = decrypterHelper.getDecryptionCredentials();

        List<KeyInfoProvider> keyInfoProviders = buildDefaultKeyInfoProviders();
        CredentialResolver keyInfoCredentialResolver =
                new CollectionKeyInfoCredentialResolver(decryptionCredentials);

        KeyInfoCredentialResolver localKeyInfoCredentialResolver =
                new EidasKeyInfoCredentialResolver(keyInfoProviders, keyInfoCredentialResolver);

        EidasKeyInfoCredentialResolver eidasKeyInfoCredentialResolver = new EidasKeyInfoCredentialResolver(keyInfoProviders, localKeyInfoCredentialResolver);

        KeyInfoResolutionContext keyInfoResolutionContext = new KeyInfoResolutionContext(decryptionCredentials);
        CriteriaSet criteriaSet = new CriteriaSet();

        eidasKeyInfoCredentialResolver.postProcess(keyInfoResolutionContext,criteriaSet,decryptionCredentials);
    }

    /**
     * Test for  {@link EidasKeyInfoCredentialResolver#postProcess(KeyInfoResolutionContext, CriteriaSet, List)}
     * when keyInfoResolutionContext, criteriaSet, decryptionCredentials parameters are valid ones
     * <p>
     * Must succeed.
     */
    @Test
    public void postProcessWithRecipientKeyInfoAndDecryptionCredentials() throws Exception {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential ecCredential = decrypterHelper.getDecryptionCredentials().get(1);
        List<Credential> decryptionCredentials = new ArrayList<>();
        decryptionCredentials.addAll(decrypterHelper.getDecryptionCredentials());

        List<KeyInfoProvider> keyInfoProviders = buildDefaultKeyInfoProviders();
        CredentialResolver keyInfoCredentialResolver =
                new CollectionKeyInfoCredentialResolver(decryptionCredentials);

        EidasKeyInfoCredentialResolver eidasKeyInfoCredentialResolver =
                new EidasKeyInfoCredentialResolver(keyInfoProviders, keyInfoCredentialResolver);

        KeyInfoGeneratorFactory keyAgreementKeyInfoGeneratorFactory = new KeyAgreementKeyInfoGeneratorFactory();
        KeyInfoGenerator keyAgreementKeyInfoGenerator = keyAgreementKeyInfoGeneratorFactory.newInstance();
        KeyInfo keyAgreementKeyInfo = keyAgreementKeyInfoGenerator.generate(getKeyAgreementCredential(ecCredential));
        KeyInfo recipientKeyInfo = keyAgreementKeyInfo.getAgreementMethods().get(0).getRecipientKeyInfo();

        List<Credential> resultCredentials = new ArrayList<>();
        resultCredentials.add(ecCredential);
        KeyInfoResolutionContext keyInfoResolutionContext = new KeyInfoResolutionContext(resultCredentials);
        keyInfoResolutionContext.setKeyInfo(recipientKeyInfo);

        KeyInfoCriterion keyInfoCriterion = new KeyInfoCriterion(recipientKeyInfo);
        CriteriaSet criteriaSet = new CriteriaSet(keyInfoCriterion);

        eidasKeyInfoCredentialResolver.postProcess(keyInfoResolutionContext, criteriaSet, resultCredentials);

        Assert.assertEquals(1, resultCredentials.size());
        Assert.assertEquals(ecCredential, resultCredentials.get(0));
    }

    /**
     * Test for  {@link EidasKeyInfoCredentialResolver#postProcess(KeyInfoResolutionContext, CriteriaSet, List)}
     * when keyInfoResolutionContext, criteriaSet, decryptionCredentials parameters are valid ones
     * but decryptionCredentials are empty.
     * <p>
     * Must succeed.
     */
    @Test
    public void postProcessWithRecipientKeyInfoAndNoCredentials() throws Exception {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential ecCredential = decrypterHelper.getDecryptionCredentials().get(1);
        List<Credential> decryptionCredentials = new ArrayList<>();
        decryptionCredentials.add(ecCredential);

        List<KeyInfoProvider> keyInfoProviders = buildDefaultKeyInfoProviders();
        CredentialResolver keyInfoCredentialResolver =
                new CollectionKeyInfoCredentialResolver(decryptionCredentials);

        EidasKeyInfoCredentialResolver eidasKeyInfoCredentialResolver =
                new EidasKeyInfoCredentialResolver(keyInfoProviders, keyInfoCredentialResolver);

        KeyInfoGeneratorFactory keyAgreementKeyInfoGeneratorFactory = new KeyAgreementKeyInfoGeneratorFactory();
        KeyInfoGenerator keyAgreementKeyInfoGenerator = keyAgreementKeyInfoGeneratorFactory.newInstance();
        KeyInfo keyAgreementKeyInfo = keyAgreementKeyInfoGenerator.generate(getKeyAgreementCredential(ecCredential));
        KeyInfo recipientKeyInfo = keyAgreementKeyInfo.getAgreementMethods().get(0).getRecipientKeyInfo();

        List<Credential> resultCredentials = new ArrayList<>();
        KeyInfoResolutionContext keyInfoResolutionContext = new KeyInfoResolutionContext(resultCredentials);
        keyInfoResolutionContext.setKeyInfo(recipientKeyInfo);

        KeyInfoCriterion keyInfoCriterion = new KeyInfoCriterion(recipientKeyInfo);
        CriteriaSet criteriaSet = new CriteriaSet(keyInfoCriterion);

        eidasKeyInfoCredentialResolver.postProcess(keyInfoResolutionContext, criteriaSet, resultCredentials);

        Assert.assertEquals(1, resultCredentials.size());
        Assert.assertEquals(ecCredential, resultCredentials.get(0));
    }

    private Credential getKeyAgreementCredential(Credential credential) throws KeyAgreementException {
        KeyAgreementProcessor keyAgreementProcessor =
                KeyAgreementSupport.getProcessor(EncryptionConstants.ALGO_ID_KEYAGREEMENT_ECDH_ES);;
        Collection<KeyAgreementParameter> keyAgreementParameterCollection = SecurityConfigurationSupport
                .getGlobalEncryptionConfiguration()
                .getKeyAgreementConfigurations()
                .get(JCAConstants.KEY_ALGO_EC)
                .getParameters();
        KeyAgreementParameters keyAgreementParameters = new KeyAgreementParameters(keyAgreementParameterCollection);
        return keyAgreementProcessor.execute(credential, EncryptionConstants.ALGO_ID_KEYWRAP_AES256, keyAgreementParameters);
    }

    private List<KeyInfoProvider> buildDefaultKeyInfoProviders() {
        ArrayList<KeyInfoProvider> keyInfoProviders = new ArrayList<>();

        AgreementMethodKeyInfoProvider keyAgreementMethodKeyInfoProvider = new AgreementMethodKeyInfoProvider();
        keyInfoProviders.add(keyAgreementMethodKeyInfoProvider);

        ECKeyValueProvider ecKeyValueProvider = new ECKeyValueProvider();
        keyInfoProviders.add(ecKeyValueProvider);

        RSAKeyValueProvider rsaKeyValueProvider = new RSAKeyValueProvider();
        keyInfoProviders.add(rsaKeyValueProvider);

        InlineX509DataProvider inlineX509DataProvider = new InlineX509DataProvider();
        keyInfoProviders.add(inlineX509DataProvider);

        return keyInfoProviders;
    }
}