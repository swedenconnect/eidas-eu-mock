/*
 * Copyright (c) 2023 by European Commission
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
package eu.eidas.node.security;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.node.ConnectorBeanNames;
import eu.eidas.node.connector.exceptions.ConnectorError;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.utils.SessionHolder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static eu.eidas.node.BeanProvider.getBean;

public class SecurityRequestFilter extends AbstractSecurityRequest implements Filter {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SecurityRequestFilter.class.getName());
    /**
     * Configured on the web.xml
     * Servlets to which apply this filter
     * Its a kind of interceptor as how was with struts
     */
    private String includedServlets;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info(LoggingMarkerMDC.SYSTEM_EVENT, "Init of SecurityRequestFilter filter");
        this.includedServlets = filterConfig.getInitParameter("includedServlets");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        LOG.trace("Execution Of filter");

        String beanName = ConnectorBeanNames.SECURITY_CONFIG.toString();
        ConfigurationSecurityBean securityBean = getBean(ConfigurationSecurityBean.class, beanName);
        this.setConfigurationSecurityBean(securityBean);

        // Class Name of the Action being invoked
        servletRequest.setCharacterEncoding("UTF-8");
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final String pathInvoked = StringUtils.remove(request.getServletPath(), "/");

        if (!matchIncludedServlets(pathInvoked)) {
            LOG.debug("Not filtered");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // get domain
        String domain = request.getHeader(EIDASValues.REFERER.toString());

        boolean performDomainCheck = !getConfigurationSecurityBean().getBypassValidation();

        if ("cspReportHandler".equals(pathInvoked)) {
            performDomainCheck = false;
        }

        if (performDomainCheck) {
            LOG.debug("Performing domain check");
            if (domain == null) {
                LOG.info(LoggingMarkerMDC.SECURITY_WARNING, "Domain is null");
                final String errorCode = EidasErrors.get(EidasErrorKey.DOMAIN.errorCode(pathInvoked));
                final String errorMsg = EidasErrors.get(EidasErrorKey.DOMAIN.errorMessage(pathInvoked));
                throw new ConnectorError(errorCode, errorMsg);
            }

            domain = domain.substring(domain.indexOf("://") + this.THREE);
            // Validate if URL ends with "/"
            final int indexStr = domain.indexOf('/');
            if (indexStr > 0) {
                domain = domain.substring(0, indexStr);
            }
            // ***CHECK DOMAIN**/
            this.checkDomain(domain, pathInvoked, request);
            // ***CHECK IPS**/

            if (this.getConfigurationSecurityBean().getIpMaxRequests() != -1) {
                this.checkRequest(request.getRemoteAddr(), this.getConfigurationSecurityBean().getIpMaxTime(), this.getConfigurationSecurityBean().getIpMaxRequests(), pathInvoked, this.spIps);
            }

            // ***CHECK SP**/

            if (this.getConfigurationSecurityBean().getSpMaxRequests() != -1) {
                this.checkRequest(domain, this.getConfigurationSecurityBean().getSpMaxTime(), this.getConfigurationSecurityBean().getSpMaxRequests(), pathInvoked, this.spRequests);
            }

        }
        filterChain.doFilter(servletRequest, servletResponse);
        // TODO: FIXME: the SessionHolder constitutes a memory leak since it is not cleaned in a finally block and any exception in doFilter() would prevent removal from the ThreadLocal
        SessionHolder.clear();
    }

    private boolean matchIncludedServlets(String url) {
        if (!StringUtils.isEmpty(url) && !StringUtils.isEmpty(this.includedServlets)) {
            List<String> servlets = Arrays.asList(this.includedServlets.split("\\s*,\\s*"));
            return servlets.contains(url);
        }
        return false;
    }

    @Override
    public void destroy() {
        LOG.info(LoggingMarkerMDC.SYSTEM_EVENT, "Destroy of SecurityRequestFilter filter");
    }
}
