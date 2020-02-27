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
package eu.eidas.node.logging.connector;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.logging.connector.messages.ConnectorIncomingEidasResponseLogger;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.utils.EidasNodeErrorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static eu.eidas.node.BeanProvider.getBean;
import static eu.eidas.node.NodeBeanNames.CONNECTOR_INCOMING_EIDAS_RESPONSE_LOGGER;

/**
 * Filter that intercepts Connector's Incoming HTTP request with the eIDAS Response
 * for logging information related to it.
 */
@WebFilter(filterName = "ConnectorIncomingEidasResponseLoggerFilter", urlPatterns = {"/ColleagueResponse"})
public class ConnectorIncomingEidasResponseLoggerFilter implements Filter {
    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorIncomingEidasResponseLoggerFilter.class.getName());

    private final Logger messageLogger = LoggerFactory.getLogger(
            EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE.toString() + "_" + ConnectorIncomingEidasResponseLoggerFilter.class.getSimpleName());

    /**
     * Preparing the logging of the incoming Saml Response received from Eidas Proxy Service.
     *
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Init of ConnectorIncomingEidasResponse filter");
    }

    /**
     * Log the incoming Saml Response received from Eidas Proxy Service
     *
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("ConnectorIncomingEidasResponse FILTER for %s", httpServletRequest.getServletPath()));
            }
            getConnectorIncomingEidasResponseLoggerImpl().logMessage(messageLogger, httpServletRequest);
            filterChain.doFilter(httpServletRequest, servletResponse);
        } catch (ServletException e) {
            LOGGER.info("ERROR : ServletException {}", e.getMessage());
            LOGGER.debug("ERROR : ServletException", e);
            throw e;
        } catch (IOException e) {
            LOGGER.info("IOException {}", e.getMessage());
            LOGGER.debug("IOException ", e);
            throw e;
        } catch (EIDASSAMLEngineException e) {
            LOGGER.info("BUSINESS EXCEPTION : SAML validation error {}", e.getMessage());
            LOGGER.debug("BUSINESS EXCEPTION : SAML validation error", e);
            EidasNodeErrorUtil.processSAMLEngineException(e, LOGGER, EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML);
        }
    }

    @Override
    public void destroy() {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Destroy of ConnectorIncomingEidasResponse filter");
    }

    /**
     * Getter for {@link ConnectorIncomingEidasResponseLogger}
     *
     * @return the instance of the {@link ConnectorIncomingEidasResponseLogger}
     */
    private ConnectorIncomingEidasResponseLogger getConnectorIncomingEidasResponseLoggerImpl() {
        return getBean(ConnectorIncomingEidasResponseLogger.class, CONNECTOR_INCOMING_EIDAS_RESPONSE_LOGGER.toString());
    }

}
