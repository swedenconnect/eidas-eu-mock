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
package eu.eidas.security;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.security.exceptions.SecurityError;
import org.apache.commons.lang.StringUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SecurityRequestFilter implements Filter {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SecurityRequestFilter.class.getName());

    /**
     * The three int value.
     */
    protected static final int THREE = 3;

    /**
     * Map containing the IP addresses of the citizens.
     */
    protected transient ConcurrentHashMap<String, List<Long>> spIps = new ConcurrentHashMap<>();

    /**
     * Map containing the IP addresses from the Service Providers.
     */
    protected transient ConcurrentHashMap<String, List<Long>> spRequests = new ConcurrentHashMap<>();

    /**
     * Configured on the web.xml
     * Servlets to which apply this filter
     * Its a kind of interceptor as how was with struts
     */
    private String includedServlets;


    /**
     * Static variable to get the number of milliseconds (seconds * MILLIS).
     */
    private static final long MILLIS = 1000L;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info(LoggingMarkerMDC.SYSTEM_EVENT, "Init of SecurityRequestFilter filter");
        this.includedServlets = filterConfig.getInitParameter("includedServlets");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ConfigurationSecurityBean springManagedSecurityConfig;
        springManagedSecurityConfig = BeanProvider.getBean(ConfigurationSecurityBean.class);
        LOG.trace("Execution Of filter");

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

        boolean performDomainCheck = !springManagedSecurityConfig.getBypassValidation();

        if ("cspReportHandler".equals(pathInvoked)) {
            performDomainCheck = false;
        }

        if (performDomainCheck) {
            LOG.debug("Performing domain check");
            if (domain == null) {
                LOG.info(LoggingMarkerMDC.SECURITY_WARNING, "Domain is null");
                throw servletSpecificDomainSecurityError(pathInvoked);
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

            if (springManagedSecurityConfig.getIpMaxRequests() != -1) {
                this.checkRequest(request.getRemoteAddr(), springManagedSecurityConfig.getIpMaxTime(), springManagedSecurityConfig.getIpMaxRequests(), pathInvoked, this.spIps);
            }

            // ***CHECK SP**/

            if (springManagedSecurityConfig.getSpMaxRequests() != -1) {
                this.checkRequest(domain, springManagedSecurityConfig.getSpMaxTime(), springManagedSecurityConfig.getSpMaxRequests(), pathInvoked, this.spRequests);
            }

        }
        filterChain.doFilter(servletRequest, servletResponse);
        // TODO: FIXME: the SessionHolder constitutes a memory leak since it is not cleaned in a finally block and any exception in doFilter() would prevent removal from the ThreadLocal
        SessionHolder.clear();
    }

    /**
     * In the "eIDAS-Node Error Codes" document, these error codes have been defined on a per-component basis.
     * This makes the Security module non-agnostic to what is on top.
     *
     * TODO Either IoC or Change the error codes to be generic
     */
    private SecurityError servletSpecificDomainSecurityError(String pathInvoked) {
        if ("ServiceProvider".equals(pathInvoked)) return securityError(EidasErrorKey.CONNECTOR_DOMAIN);
        return securityError(EidasErrorKey.CONNECTOR_DOMAIN); // default
    }

    private SecurityError servletSpecificRequestsSecurityError(String pathInvoked) {
        if ("ColleagueRequest".equals(pathInvoked)) return securityError(EidasErrorKey.REQUESTS_COLLEAGUE_REQUEST);
        return securityError(EidasErrorKey.REQUESTS_COLLEAGUE_REQUEST); // default
    }

    private SecurityError securityError(EidasErrorKey eidasErrorKey) {
        return new SecurityError(
                EidasErrors.get(eidasErrorKey.errorCode()),
                EidasErrors.get(eidasErrorKey.errorMessage()));
    }

    private boolean matchIncludedServlets(String url) {
        if (!StringUtils.isEmpty(url) && !StringUtils.isEmpty(this.includedServlets)) {
            List<String> servlets = Arrays.asList(this.includedServlets.split("\\s*,\\s*"));
            return servlets.contains(url);
        }
        return false;
    }

    /**
     * Validate if for a remote address the threshold for requests within a time
     * span has been reached.
     *
     * @param remoteAddr  The remote address of the incoming request
     * @param maxTime     The time span for receiving an amount of requests
     * @param threshold   The number of requests the same remoteAddr can issue.
     *                    within a time span.
     * @param pathInvoked The name of the class (in case of exception).
     * @param listIP      The list of allowed IP.
     * @see Map
     * @see java.util.ArrayList
     */
    public final void checkRequest(final String remoteAddr, final int maxTime,
                                   final int threshold, final String pathInvoked,
                                   final ConcurrentHashMap<String, List<Long>> listIP) {

        List<Long> knownAddressTimestampList = listIP.get(remoteAddr);
        final long currentTimeMillis = System.currentTimeMillis();
        if (knownAddressTimestampList == null) {
            listIP.put(remoteAddr, Arrays.asList(currentTimeMillis));
        } else {
            List<Long> relevantTimeStamps = knownAddressTimestampList.stream()
                    .filter(t -> !timestampHasExpired(t, currentTimeMillis, maxTime))// any timestamps have expired
                    .collect(Collectors.toList());

            if (relevantTimeStamps.size() + 1 > threshold) {
                LOG.warn(LoggingMarkerMDC.SECURITY_WARNING, "Requests/Minute reached for referer: {}", remoteAddr);
                throw servletSpecificRequestsSecurityError(pathInvoked);
            }
            relevantTimeStamps.add(currentTimeMillis);
            listIP.put(remoteAddr, relevantTimeStamps);
        }
    }

    boolean timestampHasExpired(Long timestamp, Long currentTimeMillis, int maxTimeSeconds) {
        final long limitTime = currentTimeMillis - maxTimeSeconds * MILLIS;
        return timestamp < limitTime;
    }

    /**
     * Checks if the domain is trustworthy.
     *
     * @param requestDomain    The Domain to validate.
     * @param servletClassName The Servlet Class's name that will be invoked.
     * @param request          The {@link HttpServletRequest}.
     * @see HttpServletRequest
     */
    protected final void checkDomain(final String requestDomain,
                                     final String servletClassName, final HttpServletRequest request) {

        ConfigurationSecurityBean springManagedSecurityConfig = BeanProvider.getBean(ConfigurationSecurityBean.class);

        final List<String> ltrustedDomains = new ArrayList<>(Arrays.asList(springManagedSecurityConfig.getTrustedDomains().split(EIDASValues.ATTRIBUTE_SEP.toString())));

        final boolean hasNoTrustedD = ltrustedDomains.size() == 1 && ltrustedDomains.contains(EIDASValues.NONE.toString());

        final boolean areAllTrustedD = ltrustedDomains.size() == 1 && ltrustedDomains.contains(EIDASValues.ALL.toString());

        if (hasNoTrustedD
                || (!ltrustedDomains.contains(requestDomain) && !areAllTrustedD)) {
            LOG.warn(LoggingMarkerMDC.SECURITY_WARNING, "Domain {} is not trusted", requestDomain);
            throw servletSpecificDomainSecurityError(servletClassName);
        }

        // substring starts after 'http(s)://'
        final WebRequest webRequest = new IncomingRequest(request);
        final String spUrl = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SP_URL.toString());
        if (StringUtils.isNotEmpty(spUrl) && !spUrl.substring(spUrl.indexOf("://")
                + THREE).startsWith(requestDomain + '/')) {
            LOG.warn(LoggingMarkerMDC.SECURITY_WARNING, "spUrl {} does not belong to the domain : {}", spUrl, requestDomain);
            throw servletSpecificDomainSecurityError(servletClassName);
        }
    }

    @Override
    public void destroy() {
        LOG.info(LoggingMarkerMDC.SYSTEM_EVENT, "Destroy of SecurityRequestFilter filter");
    }
}
