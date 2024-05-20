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

package eu.eidas.node.connector.logger.request;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.logging.logger.LightRequestLogger;
import eu.eidas.logging.messages.LightRequestMessageLog;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

public class ConnectorIncomingLightRequestLogger extends LightRequestLogger {

    @Override
    protected void setMessageVector(LightRequestMessageLog.Builder messageLogBuilder, HttpServletRequest httpServletRequest) {
        messageLogBuilder.setOpType(EIDASValues.SPECIFIC_EIDAS_CONNECTOR_REQUEST.toString());
        messageLogBuilder.setNodeId(getLightTokenRequestNodeId());
        messageLogBuilder.setOrigin(httpServletRequest.getHeader(EIDASValues.REFERER.toString()));
        messageLogBuilder.setDestination(httpServletRequest.getRequestURL().toString());
    }

    @Nullable
    @Override
    protected String getLightToken(HttpServletRequest httpServletRequest) {
        final WebRequest webRequest = new IncomingRequest(httpServletRequest);
        return webRequest.getEncodedLastParameterValue(EidasParameterKeys.TOKEN); // parameter == incoming
    }
}
