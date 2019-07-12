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
package eu.eidas.auth.engine.core.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.SignatureConfiguration;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorManager;
import org.opensaml.xmlsec.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xmlsec.keyinfo.impl.BasicKeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.RSAKeyValue;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.ContentReference;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * The base abstract class for implementations of {@link ProtocolSignerI}.
 *
 * @since 1.1
 */
public abstract class AbstractProtocolSigner implements ProtocolSignerI, MetadataSignerI {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProtocolSigner.class);

    static {
        BouncyCastleBootstrap.bootstrap();
    }

    private static final String DEFAULT_SIGNATURE_ALGORITHM = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512;

    private static final ImmutableSet<String> ALLOWED_ALGORITHMS_FOR_SIGNING =
            ImmutableSet.of(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
                            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA384,
                            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512,
                            // RIPEMD is not allowed to sign
                            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256,
                            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA384,
                            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA512,
                            XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1);

    private static final ImmutableSet<String> ALLOWED_ALGORITHMS_FOR_VERIFYING =
            ImmutableSet.of(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
                            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA384,
                            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512,
                            // RIPEMD is allowed to verify
                            SignatureConstants.ALGO_ID_SIGNATURE_RSA_RIPEMD160,
                            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256,
                            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA384,
                            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA512,
                            XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1);

    private static final ImmutableSet<String> DEFAULT_ALGORITHM_WHITE_LIST =
            ImmutableSet.of(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
                            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA384,
                            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512,
                            // RIPEMD is not allowed to sign
                            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256,
                            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA384,
                            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA512,
                            XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1);

    private static final String DEFAULT_DIGEST_ALGORITHM = SignatureConstants.ALGO_ID_DIGEST_SHA512;

    private static final ImmutableMap<String, String> SIGNATURE_TO_DIGEST_ALGORITHM_MAP =
            ImmutableMap.<String, String>builder()
                    .put(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, SignatureConstants.ALGO_ID_DIGEST_SHA256)
                    .put(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA384, SignatureConstants.ALGO_ID_DIGEST_SHA384)
                    .put(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512, SignatureConstants.ALGO_ID_DIGEST_SHA512)
                    // RIPEMD is allowed to verify
                    .put(SignatureConstants.ALGO_ID_SIGNATURE_RSA_RIPEMD160,
                         SignatureConstants.ALGO_ID_DIGEST_RIPEMD160)
                    .put(SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256, SignatureConstants.ALGO_ID_DIGEST_SHA256)
                    .put(SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA384, SignatureConstants.ALGO_ID_DIGEST_SHA384)
                    .put(SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA512, SignatureConstants.ALGO_ID_DIGEST_SHA512)
                    .put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1, SignatureConstants.ALGO_ID_DIGEST_SHA256)
                    .build();

    private final boolean checkedValidityPeriod;

    private final boolean disallowedSelfSignedCertificate;

    private final boolean requestSignWithKey;

    private final boolean responseSignWithKey;

    private final boolean responseSignAssertions;

    private final ImmutableSet<String> signatureAlgorithmWhiteList;

    private final X509Credential privateSigningCredential;

    private final X509Credential publicSigningCredential;

    private final X509Credential privateMetadataSigningCredential;

    private final X509Credential publicMetadataSigningCredential;

    private final ImmutableList<X509Credential> trustedCredentials;

    private final String signatureAlgorithm;

    private final ImmutableSet<X509Certificate> metadataKeystoreCertificates;

    protected AbstractProtocolSigner(@Nonnull SignatureConfiguration signatureConfiguration)
            throws ProtocolEngineConfigurationException {
        this(signatureConfiguration.isCheckedValidityPeriod(),
                signatureConfiguration.isDisallowedSelfSignedCertificate(),
                signatureConfiguration.isResponseSignAssertions(),
                signatureConfiguration.isRequestSignWithKey(),
                signatureConfiguration.isResponseSignWithKey(),
                signatureConfiguration.getSignatureKeyAndCertificate(), signatureConfiguration.getTrustedCertificates(),
                signatureConfiguration.getSignatureAlgorithm(), signatureConfiguration.getSignatureAlgorithmWhiteList(),
                signatureConfiguration.getMetadataSigningKeyAndCertificate(),
                signatureConfiguration.getMetadataKeystoreCertificates());
    }

    protected AbstractProtocolSigner(boolean checkedValidityPeriod,
                                     boolean disallowedSelfSignedCertificate,
                                     boolean responseSignAssertions,
                                     boolean requestSignWithKey,
                                     boolean responseSignWithKey,
                                     @Nonnull KeyStore.PrivateKeyEntry signatureKeyAndCertificate,
                                     @Nonnull ImmutableSet<X509Certificate> trustedCertificates,
                                     @Nullable String signatureAlgorithmVal,
                                     @Nullable String signatureAlgorithmWhiteListStr,
                                     @Nullable KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate,
                                     @Nullable ImmutableSet<X509Certificate> metadataKeystoreCertificates)
            throws ProtocolEngineConfigurationException {
        Preconditions.checkNotNull(signatureKeyAndCertificate, "signatureKeyAndCertificate");
        Preconditions.checkNotNull(trustedCertificates, "trustedCertificates");
        String signatureAlg = signatureAlgorithmVal;
        if (StringUtils.isBlank(signatureAlg)) {
            signatureAlg = DEFAULT_SIGNATURE_ALGORITHM;
        } else {
            signatureAlg = validateSigningAlgorithm(signatureAlg);
        }
        ImmutableSet<String> signatureAlgorithmWhiteSet =
                WhiteListConfigurator.getAllowedAlgorithms(DEFAULT_ALGORITHM_WHITE_LIST,
                        ALLOWED_ALGORITHMS_FOR_VERIFYING,
                        signatureAlgorithmWhiteListStr);

        this.checkedValidityPeriod = checkedValidityPeriod;
        this.disallowedSelfSignedCertificate = disallowedSelfSignedCertificate;
        this.responseSignAssertions = responseSignAssertions;
        this.requestSignWithKey = requestSignWithKey;
        this.responseSignWithKey = responseSignWithKey;
        trustedCredentials = CertificateUtil.getListOfCredential(trustedCertificates);
        this.signatureAlgorithmWhiteList = signatureAlgorithmWhiteSet;
        this.signatureAlgorithm = signatureAlg;
        privateSigningCredential = CertificateUtil.createCredential(signatureKeyAndCertificate);
        publicSigningCredential =
                CertificateUtil.toCredential((X509Certificate) signatureKeyAndCertificate.getCertificate());
        if (null != metadataSigningKeyAndCertificate) {
            privateMetadataSigningCredential = CertificateUtil.createCredential(metadataSigningKeyAndCertificate);
            publicMetadataSigningCredential =
                    CertificateUtil.toCredential((X509Certificate) metadataSigningKeyAndCertificate.getCertificate());
        } else {
            privateMetadataSigningCredential = null;
            publicMetadataSigningCredential = null;
        }

        if (null != metadataKeystoreCertificates) {
            this.metadataKeystoreCertificates = metadataKeystoreCertificates;
        } else {
            this.metadataKeystoreCertificates = null;
        }

        if (privateMetadataSigningCredential instanceof BasicX509Credential) {
            final BasicX509Credential privateMetadataSigningCredential = (BasicX509Credential) this.privateMetadataSigningCredential;
            //TODO reverted order of certificates in the chain to allow backward compatibility with eIDAS 1.4.1. Change it when this is no longer needed
            privateMetadataSigningCredential.setEntityCertificateChain(metadataKeystoreCertificates.asList().reverse());
        }

        if (publicMetadataSigningCredential instanceof BasicX509Credential) {
            final BasicX509Credential publicMetadataSigningCredential = (BasicX509Credential) this.publicMetadataSigningCredential;
            //TODO reverted order of certificates in the chain to allow backward compatibility with eIDAS 1.4.1. Change it when this is no longer needed
            publicMetadataSigningCredential.setEntityCertificateChain(metadataKeystoreCertificates.asList().reverse());
        }

        // setDigestMethodAlgorithm(signatureAlgorithm);
    }

    private static X509Certificate getSignatureCertificate(Signature signature) throws EIDASSAMLEngineException {
        KeyInfo keyInfo = signature.getKeyInfo();
        try {
            return CertificateUtil.toCertificate(keyInfo);
        } catch (CertificateException e) {
            throw new EIDASSAMLEngineException(e);
        }
    }

    /**
     * Validates whether the given signature algorithm name contains an allowed digest algorithm.
     *
     * @param signatureAlgorithmName the signature algorithm name as defined in the XML Digital Signature standards.
     * @return the canonical algorithm name of the corresponding digest algorithm if allowed, otherwise throws an
     * exception
     * @throws ProtocolEngineConfigurationException if the given algorithm is not allowed
     */
    public static String validateDigestAlgorithm(String signatureAlgorithmName)
            throws ProtocolEngineConfigurationException {
        if (StringUtils.isBlank(signatureAlgorithmName)) {
            return DEFAULT_DIGEST_ALGORITHM;
        }
        String canonicalAlgorithm = signatureAlgorithmName.trim();
        String digestAlgorithm = SIGNATURE_TO_DIGEST_ALGORITHM_MAP.get(canonicalAlgorithm);
        if (null != digestAlgorithm) {
            return digestAlgorithm;
        }
        String msg =
                "Signing algorithm \"" + signatureAlgorithmName + "\" does not contain an allowed digest algorithm";
        LOG.error(msg);
        throw new ProtocolEngineConfigurationException(msg);
    }

    /**
     * Validates whether the given signature algorithm name is allowed.
     *
     * @param signatureAlgorithmName the signature algorithm name as defined in the XML Digital Signature standards.
     * @return the canonical algorithm name if allowed, otherwise throws an exception
     * @throws ProtocolEngineConfigurationException if the given algorithm is not allowed
     */
    public static String validateSigningAlgorithm(@Nullable String signatureAlgorithmName)
            throws ProtocolEngineConfigurationException {
        if (signatureAlgorithmName == null || StringUtils.isBlank(signatureAlgorithmName)) {
            return DEFAULT_SIGNATURE_ALGORITHM;
        }
        String canonicalAlgorithm = signatureAlgorithmName.trim();
        if (ALLOWED_ALGORITHMS_FOR_SIGNING.contains(canonicalAlgorithm)) {
            return canonicalAlgorithm;
        }
        String msg = "Signing algorithm \"" + signatureAlgorithmName + "\" is not allowed";
        LOG.error(msg);
        throw new ProtocolEngineConfigurationException(msg);
    }

    protected void checkCertificateIssuer(X509Certificate certificate) throws EIDASSAMLEngineException {
        CertificateValidator.checkCertificateIssuer(disallowedSelfSignedCertificate, certificate);
    }

    protected void checkCertificateValidityPeriod(X509Certificate certificate) throws EIDASSAMLEngineException {
        CertificateValidator.checkCertificateValidityPeriod(checkedValidityPeriod, certificate);
    }

    protected Signature createSignature(@Nonnull X509Credential credential, boolean onlyKeyInfoNoCert) throws EIDASSAMLEngineException {
        checkCertificateValidityPeriod(credential.getEntityCertificate());
        checkCertificateIssuer(credential.getEntityCertificate());

        Signature signature;
        LOG.debug("Creating an OpenSAML signature object");

        signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signature.setSigningCredential(credential);

        signature.setSignatureAlgorithm(getSignatureAlgorithm());

        KeyInfo keyInfo = createKeyInfo(credential, onlyKeyInfoNoCert);

        signature.setKeyInfo(keyInfo);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        return signature;
    }

    @Nullable
    @Override
    public X509Credential getPublicMetadataSigningCredential() {
        return publicMetadataSigningCredential;
    }

    @Nonnull
    @Override
    public X509Credential getPublicSigningCredential() {
        return publicSigningCredential;
    }

    /**
     * @return the signature algorithm to be used when signing
     */
    protected String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    protected ImmutableSet<String> getSignatureAlgorithmWhiteList() {
        return signatureAlgorithmWhiteList;
    }

    /**
     * Gets the certificate from the given signature and checks whether it is trusted.
     * <p>
     * If it is trusted, returns this certificate otherwise throws an EIDASSAMLEngineException exception.
     *
     * @param signature the signature instance
     * @param trustedCredentialList the trusted certificates which can be used to sign the given signature
     * @return the signing certificate if it is trusted, or throws an exception otherwise.
     * @throws EIDASSAMLEngineException when the signature is signed by an untrusted certificate
     */
    @Nonnull
    private X509Credential getTrustedCertificate(@Nonnull Signature signature,
                                                 @Nonnull List<? extends Credential> trustedCredentialList)
            throws EIDASSAMLEngineException {
        X509Credential entityX509Cred;
        if(CertificateUtil.isSignatureWithCertificate(signature)){
            X509Certificate cert = getSignatureCertificate(signature);
            // Exist only one certificate
            entityX509Cred = CertificateUtil.toCredential(cert);

            checkCertificateValidityPeriod(cert);
            checkCertificateIssuer(cert);
        }else{
            try {
	        	RSAKeyValue signatureRsaKeyValue = signature.getKeyInfo().getKeyValues().get(0).getRSAKeyValue();
				entityX509Cred = getTrustedCertificateFromRSAKeyValue(signatureRsaKeyValue, trustedCredentialList);
			} catch (IndexOutOfBoundsException e) {
				throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(), e);
			}
        }

        checkValidTrust(signature, trustedCredentialList, entityX509Cred);

        return entityX509Cred;
    }

    /**
     * Checks the validity of trust related to certificate(s) present in the signature.
     *
     * If the signature has only one certificate, explicit certificate validation is performed.
     * Otherwise, validation of the certification path is performed.
     *
     * @param signature the {@link Signature} to be validated
     * @param trustedCredentialList that contains the trusted credentials
     * @param entityX509Cred the credential related to the key that signs
     * @throws EIDASSAMLEngineException if the either the explicit or the certification path validation fails.
     */
    private void checkValidTrust(@Nonnull Signature signature, @Nonnull List<? extends Credential> trustedCredentialList, X509Credential entityX509Cred) throws EIDASSAMLEngineException {
        try {
            final List<X509Certificate> trustedCertificatesSignature = CertificateUtil.getCertificates(signature.getKeyInfo());
            final List<X509Certificate> trustedCertificates = CertificateUtil.getCertificates(trustedCredentialList);
            final boolean hasLeafInTrusted = hasCredential(entityX509Cred, trustedCredentialList);

            if (hasLeafInTrusted) {
                CertificateUtil.checkChainTrust(entityX509Cred, trustedCredentialList);
            } else {
                final X509Certificate issuerCertificate = getIssuerCertificate(entityX509Cred, trustedCertificatesSignature, trustedCertificates);

                if (null == issuerCertificate) {
                        throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNTRUSTED_CERTIFICATE.errorCode(),
                                EidasErrorKey.SAML_ENGINE_UNTRUSTED_CERTIFICATE.errorMessage());
                } else {
                    final X509Credential issuerX509Credential = CertificateUtil.toCredential(issuerCertificate);
                    CertificateUtil.checkChainTrust(issuerX509Credential, trustedCredentialList);
                }
            }

        } catch (CertificateException e) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(), e);
        }
    }

    private X509Certificate getIssuerCertificate(X509Credential entityX509Cred, List<X509Certificate> signatureCertificates, List<X509Certificate> trustedCertificates) {
        X509Certificate issuerCertificate = CertificateUtil.getIssuerX509Certificate(entityX509Cred, trustedCertificates);
        if (null == issuerCertificate) {
            issuerCertificate = CertificateUtil.getIssuerX509Certificate(entityX509Cred, signatureCertificates);
        }
        return issuerCertificate;
    }

    /**
     * Looks up for {@param credentialToLookUp} in {@param credentialToLookUp}
     *
     * @param credentialToLookUp
     * @param credentials the list of credentials where the {@param credentialToLookUp} will be looked up
     * @return true if credential is in {@param credentials}
     */
    private boolean hasCredential(X509Credential credentialToLookUp, List<? extends Credential> credentials) {
        X509Certificate leafCertificate = credentialToLookUp.getEntityCertificate();
        for (Credential credential : credentials) {
            if (credential instanceof BasicX509Credential) {
                X509Certificate trustedCertificate = ((BasicX509Credential) credential).getEntityCertificate();
                if(trustedCertificate.equals(leafCertificate)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected ImmutableList<X509Credential> getTrustedCredentials() {
        return trustedCredentials;
    }

    private boolean isAlgorithmAllowedForVerifying(@Nonnull String signatureAlgorithm) {
        return isNotBlank(signatureAlgorithm) && getSignatureAlgorithmWhiteList().contains(
                signatureAlgorithm.trim());
    }

    /**
     * Sign the token SAML.
     *
     * @param signableObject the token SAML.
     * @return the SAML object
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    @Override
    @Nonnull
    public <T extends SignableXMLObject> T sign(@Nonnull T signableObject) throws EIDASSAMLEngineException {
        return sign(signableObject, false);
    }

    /**
     * Sign the token SAML.
     *
     * @param signableObject the token SAML.
     * @param onlyKeyInfoNoCert flag to put only the RSAKeyValue instead of the full X509Data in the Signature.
     * @return the SAML object
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    @Override
    @Nonnull
    public <T extends SignableXMLObject> T sign(@Nonnull T signableObject, boolean onlyKeyInfoNoCert) throws EIDASSAMLEngineException {
        return sign(signableObject, privateSigningCredential, onlyKeyInfoNoCert);
    }

    @Nonnull
    protected <T extends SignableXMLObject> T sign(@Nonnull T signableObject, @Nonnull X509Credential signingCredential, boolean onlyKeyInfoNoCert)
            throws EIDASSAMLEngineException {
        LOG.trace("Start Sign process.");
        try {
            Signature signature = createSignature(signingCredential, onlyKeyInfoNoCert);
            signableObject.setSignature(signature);

            // Reference/DigestMethod algorithm is set by default to SHA-1 in OpenSAML
            String digestAlgorithm = validateDigestAlgorithm(getSignatureAlgorithm());
            List<ContentReference> contentReferences = signature.getContentReferences();
            if (isNotEmpty(contentReferences)) {
                ((SAMLObjectContentReference)contentReferences.get(0)).setDigestAlgorithm(digestAlgorithm);
            } else {
                LOG.error("Unable to set DigestMethodAlgorithm - algorithm {} not set", digestAlgorithm);
            }

            LOG.trace("Marshall samlToken.");
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(signableObject).marshall(signableObject);

            LOG.trace("Sign samlToken.");
            Signer.signObject(signature);
        } catch (MarshallingException e) {
            LOG.error("ERROR : MarshallingException: " + e, e);
            throw new EIDASSAMLEngineException(e);
        } catch (SignatureException e) {
            LOG.error("ERROR : Signature exception: " + e, e);
            throw new EIDASSAMLEngineException(e);
        }

        return signableObject;
    }

    /**
     * Signs the metadata with the metadata signature key.
     *
     * @param signableObject the metadata.
     * @return the signed metadata
     * @throws EIDASMetadataException in case of signature errors
     */
    @Override
    @Nonnull
    public <T extends SignableXMLObject> T signMetadata(@Nonnull T signableObject) throws EIDASMetadataException {
        if (null == privateMetadataSigningCredential) {
            throw new EIDASMetadataException("No metadataSigningCredential configured");
        }
        try {
            return sign(signableObject, privateMetadataSigningCredential,false);
        } catch (EIDASSAMLEngineException e) {
            //TODO remove this conversion when metadata is physically decoupled
            throw new EIDASMetadataException(e);
        }
    }

    @Nonnull
    @Override
    public <T extends SignableXMLObject> T validateMetadataSignature(@Nonnull T signedMetadata)
            throws EIDASMetadataException {
        try {
            return validateSignature(signedMetadata, null);
        } catch (EIDASSAMLEngineException e) {
            //TODO remove this conversion when metadata is physically decoupled
            throw new EIDASMetadataException(e);
        }
    }

    private SAMLSignatureProfileValidator validateSamlSignatureStructure(SignableXMLObject signableObject)
            throws EIDASSAMLEngineException {
        SAMLSignatureProfileValidator sigProfValidator = new SAMLSignatureProfileValidator();
        try {
            // Indicates signature id conform to SAML Signature profile
            sigProfValidator.validate(signableObject.getSignature());
        } catch (SignatureException e) {
            LOG.error("ERROR : ValidationException: signature isn't conform to SAML Signature profile: " + e, e);
            throw new EIDASSAMLEngineException(e);
        }
        return sigProfValidator;
    }

    @Override
    @Nonnull
    public <T extends SignableXMLObject> T validateSignature(@Nonnull T signedObject,
                                                             @Nullable
                                                                     Collection<X509Certificate> trustedCertificateCollection)
            throws EIDASSAMLEngineException {
        List<? extends Credential> trustedCreds;

        // 2) Verify the cryptographic signature:
        if (CollectionUtils.isEmpty(trustedCertificateCollection)) {
            trustedCreds = getTrustedCredentials();
        } else {
            trustedCreds = CertificateUtil.getListOfCredential(trustedCertificateCollection);
        }
        return validateSignatureWithCredentials(signedObject, trustedCreds);
    }

    @Nonnull
    private <T extends SignableXMLObject> T validateSignatureWithCredentials(@Nonnull T signedObject,
                                                                             @Nonnull
                                                                                     List<? extends Credential> trustedCredentialList)
            throws EIDASSAMLEngineException {
        LOG.debug("Start signature validation.");
        // 1) Validate the structure of the SAML signature:
        validateSamlSignatureStructure(signedObject);

        // 2) Verify the cryptographic signature:
        verifyCryptographicSignature(signedObject.getSignature(), trustedCredentialList);

        return signedObject;
    }

    private void verifyCryptographicSignature(@Nonnull Signature signature,
                                              @Nonnull List<? extends Credential> trustedCredentialList)
            throws EIDASSAMLEngineException {
        // 1) check that we accept the signature algorithm
        String signatureAlgorithmVal = signature.getSignatureAlgorithm();
        LOG.trace("Key algorithm {}", signatureAlgorithmVal);
        if (!isAlgorithmAllowedForVerifying(signatureAlgorithmVal)) {
            LOG.error("ERROR : the algorithm {} used by the signature is not allowed", signatureAlgorithmVal);
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM.errorCode());
        }

        // 2) check that we trust the signing certificate
        X509Credential entityX509Cred = getTrustedCertificate(signature, trustedCredentialList);

        // 3) verify the XML Digital Signature itself (XML-DSig)
        // DOM information related to the signature should be still available at this point
        try {
            SignatureValidator.validate(signature, entityX509Cred);
        } catch (SignatureException e) {
            LOG.error("ERROR : Signature Validation Exception: " + e, e);
            throw new EIDASSAMLEngineException(e);
        }
    }

    @Override
    public boolean isResponseSignAssertions() {
        return responseSignAssertions;
    }

    @Override
    public boolean isRequestSignWithKey() {
        return requestSignWithKey;
    }

    @Override
    public boolean isResponseSignWithKey() {
        return responseSignWithKey;
    }

    @Override
	public X509Credential getTrustedCertificateFromRSAKeyValue(RSAKeyValue signatureRsaKeyValue) throws SecurityException, EIDASSAMLEngineException {
		return getTrustedCertificateFromRSAKeyValue(signatureRsaKeyValue, getTrustedCredentials());
	}

    public static X509Credential getTrustedCertificateFromRSAKeyValue(RSAKeyValue signatureRsaKeyValue, 
			List<? extends Credential> trustedCredentialList)
			throws EIDASSAMLEngineException {
		BigInteger signatureExponent = signatureRsaKeyValue.getExponent().getValueBigInt();
		BigInteger signatureModulus = signatureRsaKeyValue.getModulus().getValueBigInt();

		for (Credential credential:trustedCredentialList){
			KeyInfoGeneratorFactory keyInfoGenFac = createKeyInfoGeneratorFactory((X509Credential) credential);
			KeyInfo keyInfo = createKeyInfo(keyInfoGenFac, (X509Credential) credential);
			
			RSAKeyValue trustedCredentialRsaKeyValue = keyInfo.getKeyValues().get(0).getRSAKeyValue();
			BigInteger trustedCredentialExponent = trustedCredentialRsaKeyValue.getExponent().getValueBigInt();
			BigInteger trustedCredentialModulus = trustedCredentialRsaKeyValue.getModulus().getValueBigInt();
			
			if(signatureExponent.equals(trustedCredentialExponent) 
					&& signatureModulus.equals(trustedCredentialModulus)){
				 return(X509Credential) credential;
			}
		}
		throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNTRUSTED_CERTIFICATE.errorCode());
	}

    public static KeyInfo createKeyInfo(X509Credential credential, boolean onlyKeyInfoNoCert)
			throws EIDASSAMLEngineException {
		KeyInfoGeneratorFactory keyInfoGenFac;
        if (onlyKeyInfoNoCert){
        	keyInfoGenFac = createKeyInfoGeneratorFactory(credential);
        }else{
            SignatureSigningConfiguration secConfiguration = SecurityConfigurationSupport.getGlobalSignatureSigningConfiguration();
            NamedKeyInfoGeneratorManager keyInfoManager = secConfiguration.getKeyInfoGeneratorManager();
            KeyInfoGeneratorManager keyInfoGenManager = keyInfoManager.getDefaultManager();
            keyInfoGenFac = keyInfoGenManager.getFactory(credential);
        }
        KeyInfo keyInfo = createKeyInfo(keyInfoGenFac, credential);
		return keyInfo;
	}

	public static KeyInfo createKeyInfo(KeyInfoGeneratorFactory keyInfoGenFac, X509Credential credential) throws EIDASSAMLEngineException
			 {
		KeyInfoGenerator keyInfoGenerator = keyInfoGenFac.newInstance();
		KeyInfo keyInfo;
		try {
			keyInfo = keyInfoGenerator.generate(credential);
			return keyInfo;
		} catch (SecurityException e) {
			throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNTRUSTED_CERTIFICATE.errorCode(),e);
		}
	}

	public static KeyInfoGeneratorFactory createKeyInfoGeneratorFactory(X509Credential credential) {
		KeyInfoGeneratorFactory keyInfoGenFac;
		X509Certificate certificate = credential.getEntityCertificate();
		BasicX509Credential keyInfoCredential = new BasicX509Credential(certificate);
		keyInfoCredential.setEntityCertificate(certificate);
		keyInfoGenFac = new BasicKeyInfoGeneratorFactory();
		((BasicKeyInfoGeneratorFactory)keyInfoGenFac).setEmitPublicKeyValue(true);
		((BasicKeyInfoGeneratorFactory)keyInfoGenFac).setEmitEntityIDAsKeyName(true);
		((BasicKeyInfoGeneratorFactory)keyInfoGenFac).setEmitKeyNames(true);
		((BasicKeyInfoGeneratorFactory)keyInfoGenFac).setEmitPublicDEREncodedKeyValue(true);
		return keyInfoGenFac;
	}

}
