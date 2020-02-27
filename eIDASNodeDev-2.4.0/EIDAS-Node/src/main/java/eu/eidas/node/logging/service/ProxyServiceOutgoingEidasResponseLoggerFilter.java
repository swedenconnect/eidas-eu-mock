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
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.service.messages.ProxyServiceOutgoingEidasResponseLogger;
import eu.eidas.node.utils.EidasNodeErrorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
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
import static eu.eidas.node.NodeBeanNames.PROXY_SERVICE_OUTGOING_SAML_RESPONSE_LOGGER;

/**
 * Filter that intercepts Proxy-Service's Outgoing HTTP request with the eIDAS Response
 * for logging information related to it.
 */
@WebFilter(filterName = "ProxyServiceOutgoingEidasResponseLoggerFilter",
        urlPatterns = {"/internal/connectorRedirect.jsp", "/presentSamlResponseError.jsp",
                       "/InternalExceptionHandler"},
        dispatcherTypes = {DispatcherType.FORWARD, DispatcherType.ERROR}
)
public class ProxyServiceOutgoingEidasResponseLoggerFilter implements Filter {

    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServiceOutgoingEidasResponseLoggerFilter.class.getName());

    private final Logger messageLogger = LoggerFactory.getLogger(
            String.format("%s_%s", EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE.toString(),
                    ProxyServiceOutgoingEidasResponseLoggerFilter.class.getSimpleName()));

    /**
     * Preparing logging of the outgoing Saml Response sent to Eidas Connector.
     *
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Init of ProxyServiceOutgoingSamlResponseLogger filter");
    }

    /**
     * Log the outgoing Saml Response sent to Eidas Connector.
     *
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            filterChain.doFilter(httpServletRequest, servletResponse);
            if (httpServletRequest.getRequestURI().endsWith("connectorRedirect.jsp") ||
                    httpServletRequest.getRequestURI().endsWith("presentSamlResponseError.jsp")) {

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(String.format("ProxyServiceOutgoingSamlResponseLogger FILTER for %s", httpServletRequest.getServletPath()));
                }

                getProxyServiceOutgoingSamlResponseLoggerImpl().logMessage(messageLogger, httpServletRequest);
            }

        } catch (ServletException e) {
            LOGGER.info("ERROR : ServletException {}", e.getMessage());
            LOGGER.debug("ERROR : ServletException ", e);
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
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Destroy of ProxyServiceOutgoingSamlResponseLogger filter");
    }

    /**
     * Getter for {@link ProxyServiceOutgoingEidasResponseLogger}
     *
     * @return the instance of the {@link ProxyServiceOutgoingEidasResponseLogger}
     */
    private ProxyServiceOutgoingEidasResponseLogger getProxyServiceOutgoingSamlResponseLoggerImpl() {
        return getBean(ProxyServiceOutgoingEidasResponseLogger.class, PROXY_SERVICE_OUTGOING_SAML_RESPONSE_LOGGER.toString());
    }

}
