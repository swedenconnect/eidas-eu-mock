/*
 * Copyright (c) 2017 by European Commission
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
     * name of the map for holding requests sent from specific to node
     */
    SPECIFIC_NODE_CONNECTOR_MAP("specificNodeConnectorRequestProviderMap"),

    /**
     * name of the map for for holding responses sent from node connector to specific connector
     */
    NODE_SPECIFIC_CONNECTOR_MAP("nodeSpecificConnectorResponseProviderMap"),

    /**
     * name of the map for for holding requests sent from node proxy service to specific proxy service
     */
    NODE_SPECIFIC_PROXYSERVICE_MAP("nodeSpecificProxyserviceRequestProviderMap"),

    /**
     * name of the map for for holding responses sent from specific proxy service to node proxy service
     */
    SPECIFIC_NODE_PROXYSERVICE_MAP("specificNodeProxyserviceResponseProviderMap"),

    /**
     * name of the bean that provides services to node/specific connector for putting/getting requests/responses in the maps
     */
    SPECIFIC_CONNECTOR_COMMUNICATION_SERVICE("springManagedSpecificConnectorCommunicationService"),

    /**
     * name of the bean that provides services to node/specific proxy service for putting/getting requests/responses in the maps
     */
    SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE("springManagedSpecificProxyserviceCommunicationService"),

    /**
     * name of the  HazelcastInstanceInitializer bean
     */
    EIDAS_HAZELCAST_INSTANCE_INITIALIZER("eidasHazelcastInstanceInitializer"),


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
