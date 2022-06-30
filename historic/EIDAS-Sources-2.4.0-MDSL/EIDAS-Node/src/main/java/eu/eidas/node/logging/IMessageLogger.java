/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */
package eu.eidas.node.logging;

import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface to log the requests/responses incoming/outgoing
 * eIDAS Connector and eIDAS Proxy-Service.
 *
 * @since 2.3
 */
public interface IMessageLogger {

    /**
     * Logs information related to the request/response conveyed in the {@code HttpServletRequest} parameter
     *
     * @param logger             the instance of {@link Logger}
     * @param httpServletRequest the http servlet request that contains the {@link BinaryLightToken} or the SamlToken Base64 encoded.
     * @throws SpecificCommunicationException if the {@link BinaryLightToken} could not be created or EIDASSAMLEngineException if the Saml Object could not be unmarshalled.
     * @throws EIDASSAMLEngineException if message could not be logged
     */
     void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) throws SpecificCommunicationException, EIDASSAMLEngineException;

}
