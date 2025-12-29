/*
 * Copyright (c) 2025 by European Commission
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

package eu.eidas.encryption;

import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.security.auth.DestroyFailedException;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Test class helping for the setup of tests that use a Decrypter
 */
public class DecrypterHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecrypterHelper.class);

    public SAMLAuthnResponseDecrypter decrypter;
    public List<Credential> credentials = new ArrayList<>();

    private String keyStorePassword = "local-demo";

    public DecrypterHelper() throws DestroyFailedException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        setupCredentials();
        decrypter = new SAMLAuthnResponseDecrypter(getJcaProviderName());
    }

    public void setupCredentials() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, DestroyFailedException {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream("src/test/resources" + getKeyStorePath()), getKeyStorePassword());
        LOGGER.debug("Keystore " + getKeyStorePath() + " is loaded.");
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(getKeyPassword());
        try {
            Enumeration<String> keyAliases = keystore.aliases();
            while (keyAliases.hasMoreElements()) {
                String keyAlias = keyAliases.nextElement();
                if (keystore.isKeyEntry(keyAlias)) {
                    LOGGER.debug("Extract key with alias {} from keystore", keyAlias);
                    KeyStore.Entry keyStoreEntry = keystore.getEntry(keyAlias, passwordProtection);
                    if (keyStoreEntry instanceof KeyStore.PrivateKeyEntry) {
                        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStoreEntry;
                        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
                        Credential credential = new BasicX509Credential(certificate, privateKeyEntry.getPrivateKey());
                        credentials.add(credential);
                    }
                }
            }
        } catch (UnrecoverableEntryException e) {
            LOGGER.error("Failed to retrieve a key entry from the keystore", e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Algorithm for keystore entry retrieval is not available", e);
        } finally {
            passwordProtection.destroy();
        }
    }

    @Nullable
    public String getJcaProviderName() {
        return null;
    }

    public String getKeyStorePath() {
        return "/keystores/test.p12";
    }

    public char[] getKeyStorePassword() {
        return keyStorePassword.toCharArray();
    }

    public char[] getKeyPassword() {
        return keyStorePassword.toCharArray();
    }

    public List<Credential> getDecryptionCredentials() {
        return credentials;
    }

}
