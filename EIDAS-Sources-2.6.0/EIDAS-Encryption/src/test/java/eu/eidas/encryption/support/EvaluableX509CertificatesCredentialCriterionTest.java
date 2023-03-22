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

import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.encryption.DecrypterHelper;
import eu.eidas.encryption.EncrypterHelper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialContextSet;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 *  Test class for {@link EvaluableX509CertificatesCredentialCriterion}
 */
public class EvaluableX509CertificatesCredentialCriterionTest {

    /**
     * Test for  {@link EvaluableX509CertificatesCredentialCriterion#test(Credential)}
     * when credential is valid ones
     * <p>
     * Must succeed.
     */
    @Test
    public void test() throws DestroyFailedException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        List<Credential> decryptionCredentials = decrypterHelper.getDecryptionCredentials();
        List<X509Certificate> certificates = CertificateUtil.getCertificates(decryptionCredentials);

        EvaluableX509CertificatesCredentialCriterion evaluableX509CertificatesCredentialCriterion
                = new EvaluableX509CertificatesCredentialCriterion(certificates);

        boolean result = evaluableX509CertificatesCredentialCriterion.test(decryptionCredentials.get(0));
        Assert.assertTrue(result);
    }

    /**
     * Test for  {@link EvaluableX509CertificatesCredentialCriterion#test(Credential)}
     * when credential is null
     * <p>
     * Must succeed.
     */
    @Test
    public void testCredentialIsNull() throws DestroyFailedException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        List<Credential> decryptionCredentials = decrypterHelper.getDecryptionCredentials();
        List<X509Certificate> certificates = CertificateUtil.getCertificates(decryptionCredentials);
        EvaluableX509CertificatesCredentialCriterion evaluableX509CertificatesCredentialCriterion
                = new EvaluableX509CertificatesCredentialCriterion(certificates);

        boolean result = evaluableX509CertificatesCredentialCriterion.test(null);
        Assert.assertFalse(result);
    }

    /**
     * Test for  {@link EvaluableX509CertificatesCredentialCriterion#test(Credential)}
     * when credential is not of type {@link X509Credential}
     * <p>
     * Must succeed.
     */
    @Test
    public void testCredentialNotTypeX509Credential() throws DestroyFailedException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        List<Credential> decryptionCredentials = decrypterHelper.getDecryptionCredentials();
        List<X509Certificate> certificates = CertificateUtil.getCertificates(decryptionCredentials);
        EvaluableX509CertificatesCredentialCriterion evaluableX509CertificatesCredentialCriterion
                = new EvaluableX509CertificatesCredentialCriterion(certificates);

        Credential credentialNotX509Credential = createCredentialNotX509Credential();

        boolean result = evaluableX509CertificatesCredentialCriterion.test(credentialNotX509Credential);
        Assert.assertFalse(result);
    }

    /**
     * Test for  {@link EvaluableX509CertificatesCredentialCriterion#test(Credential)}
     * when credential is of type {@link X509Credential}
     * when null returned {@link X509Credential#getEntityCertificate()}
     * <p>
     * Must succeed.
     */
    @Test
    public void testCredentialX509CredentialX509AndHisEntityCertificateNull() throws DestroyFailedException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        List<Credential> decryptionCredentials = decrypterHelper.getDecryptionCredentials();
        List<X509Certificate> certificates = CertificateUtil.getCertificates(decryptionCredentials);
        EvaluableX509CertificatesCredentialCriterion evaluableX509CertificatesCredentialCriterion
                = new EvaluableX509CertificatesCredentialCriterion(certificates);

        X509Credential x509CredentialMock = Mockito.mock(X509Credential.class);
        when(x509CredentialMock.getEntityCertificate()).thenReturn(null);
        Credential credential = x509CredentialMock;

        boolean result = evaluableX509CertificatesCredentialCriterion.test(credential);
        Assert.assertFalse(result);
    }

    /**
     * Test for  {@link EvaluableX509CertificatesCredentialCriterion#test(Credential)}
     * when credential does not relate to a certificate that is present
     * in the selectors of {@link EvaluableX509CertificatesCredentialCriterion}
     * <p>
     * Must succeed.
     */
    @Test
    public void testWhenCredentialNotRelateCertificateInSelector() throws IOException, CertificateException, NoSuchProviderException, DestroyFailedException, KeyStoreException, NoSuchAlgorithmException {
        EncrypterHelper encrypterHelper = new EncrypterHelper();
        Credential credential = encrypterHelper.getCredential();

        DecrypterHelper decrypterHelper = new DecrypterHelper();
        List<Credential> decryptionCredentials = decrypterHelper.getDecryptionCredentials();
        List<X509Certificate> certificates = CertificateUtil.getCertificates(decryptionCredentials);

        ArrayList<X509Certificate> x509Certificates = new ArrayList<>();
        X509Certificate expectedX509Certificate = certificates.get(0);
        x509Certificates.add(expectedX509Certificate);
        X509Certificate actualX509Certificate = ((BasicX509Credential) credential).getEntityCertificate();

        Assert.assertNotSame(expectedX509Certificate, actualX509Certificate);

        EvaluableX509CertificatesCredentialCriterion evaluableX509CertificatesCredentialCriterion
                = new EvaluableX509CertificatesCredentialCriterion(x509Certificates);

        boolean result = evaluableX509CertificatesCredentialCriterion.test(credential);
        Assert.assertFalse(result);
    }


    /**
     * Test for  {@link EvaluableX509CertificatesCredentialCriterion#toString()}
     * <p>
     * Must succeed.
     */
    @Test
    public void testToString() throws Exception {
        DecrypterHelper decrypterHelper = new DecrypterHelper();
        List<Credential> decryptionCredentials = decrypterHelper.getDecryptionCredentials();
        List<X509Certificate> certificates = CertificateUtil.getCertificates(decryptionCredentials);

        EvaluableX509CertificatesCredentialCriterion evaluableX509CertificatesCredentialCriterion
                = new EvaluableX509CertificatesCredentialCriterion(certificates);

        String actualString = evaluableX509CertificatesCredentialCriterion.toString();
        String expectedString = "EvaluableX509CertificatesCredentialCriterion [selectors=<contents not displayable>]";
        Assert.assertEquals(expectedString, actualString);
    }

    private Credential createCredentialNotX509Credential() {
        return new Credential() {
            @Nullable
            @Override
            public String getEntityId() {
                return null;
            }

            @Nullable
            @Override
            public UsageType getUsageType() {
                return null;
            }

            @Nonnull
            @Override
            public Collection<String> getKeyNames() {
                return null;
            }

            @Nullable
            @Override
            public PublicKey getPublicKey() {
                return null;
            }

            @Nullable
            @Override
            public PrivateKey getPrivateKey() {
                return null;
            }

            @Nullable
            @Override
            public SecretKey getSecretKey() {
                return null;
            }

            @Nullable
            @Override
            public CredentialContextSet getCredentialContextSet() {
                return null;
            }

            @Nonnull
            @Override
            public Class<? extends Credential> getCredentialType() {
                return null;
            }
        };
    }


}