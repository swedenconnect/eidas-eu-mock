/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */
package eu.eidas.auth.engine.xml.opensaml;

import com.google.common.collect.ImmutableList;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.engine.CertificateAliasPair;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.trust.impl.ExplicitKeyTrustEvaluator;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class dealing with Certificates and keys.
 *
 * @since 1.1
 */
public final class CertificateUtil {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CertificateUtil.class);

    public final static String EX_UNTRUSTED_CERT = "untrusted certificate";
    public final static String EX_INVALID_CERT = "invalid certificate";


    private static final AtomicReference<CertificateFactory> CERTIFICATE_FACTORY_REF = new AtomicReference<>();

    public static void checkTrust(X509Credential entityX509Cred, Iterable<? extends Credential> trustedCredentials)
            throws CertificateException {
        ExplicitKeyTrustEvaluator keyTrustEvaluator = new ExplicitKeyTrustEvaluator();
        LOG.debug(entityX509Cred.getEntityId());
        LOG.debug(entityX509Cred.getEntityCertificate().getIssuerDN().getName());
        LOG.debug("" + entityX509Cred.getEntityCertificate().getNotAfter());
        LOG.debug("" + entityX509Cred.getEntityCertificate().getSerialNumber());
        if (!keyTrustEvaluator.validate(entityX509Cred, (Iterable<Credential>) trustedCredentials)) {
            throw new CertificateException(EX_UNTRUSTED_CERT);
        }
    }

    public static void checkTrust(X509Credential entityX509Cred, KeyStore trustStore) throws CertificateException {
        checkTrust(entityX509Cred, getListOfCredential(trustStore));
    }

    @Nonnull
    public static X509Credential createCredential(@Nonnull KeyStore.PrivateKeyEntry privateKeyEntry) {
        return createCredential((X509Certificate) privateKeyEntry.getCertificate(), privateKeyEntry.getPrivateKey());
    }

    /**
     * @param certificate
     * @param privateKey
     * @return a credential based on the provided elements
     */
    public static X509Credential createCredential(X509Certificate certificate, PrivateKey privateKey) {
        BasicX509Credential credential = new BasicX509Credential(certificate, privateKey);
        return credential;
    }

    public static CertificateFactory getCertificateFactory() throws CertificateException {
        CertificateFactory certificateFactory = CERTIFICATE_FACTORY_REF.get();
        if (null == certificateFactory) {
            certificateFactory = CertificateFactory.getInstance("X.509");
            CERTIFICATE_FACTORY_REF.compareAndSet(null, certificateFactory);
        }
        return certificateFactory;
    }

    /**
     * @param keyStore
     * @param serialNumber
     * @param issuer
     * @return a certificate/alias pair from the keystore, having the given issuer and serialNumber
     * @throws KeyStoreException
     * @throws CertificateException
     */
    @Nonnull
    public static CertificateAliasPair getCertificatePair(@Nonnull KeyStore keyStore,
                                                          @Nonnull String serialNumber,
                                                          @Nonnull String issuer)
            throws KeyStoreException, CertificateException {
        String alias = null;
        X509Certificate certificate = null;

        for (final Enumeration<String> e = keyStore.aliases(); e.hasMoreElements(); ) {
            String aliasCert = e.nextElement();
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(aliasCert);
            if (null != cert && matchesCertificate(serialNumber, issuer, cert)) {
                alias = aliasCert;
                certificate = cert;
                break;
            }
        }
        if (null == alias) {
            throw new CertificateException(
                    "Certificate " + issuer + "/" + serialNumber + " cannot be found in keyStore");
        }
        return new CertificateAliasPair(certificate, alias);
    }

    public static ImmutableList<X509Credential> getListOfCredential(KeyStore keyStore) throws CertificateException {
        try {
            ImmutableList.Builder<X509Credential> trustCred = ImmutableList.builder();
            for (final Enumeration<String> e = keyStore.aliases(); e.hasMoreElements(); ) {
                String aliasCert = e.nextElement();
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(aliasCert);
                if (null != certificate) {
                    trustCred.add(toCredential(certificate));
                }
            }
            return trustCred.build();
        } catch (KeyStoreException e) {
            LOG.warn("ERROR : KeyStoreException.", e.getMessage());
            LOG.debug("ERROR : KeyStoreException.", e);
            throw new CertificateException(e);
        }
    }

    public static ImmutableList<X509Credential> getListOfCredential(Iterable<X509Certificate> certificates) {
        ImmutableList.Builder<X509Credential> trustCred = ImmutableList.builder();
        for (final X509Certificate certificate : certificates) {
            trustCred.add((toCredential(certificate)));
        }
        return trustCred.build();
    }

    @Nullable
    public static X509Certificate getTrustedCertificate(@Nonnull KeyInfo keyInfo,
                                                        @Nonnull Set<X509Certificate> trustedCertificates)
            throws CertificateException {
        X509Certificate certificate = toCertificate(keyInfo);
        if (trustedCertificates.contains(certificate)) {
            return certificate;
        }
        return null;
    }

    /**
     * @param cert
     * @return true when the certificate is self signed
     */
    public static boolean isCertificateSelfSigned(X509Certificate cert) {
        return cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal());
    }

    public static boolean matchesCertificate(String serialNumber, String issuer, X509Certificate certificate) {
        if (null == certificate) {
            return false;
        }
        BigInteger serialNumberBigInteger = new BigInteger(serialNumber, 16);
        BigInteger certificateSerialNumber = certificate.getSerialNumber();

        X500Principal issuerPrincipal = new X500Principal(issuer);

        X500Principal certificateSubjectPrincipal = certificate.getSubjectX500Principal();
        //create the X500Principal based on the string representation of the X.500 distinguished name using the format defined in RFC 2253
        X500Principal unencodedCertificateSubjectPrincipal = new X500Principal(certificateSubjectPrincipal.getName());

        X500Principal certificateIssuerPrincipal = certificate.getIssuerX500Principal();
        //create the X500Principal based on the string representation of the X.500 distinguished name using the format defined in RFC 2253
        X500Principal unencodedCertificateIssuerPrincipal = new X500Principal(certificateIssuerPrincipal.getName());

        return serialNumberBigInteger.equals(certificateSerialNumber) && (
                issuerPrincipal.equals(unencodedCertificateSubjectPrincipal) || issuerPrincipal.equals(
                        unencodedCertificateIssuerPrincipal));
    }

    @Nonnull
    public static X509Certificate toCertificate(@Nonnull String base64Certificate) throws CertificateException {
        Preconditions.checkNotNull(base64Certificate, "base64Certificate");
        ByteArrayInputStream bais = new ByteArrayInputStream(EidasStringUtil.decodeBytesFromBase64(base64Certificate));
        CertificateFactory certificateFactory = getCertificateFactory();
        return (X509Certificate) certificateFactory.generateCertificate(bais);
    }

    @Nonnull
    public static X509Certificate toCertificate(@Nonnull KeyInfo keyInfo) throws CertificateException {
        Preconditions.checkNotNull(keyInfo, "keyInfo");
        org.opensaml.xmlsec.signature.X509Certificate xmlCert = keyInfo.getX509Datas().get(0).getX509Certificates().get(0);
        X509Certificate cert = toCertificate(xmlCert.getValue());
        return cert;
    }

    @Nonnull
    public static X509Credential toCredential(@Nonnull KeyInfo keyInfo) throws CertificateException {
        Preconditions.checkNotNull(keyInfo, "keyInfo");
        X509Certificate certificate = toCertificate(keyInfo);
        X509Credential credential = toCredential(certificate);
        return credential;
    }

    @Nonnull
    public static X509Credential toCredential(@Nonnull X509Certificate certificate) {
        Preconditions.checkNotNull(certificate, "certificate");
        BasicX509Credential credential = new BasicX509Credential(certificate);
        return credential;
    }

    public static String validateDigestAlgorithm(String signatureAlgorithmName) {
        if (SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256.equalsIgnoreCase(signatureAlgorithmName)) {
            return SignatureConstants.ALGO_ID_DIGEST_SHA256;
        } else if (SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA384.equalsIgnoreCase(signatureAlgorithmName)) {
            return SignatureConstants.ALGO_ID_DIGEST_SHA384;
        }
        return SignatureConstants.ALGO_ID_DIGEST_SHA512;
    }

    private CertificateUtil() {
    }

    /**
     * Gets the country from X.509 Certificate.
     *
     * @param keyInfo the key info
     * @return the country
     */
    public static String getCountry(KeyInfo keyInfo) {
        LOG.trace("Recover country information.");
        try {
            org.opensaml.xmlsec.signature.X509Certificate xmlCert =
                    keyInfo.getX509Datas().get(0).getX509Certificates().get(0);

            // Transform the KeyInfo to X509Certificate.
            X509Certificate cert = toCertificate(xmlCert.getValue());

            String distName = cert.getSubjectDN().toString();

            distName = StringUtils.deleteWhitespace(StringUtils.upperCase(distName));

            String countryCode = "C=";
            int init = distName.indexOf(countryCode);

            String result = "";
            if (init > StringUtils.INDEX_NOT_FOUND) {
                // Exist country code.
                int end = distName.indexOf(',', init);

                if (end <= StringUtils.INDEX_NOT_FOUND) {
                    end = distName.length();
                }

                if (init < end && end > StringUtils.INDEX_NOT_FOUND) {
                    result = distName.substring(init + countryCode.length(), end);
                    //It must be a two characters value
                    if (result.length() > 2) {
                        result = result.substring(0, 2);
                    }
                }
            }
            return result.trim();
        } catch (CertificateException e) {
            LOG.error(MarkerFactory.getMarker("SAML_EXCHANGE"),
                      "BUSINESS EXCEPTION : Proccess getCountry from certificate: " + e.getMessage(), e);
            //TODO refactor this with configuration validation
            throw new RuntimeException(e);
        }
    }
}
