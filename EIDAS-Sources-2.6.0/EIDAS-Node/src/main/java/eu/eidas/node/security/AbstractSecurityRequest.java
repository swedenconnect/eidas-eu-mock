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
package eu.eidas.node.security;


import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.node.logging.LoggingMarkerMDC;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Utility class used to define the markers used for logging.
 * @author vanegdi
 * @since 1.2.2
 */
public abstract class AbstractSecurityRequest {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSecurityRequest.class.getName());
    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = -4120777529787993902L;

    /**
     * Static variable to get the number of milliseconds (seconds * MILLIS).
     */
    private static final long MILLIS = 1000L;

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

    //Contains the security configuration
    private ConfigurationSecurityBean configurationSecurityBean;

    /**
     * Validate if for a remote address the threshold for requests within a time
     * span has been reached.
     *
     * @param remoteAddr      The remote address of the incoming request
     * @param maxTime         The time span for receiving an amount of requests
     * @param threshold       The number of requests the same remoteAddr can issue.
     *                        within a time span.
     * @param pathInvoked The name of the class (in case of exception).
     * @param listIP          The list of allowed IP.
     * @see Map
     * @see java.util.ArrayList
     */
    protected final void checkRequest(final String remoteAddr, final int maxTime,
                              final int threshold, final String pathInvoked,
                              final ConcurrentHashMap<String, List<Long>> listIP) {
        final String errorMsg = EidasErrors.get(EidasErrorKey.REQUESTS.errorMessage(pathInvoked));
        final String errorCode = EidasErrors.get(EidasErrorKey.REQUESTS.errorCode(pathInvoked));

        List<Long> knownAddressTimestampList = listIP.get(remoteAddr);
        final long currentTimeMillis = System.currentTimeMillis();
        if(knownAddressTimestampList == null) {
            listIP.put(remoteAddr, Arrays.asList(currentTimeMillis));
        } else {
            List<Long> relevantTimeStamps = knownAddressTimestampList.stream()
                .filter(t -> !timestampHasExpired(t, currentTimeMillis, maxTime))// any timestamps have expired
                .collect(Collectors.toList());

            if (relevantTimeStamps.size() + 1 > threshold) {
                LOG.warn(LoggingMarkerMDC.SECURITY_WARNING, "Requests/Minute reached for referer: {}", remoteAddr);
                throw new SecurityEIDASException(errorCode, errorMsg);
            }
            relevantTimeStamps.add(currentTimeMillis);
            listIP.put(remoteAddr, relevantTimeStamps);
        }
    }

    boolean timestampHasExpired(Long timestamp, Long currentTimeMillis, int maxTimeSeconds) {
        final long limitTime = currentTimeMillis - maxTimeSeconds * AbstractSecurityRequest.MILLIS;
        return timestamp < limitTime;
    }

    /**
     * Checks if the domain is trustworthy.
     *
     * @param requestDomain   The Domain to validate.
     * @param servletClassName The Servlet Class's name that will be invoked.
     * @param request         The {@link HttpServletRequest}.
     * @see HttpServletRequest
     */
    protected final void checkDomain(final String requestDomain,
                             final String servletClassName, final HttpServletRequest request) {

        final String errorCode = EidasErrors.get(EidasErrorKey.DOMAIN.errorCode(servletClassName));
        final String errorMsg = EidasErrors.get(EidasErrorKey.DOMAIN.errorMessage(servletClassName));

        final List<String> ltrustedDomains = new ArrayList<>(Arrays.asList(configurationSecurityBean.getTrustedDomains().split(EIDASValues.ATTRIBUTE_SEP.toString())));

        final boolean hasNoTrustedD = ltrustedDomains.size() == 1 && ltrustedDomains.contains(EIDASValues.NONE.toString());

        final boolean areAllTrustedD = ltrustedDomains.size() == 1 && ltrustedDomains.contains(EIDASValues.ALL.toString());

        if (hasNoTrustedD
                || (!ltrustedDomains.contains(requestDomain) && !areAllTrustedD)) {
            LOG.warn(LoggingMarkerMDC.SECURITY_WARNING,"Domain {} is not trusted", requestDomain);
            throw new SecurityEIDASException(errorCode, errorMsg);
        }

        // substring starts after 'http(s)://'
        final WebRequest webRequest = new IncomingRequest(request);
        final String spUrl = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SP_URL.toString());
        if (StringUtils.isNotEmpty(spUrl) && !spUrl.substring(spUrl.indexOf("://")
                + AbstractSecurityRequest.THREE).startsWith(requestDomain + '/')) {
            LOG.warn(LoggingMarkerMDC.SECURITY_WARNING, "spUrl {} does not belong to the domain : {}", spUrl, requestDomain);
            throw new SecurityEIDASException(errorCode, errorMsg);
        }
    }

    protected final ConfigurationSecurityBean getConfigurationSecurityBean() {
        return configurationSecurityBean;
    }

    public final void setConfigurationSecurityBean(ConfigurationSecurityBean configurationSecurityBean) {
        this.configurationSecurityBean = configurationSecurityBean;
    }
}
