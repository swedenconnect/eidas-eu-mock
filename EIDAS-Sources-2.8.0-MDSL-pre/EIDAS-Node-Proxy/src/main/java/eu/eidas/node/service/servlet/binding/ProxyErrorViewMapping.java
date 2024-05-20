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
 *  controller {@link eu.eidas.node.service.ProxyServiceErrorServlet} and
 *  view {@link NodeViewNames#PROXY_SERVICE_ERROR_PAGE}
 */
public class ProxyErrorViewMapping {

    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String CONTACT_SUPPORT_EMAIL = "contactSupportEmail";
    public static final String ERROR_ID = "errorId";

}
