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

import eu.eidas.auth.commons.light.ILightRequest;

import javax.annotation.Nonnull;

/**
 * Enum that holds the view names of the specific that need to be called from the node.
 */
public enum NodeSpecificViewNames {

    /**
     * Handles specific {@link ILightRequest} functionality internally
     */
    MONOLITH_SPECIFIC_PROXYSERVICE_REQUEST("ProxyServiceRequest"),

    /**
     * Redirects to another application using a shared {@link ILightRequest} cache and a token
     */
    REDIRECT_SPECIFIC_PROXYSERVICE_REQUEST("/internal/tokenRedirectMsProxyService.jsp"),
    ;

    /**
     * constant name.
     */
    @Nonnull
    private final transient String name;

    /**
     * Constructor
     *
     * @param nameValue name of the bean
     */
    NodeSpecificViewNames(@Nonnull String nameValue) {
        name = nameValue;
    }

    @Nonnull
    @Override
    public String toString() {
        return name;

    }
}
