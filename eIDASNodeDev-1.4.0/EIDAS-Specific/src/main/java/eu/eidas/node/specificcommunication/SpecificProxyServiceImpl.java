package eu.eidas.node.specificcommunication;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.auth.specific.IAUService;
import eu.eidas.node.CitizenAuthenticationBean;
import eu.eidas.node.SpecificIdPBean;
import eu.eidas.node.SpecificParameterNames;
import eu.eidas.node.SpecificViewNames;
import eu.eidas.node.specificcommunication.exception.SpecificException;
import eu.eidas.node.specificcommunication.protocol.IResponseCallbackHandler;

import static eu.eidas.auth.commons.EidasParameterKeys.EIDAS_SERVICE_CALLBACK;
import static eu.eidas.auth.engine.core.SAMLExtensionFormat.EIDAS_FORMAT_NAME;
import static eu.eidas.node.SpecificServletHelper.getHttpRequestAttributesHeaders;
import static eu.eidas.node.SpecificServletHelper.getHttpRequestParameters;

/**
 * SpecificProxyServiceImpl: provides a sample implementation of the specific interface {@link ISpecificProxyService}
 * For the request: it creates the message bytes to send to IdP for authentication For the response: it validates the
 * received IdP specific response and builds the LightResponse
 *
 * @since 1.1
 */
