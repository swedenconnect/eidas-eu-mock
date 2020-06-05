package eu.eidas.auth.engine;

import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;

import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SSODescriptor;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.signature.Signature;

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.engine.core.ExtensionProcessorI;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * Due to business constraints, this constitutes the contract with a DG Taxud project, so keep it as is.
 * <p>
 * Remove this interface in 1.2.
 *
 * @deprecated since 1.1, use {@link ProtocolEngineI} instead.
 */
@Deprecated
@Beta
public interface SamlEngineI {

    @Deprecated
    byte[] checkAndResignRequest(@Nonnull byte[] requestBytes) throws EIDASSAMLEngineException;

    byte[] checkAndDecryptResponse(@Nonnull byte[] responseBytes) throws EIDASSAMLEngineException;

    @Deprecated
    IRequestMessage generateEIDASAuthnRequestWithoutSign(IAuthenticationRequest request)
            throws EIDASSAMLEngineException;

    @Deprecated
    IRequestMessage generateEIDASAuthnRequestWithoutValidation(IAuthenticationRequest request)
            throws EIDASSAMLEngineException;

    IRequestMessage generateRequestMessage(IAuthenticationRequest request) throws EIDASSAMLEngineException;

    @Deprecated
    @VisibleForTesting
    IResponseMessage generateResponseMessage(IAuthenticationRequest request,
                                             IAuthenticationResponse response,
                                             String ipAddress) throws EIDASSAMLEngineException;

    IResponseMessage generateResponseMessage(IAuthenticationRequest request,
                                             IAuthenticationResponse authnResponse,
                                             boolean signAssertion,
                                             String ipAddress) throws EIDASSAMLEngineException;

    IResponseMessage generateResponseMessageFail(IAuthenticationRequest request,
                                                 IAuthenticationResponse response,
                                                 String ipAddress) throws EIDASSAMLEngineException;

    @Nonnull
    SamlEngineClock getClock();

    SamlEngineCoreProperties getCoreProperties();

    X509Certificate getDecryptionCertificate() throws EIDASSAMLEngineException;

    @Nonnull
    ExtensionProcessorI getExtensionProcessor();

    @Nonnull
    String getInstanceName();

    Signature getSignature() throws EIDASSAMLEngineException;

    Credential getSigningCredential() throws EIDASSAMLEngineException;

    boolean isEncryptedSamlResponse(byte[] tokenSaml) throws EIDASSAMLEngineException;

    boolean isResponseEncryptionMandatory();

    Response marshall(@Nonnull byte[] tokenSaml) throws EIDASSAMLEngineException;

    IAuthenticationRequest processValidateRequestToken(@Nonnull String citizenCountryCode, byte[] tokenSaml)
            throws EIDASSAMLEngineException;

    IAuthenticationResponse processValidateResponseToken(byte[] tokenSaml, String userIP, long skewTimeInMillis)
            throws EIDASSAMLEngineException;

    @Deprecated
    byte[] reSignRequest(byte[] requestBytes) throws EIDASSAMLEngineException;

    IRequestMessage resignEIDASAuthnRequest(IRequestMessage originalRequest, boolean changeDestination)
            throws EIDASSAMLEngineException;

    byte[] signAndMarshallEntitiesDescriptor(EntitiesDescriptor descriptor) throws EIDASSAMLEngineException;

    void signDescriptor(SSODescriptor descriptor) throws EIDASSAMLEngineException;

    void signEntityDescriptor(EntityDescriptor descriptor) throws EIDASSAMLEngineException;

    void validateEntityDescriptorSignature(SignableXMLObject entityDescriptorSignature) throws EIDASSAMLEngineException;

    IAuthenticationResponse validateMarshalledResponse(Response samlResponse, String userIP, long skewTimeInMillis)
            throws EIDASSAMLEngineException;
}