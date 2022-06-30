package eu.eidas.sp;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.opensaml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.impl.BinaryRequestMessage;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessor;
import eu.eidas.auth.engine.xml.opensaml.CorrelatedResponse;
import eu.eidas.auth.engine.xml.opensaml.XmlSchemaUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * SpProtocolEngine
 *
 * @since 1.1
 */
public final class SpProtocolEngine extends ProtocolEngine implements SpProtocolEngineI {

    private static final Logger LOG = LoggerFactory.getLogger(SpProtocolEngine.class);

    public SpProtocolEngine(@Nonnull ProtocolConfigurationAccessor configurationAccessor) {
        super(configurationAccessor);
    }

    /**
     * Decrypt and validate saml respons
     *
     * @param responseBytes
     * @return
     * @throws EIDASSAMLEngineException
     */
    @Override
    @Nonnull
    public byte[] checkAndDecryptResponse(@Nonnull byte[] responseBytes) throws EIDASSAMLEngineException {
        // This decrypts the given responseBytes:
        CorrelatedResponse response = (CorrelatedResponse) unmarshallResponse(responseBytes,null,false);

        // validateUnmarshalledResponse(samlResponse, userIP, skewTimeInMillis);

        try {
            // re-transform the decrypted bytes to another byte array, without signing:
            return marshall(response.getResponse());
        } catch (EIDASSAMLEngineException e) {
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : checkAndResignEIDASTokenSAML : Sign and Marshall.", e);
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : checkAndResignEIDASTokenSAML : Sign and Marshall.",
                     e.getMessage());
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
    }

    /**
     * Resign a request (for validation purpose).
     *
     * @return the resigned request
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     * @deprecated this method is only used by the validator
     */
    @Override
    @Nonnull
    public byte[] reSignRequest(@Nonnull byte[] requestBytes) throws EIDASSAMLEngineException {
        LOG.trace("Generate SAMLAuthnRequest.");

        AuthnRequest authnRequest = unmarshallRequest(requestBytes,null,false);
        releaseExtensionsDom(authnRequest);
        if (null == authnRequest) {
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorCode(), "invalid AuthnRequest");
        }

        try {
            return signAndMarshallRequest(authnRequest);
        } catch (EIDASSAMLEngineException e) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : resignEIDASTokenSAML : Sign and Marshall.", e);
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : resignEIDASTokenSAML : Sign and Marshall.", e.getMessage());
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
    }

    private void releaseExtensionsDom(AuthnRequest authnRequestAux) {
        if (authnRequestAux.getExtensions() == null) {
            return;
        }
        authnRequestAux.getExtensions().releaseDOM();
        authnRequestAux.getExtensions().releaseChildrenDOM(true);
    }

    /**
     * Unmarshalls the given bytes into a SAML Request.
     *
     * @param tokenSaml the SAML request bytes
     * @return the SAML request instance
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Override
    public AuthnRequest unmarshallRequest(@Nonnull byte[] requestBytes,
    		Collection<String> whitelistMetadataURLs, boolean checkWhitelist) throws EIDASSAMLEngineException {
        LOG.trace("Validate request bytes.");

        if (null == requestBytes) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : Saml request bytes are null.");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                    "Saml request bytes are null.");
        }

        Document document = XmlSchemaUtil.validateSamlSchema(requestBytes);
        AuthnRequest request = (AuthnRequest) unmarshall(document);

        return request;
    }


    /**
     * Resign authentication request ( for validation purpose).
     *
     * @param originalRequest
     * @param changeProtocol If true will update the protocol of the resigned request with the one within {@code
     * request}
     * @param changeDestination If true will update the destination of the resigned request with the one within {@code
     * request}
     * @return the resigned request
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     * @// TODO: 13/05/2016 move this to a specific extend of the SAMLengine dedicated to validator
     */
    @Nonnull
    @Override
    public IRequestMessage resignEIDASAuthnRequest(@Nonnull IRequestMessage originalRequest, boolean changeDestination)
            throws EIDASSAMLEngineException {
        LOG.trace("Getting the saml token.");
        AuthnRequest authnRequestAux = null;
        // Obtaining new saml Token
        byte[] tokenSaml = originalRequest.getMessageBytes();
        IAuthenticationRequest authenticationRequest = originalRequest.getRequest();
        authnRequestAux = unmarshallRequest(tokenSaml,null,false);
        authnRequestAux.setProtocolBinding(
                getProtocolProcessor().getProtocolBinding(authenticationRequest, getCoreProperties()));
        if (changeDestination) {
            authnRequestAux.setDestination(authenticationRequest.getDestination());
        }

        // copy constructor && assignment
        LOG.trace("copy contructor and assigment of token.");
        try {
            IRequestMessage resignedAuthnRequest =
                    new BinaryRequestMessage(authenticationRequest, signAndMarshallRequest(authnRequestAux));
            return resignedAuthnRequest;
        } catch (EIDASSAMLEngineException e) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : resignEIDASAuthnRequest : Sign and Marshall.{}",
                     e.getMessage());
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : resignEIDASAuthnRequest : Sign and Marshall.{}", e);
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
    }
}
