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

package eu.eidas.node.connector.logger.response;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.logging.logger.EidasResponseLogger;
import eu.eidas.logging.messages.EidasResponseMessageLog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class ConnectorIncomingEidasResponseLogger extends EidasResponseLogger {

    protected void setMessageVector(@Nonnull EidasResponseMessageLog.Builder messageLogBuilder,
                                    @Nonnull HttpServletRequest httpServletRequest,
                                    @Nullable IAuthenticationResponse authenticationResponse) {
        final Optional<String> nodeId = Optional.ofNullable(authenticationResponse)
                .map(ILightResponse::getIssuer)
                .map(messageLoggerUtils::getEntityId);

        messageLogBuilder.setOpType(EIDASValues.EIDAS_CONNECTOR_RESPONSE.toString());
        nodeId.ifPresent(messageLogBuilder::setNodeId);
        messageLogBuilder.setOrigin(httpServletRequest.getHeader(EIDASValues.REFERER.toString()));
        messageLogBuilder.setDestination(httpServletRequest.getRequestURL().toString());
    }

    @Override
    protected byte[] getSamlResponseDecodedBytes(HttpServletRequest httpServletRequest) {
        final WebRequest webRequest = new IncomingRequest(httpServletRequest);
        final String samlResponseToken = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_RESPONSE);
        return EidasStringUtil.decodeBytesFromBase64(samlResponseToken);
    }
}
