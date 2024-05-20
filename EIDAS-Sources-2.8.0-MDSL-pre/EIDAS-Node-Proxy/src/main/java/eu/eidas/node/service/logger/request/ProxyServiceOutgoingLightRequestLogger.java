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

package eu.eidas.node.service.logger.request;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.logging.LoggingConstants;
import eu.eidas.logging.logger.LightRequestLogger;
import eu.eidas.logging.messages.LightRequestMessageLog;
import eu.eidas.node.service.servlet.binding.ProxyLightRequestViewMapping;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

public class ProxyServiceOutgoingLightRequestLogger extends LightRequestLogger {

    @Override
    protected void setMessageVector(LightRequestMessageLog.Builder messageLogBuilder, HttpServletRequest httpServletRequest) {
        messageLogBuilder.setOpType(EIDASValues.EIDAS_SERVICE_SPECIFIC_REQUEST.toString());
        messageLogBuilder.setNodeId(getLightTokenRequestNodeId());
        messageLogBuilder.setOrigin(LoggingConstants.NOT_APPLICABLE);
        messageLogBuilder.setDestination((String) httpServletRequest.getAttribute(ProxyLightRequestViewMapping.REDIRECT_URL));
    }

    @Nullable
    @Override
    protected String getLightToken(HttpServletRequest httpServletRequest) {
        return (String) httpServletRequest.getAttribute(ProxyLightRequestViewMapping.LIGHT_TOKEN); // attribute == outgoing
    }
}