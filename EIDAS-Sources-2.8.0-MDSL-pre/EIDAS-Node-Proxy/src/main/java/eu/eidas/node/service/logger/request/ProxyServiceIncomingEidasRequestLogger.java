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
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.logger.EidasRequestLogger;
import eu.eidas.logging.messages.EidasRequestMessageLog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the incoming eIDAS Request
 * to eIDAS Proxy-Service from eIDAS Connector
 *
 * @since 2.3
 */
public final class ProxyServiceIncomingEidasRequestLogger extends EidasRequestLogger {

    @Override
    protected void setMessageVector(@Nonnull EidasRequestMessageLog.Builder messageLogBuilder,
                                    @Nonnull HttpServletRequest httpServletRequest,
                                    @Nullable IAuthenticationRequest authenticationRequest) {
        final Optional<String> nodeId = Optional.ofNullable(authenticationRequest)
                .map(ILightRequest::getIssuer)
                .map(messageLoggerUtils::getEntityId);
        final Optional<String> destination = Optional.ofNullable(authenticationRequest)
                .map(IAuthenticationRequest::getDestination);

        messageLogBuilder.setOpType(EIDASValues.EIDAS_SERVICE_REQUEST.toString());
        messageLogBuilder.setOrigin(httpServletRequest.getHeader(EIDASValues.REFERER.toString()));
        nodeId.ifPresent(messageLogBuilder::setNodeId);
        destination.ifPresent(messageLogBuilder::setDestination);
    }

    public byte[] getSamlRequestDecodedBytes(HttpServletRequest httpServletRequest) {
        final WebRequest webRequest = new IncomingRequest(httpServletRequest);
        final String samlRequestTokenSaml = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_REQUEST);
        if (samlRequestTokenSaml == null) {
            return new byte[0];
        }
         return EidasStringUtil.decodeBytesFromBase64(samlRequestTokenSaml);
    }
}