public class SpecificProxyServiceImpl implements ISpecificProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificProxyServiceImpl.class);

    private CitizenAuthenticationBean citizenAuthentication;

    private boolean signResponseAssertion;

    private SpecificIdPBean specificIdPResponse;

    public CitizenAuthenticationBean getCitizenAuthentication() {
        return citizenAuthentication;
    }

    public void setCitizenAuthentication(CitizenAuthenticationBean citizenAuthentication) {
        this.citizenAuthentication = citizenAuthentication;
    }

    public boolean isSignResponseAssertion() {
        return signResponseAssertion;
    }

    public void setSignResponseAssertion(boolean signResponseAssertion) {
        this.signResponseAssertion = signResponseAssertion;
    }

    public SpecificIdPBean getSpecificIdPResponse() {
        return specificIdPResponse;
    }

    public void setSpecificIdPResponse(SpecificIdPBean specificIdPResponse) {
        this.specificIdPResponse = specificIdPResponse;
    }

    @Override
    public void sendRequest(@Nonnull ILightRequest lightRequest,
                            @Nonnull HttpServletRequest httpServletRequest,
                            @Nonnull HttpServletResponse httpServletResponse) throws SpecificException {

        try {
            // build parameter list
            Map<String, Object> parameters = getHttpRequestParameters(httpServletRequest);

            IAUService specificService = citizenAuthentication.getSpecAuthenticationNode();

            ImmutableAttributeMap attrMap = lightRequest.getRequestedAttributes();

            if (citizenAuthentication.isExternalAuth()) {

                LOGGER.trace("external-authentication");

                NormalParameterValidator.paramName(EidasParameterKeys.IDP_URL)
                        .paramValue(citizenAuthentication.getIdpUrl())
                        .validate();
                handleStorkAssertionConsumerUrl(parameters, specificService);

                parameters.put(EidasParameterKeys.IDP_URL.toString(), citizenAuthentication.getIdpUrl());

                parameters.put(EidasParameterKeys.CITIZEN_COUNTRY_CODE.toString(),
                               lightRequest.getCitizenCountryCode());
                parameters.put(EidasParameterKeys.SERVICE_PROVIDER_NAME.toString(), lightRequest.getProviderName());
                parameters.put(EidasParameterKeys.CITIZEN_IP_ADDRESS.toString(),
                               IncomingRequest.getRemoteAddress(httpServletRequest));

                httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), EidasSamlBinding.POST.getName());

                parameters.put(EidasParameterKeys.EIDAS_SERVICE_LOA.toString(), lightRequest.getLevelOfAssurance());
                parameters.put(EidasParameterKeys.EIDAS_NAMEID_FORMAT.toString(), lightRequest.getNameIdFormat());

                parameters.put(EidasParameterKeys.SERVICE_PROVIDER_TYPE.toString(), lightRequest.getSpType());

                byte[] samlTokenBytes = specificService.prepareCitizenAuthentication(lightRequest, attrMap, parameters,
                                                                                     getHttpRequestAttributesHeaders(
                                                                                             httpServletRequest));
                // used by jsp
                String samlToken = EidasStringUtil.encodeToBase64(samlTokenBytes);

                httpServletRequest.setAttribute(SpecificParameterNames.SAML_TOKEN.toString(), samlToken);
                httpServletRequest.setAttribute(SpecificParameterNames.IDP_URL.toString(),
                                                citizenAuthentication.getIdpUrl());
                httpServletRequest.setAttribute(EidasParameterKeys.REQUEST_FORMAT.toString(), EIDAS_FORMAT_NAME);

            } else {
                throw new SpecificException("internal-authentication not implemented");
            }
            //redirecting to IdP
            String encodedURL = httpServletResponse.encodeURL(SpecificViewNames.IDP_REDIRECT.toString());
            RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher(encodedURL);
            dispatcher.forward(httpServletRequest, httpServletResponse);

        } catch (ServletException | IOException e) {
            LOGGER.error("Error converting the LightRequest to the specific protocol");
            throw new SpecificException(e);
        }
    }

    @Override
    public ILightResponse processResponse(@Nonnull HttpServletRequest httpServletRequest,
                                          @Nonnull HttpServletResponse httpServletResponse) throws SpecificException {

        String samlResponse = getSamlResponse(httpServletRequest);

        IAUService specificService = specificIdPResponse.getSpecificNode();

        AuthenticationExchange authenticationExchange =
                specificService.processAuthenticationResponse(EidasStringUtil.decodeBytesFromBase64(samlResponse));

        IAuthenticationResponse specificResponse = authenticationExchange.getConnectorResponse();

        IAuthenticationRequest specificAuthnRequest = authenticationExchange.getStoredRequest().getRequest();
        StoredLightRequest proxyServiceRequest = getStoredLightRequest(specificService, specificAuthnRequest);

        httpServletRequest.removeAttribute(EidasParameterKeys.ATTRIBUTE_LIST.toString());

        IAuthenticationResponse authenticationResponse;

        if (!EIDASStatusCode.SUCCESS_URI.toString().equals(specificResponse.getStatusCode())) {
            String statusCode = specificResponse.getStatusCode();
            LOGGER.debug("Message from IdP with status code: " + statusCode);

            ILightRequest proxyServiceAuthnRequest = proxyServiceRequest.getRequest();
            authenticationResponse = AuthenticationResponse.builder(specificResponse)
                    .failure(true)
                    .inResponseTo(proxyServiceAuthnRequest.getId())
                    .build();

        } else {
            httpServletRequest.setAttribute(EidasParameterKeys.EIDAS_SERVICE_LOA.toString(),
                                            specificResponse.getLevelOfAssurance());

            ImmutableAttributeMap requestedAttributes = specificAuthnRequest.getRequestedAttributes();

            if (!isAttributeListValid(specificService, requestedAttributes, specificResponse.getAttributes())) {

                String errorCode = EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorCode());
                String errorMessage = EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorMessage());

                ILightRequest proxyServiceAuthnRequest = proxyServiceRequest.getRequest();

                authenticationResponse = AuthenticationResponse.builder(specificResponse)
                        .failure(true)
                        .statusCode(errorCode)
                        .statusMessage(errorMessage)
                        .inResponseTo(proxyServiceAuthnRequest.getId())
                        .build();

            } else {
                ILightRequest proxyServiceAuthnRequest = proxyServiceRequest.getRequest();
                authenticationResponse = AuthenticationResponse.builder(specificResponse)
                        .inResponseTo(proxyServiceAuthnRequest.getId())
                        .build();

            }
        }
        //build the LightResponse
        return LightResponse.builder(authenticationResponse).build();
    }

    @Override
    public void setResponseCallbackHandler(@Nonnull IResponseCallbackHandler responseCallbackHandler) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    private void handleStorkAssertionConsumerUrl(Map<String, Object> parameters, IAUService specificService) {
        // Correct URl redirect cookie implementation
        String callbackURL = specificService.getCallBackURL();
        LOGGER.debug("Setting callbackURL: " + callbackURL);

        NormalParameterValidator.paramName(EIDAS_SERVICE_CALLBACK).paramValue(callbackURL).validate();

        parameters.put(EIDAS_SERVICE_CALLBACK.toString(), callbackURL);
    }

    private String getSamlResponse(@Nonnull HttpServletRequest httpServletRequest) {
        String samlResponse = httpServletRequest.getParameter(EidasParameterKeys.SAML_RESPONSE.toString());

        NormalParameterValidator.paramName(EidasParameterKeys.SAML_RESPONSE)
                .paramValue(samlResponse)
                .eidasError(EidasErrorKey.IDP_SAML_RESPONSE)
                .validate();
        return samlResponse;
    }

    private StoredLightRequest getStoredLightRequest(IAUService specificService,
                                                     IAuthenticationRequest specificAuthnRequest)
            throws SpecificException {

        CorrelationMap<StoredLightRequest> proxyServiceRequestCorrelationMap =
                specificService.getProxyServiceRequestCorrelationMap();
        StoredLightRequest proxyServiceRequest = proxyServiceRequestCorrelationMap.get(specificAuthnRequest.getId());

        if (null == proxyServiceRequest) {
            LOGGER.error(
                    "ProxyService Request cannot be found for Specific Request ID: \"" + specificAuthnRequest.getId()
                            + "\"");
            throw new SpecificException(
                    "ProxyService Request cannot be found for Specific Request ID: \"" + specificAuthnRequest.getId()
                            + "\"");
        }
        //clean up
        proxyServiceRequestCorrelationMap.remove(specificAuthnRequest.getId());

        return proxyServiceRequest;
    }

    private boolean isAttributeListValid(IAUService specificService,
                                         ImmutableAttributeMap requestedAttributes,
                                         ImmutableAttributeMap responseAttributes) {

        return specificService.compareAttributeLists(requestedAttributes, responseAttributes);
    }
}
