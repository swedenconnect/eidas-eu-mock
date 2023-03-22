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
import eu.eidas.encryption.EncrypterHelper;
import eu.eidas.encryption.SAMLAuthnResponseEncrypter;
import eu.eidas.encryption.utils.DecryptionUtils;
import org.apache.xml.security.encryption.XMLCipher;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.DecryptionParameters;
import org.opensaml.xmlsec.encryption.EncryptedData;
import org.opensaml.xmlsec.encryption.EncryptedKey;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.w3c.dom.Element;

import java.security.Key;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 *  Test class for {@link Pkcs11Decrypter}
 */
public class Pkcs11DecrypterTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test for  {@link Pkcs11Decrypter#decryptKey(EncryptedKey, String, Key)}
     * when encryptedKey, algorithm, privateKey params are valid ones
     * <p>
     * Must succeed.
     */
    @Test
    public void decryptKey() throws Exception {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential credential = decrypterHelper.getDecryptionCredentials().get(0);
        EncrypterHelper encrypterHelper = new EncrypterHelper();
        final List<Credential> credentialsList = Arrays.asList(encrypterHelper.credential);
        DecryptionParameters decryptionParameters = DecryptionUtils.createDecryptionParameters(credentialsList);
        SAMLAuthnResponseEncrypter encrypter = encrypterHelper.encrypter;
        Response defaultResponse = encrypterHelper.getDefaultResponse();
        Response result = encrypter.encryptSAMLResponse(
                defaultResponse,
                credential,
                encrypterHelper.isKeyInfoDisplayedAsKeyValue());

        Pkcs11Decrypter pkcs11Decrypter = new Pkcs11Decrypter(decryptionParameters);
        EncryptedData actualEncryptedData = result.getEncryptedAssertions().get(0).getEncryptedData();
        EncryptedKey encryptedKey = actualEncryptedData.getKeyInfo().getEncryptedKeys().get(0);
        String algorithm = encryptedKey.getEncryptionMethod().getAlgorithm();

        PrivateKey privateKey = credential.getPrivateKey();
        pkcs11Decrypter.decryptKey(encryptedKey,algorithm, privateKey);
    }

    /**
     * Test for  {@link Pkcs11Decrypter#decryptKey(EncryptedKey, String, Key)}
     * when encryptedKey is null
     * <p>
     * Must fail.
     */
    @Test
    public void decryptKeyWithKeyNull() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        EncrypterHelper encrypterHelper = new EncrypterHelper();
        final List<Credential> credentialsList = Arrays.asList(encrypterHelper.credential);
        DecryptionParameters decryptionParameters = DecryptionUtils.createDecryptionParameters(credentialsList);

