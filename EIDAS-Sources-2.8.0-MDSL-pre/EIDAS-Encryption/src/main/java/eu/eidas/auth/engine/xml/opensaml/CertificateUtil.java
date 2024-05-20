/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.auth.engine.xml.opensaml;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.lang.StringUtils;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.google.common.collect.ImmutableList;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.engine.xml.opensaml.dss.DSSCertificateTrustVerifier;
import eu.eidas.auth.engine.xml.opensaml.exception.CertificateRevokedException;
import eu.eidas.auth.engine.xml.opensaml.exception.MissingCertificateRevocationDataException;
import eu.eidas.auth.engine.xml.opensaml.exception.UntrustedCertificateException;
import eu.eidas.util.Preconditions;

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

    private static final CertificateTrustVerifier certificateTrustVerifier = new DSSCertificateTrustVerifier();

    public static void checkChainTrust(final X509Credential x509Credential,
                                       final Iterable<? extends Credential> trustedCredentials) throws CertificateException {
        checkChainTrust(x509Credential, trustedCredentials, new CertificateVerifierParams());
    }

    /**
     * Performs a certification path key evaluation between the param x509Credential and all the credential contained in param trustedCredentials.
     * Verify the revocation state of certificates if it applies.
     *
     * @param x509Credential the {@link X509Credential} to be evaluated
     * @param trustedCredentials the list of credentials that the param x509Credential will be evaluated against.
     * @param verifierParams the configuration for revocation checking
     * @throws CertificateException if the credential is not trusted or if the certificate is revoked
     */
    public static void checkChainTrust(final X509Credential x509Credential,
                                       final Iterable<? extends Credential> trustedCredentials,
                                       final CertificateVerifierParams verifierParams)
            throws CertificateException {

        LOG.debug(x509Credential.getEntityId());
        LOG.debug(x509Credential.getEntityCertificate().getIssuerDN().getName());
        LOG.debug("" + x509Credential.getEntityCertificate().getNotAfter());
        LOG.debug("" + x509Credential.getEntityCertificate().getSerialNumber());

        final List<X509Certificate> trustAnchors = new ArrayList<>();

        for (final Credential trustAnchor : trustedCredentials) {
            if (trustAnchor instanceof X509Credential) {
                trustAnchors.add(((X509Credential) trustAnchor).getEntityCertificate());
            } else {
                LOG.warn("Trust anchor is not an X509Credential: {}", trustAnchor.getEntityId());
            }
        }

        try {
            certificateTrustVerifier.verify(x509Credential, trustAnchors, verifierParams);
        } catch (final CertificateRevokedException | MissingCertificateRevocationDataException | UntrustedCertificateException ex) {
            throw new CertificateException(ex); // Just used to keep old behavior, might want to update the exception
        }
    }

    public static X509Credential createCredential(KeyStore.PrivateKeyEntry privateKeyEntry) {
        if (null == privateKeyEntry) {
            return null;
        }
        List<X509Certificate> certificateChain = Arrays.asList(privateKeyEntry.getCertificateChain()).stream()
                .map(X509Certificate.class::cast)
                .collect(Collectors.toList());
        return createCredential(privateKeyEntry.getPrivateKey(), certificateChain);
    }

    /**
     * @param privateKey the private key {@link PrivateKey}
     * @param certificateChain the {@link X509Credential}  chain to be evaluated
     * @return a credential based on the provided elements
     */
    public static X509Credential createCredential(PrivateKey privateKey, @Nonnull List<X509Certificate> certificateChain) {
        if (certificateChain == null || certificateChain.isEmpty()) {
            throw new IllegalArgumentException("CertificateChain may not be null or empty.");
        }
        BasicX509Credential credential = new BasicX509Credential(certificateChain.get(0), privateKey);
        credential.setEntityCertificateChain(certificateChain);
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

    public static ImmutableList<X509Credential> getListOfCredential(Iterable<X509Certificate> certificates) {
        ImmutableList.Builder<X509Credential> trustCred = ImmutableList.builder();
        for (final X509Certificate certificate : certificates) {
            trustCred.add((toCredential(certificate)));
        }
        return trustCred.build();
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

        final List<X509Data> x509Datas = getX509Datas(keyInfo);
        if (!x509Datas.isEmpty()) {
            final List<org.opensaml.xmlsec.signature.X509Certificate> x509Certificates = x509Datas.get(0).getX509Certificates();
            for (org.opensaml.xmlsec.signature.X509Certificate x509Certificate : x509Certificates) {
                X509Certificate cert = toCertificate(x509Certificate.getValue());
                x509CertificatesOut.add(cert);
            }
        }

        return x509CertificatesOut;
    }


    @Nonnull
    private static List<X509Data> getX509Datas(@Nonnull final KeyInfo keyInfo) {
        Preconditions.checkNotNull(keyInfo,  KeyInfo.DEFAULT_ELEMENT_LOCAL_NAME);
        if (!keyInfo.getX509Datas().isEmpty()) {
            return keyInfo.getX509Datas();
        } else if (!keyInfo.getAgreementMethods().isEmpty()) {
            return keyInfo.getAgreementMethods().get(0).getRecipientKeyInfo().getX509Datas();
        }
        return Collections.emptyList();
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
    public static X509Credential toCredential(@Nonnull X509Certificate certificate) {
        Preconditions.checkNotNull(certificate, "certificate");
        BasicX509Credential credential = new BasicX509Credential(certificate);
        return credential;
    }

    private CertificateUtil() {
    }

    /**
     * Gets the country from X.509 Certificate.
     *
     * @param keyInfo the key info
     * @return the country
     */
    public static String getCountry(KeyInfo keyInfo) throws eu.eidas.encryption.exception.CertificateException {
        LOG.trace("Recover country information.");
        try {
            org.opensaml.xmlsec.signature.X509Certificate xmlCert =
                    keyInfo.getX509Datas().get(0).getX509Certificates().get(0);

            return getCountry(xmlCert);
        } catch (CertificateException e) {
            LOG.error(MarkerFactory.getMarker("SAML_EXCHANGE"),
                      "BUSINESS EXCEPTION : Proccess getCountry from certificate: " + e.getMessage(), e);
            throw new eu.eidas.encryption.exception.CertificateException("Invalid certificate fail to determine country", e);
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
     * Gets the list of {@link X509Certificate} contained in a {@link Signature}
     *
     * @param signature that contains the certificates
     * @return the List of {@link X509Certificate} extracted from the given {@link Signature}
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
