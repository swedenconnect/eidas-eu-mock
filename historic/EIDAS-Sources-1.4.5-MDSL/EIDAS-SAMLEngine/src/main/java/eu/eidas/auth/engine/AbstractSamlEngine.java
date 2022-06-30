/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.eidas.auth.engine;

import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;

import org.opensaml.Configuration;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SSODescriptor;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.impl.XSAnyBuilder;
import org.opensaml.xml.schema.impl.XSAnyMarshaller;
import org.opensaml.xml.schema.impl.XSAnyUnmarshaller;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.signature.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.configuration.ConfigurationAccessor;
import eu.eidas.auth.engine.configuration.SamlEngineConfiguration;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.core.ExtensionProcessorI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.core.SamlEngineEncryptionI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.xml.opensaml.SAMLBootstrap;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.util.Preconditions;

/**
 * Due to business constraints, this class is part of the contract with a DG Taxud project, so keep it as is.
 * <p>
 * Remove this class in 1.2.
 *
 * @deprecated since 1.1, use {@link AbstractProtocolEngine} instead.
 */
@Deprecated
@Beta
public abstract class AbstractSamlEngine {

    /*Dedicated marker for the SAML exchanges*/
    public static final Marker SAML_EXCHANGE = MarkerFactory.getMarker("SAML_EXCHANGE");

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSamlEngine.class);

    @Nonnull
    private final ConfigurationAccessor configurationAccessor;

    protected AbstractSamlEngine(@Nonnull ConfigurationAccessor configAccessor) {
        Preconditions.checkNotNull(configAccessor, "configurationAccessor");
        configurationAccessor = configAccessor;
    }

    static {
        try {
            SAMLBootstrap.bootstrap();

            Configuration.registerObjectProvider(XSAny.TYPE_NAME, new XSAnyBuilder(), new XSAnyMarshaller(),
                                                 new XSAnyUnmarshaller());
        } catch (ConfigurationException ce) {
            LOG.error("Problem initializing the OpenSAML library: " + ce, ce);
            throw new IllegalStateException(ce);
        }
    }

    public static ParserPool getSecuredParserPool() {
        return Configuration.getParserPool();
    }

    /**
     * check whether the unencrypted responses are allowed
     *
     * @throws EIDASSAMLEngineException
     */
    private void checkUnencryptedResponsesAllowed() throws EIDASSAMLEngineException {
        if (isResponseEncryptionMandatory()) {
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorCode(),
                                               EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorMessage());
        }
    }

    /**
     * Returns if the response should be decrypted
     */
    protected boolean decryptResponse() {
        return null != getCipher();
    }

    @Nonnull
    @SuppressWarnings("squid:S2583")
    protected SamlEngineConfiguration getConfiguration() {
        try {
            SamlEngineConfiguration samlEngineConfiguration = configurationAccessor.get();
            if (null == samlEngineConfiguration) {
                throw new EIDASSAMLEngineRuntimeException("ProtocolEngine Configuration cannot be obtained");
            }
            return samlEngineConfiguration;
        } catch (SamlEngineConfigurationException e) {
            throw new EIDASSAMLEngineRuntimeException(e);
        }
    }

    /**
     * Gets the Encrypter.
     */
    protected SamlEngineEncryptionI getCipher() {
        return getConfiguration().getCipher();
    }

    public X509Certificate getDecryptionCertificate() throws EIDASSAMLEngineException {
        return getCipher() == null ? null : getCipher().getDecryptionCertificate();
    }

    /**
     * Gets the SAML core properties.
     *
     * @return the SAML core properties
     */
    public final SamlEngineCoreProperties getCoreProperties() {
        return getConfiguration().getCoreProperties();
    }

    public Signature getSignature() throws EIDASSAMLEngineException {
        return null;
    }

    /**
     * Gets the Signer properties.
     *
     * @return the SAML Sign properties
     */
    protected ProtocolSignerI getSigner() {
        return getConfiguration().getSigner();
    }

    @Nonnull
    public String getInstanceName() {
        return getConfiguration().getInstanceName();
    }

    @Nonnull
    public ExtensionProcessorI getExtensionProcessor() {
        return getConfiguration().getExtensionProcessor();
    }

    @Nonnull
    public SamlEngineClock getClock() {
        return getConfiguration().getClock();
    }

    public Credential getSigningCredential() throws EIDASSAMLEngineException {
        return getSigner() == null ? null : getSigner().getPublicSigningCredential();
    }

    public boolean isResponseEncryptionMandatory() {
        return getConfiguration().isResponseEncryptionMandatory();
    }

    /**
     * Method that transform the received SAML object into a byte array representation.
     *
     * @param samlToken the SAML token.
     * @return the byte[] of the SAML token.
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    protected byte[] marshall(XMLObject samlToken) throws EIDASSAMLEngineException {
        try {
            return OpenSamlHelper.marshall(samlToken);
        } catch (MarshallException e) {
            LOG.error(e.getMessage(), e);
            throw new EIDASSAMLEngineException(e);
        }
    }

    protected final byte[] noSignAndMarshall(SignableSAMLObject samlToken) throws EIDASSAMLEngineException {
        LOG.debug("Marshall Saml Token.");
        return marshall(samlToken);
    }

    public byte[] signAndMarshallEntitiesDescriptor(EntitiesDescriptor descriptor) throws EIDASSAMLEngineException {
        ((MetadataSignerI)getSigner()).signMetadata(descriptor);
        return marshall(descriptor);
    }

    /**
     * Sign and transform to byte array.
     *
     * @return the byte[] of the SAML token
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    @Nonnull
    protected final byte[] signAndMarshallRequest(@Nonnull AuthnRequest request) throws EIDASSAMLEngineException {
        LOG.debug("Sign and marshall SAML Request.");
        AuthnRequest signedRequest = signRequest(request);
        return marshall(signedRequest);
    }

    /**
     * Sign and transform to byte array.
     *
     * @return the byte[] of the SAML token
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    @Nonnull
    protected final byte[] signAndMarshallResponse(@Nonnull IAuthenticationRequest request, @Nonnull Response response)
            throws EIDASSAMLEngineException {
        LOG.debug("Marshall Saml Token.");
        Response signedResponse = signResponse(request, response);
        return marshall(signedResponse);
    }

    public void signDescriptor(SSODescriptor descriptor) throws EIDASSAMLEngineException {
        getSigner().sign(descriptor);
    }

    public void signEntityDescriptor(EntityDescriptor descriptor) throws EIDASSAMLEngineException {
        ((MetadataSignerI)getSigner()).signMetadata(descriptor);
    }

    /**
     * Method that signs a SAML Token.
     *
     * @return the SAML object sign
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    @Nonnull
    protected Assertion signAssertion(@Nonnull Assertion assertion) throws EIDASSAMLEngineException {
        LOG.debug("Sign SAML Assertion.");
        return getSigner().sign(assertion);
    }

    /**
     * Method that signs a SAML Token.
     *
     * @return the SAML object sign
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    @Nonnull
    protected AuthnRequest signRequest(@Nonnull AuthnRequest request) throws EIDASSAMLEngineException {
        LOG.debug("Signing SAML Request.");
        return getSigner().sign(request);
    }

    /**
     * Method that signs a SAML Token.
     *
     * @return the SAML object sign
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    @Nonnull
    protected Response signResponse(@Nonnull IAuthenticationRequest request, @Nonnull Response response)
            throws EIDASSAMLEngineException {
        Response responseToSign = response;
        // Encrypt the SamlObject before signing it (encrypt-then-sign paradigm to prevent tampering)
        if (null != getCipher() && !SAMLEngineUtils.isErrorSamlResponse(responseToSign)) {

            X509Certificate destinationCertificate =
                    getEncryptionCertificate(request.getIssuer(), request.getOriginCountryCode());

            if (null != destinationCertificate) {
                LOG.debug("Encryption Executing...");
                responseToSign = getCipher().encryptSamlResponse(responseToSign, destinationCertificate);
                LOG.debug("Encryption finished: " + responseToSign);
            } else {
                LOG.debug("Encryption not performed, no matching certificate for issuer=" + request.getIssuer()
                                  + " and country=" + request.getOriginCountryCode());
            }
        } else if (!SAMLEngineUtils.isErrorSamlResponse(responseToSign)) {
            checkUnencryptedResponsesAllowed();
        }
        // SIGN
        LOG.debug("Signing SAML Response.");
        return getSigner().sign(responseToSign);
    }

    @Nullable
    protected X509Certificate getEncryptionCertificate(@Nullable String requestIssuer,
                                                       @Nullable String destinationCountryCode)
            throws EIDASSAMLEngineException {
        X509Certificate encryptionCertificate = getExtensionProcessor().getEncryptionCertificate(requestIssuer);
        if (null == encryptionCertificate && null != getCipher()) {
            return getCipher().getEncryptionCertificate(destinationCountryCode);
        }
        return encryptionCertificate;
    }

    @Nonnull
    protected Response signResponse(@Nullable X509Certificate destinationCertificate, @Nonnull Response response)
            throws EIDASSAMLEngineException {
        // ENCRYPT THE SAMLObject BEFORE SIGN
        Response responseVar = response;
        if (null != getCipher() && !SAMLEngineUtils.isErrorSamlResponse(responseVar) && null != destinationCertificate) {
            LOG.debug("Encryption Executing...");

            responseVar = getCipher().encryptSamlResponse(responseVar, destinationCertificate);

            LOG.debug("Encryption finished: " + responseVar);
        } else if (!SAMLEngineUtils.isErrorSamlResponse(responseVar)) {
            checkUnencryptedResponsesAllowed();
        }
        // SIGN
        LOG.debug("Signing SAML Response.");
        Response signedResponse = getSigner().sign(responseVar);
        return signedResponse;
    }

    /**
     * Method that unmarshalls a SAML Object from a byte array representation to an XML Object.
     *
     * @param samlToken Byte array representation of a SAML Object
     * @return XML Object (superclass of SAMLObject)
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    protected XMLObject unmarshall(byte[] samlToken) throws EIDASSAMLEngineException {
        try {
            return OpenSamlHelper.unmarshall(samlToken);
        } catch (UnmarshallException e) {
            LOG.error(SAML_EXCHANGE, "BUSINESS EXCEPTION : SAMLEngineException unmarshall.", e.getMessage(), e);
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
    }

    public void validateEntityDescriptorSignature(SignableXMLObject entityDescriptorSignature)
            throws EIDASSAMLEngineException {
        getSigner().validateSignature(entityDescriptorSignature, null);
    }

    /**
     * Method that validates an XML Signature contained in a SAML Token and decrypts it if it was encrypted.
     *
     * @param samlToken the SAML token
     * @return the SAML object
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    protected final Response validateSignatureAndDecrypt(Response response) throws EIDASSAMLEngineException {
        LOG.debug("Validate response Signature");
        Response validResponse = response;
        X509Certificate signatureCertificate =
                getExtensionProcessor().getResponseSignatureCertificate(validResponse.getIssuer().getValue());
        getSigner().validateSignature(validResponse,
                                      null == signatureCertificate ? null : ImmutableSet.of(signatureCertificate));

        if (this.decryptResponse() && !(validResponse).getEncryptedAssertions().isEmpty()) {
            // DECRYPT THE SAMLObject AFTER VALIDATION
            LOG.debug("Decryption Executing...");
            validResponse = getCipher().decryptSamlResponse(validResponse);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Decryption finished: " + EidasStringUtil.toString(marshall(validResponse)));
            } else {
                LOG.debug("Decryption finished.");
            }
        } else if (StatusCode.SUCCESS_URI.equals(validResponse.getStatus().getStatusCode().getValue())) {
            checkUnencryptedResponsesAllowed();
        }
        return validResponse;
    }
}