        Pkcs11Decrypter pkcs11Decrypter = new Pkcs11Decrypter(decryptionParameters);
        Key privateKey = null;
        pkcs11Decrypter.decryptKey(null,"", privateKey);
    }

    /**
     * Test for  {@link Pkcs11Decrypter#decryptKey(EncryptedKey, String, Key)}
     * when privateKey is null
     * <p>
     * Must fail.
     */
    @Test
    public void decryptKeyWithKeyNotNullAndEmptyAlgorithm() throws Exception {
        expectedException.expect(DecryptionException.class);

        Key keyMock = mock(Key.class);
        EncrypterHelper encrypterHelper = new EncrypterHelper();
        final List<Credential> credentialsList = Arrays.asList(encrypterHelper.credential);
        DecryptionParameters decryptionParameters = DecryptionUtils.createDecryptionParameters(credentialsList);

        Pkcs11Decrypter pkcs11Decrypter = new Pkcs11Decrypter(decryptionParameters);
        EncryptedKey encryptedKey = null;
        pkcs11Decrypter.decryptKey(encryptedKey,"", keyMock);
    }

    /**
     * Test for  {@link Pkcs11Decrypter#decryptP11Key(EncryptedKey, String, Key)}
     * when encryptedKey, algorithm, privateKey params are valid ones
     * <p>
     * Must succeed.
     */
    @Test
    public void decryptP11KeyRSA() throws Exception {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential credential = decrypterHelper.getDecryptionCredentials().get(0);

        EncrypterHelper encrypterHelper = new EncrypterHelper();
        final List<Credential> credentialsList = Arrays.asList(encrypterHelper.credential);
        DecryptionParameters decryptionParameters = DecryptionUtils.createDecryptionParameters(credentialsList);
        SAMLAuthnResponseEncrypter encrypter = encrypterHelper.encrypter;
        Response defaultResponse = encrypterHelper.getDefaultResponse();
        Response result = encrypter.encryptSAMLResponse(
                defaultResponse,
                credential,
                encrypterHelper.isKeyInfoDisplayedAsKeyValue());

        EncryptedData actualEncryptedData = result.getEncryptedAssertions().get(0).getEncryptedData();
        EncryptedKey encryptedKey = actualEncryptedData.getKeyInfo().getEncryptedKeys().get(0);
        String algorithm = encryptedKey.getEncryptionMethod().getAlgorithm();
        PrivateKey privateKey = credential.getPrivateKey();

        Pkcs11Decrypter pkcs11Decrypter = new Pkcs11Decrypter(decryptionParameters);
        pkcs11Decrypter.decryptP11Key(encryptedKey,algorithm, privateKey);
    }

    @Ignore
    @Test
    public void decryptP11KeyEC() throws Exception {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential decrypterCredential = decrypterHelper.getDecryptionCredentials().get(2);

        EncrypterHelper encrypterHelper = new EncrypterHelper();
        final List<Credential> credentialsList = Arrays.asList(encrypterHelper.credential);
        DecryptionParameters decryptionParameters = DecryptionUtils.createDecryptionParameters(credentialsList);
        SAMLAuthnResponseEncrypter encrypter = encrypterHelper.encrypter;
        Response defaultResponse = encrypterHelper.getDefaultResponse();
        Response result = encrypter.encryptSAMLResponse(
                defaultResponse,
                decrypterCredential,
                encrypterHelper.isKeyInfoDisplayedAsKeyValue());

        EncryptedData actualEncryptedData = result.getEncryptedAssertions().get(0).getEncryptedData();
        EncryptedKey encryptedKey = actualEncryptedData.getKeyInfo().getEncryptedKeys().get(0);
        String algorithm = encryptedKey.getEncryptionMethod().getAlgorithm();
        PrivateKey privateKey = decrypterCredential.getPrivateKey();

        Pkcs11Decrypter pkcs11Decrypter = new Pkcs11Decrypter(decryptionParameters);
        pkcs11Decrypter.decryptP11Key(encryptedKey,algorithm, privateKey);
    }

    @Ignore
    @Test
    public void decryptECKey() throws Exception {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential decrypterCredential = decrypterHelper.getDecryptionCredentials().get(2);

        EncrypterHelper encrypterHelper = new EncrypterHelper();
        final List<Credential> credentialsList = Arrays.asList(encrypterHelper.credential);
        DecryptionParameters decryptionParameters = DecryptionUtils.createDecryptionParameters(credentialsList);
        SAMLAuthnResponseEncrypter encrypter = encrypterHelper.encrypter;
        Response defaultResponse = encrypterHelper.getDefaultResponse();
        Response result = encrypter.encryptSAMLResponse(
                defaultResponse,
                decrypterCredential,
                encrypterHelper.isKeyInfoDisplayedAsKeyValue());

        EncryptedData actualEncryptedData = result.getEncryptedAssertions().get(0).getEncryptedData();
        EncryptedKey encryptedKey = actualEncryptedData.getKeyInfo().getEncryptedKeys().get(0);
        String algorithm = encryptedKey.getEncryptionMethod().getAlgorithm();
        PrivateKey privateKey = decrypterCredential.getPrivateKey();

        Pkcs11Decrypter pkcs11Decrypter = new Pkcs11Decrypter(decryptionParameters);
        pkcs11Decrypter.decryptECKey(encryptedKey,algorithm, privateKey);
    }

    /**
     * Test for  {@link Pkcs11Decrypter#decryptRSAKey(EncryptedKey, String, Key)}
     * when encryptedKey, algorithm, privateKey params are valid ones
     * <p>
     * Must succeed.
     */
    @Test
    public void decryptRSAKey() throws Exception {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential decrypterCredential = decrypterHelper.getDecryptionCredentials().get(0);

        EncrypterHelper encrypterHelper = new EncrypterHelper();
        final List<Credential> credentialsList = Arrays.asList(encrypterHelper.credential);
        DecryptionParameters decryptionParameters = DecryptionUtils.createDecryptionParameters(credentialsList);
        SAMLAuthnResponseEncrypter encrypter = encrypterHelper.encrypter;
        Response defaultResponse = encrypterHelper.getDefaultResponse();
        Response result = encrypter.encryptSAMLResponse(
                defaultResponse,
                decrypterCredential,
                encrypterHelper.isKeyInfoDisplayedAsKeyValue());

        EncryptedData actualEncryptedData = result.getEncryptedAssertions().get(0).getEncryptedData();
        EncryptedKey encryptedKey = actualEncryptedData.getKeyInfo().getEncryptedKeys().get(0);
        String algorithm = encryptedKey.getEncryptionMethod().getAlgorithm();
        PrivateKey privateKey = decrypterCredential.getPrivateKey();

        Pkcs11Decrypter pkcs11Decrypter = new Pkcs11Decrypter(decryptionParameters);
        pkcs11Decrypter.decryptRSAKey(encryptedKey,algorithm, privateKey);
    }

    /**
     * Test for  {@link Pkcs11Decrypter#decryptRsaOaepDecryptKey(org.apache.xml.security.encryption.EncryptedKey, String, Key)}
     * when encryptedKey, algorithm, privateKey params are valid ones
     * <p>
     * Must succeed.
     */
    @Test
    public void customizedDecryptKey() throws Exception {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        Credential decrypterCredential = decrypterHelper.getDecryptionCredentials().get(0);

        EncrypterHelper encrypterHelper = new EncrypterHelper();
        final List<Credential> credentialsList = Arrays.asList(encrypterHelper.credential);
        DecryptionParameters decryptionParameters = DecryptionUtils.createDecryptionParameters(credentialsList);
        SAMLAuthnResponseEncrypter encrypter = encrypterHelper.encrypter;
        Response defaultResponse = encrypterHelper.getDefaultResponse();
        Response encryptedResponse = encrypter.encryptSAMLResponse(
                defaultResponse,
                decrypterCredential,
                encrypterHelper.isKeyInfoDisplayedAsKeyValue());

        EncryptedData actualEncryptedData = encryptedResponse.getEncryptedAssertions().get(0).getEncryptedData();
        EncryptedKey encryptedKey = actualEncryptedData.getKeyInfo().getEncryptedKeys().get(0);
        String algorithm = encryptedKey.getEncryptionMethod().getAlgorithm();
        PrivateKey privateKey = decrypterCredential.getPrivateKey();

        Element encryptedKeyDOM = encryptedKey.getDOM();

        XMLCipher xmlCipher = XMLCipher.getInstance();
        xmlCipher.init(XMLCipher.DECRYPT_MODE, privateKey);
        org.apache.xml.security.encryption.EncryptedKey encKey = xmlCipher.loadEncryptedKey(encryptedKeyDOM.getOwnerDocument(), encryptedKeyDOM);

        Pkcs11Decrypter pkcs11Decrypter = new Pkcs11Decrypter(decryptionParameters);
        pkcs11Decrypter.decryptRsaOaepDecryptKey(encKey,algorithm, privateKey);
    }
}