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

package eu.eidas.node.service.servlet.binding;

import eu.eidas.node.service.servlet.view.NodeViewNames;

/**
 *  Mapping between
 *  happy flow controller {@link eu.eidas.node.service.SpecificProxyServiceResponse},
 *  error flow controller {@link eu.eidas.node.service.SamlResponseFailureServlet} and
 *  view {@link NodeViewNames#EIDAS_CONNECTOR_REDIRECT}
 */

public class ProxySamlResponseViewMapping {

    public static final String REDIRECT_URL = "redirectUrl";
    public static final String SAML_TOKEN = "samlToken";
    public static final String RELAY_STATE = "RelayState";
    /**
     * Currently hardcoded in jsp
     */
    public static final String BINDING = "binding";
    /**
     * Unused
     */
    public static final String EXCEPTION = "exception";

    public static final String EIDAS_ATTRIBUTES_PARAM = "eidasAttributes";

    public static final String LOA_VALUE = "eidasAttributes";

}
