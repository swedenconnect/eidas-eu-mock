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

package eu.eidas.logging;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface to log the full requests/responses incoming/outgoing
 * eIDAS Connector and eIDAS Proxy-Service.
 *
 * @since 2.5
 */
public interface IFullMessageLogger extends  IMessageLogger {

    /**
     * Method to get the logger name of the logger to use for the Full Message Logging.
     *
     * @param implementation the current implementation of this interface.
     * @return the fully qualified name for the logger.
     */
    static String getLoggerName(Class<? extends IFullMessageLogger> implementation) {
        return String.format("%s.%s", EIDASValues.EIDAS_PACKAGE_LOGGING_FULL, implementation.getSimpleName());
    }

    /**
     * Logs the full content of the request/response conveyed in the {@code HttpServletRequest} parameter
     *
     * @param httpServletRequest the http servlet request that contains the LightRequest or the eIDASRequest.
     */
    void logFullMessage(final HttpServletRequest httpServletRequest);
}
