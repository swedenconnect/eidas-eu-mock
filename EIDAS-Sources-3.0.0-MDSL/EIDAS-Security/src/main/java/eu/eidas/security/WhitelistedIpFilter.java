/*
 * Copyright (c) 2025 by European Commission
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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servlet filter that restricts access to telemetry endpoints based on a whitelist of IP addresses.
 * <p>
 * Allows the request if the client IP is whitelisted, otherwise responds with
 * {@link HttpServletResponse#SC_NOT_FOUND}.
 */
public class WhitelistedIpFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(WhitelistedIpFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Set<String> whitelistedIps;
        Set<String> allowedAddresses = new HashSet<>();
        ConfigurationSecurityBean springManagedSecurityConfig = ContextLoader.getCurrentWebApplicationContext().getBean(ConfigurationSecurityBean.class);
        whitelistedIps = springManagedSecurityConfig.getWhitelistedIps(WhiteListType.TELEMETRY);
        List<InetAddress> addresses = new ArrayList<>();
        for (String whiteListedIp : whitelistedIps) {
            try{
                addresses.addAll(List.of(InetAddress.getAllByName(whiteListedIp)));
            } catch (UnknownHostException e) {
                logger.warn("Could not resolve IP or Domain name: " + whiteListedIp);
            }
        }

        for (InetAddress inetAddress : addresses) {
            allowedAddresses.add(inetAddress.getHostAddress());
        }

        if (whitelistedIps.contains(servletRequest.getRemoteAddr()) || allowedAddresses.contains(servletRequest.getRemoteAddr())) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
