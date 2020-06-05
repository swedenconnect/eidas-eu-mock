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
 * limitations under the Licence
 */
package eu.eidas.auth.engine.xml.opensaml;

import com.google.common.collect.ImmutableList;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.engine.CertificateAliasPair;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.trust.impl.ExplicitKeyTrustEvaluator;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.PKIXValidationInformation;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.security.x509.impl.BasicPKIXValidationInformation;
import org.opensaml.security.x509.impl.CertPathPKIXTrustEvaluator;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Data;
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
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
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

    private static final Set<X509CRL> EMPTY_CRLS = new HashSet<>();

    private static final Integer MAX_DEPTH  = 5;//TODO get this value form external configuration

    private static final AtomicReference<CertificateFactory> CERTIFICATE_FACTORY_REF = new AtomicReference<>();

    /**
     * Performs an certification path key evaluation between the param x509Credential and all the credential contained in param trustedCredentials.
     *
     * @param x509Credential the {@link X509Credential} to be evaluated
     * @param trustedCredentials the list of credentials that the param x509Credential will be evaluated against.
     * @throws CertificateException if the credential is not trusted
     */
    public static void checkChainTrust(X509Credential x509Credential, Iterable<? extends Credential> trustedCredentials)
            throws CertificateException {

        LOG.debug(x509Credential.getEntityId());
        LOG.debug(x509Credential.getEntityCertificate().getIssuerDN().getName());
        LOG.debug("" + x509Credential.getEntityCertificate().getNotAfter());
        LOG.debug("" + x509Credential.getEntityCertificate().getSerialNumber());

        final CertPathPKIXTrustEvaluator keyTrustEvaluator = new CertPathPKIXTrustEvaluator();

        final boolean isTrusted = isTrustValid(x509Credential, keyTrustEvaluator, trustedCredentials);
        if (!isTrusted) {
            throw new CertificateException(EX_UNTRUSTED_CERT);
        }

    }

    /**
     * Performs an explicit key evaluation between the param x509Credential and all the credential contained in param trustedCredentials.
     *
     * @param x509Credential the {@link X509Credential} to be evaluated
     * @param trustedCredentials the list of credentials that the param x509Credential will be evaluated against.
     * @throws CertificateException if the credential is not trusted
     */
    public static void checkExplicitTrust(X509Credential x509Credential, Iterable<? extends Credential> trustedCredentials) throws CertificateException {

        LOG.debug(x509Credential.getEntityId());
        LOG.debug(x509Credential.getEntityCertificate().getIssuerDN().getName());
        LOG.debug("" + x509Credential.getEntityCertificate().getNotAfter());
        LOG.debug("" + x509Credential.getEntityCertificate().getSerialNumber());

        final ExplicitKeyTrustEvaluator keyTrustEvaluator = new ExplicitKeyTrustEvaluator();

        final boolean isTrusted = keyTrustEvaluator.validate(x509Credential, (Iterable<Credential>) trustedCredentials);
        if (!isTrusted) {
            throw new CertificateException(EX_UNTRUSTED_CERT);
        }
    }


    private static boolean isTrustValid(final X509Credential entityX509Cred,
                                        final CertPathPKIXTrustEvaluator keyTrustEvaluator,
                                        final Iterable<? extends Credential> trustedCredentials) throws CertificateException {

        ArrayList<X509Certificate> trustedx509Certificates = new ArrayList<>();
        for (Credential trustedCredential : trustedCredentials) {
            X509Certificate entityCertificate = ((BasicX509Credential) trustedCredential).getEntityCertificate();
            trustedx509Certificates.add(entityCertificate);
        }

        final PKIXValidationInformation pkixValidationInformation = getPKIXInfoSet(trustedx509Certificates,
                EMPTY_CRLS,
                MAX_DEPTH);

        try {
            return keyTrustEvaluator.validate(pkixValidationInformation, entityX509Cred);
        } catch (SecurityException e) {
            throw new CertificateException(EX_UNTRUSTED_CERT);
        }
    }

    private static PKIXValidationInformation getPKIXInfoSet(Collection<X509Certificate> certs,
                                                     Collection<X509CRL> crls, Integer depth) {
        return new BasicPKIXValidationInformation(certs, crls, depth);
    }

    @Nonnull
    public static X509Credential createCredential(@Nonnull KeyStore.PrivateKeyEntry privateKeyEntry) {
        return createCredential((X509Certificate) privateKeyEntry.getCertificate(), privateKeyEntry.getPrivateKey());
    }

    /**
     * @param certificate the {@link X509Credential} to be evaluated
     * @param privateKey the private key {@link PrivateKey}
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
     * @param keyStore the keyStore
     * @param serialNumber the serialNumber
     * @param issuer the issuer
     * @return a certificate/alias pair from the keystore, having the given issuer and serialNumber
     * @throws KeyStoreException the KeyStore exception
     * @throws CertificateException if the credential is not trusted
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
     * @param cert the {@link X509Credential} to be evaluated
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

    /**
     * Retrieves the {@link X509Certificate} which is the issuer of param entityX509Cred from a {@link List} of {@link X509Certificate}
     *
     * @param x509Credential the credential which holds the issuer information
     * @param x509Certificates the list of {@link X509Certificate} where the issuer will be looked up and if present retrieved from
     * @return the {@link X509Certificate} that is the issuer of param entityX509Cred or null if not found in the param  x509Certificates
     */
    public static X509Certificate getIssuerX509Certificate(X509Credential x509Credential, List<X509Certificate> x509Certificates) {
        String issuerDN = x509Credential.getEntityCertificate().getIssuerDN().getName();

        for(X509Certificate certificate : x509Certificates){
            if(StringUtils.equals(certificate.getSubjectDN().getName(),issuerDN)) {
                return certificate;
            }
        }
        return null;
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
        List<org.opensaml.xmlsec.signature.X509Certificate> x509Certificates = keyInfo.getX509Datas().get(0).getX509Certificates();

        final int size = x509Certificates.size();
        //TODO to allow backward compatibility with eIDAS 1.4.1, use first certificate (assuming 1st to be the metadata signing certificate). Change it when no longer needed an use the last index instead as before.
//        final int index = size == 0 ? 0 : size - 1;
        final int index = 0;

        org.opensaml.xmlsec.signature.X509Certificate xmlCert = x509Certificates.get(index);
        X509Certificate cert = toCertificate(xmlCert.getValue());
        return cert;
    }


    /**
     * Retrieves the certificates contained in the keyinfo parameter.
     *
     * @param keyInfo the instance containing the certificates
     * @return the List of certificates contained in the keyinfo
     * @throws CertificateException if the one certificate cannot be converted to {@link X509Certificate}
     */
    @Nonnull
    public static List<X509Certificate> getCertificates(@Nonnull final KeyInfo keyInfo) throws CertificateException {
        Preconditions.checkNotNull(keyInfo,  KeyInfo.DEFAULT_ELEMENT_LOCAL_NAME);
        final List<X509Certificate> x509CertificatesOut = new ArrayList<>();

        final List<X509Data> x509Datas = keyInfo.getX509Datas();
        if (!x509Datas.isEmpty()) {
            final List<org.opensaml.xmlsec.signature.X509Certificate> x509Certificates = x509Datas.get(0).getX509Certificates();
            for (org.opensaml.xmlsec.signature.X509Certificate x509Certificate : x509Certificates) {
                X509Certificate cert = toCertificate(x509Certificate.getValue());
                x509CertificatesOut.add(cert);
            }
        }

        return x509CertificatesOut;
    }


    /**
     * Retrieves the certificates contained in the credentials parameter.
     *
     * @param credentials the list of credentials
     * @return the List of certificates for each one of the credential in credentials
     */
    @Nonnull
    public static List<X509Certificate> getCertificates(List<? extends Credential> credentials) {
        Preconditions.checkNotNull(credentials,  KeyInfo.DEFAULT_ELEMENT_LOCAL_NAME);

        final List<X509Certificate> certificates = new ArrayList<>();
        for (Credential credential : credentials){
            X509Certificate entityCertificate = ((BasicX509Credential) credential).getEntityCertificate();
            certificates.add(entityCertificate);
        }

        return certificates;
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

            return getCountry(xmlCert);
        } catch (CertificateException e) {
            LOG.error(MarkerFactory.getMarker("SAML_EXCHANGE"),
                      "BUSINESS EXCEPTION : Proccess getCountry from certificate: " + e.getMessage(), e);
            //TODO refactor this with configuration validation
            throw new RuntimeException(e);
        }
    }

	public static String getCountry(org.opensaml.xmlsec.signature.X509Certificate xmlCert) throws CertificateException {
		// Transform the KeyInfo to X509Certificate.
		X509Certificate cert = toCertificate(xmlCert.getValue());

		return getCountry(cert);
	}

	public static String getCountry(X509Certificate cert) {
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
	}


	public static boolean isSignatureWithCertificate(Signature signature) {
		return !signature.getKeyInfo().getX509Datas().isEmpty();
	}

    /**
     * Gets all certificates which are in a {@link Signature} as {@link List<X509Certificate>}
     *
     * @param signature that contains the certificates
     * @return the List<X509Certificate> of the signature
     * @throws CertificateException when could not create a {@link X509Certificate}
     */
    public static List<X509Certificate> getAllSignatureCertificates(Signature signature) throws CertificateException {
        List<X509Certificate> x509Certificates = new ArrayList<>();

        List<X509Data> x509Datas = signature.getKeyInfo().getX509Datas();
        for (X509Data x509Data : x509Datas) {
            List<org.opensaml.xmlsec.signature.X509Certificate> x509OpensamlCertificates = x509Data.getX509Certificates();
            for (org.opensaml.xmlsec.signature.X509Certificate x509OpensamlCertificate : x509OpensamlCertificates) {
                    final X509Certificate x509Certificate = CertificateUtil.toCertificate(x509OpensamlCertificate.getValue());
                    x509Certificates.add(x509Certificate);

            }
        }

        return x509Certificates;
    }
}
