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
 * This class contains bean names.
 */
public enum ConnectorBeanNames {

    CONNECTOR_AS_IDP_METADATA_GENERATOR("connectorMetadataGeneratorIDP"),

    CONNECTOR_METADATA_GENERATOR("connectorMetadataGeneratorSP"),

    EIDAS_CONNECTOR_CONTROLLER("connectorController"),

    EIDAS_CONNECTOR_UTIL("springManagedAUCONNECTORUtil"),

    EIDAS_CONNECTOR_FLOWID_CACHE("connectorFlowIdCache"),

    /**
     * name of the bean that provides services to node/specific connector for putting/getting requests/responses in the maps
     */
    SPECIFIC_CONNECTOR_COMMUNICATION_SERVICE("connectorCommunicationService"),

    CONNECTOR_INCOMING_LIGHT_REQUEST_LOGGER("connectorIncomingLightRequestLogger"),

    CONNECTOR_OUTGOING_LIGHT_RESPONSE_LOGGER("connectorOutgoingLightResponseLogger"),

    CONNECTOR_OUTGOING_EIDAS_REQUEST_LOGGER("connectorOutgoingEidasRequestLogger"),

    CONNECTOR_INCOMING_EIDAS_RESPONSE_LOGGER("connectorIncomingEidasResponseLogger"),

    EIDAS_SERVICE_BINDING("serviceBinding"), // TODO unused?

    PLACEHOLDER_CONFIG("placeholderConfig"), // TODO unused?

    SECURITY_CONFIG("springManagedSecurityConfig"),

    SERVICE_AS_REQUESTER_METADATA_GENERATOR("serviceMetadataGeneratorSP"),

    SYSADMIN_MESSAGE_RESOURCES("sysadminMessageSource"),

    SPECIFIC_CONNECTOR_DEPLOYED_JAR("specificConnectorJar"),

    CONNECTOR_METADATA_FETCHER("connectorMetadataFetcher"),

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
    ConnectorBeanNames(@Nonnull final String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
