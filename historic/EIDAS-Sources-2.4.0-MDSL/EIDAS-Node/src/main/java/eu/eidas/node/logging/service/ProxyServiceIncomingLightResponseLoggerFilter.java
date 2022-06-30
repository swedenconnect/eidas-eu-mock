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
package eu.eidas.node.logging.service;

import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.service.messages.ProxyServiceIncomingLightResponseLogger;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
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

import static eu.eidas.auth.commons.EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE;
import static eu.eidas.node.BeanProvider.getBean;
import static eu.eidas.node.NodeBeanNames.PROXY_SERVICE_INCOMING_LIGHT_RESPONSE_LOGGER;

/**
 * Filter that intercepts Proxy-Service's Incoming HTTP request with the Light Response
 * for logging information related to it.
 */
@WebFilter(filterName = "ProxyServiceIncomingLightResponseLoggerFilter", urlPatterns = {"/SpecificProxyServiceResponse"})
public class ProxyServiceIncomingLightResponseLoggerFilter implements Filter {
    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServiceIncomingLightResponseLoggerFilter.class.getName());

    private final Logger messageLogger = LoggerFactory.getLogger(
            String.format("%s_%s", EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE.toString(),
                    ProxyServiceIncomingLightResponseLoggerFilter.class.getSimpleName()));

    /**
     * Preparing logging of the incoming Light Response received from Specific Proxy Service.
     *
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Init of LightResponseIncomingLogger filter");
    }

    /**
     * Log the incoming Light Response received from Specific Proxy Service.
     *
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("LightResponseIncomingLogger FILTER for %s", httpServletRequest.getServletPath()));
            }

            getLightResponseIncomingLoggerImpl().logMessage(messageLogger, httpServletRequest);

            filterChain.doFilter(httpServletRequest, servletResponse);
        } catch (ServletException e) {
            LOGGER.info("ERROR : ServletException {}", e.getMessage());
            LOGGER.debug("ERROR : ServletException ", e);
            throw e;
        } catch (IOException e) {
            LOGGER.info("IOException {}", e.getMessage());
            LOGGER.debug("IOException ", e);
            throw e;
        } catch (SpecificCommunicationException e) {
            LOGGER.info("ERROR : SpecificCommunicationException {}", e.getMessage());
            LOGGER.debug("ERROR : SpecificCommunicationException ", e);
        }
    }

    @Override
    public void destroy() {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Destroy of LightResponseIncomingLogger filter");
    }

    /**
     * Getter for {@link ProxyServiceIncomingLightResponseLogger}
     *
     * @return the instance of the {@link ProxyServiceIncomingLightResponseLogger}
     */
    private ProxyServiceIncomingLightResponseLogger getLightResponseIncomingLoggerImpl() {
        return getBean(ProxyServiceIncomingLightResponseLogger.class, PROXY_SERVICE_INCOMING_LIGHT_RESPONSE_LOGGER.toString());
    }

}
