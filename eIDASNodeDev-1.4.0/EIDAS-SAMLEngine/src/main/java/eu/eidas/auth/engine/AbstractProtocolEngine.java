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

import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang.StringUtils;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.impl.XSAnyBuilder;
import org.opensaml.xml.schema.impl.XSAnyMarshaller;
import org.opensaml.xml.schema.impl.XSAnyUnmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.w3c.dom.Document;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.core.ProtocolCipherI;
import eu.eidas.auth.engine.core.ProtocolDecrypterI;
import eu.eidas.auth.engine.core.ProtocolEncrypterI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.xml.opensaml.SAMLBootstrap;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.util.Preconditions;

/**
 * Base class for the ProtocolEngine.
 *
 * @since 1.1
 */
public abstract class AbstractProtocolEngine {

    /*Dedicated marker for the SAML exchanges*/
    public static final Marker SAML_EXCHANGE = MarkerFactory.getMarker("SAML_EXCHANGE");

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProtocolEngine.class);

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

    @Nonnull
    private final ProtocolConfigurationAccessor configurationAccessor;

    protected AbstractProtocolEngine(@Nonnull ProtocolConfigurationAccessor configAccessor) {
        Preconditions.checkNotNull(configAccessor, "configurationAccessor");
        configurationAccessor = configAccessor;
    }

    /**
     * Checks whether the unencrypted responses are allowed to be received
     *
     * @throws EIDASSAMLEngineException
     */
    private void checkReceivingUnencryptedResponsesAllowed() throws EIDASSAMLEngineException {
        ProtocolDecrypterI protocolDecrypter = getProtocolDecrypter();
        if (null != protocolDecrypter) {
            if (protocolDecrypter.isResponseEncryptionMandatory()) {
                throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_DECRYPTING_RESPONSE.errorCode(),
                                                   EidasErrorKey.SAML_ENGINE_DECRYPTING_RESPONSE.errorMessage());
            }
        }
    }

    /**
     * Checks whether the unencrypted responses are allowed to be sent
     *
     * @throws EIDASSAMLEngineException
     */
    private void checkSendingUnencryptedResponsesAllowed() throws EIDASSAMLEngineException {
        ProtocolEncrypterI encrypter = getProtocolEncrypter();
        if (null != encrypter) {
            if (encrypter.isResponseEncryptionMandatory()) {
                throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorCode(),
                                                   EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorMessage());
            }
        }
    }

    @Nullable
    private ProtocolEncrypterI getProtocolEncrypter() {
        ProtocolCipherI cipher = getConfiguration().getCipher();
        if (cipher instanceof ProtocolEncrypterI) {
            return (ProtocolEncrypterI) cipher;
        }
        return null;
    }

    @Nullable
    private ProtocolDecrypterI getProtocolDecrypter() {
        ProtocolCipherI cipher = getConfiguration().getCipher();
        if (cipher instanceof ProtocolDecrypterI) {
            return (ProtocolDecrypterI) cipher;
        }
        return null;
    }

    @Nonnull
    @SuppressWarnings("squid:S2583")
    protected ProtocolEngineConfiguration getConfiguration() {
        try {
            ProtocolEngineConfiguration protocolEngineConfiguration = configurationAccessor.get();
            if (null == protocolEngineConfiguration) {
                throw new EIDASSAMLEngineRuntimeException("ProtocolEngine Configuration cannot be obtained");
            }
            return protocolEngineConfiguration;
        } catch (SamlEngineConfigurationException e) {
            throw new EIDASSAMLEngineRuntimeException(e);
        }
    }

    /**
     * Gets the Encrypter.
     */
    public ProtocolCipherI getCipher() {
        return getConfiguration().getCipher();
    }

    public X509Certificate getDecryptionCertificate() throws EIDASSAMLEngineException {
        return null == getProtocolDecrypter() ? null : getProtocolDecrypter().getDecryptionCertificate();
    }

    /**
     * Gets the SAML core properties.
     *
     * @return the SAML core properties
     */
    public final SamlEngineCoreProperties getCoreProperties() {
        return getConfiguration().getCoreProperties();
    }

    /**
     * Gets the Signer properties.
     *
     * @return the SAML Sign properties
     */
    @Nonnull
    public ProtocolSignerI getSigner() {
        return getConfiguration().getSigner();
    }

    @Nonnull
    public String getInstanceName() {
        return getConfiguration().getInstanceName();
    }

    @Nonnull
    public ProtocolProcessorI getProtocolProcessor() {
        return getConfiguration().getProtocolProcessor();
    }

    @Nonnull
    public SamlEngineClock getClock() {
        return getConfiguration().getClock();
    }

    @Nonnull
    public X509Certificate getSigningCertificate() throws EIDASSAMLEngineException {
        return getSigner().getPublicSigningCredential().getEntityCertificate();
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

    /**
     * Method that signs a SAML Token.
     *
     * @return the SAML object sign
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    @Nonnull
    protected Assertion signAssertion(@Nonnull Assertion assertion) throws EIDASSAMLEngineException {
        LOG.debug("Sign SAML Assertion: {}", assertion.getID());
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
        if (null != getProtocolEncrypter() && !SAMLEngineUtils.isErrorSamlResponse(responseToSign)) {

            X509Certificate destinationCertificate =
                    getEncryptionCertificate(request.getIssuer(), request.getOriginCountryCode());

            if (null != destinationCertificate) {
                LOG.debug("Encryption Executing...");
                responseToSign = getProtocolEncrypter().encryptSamlResponse(responseToSign, destinationCertificate);
                LOG.debug("Encryption finished: " + responseToSign);
            } else if (getProtocolEncrypter().isEncryptionEnabled(request.getOriginCountryCode())) {
                LOG.error(SAML_EXCHANGE, "BUSINESS EXCEPTION : encryption cannot be performed, no matching certificate for issuer="+ request.getIssuer()
                        + " and country=" + request.getOriginCountryCode());
                throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE.errorCode(),
                        EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE.errorMessage());

            }
        } else if (!SAMLEngineUtils.isErrorSamlResponse(responseToSign)) {
            checkSendingUnencryptedResponsesAllowed();
        }
        // SIGN
        LOG.debug("Signing SAML Response.");
        return getSigner().sign(responseToSign);
    }

    @Nullable
    protected X509Certificate getEncryptionCertificate(@Nonnull String requestIssuer,
                                                       @Nonnull String destinationCountryCode)
            throws EIDASSAMLEngineException {

        if (StringUtils.isNotBlank(destinationCountryCode) && null != getProtocolEncrypter()
                && getProtocolEncrypter().isEncryptionEnabled(destinationCountryCode)) {
            X509Certificate encryptionCertificate = getProtocolProcessor().getEncryptionCertificate(requestIssuer);
            if (null == encryptionCertificate) {
                return getProtocolEncrypter().getEncryptionCertificate(destinationCountryCode);
            }
            return encryptionCertificate;
        }
        return null;
    }

    /**
     * Method that unmarshalls a SAML Object from a dom document representation to an XML Object.
     *
     * @param document DOM Document representation of a SAML Object
     * @return XML Object (superclass of SAMLObject)
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    protected XMLObject unmarshall(@Nonnull Document document) throws EIDASSAMLEngineException {
        try {
            return OpenSamlHelper.unmarshallFromDom(document);
        } catch (UnmarshallException e) {
            LOG.error(SAML_EXCHANGE, "BUSINESS EXCEPTION : SAMLEngineException unmarshall.", e.getMessage(), e);
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
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
                getProtocolProcessor().getResponseSignatureCertificate(validResponse.getIssuer().getValue());
        getSigner().validateSignature(validResponse,
                                      null == signatureCertificate ? null : ImmutableSet.of(signatureCertificate));

        if (null != getProtocolDecrypter()) {
            if (!validResponse.getEncryptedAssertions().isEmpty()) {
                // DECRYPT THE SAMLObject AFTER VALIDATION
                LOG.debug("Decryption Executing...");
                validResponse = getProtocolDecrypter().decryptSamlResponse(validResponse);
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Decryption finished: " + EidasStringUtil.toString(marshall(validResponse)));
                } else {
                    LOG.debug("Decryption finished.");
                }
            } else if (StatusCode.SUCCESS_URI.equals(validResponse.getStatus().getStatusCode().getValue())) {
                checkReceivingUnencryptedResponsesAllowed();
            }
        }
        return validResponse;
    }
}
