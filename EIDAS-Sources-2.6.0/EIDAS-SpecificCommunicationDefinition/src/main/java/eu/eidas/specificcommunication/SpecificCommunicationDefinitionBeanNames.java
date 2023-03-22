/*
 * Copyright (c) 2021 by European Commission
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

package eu.eidas.specificcommunication;

import javax.annotation.Nonnull;

/**
 * This enum class contains bean names.
 *
 * @since 2.0
 */
public enum SpecificCommunicationDefinitionBeanNames {

    /**
     * name of the cache for holding requests sent from specific to node
     */
    SPECIFIC_NODE_CONNECTOR_CACHE("specificNodeConnectorRequestCache"),

    /**
     * name of the cache for for holding requests sent from node proxy service to specific proxy service
     */
    NODE_SPECIFIC_PROXYSERVICE_CACHE("nodeSpecificProxyserviceRequestCache"),

    /**
     * name of the cache for for holding responses sent from specific proxy service to node proxy service
     */
    SPECIFIC_NODE_PROXYSERVICE_CACHE("specificNodeProxyserviceResponseCache"),

    /**
     * name of the cache for for holding responses sent from node connector to specific connector
     */
    NODE_SPECIFIC_CONNECTOR_CACHE("nodeSpecificConnectorResponseCache"),

    /**
     * name of the bean that provides services to node/specific connector for putting/getting requests/responses in the maps
     */
    SPECIFIC_CONNECTOR_COMMUNICATION_SERVICE("springManagedSpecificConnectorCommunicationService"),

    /**
     * name of the bean that provides services to node/specific proxy service for putting/getting requests/responses in the maps
     */
    SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE("springManagedSpecificProxyserviceCommunicationService"),

    /**
     * name of the bean that provides services to node/specific connector for putting requests/responses in the maps
     */
    SPECIFIC_CONNECTOR_COMMUNICATION_SERVICE_EXTENSION("springManagedSpecificConnectorCommunicationServiceExtension"),

    /**
     * name of the bean that provides services to node/specific proxy service for putting requests/responses in the maps
     */
    SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE_EXTENSION("springManagedSpecificProxyserviceCommunicationServiceExtension"),

    /**
     * name of the map for holding requests sent from specific to node
     */
    IGNITE_SPRING_BEAN("igniteSpringBean"),

    /**
     * name of the EidasIgniteInstanceInitializer bean
     */
    EIDAS_IGNITE_INSTANCE_INITIALIZER_BEAN("eidasIgniteInstanceInitializer"),

    /**
     * name of the Incoming Light Request Validator bean
     */
    INCOMING_LIGHT_REQUEST_VALIDATOR("incomingLightRequestValidator"),

    /**
     * name of the Incoming Light Response Validator bean
     */
    INCOMING_LIGHT_RESPONSE_VALIDATOR("incomingLightResponseValidator"),

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
    SpecificCommunicationDefinitionBeanNames(@Nonnull final String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
