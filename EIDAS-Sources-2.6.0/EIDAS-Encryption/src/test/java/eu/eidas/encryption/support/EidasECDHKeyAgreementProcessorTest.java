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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.crypto.JCAConstants;
import org.opensaml.xmlsec.agreement.KeyAgreementException;
import org.opensaml.xmlsec.agreement.KeyAgreementParameters;

import java.security.PublicKey;

/**
 *  Test class for {@link EidasECDHKeyAgreementProcessorTest}
 */
public class EidasECDHKeyAgreementProcessorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * Test for  {@link EidasECDHKeyAgreementProcessor#generateAgreementSecret(Credential, Credential, KeyAgreementParameters)}
     * when publicCredential, privateCredential, keyAgreementParameters parameters are valid ones
     * <p>
     * Must succeed.
     */
    @Test
    public void generateAgreementSecret() throws Exception {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential publicCredential = decrypterHelper.getDecryptionCredentials().get(1);
        Credential privateCredential = publicCredential;
        EidasECDHKeyAgreementProcessor eidasECDHKeyAgreementProcessor = new EidasECDHKeyAgreementProcessor();
        KeyAgreementParameters keyAgreementParameters = new KeyAgreementParameters();

        byte[] bytes = eidasECDHKeyAgreementProcessor.generateAgreementSecret(publicCredential, privateCredential, keyAgreementParameters);

        Assert.assertEquals(32, bytes.length);
    }

    /**
     * Test for  {@link EidasECDHKeyAgreementProcessor#generateAgreementSecret(Credential, Credential, KeyAgreementParameters)}
     * when publicCredential does not contain a Public Key
     * <p>
     * Must fail.
     */
    @Test
    public void generateAgreementSecretPublicKeyNull() throws Exception {
        exception.expect(KeyAgreementException.class);
        exception.expectMessage("Public credential's public key is not an EC key");

        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential publicCredential = decrypterHelper.getDecryptionCredentials().get(1);
        Credential privateCredential = publicCredential;
        EidasECDHKeyAgreementProcessor eidasECDHKeyAgreementProcessor = new EidasECDHKeyAgreementProcessor();
        KeyAgreementParameters keyAgreementParameters = new KeyAgreementParameters();
        Credential publicCredentialSpy = Mockito.spy(publicCredential);
        Mockito.when(publicCredentialSpy.getPublicKey()).thenReturn(null);

        eidasECDHKeyAgreementProcessor.generateAgreementSecret(publicCredentialSpy, privateCredential, keyAgreementParameters);
    }

    /**
     * Test for  {@link EidasECDHKeyAgreementProcessor#generateAgreementSecret(Credential, Credential, KeyAgreementParameters)}
     * when publicCredential contains a key other then {@link JCAConstants#KEY_ALGO_EC}
     * <p>
     * Must fail.
     */
    @Test
    public void generateAgreementSecretPublicKeyNotEC() throws Exception {
        exception.expect(KeyAgreementException.class);
        exception.expectMessage("Public credential's public key is not an EC key");

        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential publicCredential = decrypterHelper.getDecryptionCredentials().get(1);
        Credential privateCredential = publicCredential;
        EidasECDHKeyAgreementProcessor eidasECDHKeyAgreementProcessor = new EidasECDHKeyAgreementProcessor();
        KeyAgreementParameters keyAgreementParameters = new KeyAgreementParameters();
        Credential publicCredentialSpy = Mockito.spy(publicCredential);

        final PublicKey publicKeyMock = Mockito.mock(PublicKey.class);
        Mockito.when(publicKeyMock.getAlgorithm()).thenReturn(JCAConstants.KEY_ALGO_RSA);
        Mockito.when(publicCredentialSpy.getPublicKey()).thenReturn(publicKeyMock);


        eidasECDHKeyAgreementProcessor.generateAgreementSecret(publicCredentialSpy, privateCredential, keyAgreementParameters);
    }

    /**
     * Test for  {@link EidasECDHKeyAgreementProcessor#generateAgreementSecret(Credential, Credential, KeyAgreementParameters)}
     * when privateCredential does not contain a Private Key
     * <p>
     * Must fail.
     */
    @Test
    public void generateAgreementSecretPrivateKeyNull() throws Exception {
        exception.expect(KeyAgreementException.class);
        exception.expectMessage("Private credential's private key is not an EC key");

        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential publicCredential = decrypterHelper.getDecryptionCredentials().get(1);
        Credential privateCredential = publicCredential;
        EidasECDHKeyAgreementProcessor eidasECDHKeyAgreementProcessor = new EidasECDHKeyAgreementProcessor();
        KeyAgreementParameters keyAgreementParameters = new KeyAgreementParameters();
        Credential privateCredentialSpy = Mockito.spy(privateCredential);
        Mockito.when(privateCredentialSpy.getPrivateKey()).thenReturn(null);

        eidasECDHKeyAgreementProcessor.generateAgreementSecret(publicCredential, privateCredentialSpy, keyAgreementParameters);
    }

    /**
     * Test for  {@link EidasECDHKeyAgreementProcessor#generateAgreementSecret(Credential, Credential, KeyAgreementParameters)}
     * when publicCredential contains a Public Key that is not usable
     * <p>
     * Must fail.
     */
    @Test
    public void generateAgreementSecretPublicKeyInvalidKeyException() throws Exception {
        exception.expect(KeyAgreementException.class);
        exception.expectMessage("Error generating secret from public and private EC keys");

        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential publicCredential = decrypterHelper.getDecryptionCredentials().get(1);
        Credential privateCredential = publicCredential;
        EidasECDHKeyAgreementProcessor eidasECDHKeyAgreementProcessor = new EidasECDHKeyAgreementProcessor();
        KeyAgreementParameters keyAgreementParameters = new KeyAgreementParameters();
        Credential publicCredentialSpy = Mockito.spy(publicCredential);

        final PublicKey publicKeyMock = Mockito.mock(PublicKey.class);
        Mockito.when(publicKeyMock.getAlgorithm()).thenReturn("EC");
        Mockito.when(publicCredentialSpy.getPublicKey()).thenReturn(publicKeyMock);

        eidasECDHKeyAgreementProcessor.generateAgreementSecret(publicCredentialSpy, privateCredential, keyAgreementParameters);
    }
}