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

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.service.messages.ProxyServiceIncomingEidasRequestLogger;
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
import static eu.eidas.node.NodeBeanNames.PROXY_SERVICE_INCOMING_EIDAS_REQUEST_LOGGER;

/**
 * Filter that intercepts Proxy-Service's Incoming HTTP request with the eIDAS Request
 * for logging information related to it.
 */
@WebFilter(filterName = "ProxyServiceIncomingEidasRequestLoggerFilter", urlPatterns = {"/ColleagueRequest"})
public class ProxyServiceIncomingEidasRequestLoggerFilter implements Filter {

    /**
     * Logger object.
     */
    private static final Logger logger = LoggerFactory.getLogger(ProxyServiceIncomingEidasRequestLoggerFilter.class.getName());

    private final Logger messageLogger = LoggerFactory.getLogger(
            String.format("%s_%s", EIDAS_PACKAGE_REQUEST_LOGGER_VALUE.toString(),
                    ProxyServiceIncomingEidasRequestLoggerFilter.class.getSimpleName()));

    /**
     * Preparing the logging point of the incoming Saml Request received from Eidas Connector.
     *
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) {
        logger.info(LoggingMarkerMDC.SYSTEM_EVENT, "Init of ProxyServiceIncomingEidasRequest filter");
    }

    /**
     * Log the incoming Saml Request received from Eidas Connector.
     *
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            if (logger.isTraceEnabled()) {
                logger.trace(String.format("ProxyServiceIncomingEidasRequest FILTER for %s", httpServletRequest.getServletPath()));
            }

            getProxyServiceIncomingEidasRequestLoggerImpl().logMessage(messageLogger, httpServletRequest);

            filterChain.doFilter(httpServletRequest, servletResponse);
        } catch (ServletException e) {
            logger.info("ERROR : ServletException {}", e.getMessage());
            logger.debug("ERROR : ServletException ", e);
            throw e;
        } catch (IOException e) {
            logger.info("IOException {}", e.getMessage());
            logger.debug("IOException ", e);
            throw e;
        } catch (EIDASSAMLEngineException e) {
            logger.info("BUSINESS EXCEPTION : Error generating SAMLToken");
            logger.debug("BUSINESS EXCEPTION : Error generating SAMLToken", e);
            throw new EidasNodeException(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()), e);
        }
    }

    @Override
    public void destroy() {
        logger.info(LoggingMarkerMDC.SYSTEM_EVENT, "Destroy of ProxyServiceIncomingEidasRequest filter");
    }

    /**
     * Getter for {@link ProxyServiceIncomingEidasRequestLogger}
     *
     * @return the instance of the {@link ProxyServiceIncomingEidasRequestLogger}
     */
    private ProxyServiceIncomingEidasRequestLogger getProxyServiceIncomingEidasRequestLoggerImpl() {
        return getBean(ProxyServiceIncomingEidasRequestLogger.class, PROXY_SERVICE_INCOMING_EIDAS_REQUEST_LOGGER.toString());
    }

}
