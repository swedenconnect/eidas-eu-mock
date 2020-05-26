/*
 * Copyright (c) 2019 by European Commission
 *
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.eidas.node.auth.connector;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.BinaryRequestMessage;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.cache.Cache;

import static java.lang.Boolean.parseBoolean;

/**
 * The AUCONNECTOR class serves as the middle-man in the communications between the Service Provider and the eIDAS
 * ProxyService. It is responsible for handling the requests coming from the Service Provider and forward them to the
 * eIDAS ProxyService, and vice-versa.
 *
 * @see ICONNECTORService
 */
public final class AUCONNECTOR implements ICONNECTORService {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AUCONNECTOR.class);

    /**
     * Service for country related operations.
     */
    private ICONNECTORCountrySelectorService countryService;

    /**
     * Service for SAML related operations.
     */
    private ICONNECTORSAMLService samlService;

    /**
     * Connector configuration: Never null. initialized via Spring Application Context
     */
    private AUCONNECTORUtil connectorUtil;

    private Cache<String, StoredLightRequest> specificSpRequestCorrelationCache;

    private Cache<String, StoredAuthenticationRequest> connectorRequestCorrelationCache;

    /**
     * {@inheritDoc}
     */
    @Override
    public IRequestMessage getAuthenticationRequest(@Nonnull WebRequest webRequest, ILightRequest lightRequest) {

        checkLightRequestAntiReplay(lightRequest);

        IAuthenticationRequest serviceProviderRequest = samlService.processSpRequest(lightRequest, webRequest);

        String citizenIpAddress = webRequest.getRemoteIpAddress();

        IAuthenticationRequest fullRequest = prepareEidasRequest((IEidasAuthenticationRequest) serviceProviderRequest);

        // generate SAML Token
        IRequestMessage connectorRequest = samlService.generateServiceAuthnRequest(webRequest, fullRequest);

        byte[] samlToken = connectorRequest.getMessageBytes();

        connectorRequest = new BinaryRequestMessage(connectorRequest.getRequest(), sendRedirect(samlToken));

        String connectorRequestSamlId = connectorRequest.getRequest().getId();

        specificSpRequestCorrelationCache.put(connectorRequestSamlId, StoredLightRequest.builder()
                .request(lightRequest)
                .remoteIpAddress(citizenIpAddress)
                .build());

        connectorRequestCorrelationCache.put(connectorRequestSamlId, StoredAuthenticationRequest.builder()
                .request(connectorRequest.getRequest())
                .remoteIpAddress(citizenIpAddress)
                .build());

        return connectorRequest;
    }

    /**
     * Prevents multiple submission of the same {@link eu.eidas.auth.commons.light.impl.LightRequest}
     * @param lightRequest the {@link eu.eidas.auth.commons.light.impl.LightRequest} under anti replay check
     */
    private void checkLightRequestAntiReplay(ILightRequest lightRequest) {
        String lightRequestId = lightRequest.getId();
        String citizenCountryCode = lightRequest.getCitizenCountryCode();
        final boolean isNotPresentInCache = connectorUtil.checkNotPresentInCache(lightRequestId, citizenCountryCode).booleanValue();
        if (!isNotPresentInCache) {
            // There is no (code, message) defined for Light Request error handling in the Connector
            throw new SecurityEIDASException(StringUtils.EMPTY, StringUtils.EMPTY);
        }
    }

    private IEidasAuthenticationRequest prepareEidasRequest(IEidasAuthenticationRequest authData) {

        //Validate MDS
        if (connectorUtil != null && !parseBoolean(
                connectorUtil.getConfigs().getProperty(EIDASValues.DISABLE_CHECK_MANDATORY_ATTRIBUTES.toString())) &&
                !samlService.checkMandatoryAttributes(authData.getRequestedAttributes())) {
            LOG.error("BUSINESS EXCEPTION : incomplete mandatory set");
            throw new EidasNodeException(EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorCode()),
                    EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorMessage()));
        }

        //Validate Representative
        if (connectorUtil != null && !parseBoolean(
                connectorUtil.getConfigs().getProperty(EIDASValues.DISABLE_CHECK_REPRESENTATIVE_ATTRS.toString())) &&
                !samlService.checkRepresentativeAttributes(authData.getRequestedAttributes())) {
            throw new EidasNodeException(EidasErrors.get(EidasErrorKey.EIDAS_REPRESENTATIVE_ATTRIBUTES.errorCode()),
                    EidasErrors.get(EidasErrorKey.EIDAS_REPRESENTATIVE_ATTRIBUTES.errorMessage()));
        }

        LOG.trace("do not fill in the assertion url");

        EidasAuthenticationRequest.Builder builder = EidasAuthenticationRequest.builder(authData);
        boolean modified = false;

        if (null != authData.getAssertionConsumerServiceURL()) {
            builder.assertionConsumerServiceURL(null);
            modified = true;
        }
        if (null != authData.getBinding()) {
            builder.binding(null);
            modified = true;
        }

        // SP type setting to Request depending on Node type
        String nodeSpType = null;
        if (connectorUtil != null) {
            nodeSpType = connectorUtil.getConfigs().getProperty(EIDASValues.EIDAS_SPTYPE.toString());
        }
        String spType = authData.getSpType();
        if (StringUtils.isNotBlank(nodeSpType)) {
            if (StringUtils.isNotBlank(spType)) {
                if (spType.equalsIgnoreCase(nodeSpType)) {
                    // if exists in both config and Request and equals, then silently remove fom request
                    builder.spType(null);
                    modified = true;
                } else {
                    // if exists and not equals then throw error
                    String message = "Node SP type not set differently than requested in authentication request";
                    LOG.error(message);
                    throw new EidasNodeException(
                            EidasErrors.get(EidasErrorKey.CONNECTOR_INVALID_SPTYPE.errorCode()),
                            EidasErrors.get(EidasErrorKey.CONNECTOR_INVALID_SPTYPE.errorMessage()));
                }
            }
            // default is in Request not provided
        } else {
            // no SP type at all
            if (StringUtils.isBlank(authData.getSpType())) {
                String message = "Node SP type not set, authentication request has no SP type";
                LOG.error(message);
                throw new EidasNodeException(
                        EidasErrors.get(EidasErrorKey.CONNECTOR_INVALID_SPTYPE.errorCode()),
                        EidasErrors.get(EidasErrorKey.CONNECTOR_INVALID_SPTYPE.errorMessage()));
            }
        }

        if (modified) {
            return builder.build();
        }

        return authData;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    public AuthenticationExchange getAuthenticationResponse(@Nonnull WebRequest webRequest) {

        // processing the webRequest
        AuthenticationExchange authenticationExchange =
                samlService.processProxyServiceResponse(webRequest, connectorRequestCorrelationCache,
                        specificSpRequestCorrelationCache);

        IAuthenticationResponse connectorResponse = authenticationExchange.getConnectorResponse();

        // do not validate mandatory attributes in case of failure
        if (!connectorResponse.isFailure()) {
            ImmutableAttributeMap attributeMap = connectorResponse.getAttributes();

            boolean disableCheckMandatoryAttributes = parseBoolean(
                    connectorUtil.getConfigs().getProperty(EIDASValues.DISABLE_CHECK_MANDATORY_ATTRIBUTES.toString()));

            if (!disableCheckMandatoryAttributes && !samlService.checkMandatoryAttributes(attributeMap)) {
                throw new EidasNodeException(EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorCode()),
                        EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorMessage()));
            }
        }
        return authenticationExchange;
    }

    /**
     * Encodes, {@link org.bouncycastle.util.encoders.Base64}, a SAML Token.
     *
     * @param samlToken The Saml Token to encode.
     * @return The encoded SAML Token.
     */
    public byte[] sendRedirect(byte[] samlToken) {
        LOG.trace("Setting attribute SAML_TOKEN_PARAM");
        return Base64.encode(samlToken);
    }

    /**
     * Setter for countryService.
     *
     * @param theCountryService The countryService to set.
     * @see ICONNECTORCountrySelectorService
     */
    public void setCountryService(ICONNECTORCountrySelectorService theCountryService) {

        this.countryService = theCountryService;
    }

    /**
     * Getter for countryService.
     *
     * @return The countryService value.
     * @see ICONNECTORCountrySelectorService
     */
    public ICONNECTORCountrySelectorService getCountryService() {
        return countryService;
    }

    /**
     * Setter for samlService.
     *
     * @param theSamlService The samlService to set.
     * @see ICONNECTORSAMLService
     */
    public void setSamlService(ICONNECTORSAMLService theSamlService) {
        this.samlService = theSamlService;
    }

    /**
     * Getter for samlService.
     *
     * @return The samlService value.
     * @see ICONNECTORSAMLService
     */
    public ICONNECTORSAMLService getSamlService() {
        return samlService;
    }

    public void setConnectorUtil(AUCONNECTORUtil connectorUtil) {
        this.connectorUtil = connectorUtil;
    }

    public Cache<String, StoredLightRequest> getSpecificSpRequestCorrelationCache() {
        return specificSpRequestCorrelationCache;
    }

    public void setSpecificSpRequestCorrelationCache(Cache<String, StoredLightRequest> specificSpRequestCorrelationCache) {
        this.specificSpRequestCorrelationCache = specificSpRequestCorrelationCache;
    }

    public Cache<String, StoredAuthenticationRequest> getConnectorRequestCorrelationCache() {
        return connectorRequestCorrelationCache;
    }

    public void setConnectorRequestCorrelationCache(Cache<String, StoredAuthenticationRequest> connectorRequestCorrelationCache) {
        this.connectorRequestCorrelationCache = connectorRequestCorrelationCache;
    }


}
