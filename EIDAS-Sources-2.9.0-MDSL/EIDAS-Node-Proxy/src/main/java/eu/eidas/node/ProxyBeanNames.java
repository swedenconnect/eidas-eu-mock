/*
 * Copyright (c) 2023 by European Commission
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

package eu.eidas.node;

import javax.annotation.Nonnull;

/**
 * This enum class contains bean names.
 */
public enum ProxyBeanNames {

    CONNECTOR_AS_IDP_METADATA_GENERATOR("connectorMetadataGeneratorIDP"),

    EIDAS_CONNECTOR_UTIL("springManagedAUCONNECTORUtil"),

    EIDAS_PROXYSERVICE_FLOWID_CACHE("proxyServiceFlowIdCache"),

    /**
     * name of the bean that provides services to node/specific proxy service for putting/getting requests/responses in the maps
     */
    SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE("proxyCommunicationService"),

    PROXY_SERVICE_OUTGOING_LIGHT_REQUEST_LOGGER("proxyServiceOutgoingLightRequestLogger"),

    PROXY_SERVICE_INCOMING_LIGHT_RESPONSE_LOGGER("proxyServiceIncomingLightResponseLogger"),

    PROXY_SERVICE_INCOMING_EIDAS_REQUEST_LOGGER("proxyServiceIncomingEidasRequestLogger"),

    PROXY_SERVICE_OUTGOING_SAML_RESPONSE_LOGGER("proxyServiceOutgoingEidasResponseLogger"),

    EIDAS_SERVICE_BINDING("serviceBinding"), // TODO unused?

    EIDAS_SERVICE_CONTROLLER("serviceController"),

    PLACEHOLDER_CONFIG("placeholderConfig"), // TODO unused?

    SECURITY_CONFIG("springManagedSecurityConfig"),

    SERVICE_AS_REQUESTER_METADATA_GENERATOR("serviceMetadataGeneratorSP"),

    SERVICE_METADATA_GENERATOR("serviceMetadataGeneratorIDP"),

    SYSADMIN_MESSAGE_RESOURCES("sysadminMessageSource"),

    SPECIFIC_PROXYSERVICE_DEPLOYED_JAR("specificProxyServiceJar"),

    PROXYSERVICE_METADATA_FETCHER("proxyServiceMetadataFetcher"),

    NODE_PROTOCOL_ENGINE_FACTORY("NodeProtocolEngineFactory")

    // put the ; on a separate line to make merges easier
    ;

    /**
     * constant name.
     */
    @Nonnull
    private final transient String name;

    /**
     * Constructor
     * @param name name of the bean
     */
    ProxyBeanNames(@Nonnull final String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
