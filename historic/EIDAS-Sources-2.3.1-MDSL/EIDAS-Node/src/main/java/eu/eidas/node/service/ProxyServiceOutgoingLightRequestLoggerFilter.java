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
package eu.eidas.node.service;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.service.messages.ProxyServiceOutgoingLightRequestLogger;
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

import static eu.eidas.node.BeanProvider.getBean;

/**
 * Filter that intercepts Proxy-Service's Outgoing HTTP request with the Light Request
 * for logging information related to it.
 */
@WebFilter(filterName = "ProxyServiceOutgoingLightRequestLoggerFilter", urlPatterns = {"/ColleagueRequest"})
public class ProxyServiceOutgoingLightRequestLoggerFilter implements Filter {

    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServiceOutgoingLightRequestLoggerFilter.class.getName());

    private final Logger logger = LoggerFactory.getLogger(
            EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE.toString() + "_" + ProxyServiceOutgoingLightRequestLoggerFilter.class.getSimpleName());


    /**
     * Preparing the logging of the outgoing Light Request sent to Specific Proxy Service.
     *
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Init of LightRequestOutgoingLogger filter");
    }

    /**
     * Log the outgoing Light Request sent to Specific Proxy Service.
     *
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

            LOGGER.trace("LightRequestOutgoingLogger FILTER for " + httpServletRequest.getServletPath());

            filterChain.doFilter(httpServletRequest, servletResponse);

            getLightRequestOutgoingLoggerImpl().logMessage(logger, httpServletRequest);
        } catch (ServletException e) {
            LOGGER.info("ERROR : ServletException {}", e.getMessage());
            LOGGER.debug("ERROR : ServletException {}", e);
            throw e;
        } catch (IOException e) {
            LOGGER.info("IOException {}", e.getMessage());
            LOGGER.debug("IOException {}", e);
            throw e;
        } catch (SpecificCommunicationException e) {
            LOGGER.info("ERROR : SpecificCommunicationException {}", e.getMessage());
            LOGGER.debug("ERROR : SpecificCommunicationException {}", e);
        }
    }

    @Override
    public void destroy() {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Destroy of LightRequestOutgoingLogger filter");
    }

    /**
     * Getter for {@link ProxyServiceOutgoingLightRequestLogger}
     *
     * @return the instance of the {@link ProxyServiceOutgoingLightRequestLogger}
     */
    public ProxyServiceOutgoingLightRequestLogger getLightRequestOutgoingLoggerImpl() {
        ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger =
                getBean(ProxyServiceOutgoingLightRequestLogger.class,
                        NodeBeanNames.PROXY_SERVICE_OUTGOING_LIGHT_REQUEST_LOGGER.toString());

        return proxyServiceOutgoingLightRequestLogger;
    }

}