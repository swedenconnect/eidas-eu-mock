/*
 * Copyright (c) 2021 by European Commission
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
package eu.eidas.node.logging.connector;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.connector.messages.ConnectorIncomingLightRequestLogger;
import eu.eidas.node.utils.PropertiesUtil;
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

import static eu.eidas.auth.commons.EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE;
import static eu.eidas.node.BeanProvider.getBean;
import static eu.eidas.node.NodeBeanNames.CONNECTOR_INCOMING_LIGHT_REQUEST_LOGGER;

/**
 * Filter that intercepts Connector's Incoming HTTP request with the Light Request
 * for logging information related to it.
 */
@WebFilter(filterName = "connectorIncomingLightRequestLoggerFilter", urlPatterns = {"/SpecificConnectorRequest"})
public class ConnectorIncomingLightRequestLoggerFilter implements Filter {

    /**
     * Logger object.
     */
    private static final Logger logger = LoggerFactory.getLogger(ConnectorIncomingLightRequestLoggerFilter.class.getName());

    private final Logger messageLogger = LoggerFactory.getLogger(
            String.format("%s_%s", EIDAS_PACKAGE_REQUEST_LOGGER_VALUE.toString(),
                                   ConnectorIncomingLightRequestLoggerFilter.class.getSimpleName()));

    /**
     * Preparing the logging of the incoming Light Request received from Specific Connector
     *
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig fConfig) {
        logger.info(LoggingMarkerMDC.SYSTEM_EVENT, "Init of LightRequestIncomingLogger filter");
    }

    /**
     * Log the incoming Light Request received from Specific Connector
     *
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        servletRequest.setAttribute(NodeParameterNames.CONTACT_EMAIL.toString(), PropertiesUtil.getProperty(EidasParameterKeys.CONNECTOR_CONTACT_SUPPORT.toString()));
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            if (logger.isTraceEnabled()) {
                logger.trace(String.format("LightRequestIncomingLogger FILTER for %s", httpServletRequest.getServletPath()));
            }
            getLightRequestIncomingLoggerImpl().logMessage(messageLogger, httpServletRequest);
            getLightRequestIncomingLoggerImpl().logFullMessage(httpServletRequest);
            filterChain.doFilter(httpServletRequest, servletResponse);
        } catch (ServletException e) {
            logger.info("ERROR : ServletException {}", e.getMessage());
            logger.debug("ERROR : ServletException ", e);
            throw e;
        } catch (IOException e) {
            logger.info("IOException {}", e.getMessage());
            logger.debug("IOException ", e);
            throw e;
        }
    }

    @Override
    public void destroy() {
        logger.info(LoggingMarkerMDC.SYSTEM_EVENT, "Destroy of LightRequestIncomingLogger filter");
    }

    private ConnectorIncomingLightRequestLogger getLightRequestIncomingLoggerImpl() {
        return getBean(ConnectorIncomingLightRequestLogger.class, CONNECTOR_INCOMING_LIGHT_REQUEST_LOGGER.toString());
    }

}
