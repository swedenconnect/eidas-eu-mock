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
package eu.eidas.auth.engine.core.impl;

import static eu.eidas.auth.engine.core.eidas.spec.EidasSignatureConstants.DEFAULT_DIGEST_ALGORITHM;
import static eu.eidas.auth.engine.core.eidas.spec.EidasSignatureConstants.DEFAULT_DIGEST_ALGORITHM_WHITE_LIST;
import static eu.eidas.auth.engine.core.eidas.spec.EidasSignatureConstants.MIN_EC_KEY_LENGTH;
import static eu.eidas.auth.engine.core.eidas.spec.EidasSignatureConstants.MIN_RSA_KEY_LENGTH;
import static eu.eidas.auth.engine.core.eidas.spec.EidasSignatureConstants.MIN_SIGNATURE_HASH_LENGTH;
import static eu.eidas.auth.engine.xml.opensaml.CertificateUtil.getAllSignatureCertificates;
import static eu.eidas.auth.engine.xml.opensaml.CertificateVerifierParams.DISABLE_REVOCATION_CHECK;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.crypto.JCAConstants;
import org.opensaml.security.crypto.KeySupport;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.SignatureConfiguration;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SigningContext;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.auth.engine.xml.opensaml.CertificateVerifierParams;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;

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

    private SignatureConfiguration signatureConfiguration;

    private final SigningContext requestSigningContext;
    private final SigningContext responseSigningContext;
    private final SigningContext metadataSigningContext;

    private final ImmutableList<X509Credential> trustedCredentials;

    private final CertificateVerifierParams metadataCertificateVerifierParams;

    protected AbstractProtocolSigner(@Nonnull SignatureConfiguration signatureConfiguration)
            throws ProtocolEngineConfigurationException {
        Preconditions.checkNotNull(signatureConfiguration, "signatureConfiguration");
        Preconditions.checkNotNull(signatureConfiguration.getSignatureKeyAndCertificate(), "signatureKeyAndCertificate");
        Preconditions.checkNotNull(signatureConfiguration.getTrustedCertificates(), "trustedCertificates");
        this.signatureConfiguration = signatureConfiguration;

        validateSigningConfiguration(signatureConfiguration);

        trustedCredentials = CertificateUtil.getListOfCredential(signatureConfiguration.getTrustedCertificates());

        X509Credential privateSigningCredential = CertificateUtil
                .createCredential(signatureConfiguration.getSignatureKeyAndCertificate());
        requestSigningContext = new SigningContext.Builder()
                .setSigningAlgorithm(signatureConfiguration.getSignatureAlgorithm())
                .setSigningCredential(privateSigningCredential)
                .setSignWithKeyValue(signatureConfiguration.isRequestSignWithKey())
                .build();
        responseSigningContext = new SigningContext.Builder()
                .setSigningAlgorithm(signatureConfiguration.getSignatureAlgorithm())
                .setSigningCredential(privateSigningCredential)
                .setSignWithKeyValue(signatureConfiguration.isResponseSignWithKey())
                .build();

        X509Credential privateMetadataSigningCredential = CertificateUtil
                .createCredential(signatureConfiguration.getMetadataSigningKeyAndCertificate());
        metadataSigningContext = new SigningContext.Builder()
                .setSigningAlgorithm(signatureConfiguration.getMetadataSignatureAlgorithm())
                .setSigningCredential(privateMetadataSigningCredential)
                .setSignWithKeyValue(false)
                .build();

        // TODO remove when handling default configuration for Digests
        boolean shouldUpdateDigestValues = false;
        String digestMethodAlgorithm = signatureConfiguration.getDigestAlgorithm();
        if (digestMethodAlgorithm == null) {
            digestMethodAlgorithm = DEFAULT_DIGEST_ALGORITHM;
            shouldUpdateDigestValues = true;
        }
        ImmutableSet<String> digestMethodAlgorithmWhiteList = signatureConfiguration.getDigestMethodAlgorithmWhiteList();
        if (digestMethodAlgorithmWhiteList == null) {
            digestMethodAlgorithmWhiteList = DEFAULT_DIGEST_ALGORITHM_WHITE_LIST;
            shouldUpdateDigestValues = true;
        }

        if (shouldUpdateDigestValues) {
            this.signatureConfiguration = new SignatureConfiguration.Builder(signatureConfiguration)
                    .setDigestAlgorithm(digestMethodAlgorithm)
                    .setDigestMethodAlgorithmWhiteList(digestMethodAlgorithmWhiteList)
                    .build();
        }

        this.metadataCertificateVerifierParams = new CertificateVerifierParams(
                signatureConfiguration.isEnableCertificateRevocationChecking(),
                signatureConfiguration.isEnableCertificateRevocationSoftFail()
        );
    }

    /**
     * @deprecated use AbstractProtocolSigner(@Nonnull SignatureConfiguration signatureConfiguration) instead
     */
    @Deprecated
    protected AbstractProtocolSigner(boolean checkedValidityPeriod,
                                     boolean disallowedSelfSignedCertificate,
                                     boolean responseSignAssertions,
                                     boolean requestSignWithKey,
                                     boolean responseSignWithKey,
                                     boolean enableCertificateRevocationChecking,
                                     boolean enableCertificateRevocationSoftFail,
                                     @Nonnull KeyStore.PrivateKeyEntry signatureKeyAndCertificate,
                                     @Nonnull ImmutableSet<X509Certificate> trustedCertificates,
                                     @Nullable String signatureAlgorithmVal,
                                     @Nullable String signatureAlgorithmWhiteListStr,
                                     @Nullable KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate,
                                     @Nullable ImmutableSet<X509Certificate> metadataKeystoreCertificates)
            throws ProtocolEngineConfigurationException {
        this(new SignatureConfiguration.Builder()
                .setCheckedValidityPeriod(checkedValidityPeriod)
                .setDisallowedSelfSignedCertificate(disallowedSelfSignedCertificate)
                .setResponseSignAssertions(responseSignAssertions)
                .setRequestSignWithKey(requestSignWithKey)
                .setResponseSignWithKey(responseSignWithKey)
                .setEnableCertificateRevocationChecking(enableCertificateRevocationChecking)
                .setEnableCertificateRevocationSoftFail(enableCertificateRevocationSoftFail)
                .setSignatureKeyAndCertificate(signatureKeyAndCertificate)
                .setTrustedCertificates(trustedCertificates)
                .setSignatureAlgorithm(signatureAlgorithmVal)
                .setSignatureAlgorithmWhiteList(signatureAlgorithmWhiteListStr)
                .setDigestAlgorithm(DEFAULT_DIGEST_ALGORITHM)
                .setDigestMethodAlgorithmWhiteList(DEFAULT_DIGEST_ALGORITHM_WHITE_LIST)
                .setMetadataSigningKeyAndCertificate(metadataSigningKeyAndCertificate)
                .setMetadataKeystoreCertificates(metadataKeystoreCertificates)
                .build()
                );
    }

    /**
     * @deprecated use AbstractProtocolSigner(@Nonnull SignatureConfiguration signatureConfiguration) instead
     */
    @Deprecated
    protected AbstractProtocolSigner(boolean checkedValidityPeriod,
                                     boolean disallowedSelfSignedCertificate,
                                     boolean responseSignAssertions,
                                     boolean requestSignWithKey,
                                     boolean responseSignWithKey,
                                     boolean enableCertificateRevocationChecking,
                                     boolean enableCertificateRevocationSoftFail,
                                     @Nonnull KeyStore.PrivateKeyEntry signatureKeyAndCertificate,
                                     @Nonnull ImmutableSet<X509Certificate> trustedCertificates,
                                     @Nullable String signatureAlgorithmVal,
                                     @Nullable String signatureAlgorithmWhiteListStr,
                                     @Nullable String digestMethodAlgorithm,
                                     @Nullable ImmutableSet digestMethodAlgorithmWhiteList,
                                     @Nullable KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate,
                                     @Nullable ImmutableSet<X509Certificate> metadataKeystoreCertificates)
            throws ProtocolEngineConfigurationException {
        this(new SignatureConfiguration.Builder()
                .setCheckedValidityPeriod(checkedValidityPeriod)
                .setDisallowedSelfSignedCertificate(disallowedSelfSignedCertificate)
                .setResponseSignAssertions(responseSignAssertions)
                .setRequestSignWithKey(requestSignWithKey)
                .setResponseSignWithKey(responseSignWithKey)
                .setEnableCertificateRevocationChecking(enableCertificateRevocationChecking)
                .setEnableCertificateRevocationSoftFail(enableCertificateRevocationSoftFail)
                .setSignatureKeyAndCertificate(signatureKeyAndCertificate)
                .setTrustedCertificates(trustedCertificates)
                .setSignatureAlgorithm(signatureAlgorithmVal)
                .setSignatureAlgorithmWhiteList(signatureAlgorithmWhiteListStr)
                .setDigestAlgorithm(digestMethodAlgorithm)
                .setDigestMethodAlgorithmWhiteList(digestMethodAlgorithmWhiteList)
                .setMetadataSigningKeyAndCertificate(metadataSigningKeyAndCertificate)
                .setMetadataKeystoreCertificates(metadataKeystoreCertificates)
                .build()
                );
    }

    private static X509Certificate getSignatureCertificate(Signature signature) throws EIDASSAMLEngineException {
        KeyInfo keyInfo = signature.getKeyInfo();
        try {
            return CertificateUtil.toCertificate(keyInfo);
        } catch (CertificateException e) {
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE,
                    "Failed to extract X509 from XML", e);
        }
    }

    /**
     * Validates the signature configuration
     *
     * @param signatureConfiguration the signature configuration.
     * @throws ProtocolEngineConfigurationException if the given configuration is not valid
     */
    private void validateSigningConfiguration(final SignatureConfiguration signatureConfiguration)
            throws ProtocolEngineConfigurationException {

        validateIsConfiguredSignatureAlgorithmsWhitelist(signatureConfiguration);
        validateWhitelistedSignatureAlgorithm(signatureConfiguration, signatureConfiguration.getSignatureAlgorithm(), "Signing algorithm \"");
        validateWhitelistedSignatureAlgorithm(signatureConfiguration, signatureConfiguration.getMetadataSignatureAlgorithm(), "Metadata signing algorithm \"");
    }

    /**
     *  Validates if signature algorithm whitelist is configured
     *
     * @param signatureConfiguration the signature configuration
     * @throws ProtocolEngineConfigurationException if the given configuration is not valid
     */
    private void validateIsConfiguredSignatureAlgorithmsWhitelist(SignatureConfiguration signatureConfiguration) throws ProtocolEngineConfigurationException {
        if (signatureConfiguration.getSignatureAlgorithmWhitelist() == null
                || signatureConfiguration.getSignatureAlgorithmWhitelist().isEmpty()) {
            String msg = "Signing algorithm whitelist must be defined";
            LOG.error(msg);
            throw new ProtocolEngineConfigurationException(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM, msg);
        }
    }

    /**
     * Validated if the signature algorithm is in the signature whitelist
     *
     * @param signatureConfiguration the signature configuration
     * @param signatureAlgorithm the signature algorithm
     * @param errorMessage the error message in case of invalid signature algorithm
     * @throws ProtocolEngineConfigurationException if the given configuration is not valid
     */
    private void validateWhitelistedSignatureAlgorithm(SignatureConfiguration signatureConfiguration, String signatureAlgorithm, String errorMessage) throws ProtocolEngineConfigurationException {
        final String canonicalSignatureAlgorithm = signatureAlgorithm.trim();
        if (!signatureConfiguration.getSignatureAlgorithmWhitelist().contains(canonicalSignatureAlgorithm)) {
            String msg = errorMessage + signatureAlgorithm + "\" is not allowed";
            LOG.error(msg);
            throw new ProtocolEngineConfigurationException(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM, msg);
        }
    }

    /**
     * Validates whether the given digest algorithm name is allowed by the digest algorithm whitelist.
     *
     * @param digestMethodAlgorithmName the digest method algorithm name
     * @return the digest algorithm name if allowed, otherwise throws an exception
     * @throws ProtocolEngineConfigurationException if the given algorithm is not allowed
     */
    public static String validateDigestAlgorithm(String digestMethodAlgorithmName)
            throws ProtocolEngineConfigurationException {
        if (StringUtils.isBlank(digestMethodAlgorithmName)) {
            return DEFAULT_DIGEST_ALGORITHM;
        }

        if (DEFAULT_DIGEST_ALGORITHM_WHITE_LIST.contains(digestMethodAlgorithmName.trim())) {
            return digestMethodAlgorithmName;
        }
        String msg =
                "Digest method algorithm \"" + digestMethodAlgorithmName + "\" is not part of the digest algorithm whitelist";
        LOG.error(msg);
        throw new ProtocolEngineConfigurationException(EidasErrorKey.INVALID_HASH_ALGORITHM, msg);
    }

    protected void checkCertificateIssuer(X509Certificate certificate) throws EIDASSAMLEngineException {
        CertificateValidator.checkCertificateIssuer(signatureConfiguration.isDisallowedSelfSignedCertificate(), certificate);
    }

    protected void checkCertificateValidityPeriod(X509Certificate certificate) throws EIDASSAMLEngineException {
        CertificateValidator.checkCertificateValidityPeriod(signatureConfiguration.isCheckedValidityPeriod(), certificate);
    }

    protected Signature createSignature(SigningContext signingContext) throws EIDASSAMLEngineException {
        X509Credential credential = signingContext.getSigningCredential();
        checkCertificateValidityPeriod(credential.getEntityCertificate());
        checkCertificateIssuer(credential.getEntityCertificate());

        Signature signature;
        LOG.debug("Creating an OpenSAML signature object");

        signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signature.setSigningCredential(credential);

        signature.setSignatureAlgorithm(signingContext.getSigningAlgorithm());

        KeyInfo keyInfo = createKeyInfo(credential, signingContext.isSignWithKeyValue());

        signature.setKeyInfo(keyInfo);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        return signature;
    }

    /**
     * @return the signature algorithm to be used when signing
     */
    protected String getSignatureAlgorithm() {
        return signatureConfiguration.getSignatureAlgorithm();
    }

    protected ImmutableSet<String> getSignatureAlgorithmWhiteList() {
        return this.signatureConfiguration.getSignatureAlgorithmWhitelist();
    }

    /**
     * @return the digest method algorithm to be used when signing
     */
    protected String getDigestMethodAlgorithm() {
        return this.signatureConfiguration.getDigestAlgorithm();
    }

    /**
     * @return the digest method algorithm to be used when signing
     */
    protected ImmutableSet<String> getDigestMethodAlgorithmWhitelist() {
        return this.signatureConfiguration.getDigestMethodAlgorithmWhiteList();
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
                                                 @Nonnull List<? extends Credential> trustedCredentialList,
                                                 @Nonnull final CertificateVerifierParams verifierParams)
            throws EIDASSAMLEngineException {
        X509Credential entityX509Cred;
        if(CertificateUtil.isSignatureWithCertificate(signature)){
            X509Certificate cert = getSignatureCertificate(signature);
            // Exist only one certificate
            entityX509Cred = CertificateUtil.toCredential(cert);

            addAllSignatureCertificatesToCredential(signature, entityX509Cred);

            checkCertificateValidityPeriod(cert);
            checkCertificateIssuer(cert);
            LOG.info("isSignatureWithCertificate = " + entityX509Cred.getEntityId());
        }else{
            try {
	        	RSAKeyValue signatureRsaKeyValue = signature.getKeyInfo().getKeyValues().get(0).getRSAKeyValue();
				entityX509Cred = getTrustedCertificateFromRSAKeyValue(signatureRsaKeyValue, trustedCredentialList);
                LOG.info("isSignatureWithOutCertificate = " + entityX509Cred.getEntityId());
			} catch (IndexOutOfBoundsException e) {
				throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                        "Failed to extract Certificate from XML" , e);
			}
        }

        checkValidTrust(trustedCredentialList, entityX509Cred, verifierParams);

        return entityX509Cred;
    }

    private void addAllSignatureCertificatesToCredential(@Nonnull Signature signature, X509Credential entityX509Cred) throws EIDASSAMLEngineException {
        if (entityX509Cred instanceof BasicX509Credential) {
            List<X509Certificate> signatureCertificates;
            try {
                signatureCertificates = getAllSignatureCertificates(signature);
            } catch (CertificateException e) {
                throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE,
                        "Failed to extract X509 from XML", e);
            }

            ((BasicX509Credential) entityX509Cred).setEntityCertificateChain(signatureCertificates);
        }
    }

    /**
     * Checks the validity of trust related to certificate(s) present in the signature.
     *
     * If the signature has only one certificate, explicit certificate validation is performed.
     * Otherwise, validation of the certification path is performed.
     *
     * @param trustedCredentialList that contains the trusted credentials
     * @param entityX509Cred the credential related to the key that signs
     * @throws EIDASSAMLEngineException if the either the explicit or the certification path validation fails.
     */
    private void checkValidTrust(@Nonnull List<? extends Credential> trustedCredentialList,
                                 X509Credential entityX509Cred,
                                 @Nonnull final CertificateVerifierParams verifierParams) throws EIDASSAMLEngineException {
        try {
            CertificateUtil.checkChainTrust(entityX509Cred, trustedCredentialList, verifierParams);
        } catch (final CertificateException e) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR, "Invalid chain of trust", e);
        }
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
        if (signableObject instanceof AuthnRequest) {
            return sign(signableObject, requestSigningContext);
        } else {
            return sign(signableObject, responseSigningContext);
        }
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
    public <T extends SignableXMLObject> T sign(@Nonnull T signableObject, SigningContext signingContext) throws EIDASSAMLEngineException {
        LOG.trace("Start Sign process.");
        try {
            Signature signature = createSignature(signingContext);
            signableObject.setSignature(signature);

            // Reference/DigestMethod algorithm is set by default to SHA-1 in OpenSAML
            String digestAlgorithm = validateDigestAlgorithm(getDigestMethodAlgorithm());
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
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR, "ERROR : MarshallingException: ", e);
        } catch (SignatureException e) {
            LOG.error("ERROR : Signature exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR, "ERROR : Signature exception: ",e);
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
        if (null == metadataSigningContext || null == metadataSigningContext.getSigningCredential()) {
            throw new EIDASMetadataException("No metadataSigningCredential configured");
        }
        try {
            return sign(signableObject, metadataSigningContext);
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
            return validateSignature(signedMetadata, null, this.metadataCertificateVerifierParams);
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
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_INVALID_METADATA,
                    "ERROR : ValidationException: signature isn't conform to SAML Signature profile: ", e);
        }
        return sigProfValidator;
    }

    @Override
    @Nonnull
    public <T extends SignableXMLObject> T validateSignature(@Nonnull T signedObject,
                                                             @Nullable Collection<X509Certificate> trustedCertificateCollection)
            throws EIDASSAMLEngineException {
        return validateSignature(signedObject, trustedCertificateCollection, DISABLE_REVOCATION_CHECK);

    }
    @Nonnull
    public <T extends SignableXMLObject> T validateSignature(@Nonnull T signedObject,
                                                             @Nullable Collection<X509Certificate> trustedCertificateCollection,
                                                             @Nonnull CertificateVerifierParams verifierParams)
            throws EIDASSAMLEngineException {
        List<? extends Credential> trustedCreds;

        // 2) Verify the cryptographic signature:
        if (CollectionUtils.isEmpty(trustedCertificateCollection)) {
            trustedCreds = getTrustedCredentials();
        } else {
            trustedCreds = CertificateUtil.getListOfCredential(trustedCertificateCollection);
        }
        return validateSignatureWithCredentials(signedObject, trustedCreds, verifierParams);
    }

    @Override
    @Nonnull
    public Collection<String> getTrustedCredentialGraphIdentifiers() {

        Base64.Encoder encoder = Base64.getEncoder();

        return this.getTrustedCredentials().stream()
                .filter(Objects::nonNull)
                .map(X509Credential::getEntityCertificate)
                .map(this::getCertificateEncodedForm)
                .map(DigestUtils::sha256)
                .map(encoder::encodeToString)
                .collect(Collectors.toList());
    }

    @Nonnull
    private <T extends SignableXMLObject> T validateSignatureWithCredentials(@Nonnull T signedObject,
                                                                             @Nonnull List<? extends Credential> trustedCredentialList,
                                                                             @Nonnull CertificateVerifierParams verifierParams)
            throws EIDASSAMLEngineException {
        LOG.debug("Start signature validation.");
        // 1) Validate the structure of the SAML signature:
        validateSamlSignatureStructure(signedObject);

        // 2) Verify the cryptographic signature:
        verifyCryptographicSignature(signedObject.getSignature(), trustedCredentialList, verifierParams);

        return signedObject;
    }

    private void verifyCryptographicSignature(@Nonnull Signature signature,
                                              @Nonnull List<? extends Credential> trustedCredentialList,
                                              @Nonnull CertificateVerifierParams verifierParams)
            throws EIDASSAMLEngineException {
        // 1) check that we accept the signature algorithm
        validateSignatureAlgorithm(signature);
        validateDigestAlgorithm(signature);

        // 2) check that we trust the signing certificate
        X509Credential entityX509Cred = getTrustedCertificate(signature, trustedCredentialList, verifierParams);
        validateCertificatesSignature(entityX509Cred.getEntityCertificateChain());

        // 3) verify the XML Digital Signature itself (XML-DSig)
        // DOM information related to the signature should be still available at this point
        try {
            SignatureValidator.validate(signature, entityX509Cred);
        } catch (SignatureException e) {
            LOG.error("ERROR : Signature Validation Exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM, "ERROR : Signature Validation Exception: ",e);
        }
    }

    private void validateSignatureAlgorithm(Signature signature) throws EIDASSAMLEngineException {
        String signatureAlgorithmVal = signature.getSignatureAlgorithm();
        LOG.trace("Key algorithm {}", signatureAlgorithmVal);
        if (!isAlgorithmAllowedForVerifying(signatureAlgorithmVal)) {
            LOG.error("ERROR : the algorithm {} used by the signature is not allowed", signatureAlgorithmVal);
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM,
                    "ERROR : the algorithm "+ signatureAlgorithmVal +" used by the signature is not allowed");
        }
    }

    private void validateDigestAlgorithm(Signature signature) throws EIDASSAMLEngineException {
        String digestAlgorithmVal = SAMLEngineUtils.getDigestMethodAlgorithm(signature);
        if (!getDigestMethodAlgorithmWhitelist().contains(digestAlgorithmVal)) {
            LOG.error("ERROR : the digest algorithm {} used by the signature is not allowed", digestAlgorithmVal);
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM,
                    "ERROR : the digest algorithm " + digestAlgorithmVal + " used by the signature is not allowed");
        }
    }

    private void validateCertificatesSignature(Collection<X509Certificate> certificateChain)
            throws EIDASSAMLEngineException {
        for (X509Certificate certificate : certificateChain) {
            validateCertificateSignatureHash(certificate);
            validateKeyLength(certificate.getPublicKey());
        }
    }

    private void validateCertificateSignatureHash(X509Certificate certificate) throws EIDASSAMLEngineException {
        int certificateHashLength = 0;
        String certificateHashAlgorithm = null;
        String certificateSigAlgName = certificate.getSigAlgName();
        try {
            if (certificateSigAlgName != null && !certificateSigAlgName.toUpperCase().contains("WITH")) {
                AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(certificateSigAlgName);
                algorithmParameters.init(certificate.getSigAlgParams());
                PSSParameterSpec pssParams = algorithmParameters.getParameterSpec(PSSParameterSpec.class);
                certificateHashAlgorithm = pssParams.getDigestAlgorithm();
            } else {
                certificateHashAlgorithm = getDigestAlgFromCertificateSigningAlg(certificateSigAlgName);
            }
            certificateHashLength = getHashAlgorithmBitsLength(certificateHashAlgorithm);
        } catch (NoSuchAlgorithmException | IOException | InvalidParameterSpecException e) {
            LOG.error("ERROR : Unknown hash algorithm {} for Signature algorithm {}", certificateHashAlgorithm, certificateSigAlgName);
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_HASH_ALGORITHM,
                    "ERROR : Unknown hash algorithm " + certificateHashAlgorithm +
                            " for Signature algorithm" + certificateSigAlgName);
        }
        if (isInvalidHashLength(certificateHashLength)) {
            LOG.error("ERROR : The hash length {} bits (< " + MIN_SIGNATURE_HASH_LENGTH
                    + ") used to sign the certificate is not allowed", certificateHashLength);
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_MINIMUM_SIGNATURE_HASH_LENGTH,
                    "ERROR : The hash length " + certificateHashLength + " bits (< " + MIN_SIGNATURE_HASH_LENGTH
                            + ") used to sign the certificate is not allowed");
        }
    }

    private boolean isInvalidHashLength(int hashLength) {
        return hashLength < MIN_SIGNATURE_HASH_LENGTH;
    }

    /**
     * Method inspired from sun.security.x509.AlgorithmId#getDigAlgFromSigAlg to retrieve the digest algorithm
     * from the certificate signing algorithm.
     * @param certificateSigningAlgorithm the certificate signing algorithm
     * @return the digest algorithm.
     */
    private String getDigestAlgFromCertificateSigningAlg(String certificateSigningAlgorithm) {
        String signAlgorithmUpperCase = certificateSigningAlgorithm.toUpperCase(Locale.ENGLISH);
        int separationPosition = signAlgorithmUpperCase.indexOf("WITH");
        String digestAlg = separationPosition > 0 ? certificateSigningAlgorithm.substring(0, separationPosition) : null;
        if (digestAlg != null && digestAlg.startsWith("SHA") && !digestAlg.contains("-")) {
            return digestAlg.replace("SHA", "SHA-");
        }
        return digestAlg;
    }

    private int getHashAlgorithmBitsLength(String hashAlgorithmName) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(hashAlgorithmName);
        int digestLengthBytes = md.getDigestLength();
        // transpose from byte to bits length
        return digestLengthBytes * 8;
    }

    private void validateKeyLength(Key key) throws EIDASSAMLEngineException {
        Integer keyLength = KeySupport.getKeyLength(key);
        String keyAlgorithm = key.getAlgorithm();
        if (!isKeyLengthValid(keyLength, keyAlgorithm)) {
            LOG.error("ERROR : Invalid key length {} for key algorithm {}", keyLength, keyAlgorithm);
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM,
                    "ERROR : Invalid key length " + keyLength + " for key algorithm " + keyAlgorithm);
        }
    }

    private boolean isKeyLengthValid(Integer keyLength, String keyAlgorithm) {
        if (JCAConstants.KEY_ALGO_RSA.equalsIgnoreCase(keyAlgorithm)) {
            return keyLength >= MIN_RSA_KEY_LENGTH;
        } else if (JCAConstants.KEY_ALGO_EC.equalsIgnoreCase(keyAlgorithm)) {
            return keyLength >= MIN_EC_KEY_LENGTH;
        } else {
            // Even though other algorithms should not be accepted regarding the specs
            // if the config has been overridden to accept others algorithms, we don't
            // want to block it.
            return true;
        }
    }

    @Override
    public SignatureConfiguration getSignatureConfiguration() {
        return this.signatureConfiguration;
    }

    @Override
    public boolean isResponseSignAssertions() {
        return signatureConfiguration.isResponseSignAssertions();
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
		    if (null != credential.getPublicKey()
                && JCAConstants.KEY_ALGO_RSA.equalsIgnoreCase(credential.getPublicKey().getAlgorithm())) {
                KeyInfoGeneratorFactory keyInfoGenFac = createKeyInfoGeneratorFactory((X509Credential) credential);
                KeyInfo keyInfo = createKeyInfo(keyInfoGenFac, (X509Credential) credential);

                RSAKeyValue trustedCredentialRsaKeyValue = keyInfo.getKeyValues().get(0).getRSAKeyValue();
                BigInteger trustedCredentialExponent = trustedCredentialRsaKeyValue.getExponent().getValueBigInt();
                BigInteger trustedCredentialModulus = trustedCredentialRsaKeyValue.getModulus().getValueBigInt();

                if (signatureExponent.equals(trustedCredentialExponent)
                        && signatureModulus.equals(trustedCredentialModulus)) {
                    return (X509Credential) credential;
                }
            }
		}
		throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNTRUSTED_CERTIFICATE,
                "Unable to find certificate matching private key");
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
			throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNTRUSTED_CERTIFICATE,
                    "Failed to generate KeyInfo",e);
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

    @Nonnull
    private byte[] getCertificateEncodedForm(@Nonnull X509Certificate certificate) {
        try {
            return certificate.getEncoded();
        } catch (final CertificateEncodingException e) {
            throw new IllegalStateException("Certificate does not have a representation");
        }
    }
}
