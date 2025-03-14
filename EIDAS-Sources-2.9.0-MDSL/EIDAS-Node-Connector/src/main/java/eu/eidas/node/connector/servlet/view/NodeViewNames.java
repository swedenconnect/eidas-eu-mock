/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.node.connector.servlet.view;

/**
 * See "Effective Java edition 2 (Joshua Bloch - Addison Wesley 20012)" item 30
 */
public enum NodeViewNames {

    /**
     * EidasResponse page that goes to the ProxyService
     * Populated by request.setAttribute from {@link eu.eidas.node.connector.servlet.binding.ConnectorSamlRequestViewMapping}
     */
    EIDAS_CONNECTOR_COLLEAGUE_REQUEST_REDIRECT("/internal/colleagueRequestRedirect.jsp"),

    /**
     * Error page that stops the flow
     * Populated by request.setAttribute from {@link eu.eidas.node.connector.servlet.binding.ConnectorErrorViewMapping}
     */
    CONNECTOR_ERROR_PAGE("/connectorErrorPage.jsp"),

    SERVLET_PATH_SERVICE_PROVIDER ( "/ServiceProvider"),
    ;

    /**
     * constant name.
     */
    private String name;

    /**
     * Constructor
     * @param name name of the bean
     */
    NodeViewNames(final String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;

    }
}
