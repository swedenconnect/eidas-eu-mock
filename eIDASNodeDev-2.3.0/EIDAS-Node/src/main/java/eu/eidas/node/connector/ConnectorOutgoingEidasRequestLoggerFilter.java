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
package eu.eidas.node.connector;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.connector.messages.ConnectorOutgoingEidasRequestLogger;
import eu.eidas.node.logging.LoggingMarkerMDC;
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

/**
 * Filter that intercepts Connector's Outgoing HTTP request with the eIDAS Request
 * for logging information related to it.
 */
@WebFilter(filterName = "ConnectorOutgoingEidasRequestLoggerFilter", urlPatterns = {"/SpecificConnectorRequest"})
public class ConnectorOutgoingEidasRequestLoggerFilter implements Filter {

    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorOutgoingEidasRequestLoggerFilter.class.getName());

    private final Logger logger = LoggerFactory.getLogger(
            EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE.toString() + "_" + ConnectorOutgoingEidasRequestLoggerFilter.class.getSimpleName());


    /**
     * Preparing the logging of the outgoing SAML Request sent to Eidas Proxy Service
     *
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Init of ConnectorOutgoingEidasRequest filter");
    }

    /**
     * Log the outgoing SAML Request sent to Eidas Proxy Service
     *
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

            LOGGER.trace("ConnectorOutgoingEidasRequest FILTER for " + httpServletRequest.getServletPath());

            filterChain.doFilter(httpServletRequest, servletResponse);

            getConnectorOutgoingEidasRequestLoggerImpl().logMessage(logger, httpServletRequest);
        } catch (ServletException e) {
            LOGGER.info("ERROR : ServletException {}", e.getMessage());
            LOGGER.debug("ERROR : ServletException {}", e);
            throw e;
        } catch (IOException e) {
            LOGGER.info("IOException {}", e.getMessage());
            LOGGER.debug("IOException {}", e);
            throw e;
        } catch (EIDASSAMLEngineException e) {
            LOGGER.info("BUSINESS EXCEPTION : Error generating SAMLToken", e.getMessage());
            LOGGER.debug("BUSINESS EXCEPTION : Error generating SAMLToken", e);
            throw new EidasNodeException(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()), e);
        }
    }

    @Override
    public void destroy() {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Destroy of ConnectorOutgoingEidasRequest filter");
    }

    /**
     * Getter for {@link ConnectorOutgoingEidasRequestLogger}
     *
     * @return the instance of the {@link ConnectorOutgoingEidasRequestLogger}
     */
    public ConnectorOutgoingEidasRequestLogger getConnectorOutgoingEidasRequestLoggerImpl() {
        ConnectorOutgoingEidasRequestLogger connectorOutgoingEidasRequestLogger =
                getBean(ConnectorOutgoingEidasRequestLogger.class,
                        NodeBeanNames.CONNECTOR_OUTGOING_EIDAS_REQUEST_LOGGER.toString());

        return connectorOutgoingEidasRequestLogger;
    }

}
