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

import eu.eidas.logging.BeanProvider;
import eu.eidas.logging.IFullMessageLogger;
import eu.eidas.logging.LoggingMarkerMDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Filter that intercepts Proxy-Service's Incoming HTTP request with the eIDAS Request
 * for logging information related to it.
 */
public abstract class MessageLoggerFilter implements Filter {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MessageLoggerFilter.class.getName());
    private String iFullMessageLoggerBeanName;

    protected IFullMessageLogger fullMessageLogger;

    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Init of ProxyServiceIncomingEidasRequest filter");
        iFullMessageLoggerBeanName = filterConfig.getInitParameter("IFullMessageLogger");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        fullMessageLogger = BeanProvider.getBean(IFullMessageLogger.class, iFullMessageLoggerBeanName);
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            doOrderOfLogging(httpServletRequest, servletResponse, filterChain);
        } catch (ServletException e) {
            LOGGER.info("ERROR : ServletException {}", e.getMessage());
            LOGGER.debug("ERROR : ServletException ", e);
            throw e;
        } catch (IOException e) {
            LOGGER.info("IOException {}", e.getMessage());
            LOGGER.debug("IOException ", e);
            throw e;
        }
    }

    protected abstract void doOrderOfLogging(
            HttpServletRequest httpServletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws ServletException, IOException;

    @Override
    public void destroy() {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Destroy of ProxyServiceIncomingEidasRequest filter");
    }
}
