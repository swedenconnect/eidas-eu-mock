package eu.eidas.auth.engine.xml.opensaml;

import com.google.common.collect.ImmutableList;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.AbstractProtocolEngine;
import eu.eidas.auth.engine.CertificateAliasPair;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.trust.ExplicitKeyTrustEvaluator;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final AtomicReference<CertificateFactory> CERTIFICATE_FACTORY_REF = new AtomicReference<>();

    public static void checkTrust(X509Credential entityX509Cred, Iterable<? extends Credential> trustedCredentials)
            throws EIDASSAMLEngineException {
        ExplicitKeyTrustEvaluator keyTrustEvaluator = new ExplicitKeyTrustEvaluator();
        LOG.debug(entityX509Cred.getEntityId());
        LOG.debug(entityX509Cred.getEntityCertificate().getIssuerDN().getName());
        LOG.debug("" + entityX509Cred.getEntityCertificate().getNotAfter());
        LOG.debug("" + entityX509Cred.getEntityCertificate().getSerialNumber());
        if (!keyTrustEvaluator.validate(entityX509Cred, (Iterable<Credential>) trustedCredentials)) {
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNTRUSTED_CERTIFICATE.errorCode(),
                                               EidasErrorKey.SAML_ENGINE_UNTRUSTED_CERTIFICATE.errorMessage());
        }
    }

    public static void checkTrust(X509Credential entityX509Cred, KeyStore trustStore) throws EIDASSAMLEngineException {
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
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(certificate);
        credential.setPrivateKey(privateKey);
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
     * @throws EIDASSAMLEngineException
     */
    @Nonnull
    public static CertificateAliasPair getCertificatePair(@Nonnull KeyStore keyStore,
                                                          @Nonnull String serialNumber,
                                                          @Nonnull String issuer)
            throws KeyStoreException, EIDASSAMLEngineException {
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
            throw new EIDASSAMLEngineException(
                    "Certificate " + issuer + "/" + serialNumber + " cannot be found in keyStore");
        }
        return new CertificateAliasPair(certificate, alias);
    }

    public static ImmutableList<X509Credential> getListOfCredential(KeyStore keyStore) throws EIDASSAMLEngineException {
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
            throw new EIDASSAMLEngineException(e);
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
            throws EIDASSAMLEngineException {
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
    public static X509Certificate toCertificate(@Nonnull String base64Certificate) throws EIDASSAMLEngineException {
        Preconditions.checkNotNull(base64Certificate, "base64Certificate");
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(base64Certificate));
            CertificateFactory certificateFactory = getCertificateFactory();
            return (X509Certificate) certificateFactory.generateCertificate(bais);
        } catch (CertificateException ce) {
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE.errorCode(),
                                               EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE.errorMessage(), ce);
        }
    }

    @Nonnull
    public static X509Certificate toCertificate(@Nonnull KeyInfo keyInfo) throws EIDASSAMLEngineException {
        Preconditions.checkNotNull(keyInfo, "keyInfo");
        org.opensaml.xml.signature.X509Certificate xmlCert = keyInfo.getX509Datas().get(0).getX509Certificates().get(0);
        X509Certificate cert = toCertificate(xmlCert.getValue());
        return cert;
    }

    @Nonnull
    public static X509Credential toCredential(@Nonnull KeyInfo keyInfo) throws EIDASSAMLEngineException {
        Preconditions.checkNotNull(keyInfo, "keyInfo");
        X509Certificate certificate = toCertificate(keyInfo);
        X509Credential credential = toCredential(certificate);
        return credential;
    }

    @Nonnull
    public static X509Credential toCredential(@Nonnull X509Certificate certificate) {
        Preconditions.checkNotNull(certificate, "certificate");
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(certificate);
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
            org.opensaml.xml.signature.X509Certificate xmlCert =
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
        } catch (EIDASSAMLEngineException e) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE,
                      "BUSINESS EXCEPTION : Procces getCountry from certificate: " + e.getMessage(), e);
            throw new EIDASSAMLEngineRuntimeException(e);
        }
    }
}
