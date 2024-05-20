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
package eu.eidas.logging.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Filter that intercepts Proxy-Service's Outgoing HTTP request with the eIDAS Response
 * for logging information related to it.
 */
public class OutgoingLoggerFilter extends MessageLoggerFilter {

    @Override
    protected void doOrderOfLogging(HttpServletRequest httpServletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(httpServletRequest, servletResponse);
        traceBeforeLogging(httpServletRequest);
        fullMessageLogger.logMessage(httpServletRequest);
        fullMessageLogger.logFullMessage(httpServletRequest);
    }

    private void traceBeforeLogging(HttpServletRequest httpServletRequest) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("%s FILTER for %s",
                    fullMessageLogger.getClass().getSimpleName(),
                    httpServletRequest.getServletPath()
            ));
        }
    }

}
