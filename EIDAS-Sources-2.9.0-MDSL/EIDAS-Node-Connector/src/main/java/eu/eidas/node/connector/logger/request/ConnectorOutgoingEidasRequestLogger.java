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
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.logging.LoggingConstants;
import eu.eidas.logging.logger.EidasRequestLogger;
import eu.eidas.logging.messages.EidasRequestMessageLog;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.connector.servlet.binding.ConnectorSamlRequestViewMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class ConnectorOutgoingEidasRequestLogger extends EidasRequestLogger {
    private AUCONNECTORUtil springManagedAULoggingUtil;

    @Override
    protected void setMessageVector(@Nonnull EidasRequestMessageLog.Builder messageLogBuilder,
                                    @Nonnull HttpServletRequest httpServletRequest,
                                    @Nullable IAuthenticationRequest authenticationRequest) {
        final Optional<String> destination = Optional.ofNullable(authenticationRequest)
                .map(IAuthenticationRequest::getDestination);
        final Optional<String> nodeId = Optional.ofNullable(authenticationRequest)
                .map(ar -> getNodeId(httpServletRequest));

        messageLogBuilder.setOpType(EIDASValues.CONNECTOR_SERVICE_REQUEST.toString());
        messageLogBuilder.setOrigin(LoggingConstants.NOT_APPLICABLE);
        nodeId.ifPresent(messageLogBuilder::setNodeId);
        destination.ifPresent(messageLogBuilder::setDestination);
    }

    @Override
    protected byte[] getSamlRequestDecodedBytes(HttpServletRequest httpServletRequest) {
        final String samlRequestTokenSaml = (String) httpServletRequest.getAttribute(ConnectorSamlRequestViewMapping.SAML_REQUEST);
        if (samlRequestTokenSaml == null) {
            return new byte[0];
        }
        return EidasStringUtil.decodeBytesFromBase64(samlRequestTokenSaml);
    }

    private String getNodeId(HttpServletRequest httpServletRequest) {
        final String citizenCountryCode = (String) httpServletRequest.getAttribute(ConnectorSamlRequestViewMapping.CITIZEN_COUNTRY_CODE);
        String issuer = springManagedAULoggingUtil.loadConfigServiceMetadataURL(citizenCountryCode);
        return messageLoggerUtils.getEntityId(issuer);
    }

    public void setSpringManagedAULoggingUtil(AUCONNECTORUtil springManagedAULoggingUtil) {
        this.springManagedAULoggingUtil = springManagedAULoggingUtil;
    }
}
