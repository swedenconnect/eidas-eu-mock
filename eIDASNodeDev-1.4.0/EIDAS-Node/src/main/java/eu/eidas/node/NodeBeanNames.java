/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.node;

import javax.annotation.Nonnull;

/**
 * This enum class contains bean names.
 */
public enum NodeBeanNames {

    IdP_RESPONSE("springManagedIdPResponse"),

    CONNECTOR_AS_IDP_METADATA_GENERATOR("connectorMetadataGeneratorIDP"),

    CONNECTOR_METADATA_GENERATOR("connectorMetadataGeneratorSP"),

    EIDAS_CONNECTOR_CONTROLLER("connectorController"),

    EIDAS_SERVICE_BINDING("serviceBinding"), // TODO unused?

    EIDAS_SERVICE_CONTROLLER("serviceController"),

    PLACEHOLDER_CONFIG("placeholderConfig"), // TODO unused?

    SECURITY_CONFIG("springManagedSecurityConfig"),

    SERVICE_AS_REQUESTER_METADATA_GENERATOR("serviceMetadataGeneratorSP"),

    SERVICE_METADATA_GENERATOR("serviceMetadataGeneratorIDP"),

    SYSADMIN_MESSAGE_RESOURCES("sysadminMessageSource"),

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
    NodeBeanNames(@Nonnull final String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
