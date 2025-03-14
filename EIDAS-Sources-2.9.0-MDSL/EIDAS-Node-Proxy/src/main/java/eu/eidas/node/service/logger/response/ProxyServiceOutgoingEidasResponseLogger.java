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
package eu.eidas.node.service.logger.response;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.LoggingConstants;
import eu.eidas.logging.logger.EidasResponseLogger;
import eu.eidas.logging.messages.EidasResponseMessageLog;
import eu.eidas.node.service.servlet.binding.ProxySamlResponseViewMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the outgoing eIDAS Response
 * to eIDAS Connector from eIDAS Proxy-Service
 *
 * @since 2.3
 */
public final class ProxyServiceOutgoingEidasResponseLogger extends EidasResponseLogger {

    @Override
    protected void setMessageVector(@Nonnull EidasResponseMessageLog.Builder messageLogBuilder,
                                    @Nonnull HttpServletRequest httpServletRequest,
                                    @Nullable IAuthenticationResponse authenticationResponse) {
        final Optional<String> nodeId = Optional.ofNullable(authenticationResponse)
                .map(ILightResponse::getInResponseToId)
                .map(inResponseToId -> messageLoggerUtils.getIssuer(inResponseToId, httpServletRequest)); // TODO pletipi this accesses proxyServiceRequestCorrelationCache via messageLoggerUtils

        messageLogBuilder.setOpType(EIDASValues.EIDAS_SERVICE_CONNECTOR_RESPONSE.toString());
        nodeId.ifPresent(messageLogBuilder::setNodeId);
        messageLogBuilder.setOrigin(LoggingConstants.NOT_APPLICABLE);
        messageLogBuilder.setDestination(getDestinationUrl(httpServletRequest));
    }

    protected byte[] getSamlResponseDecodedBytes(HttpServletRequest httpServletRequest) {
        final String samlResponseToken = (String) httpServletRequest.getAttribute(ProxySamlResponseViewMapping.SAML_TOKEN);
        if (samlResponseToken == null) {
            return new byte[0];
        }
        return EidasStringUtil.decodeBytesFromBase64(samlResponseToken);
    }

    private String getDestinationUrl(HttpServletRequest httpServletRequest) {
        String destinationUrl = (String) httpServletRequest.getAttribute(ProxySamlResponseViewMapping.REDIRECT_URL);
        destinationUrl = removeSessionIdFromUrl(destinationUrl);
        return destinationUrl;
    }

    private String removeSessionIdFromUrl(String destinationUrl) {
        return destinationUrl.replaceAll(";.*", "");
    }
}
