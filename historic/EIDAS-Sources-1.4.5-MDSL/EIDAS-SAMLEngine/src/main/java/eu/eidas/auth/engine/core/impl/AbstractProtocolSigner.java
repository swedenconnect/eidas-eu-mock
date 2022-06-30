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

package eu.eidas.auth.engine.core.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.SignatureConfiguration;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.Configuration;
import org.opensaml.common.impl.SAMLObjectContentReference;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.BasicSecurityConfiguration;
import org.opensaml.xml.security.SecurityConfiguration;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorManager;
import org.opensaml.xml.security.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.*;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.KeyStore;
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

    private final boolean responseSignAssertions;

    private final ImmutableSet<String> signatureAlgorithmWhiteList;

    private final X509Credential privateSigningCredential;

    private final X509Credential publicSigningCredential;

    private final X509Credential privateMetadataSigningCredential;

    private final X509Credential publicMetadataSigningCredential;

    private final ImmutableList<X509Credential> trustedCredentials;

    private final String signatureAlgorithm;

    protected AbstractProtocolSigner(@Nonnull SignatureConfiguration signatureConfiguration)
            throws SamlEngineConfigurationException {
        this(signatureConfiguration.isCheckedValidityPeriod(),
                signatureConfiguration.isDisallowedSelfSignedCertificate(),
                signatureConfiguration.isResponseSignAssertions(),
                signatureConfiguration.getSignatureKeyAndCertificate(), signatureConfiguration.getTrustedCertificates(),
                signatureConfiguration.getSignatureAlgorithm(), signatureConfiguration.getSignatureAlgorithmWhiteList(),
                signatureConfiguration.getMetadataSigningKeyAndCertificate());
    }

    protected AbstractProtocolSigner(boolean checkedValidityPeriod,
                                     boolean disallowedSelfSignedCertificate,
                                     boolean responseSignAssertions,
                                     @Nonnull KeyStore.PrivateKeyEntry signatureKeyAndCertificate,
                                     @Nonnull ImmutableSet<X509Certificate> trustedCertificates,
                                     @Nullable String signatureAlgorithmVal,
                                     @Nullable String signatureAlgorithmWhiteListStr,
                                     @Nullable KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate)
            throws SamlEngineConfigurationException {
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
        // setDigestMethodAlgorithm(signatureAlgorithm);
    }

    private static X509Certificate getSignatureCertificate(Signature signature) throws EIDASSAMLEngineException {
        KeyInfo keyInfo = signature.getKeyInfo();
        return CertificateUtil.toCertificate(keyInfo);
    }

    /**
     * @deprecated this is a global static OpenSAML configuration, it should not be invoked!
     */
    @Deprecated
    protected static void setDigestMethodAlgorithm(@Nonnull String signatureAlgorithm)
            throws SamlEngineConfigurationException {
        BasicSecurityConfiguration config = (BasicSecurityConfiguration) Configuration.getGlobalSecurityConfiguration();
        if (config != null && isNotBlank(signatureAlgorithm)) {
            String digestAlgorithm = validateDigestAlgorithm(signatureAlgorithm);
            config.setSignatureReferenceDigestMethod(digestAlgorithm);
        } else {
            LOG.error("Configuration error - Unable to set DigestMethodAlgorithm - config {} algorithm {} not set",
                      config, signatureAlgorithm);
        }
    }

    /**
     * Validates whether the given signature algorithm name contains an allowed digest algorithm.
     *
     * @param signatureAlgorithmName the signature algorithm name as defined in the XML Digital Signature standards.
     * @return the canonical algorithm name of the corresponding digest algorithm if allowed, otherwise throws an
     * exception
     * @throws SamlEngineConfigurationException if the given algorithm is not allowed
     */
    public static String validateDigestAlgorithm(String signatureAlgorithmName)
            throws SamlEngineConfigurationException {
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
        throw new SamlEngineConfigurationException(msg);
    }

    /**
     * Validates whether the given signature algorithm name is allowed.
     *
     * @param signatureAlgorithmName the signature algorithm name as defined in the XML Digital Signature standards.
     * @return the canonical algorithm name if allowed, otherwise throws an exception
     * @throws SamlEngineConfigurationException if the given algorithm is not allowed
     */
    public static String validateSigningAlgorithm(@Nullable String signatureAlgorithmName)
            throws SamlEngineConfigurationException {
        if (signatureAlgorithmName == null || StringUtils.isBlank(signatureAlgorithmName)) {
            return DEFAULT_SIGNATURE_ALGORITHM;
        }
        String canonicalAlgorithm = signatureAlgorithmName.trim();
        if (ALLOWED_ALGORITHMS_FOR_SIGNING.contains(canonicalAlgorithm)) {
            return canonicalAlgorithm;
        }
        String msg = "Signing algorithm \"" + signatureAlgorithmName + "\" is not allowed";
        LOG.error(msg);
        throw new SamlEngineConfigurationException(msg);
    }

    protected void checkCertificateIssuer(X509Certificate certificate) throws EIDASSAMLEngineException {
        CertificateValidator.checkCertificateIssuer(disallowedSelfSignedCertificate, certificate);
    }

    protected void checkCertificateValidityPeriod(X509Certificate certificate) throws EIDASSAMLEngineException {
        CertificateValidator.checkCertificateValidityPeriod(checkedValidityPeriod, certificate);
    }

    protected Signature createSignature(@Nonnull X509Credential credential) throws EIDASSAMLEngineException {
        checkCertificateValidityPeriod(credential.getEntityCertificate());
        checkCertificateIssuer(credential.getEntityCertificate());

        Signature signature;
        try {
            LOG.debug("Creating an OpenSAML signature object");

            signature = (Signature) Configuration.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                    .buildObject(Signature.DEFAULT_ELEMENT_NAME);

            signature.setSigningCredential(credential);

            signature.setSignatureAlgorithm(getSignatureAlgorithm());

            SecurityConfiguration secConfiguration = Configuration.getGlobalSecurityConfiguration();
            NamedKeyInfoGeneratorManager keyInfoManager = secConfiguration.getKeyInfoGeneratorManager();
            KeyInfoGeneratorManager keyInfoGenManager = keyInfoManager.getDefaultManager();
            KeyInfoGeneratorFactory keyInfoGenFac = keyInfoGenManager.getFactory(credential);
            KeyInfoGenerator keyInfoGenerator = keyInfoGenFac.newInstance();

            KeyInfo keyInfo = keyInfoGenerator.generate(credential);

            signature.setKeyInfo(keyInfo);
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        } catch (org.opensaml.xml.security.SecurityException e) {
            LOG.error("ERROR : Security exception: " + e, e);
            throw new EIDASSAMLEngineException(e);
        }
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
        X509Certificate cert = getSignatureCertificate(signature);
        // Exist only one certificate
        X509Credential entityX509Cred = CertificateUtil.toCredential(cert);

        CertificateUtil.checkTrust(entityX509Cred, trustedCredentialList);
        checkCertificateValidityPeriod(cert);
        checkCertificateIssuer(cert);
        return entityX509Cred;
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
        return sign(signableObject, privateSigningCredential);
    }

    @Nonnull
    protected <T extends SignableXMLObject> T sign(@Nonnull T signableObject, @Nonnull X509Credential signingCredential)
            throws EIDASSAMLEngineException {
        LOG.trace("Start Sign process.");
        try {
            Signature signature = createSignature(signingCredential);
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
            Configuration.getMarshallerFactory().getMarshaller(signableObject).marshall(signableObject);

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
     * @throws EIDASSAMLEngineException in case of signature errors
     */
    @Override
    @Nonnull
    public <T extends SignableXMLObject> T signMetadata(@Nonnull T signableObject) throws EIDASSAMLEngineException {
        if (null == privateMetadataSigningCredential) {
            throw new SamlEngineConfigurationException("No metadataSigningCredential configured");
        }
        return sign(signableObject, privateMetadataSigningCredential);
    }

    @Nonnull
    @Override
    public <T extends SignableXMLObject> T validateMetadataSignature(@Nonnull T signedMetadata)
            throws EIDASSAMLEngineException {
        return validateSignature(signedMetadata, null);
    }

    private SAMLSignatureProfileValidator validateSamlSignatureStructure(SignableXMLObject signableObject)
            throws EIDASSAMLEngineException {
        SAMLSignatureProfileValidator sigProfValidator = new SAMLSignatureProfileValidator();
        try {
            // Indicates signature id conform to SAML Signature profile
            sigProfValidator.validate(signableObject.getSignature());
        } catch (ValidationException e) {
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
        LOG.trace("Key algorithm {}", SecurityHelper.getKeyAlgorithmFromURI(signatureAlgorithmVal));
        if (!isAlgorithmAllowedForVerifying(signatureAlgorithmVal)) {
            LOG.error("ERROR : the algorithm {} used by the signature is not allowed", signatureAlgorithmVal);
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM.errorCode());
        }

        // 2) check that we trust the signing certificate
        X509Credential entityX509Cred = getTrustedCertificate(signature, trustedCredentialList);

        // 3) verify the XML Digital Signature itself (XML-DSig)
        // DOM information related to the signature should be still available at this point
        SignatureValidator sigValidator = new SignatureValidator(entityX509Cred);
        try {
            sigValidator.validate(signature);
        } catch (ValidationException e) {
            LOG.error("ERROR : Signature Validation Exception: " + e, e);
            throw new EIDASSAMLEngineException(e);
        }
    }

    @Override
    public boolean isResponseSignAssertions() {
        return responseSignAssertions;
    }
}
