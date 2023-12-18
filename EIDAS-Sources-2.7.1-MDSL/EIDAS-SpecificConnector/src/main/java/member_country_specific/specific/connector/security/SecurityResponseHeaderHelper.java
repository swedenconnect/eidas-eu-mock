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
package member_country_specific.specific.connector.security;

import eu.eidas.auth.commons.EidasParameterKeys;
import member_country_specific.specific.connector.logging.LoggingMarkerMDC;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static member_country_specific.specific.connector.SpecificConnectorApplicationContextProvider.getApplicationContext;
import static member_country_specific.specific.connector.SpecificConnectorBeanNames.SECURITY_CONFIG;

/**
 * This filter set CSP policies using all HTTP headers defined into W3C specification.<br>
 * <p>
 * Purposes :
 * <p>
 * XSS countermeasures :
 * 1. Content Security Policy (CSP)
 * Sample generated : X-Content-Security-Policy:default-src 'none'; object-src 'self'; style-src 'self'; img-src 'self'; connect-src 'self';script-src 'self'; report-uri http://node:8080/SpecificConnector/cspReportHandler
 * - X-Content-Security-Policy for backward compatibility
 * - X-WebKit-CSP for backward compatibility
 * - Content-Security-Policy
 * - Report handler logging all the CSP violations
 * 2. X-XSS-Protection header
 * 3. X-Content-Type-Options: nosniff
 * Click-jacking countermeasures :
 * X-Frame-Options header
 */
public class SecurityResponseHeaderHelper {

    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityResponseHeaderHelper.class.getName());

    protected static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";
    protected static final String X_CONTENT_SECURITY_POLICY_HEADER = "X-Content-Security-Policy";
    protected static final String X_WEB_KIT_CSP_HEADER = "X-WebKit-CSP";
    protected static final String X_XSS_PROTECTION_HEADER = "X-XSS-Protection";
    protected static final String X_CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options";
    protected static final String X_FRAME_OPTIONS_HEADER = "X-Frame-Options";
    protected static final String STRICT_TRANSPORT_SECURITY_HEADER = "Strict-Transport-Security";
    protected static final String X_ROBOTS_TAG = "X-Robots-Tag";

    protected static final String SELF_ORIGIN_LOCATION_REF = "'self'";
    protected static final String X_FRAME_OPTIONS_SAME_ORIGIN = "SAMEORIGIN";
    protected static final String X_CONTENT_TYPE_OPTIONS_NO_SNIFF = "nosniff";
    protected static final String X_XSS_PROTECTION_MODE_BLOCK = "1; mode=block";
    protected static final String STRICT_TRANSPORT_SECURITY = "max-age=600000; includeSubdomains"; // 10 minutes
    protected static final String X_ROBOTS_TAG_NOINDEX_NOFOLLOW = "noindex, nofollow";

    protected static final String HTTP_1_1_CACHE_CONTROL = "Cache-Control";
    protected static final String HTTP_1_1_CACHE_CONTROL_NOCACHE = "no-cache, no-store, max-age=0, must-revalidate, private";
    protected static final String HTTP_1_0_PRAGMA = "Pragma";
    protected static final String HTTP_1_0_PRAGMA_NOCACHE = "no-cache";
    protected static final String PROXIES_EXPIRES = "Expires";
    protected static final String PROXIES_EXPIRES_0 = "0";

    /**
     *  Contains the security configuration
     */
    protected ConfigurationSecurityBean configurationSecurityBean;

    /**
     * Configuration member to specify if web app use web fonts
     */
    protected static final boolean APP_USE_WEBFONTS = true;
    /**
     * Configuration member to specify if web app use videos or audios
     */
    protected static final boolean APP_USE_AUDIOS_OR_VIDEOS = false;

    /**
     * List CSP HTTP Headers
     */
    protected List<String> cspHeaders = new ArrayList<String>();

    /**
     * Collection of CSP polcies that will be applied
     */
    protected final String policies;


    /**
     * Used to prepare (one time for all) set of CSP policies that will be applied on each HTTP response.
     */
    public SecurityResponseHeaderHelper() {
        LOGGER.debug("Init of contentSecurityPolicy HELPER");

        String beanName = SECURITY_CONFIG.toString();
        configurationSecurityBean = (ConfigurationSecurityBean) getApplicationContext()
                .getBean(beanName);

        // Define list of CSP HTTP Headers : used for reverse compatibility
        this.cspHeaders.add(CONTENT_SECURITY_POLICY_HEADER);
        this.cspHeaders.add(X_CONTENT_SECURITY_POLICY_HEADER);
        this.cspHeaders.add(X_WEB_KIT_CSP_HEADER);

        this.policies = defineCspPolicies();
        LOGGER.trace("contentSecurityPolicy Config - ContentSecurityPolicyActive {} - includeXSSProtection {} - includeHSTS {} - XContentTypeOptions {} - includeMozillaDirectives {} - includeXFrameOptions {}",
                configurationSecurityBean.getIsContentSecurityPolicyActive(),
                configurationSecurityBean.isIncludeXXssProtection(),
                configurationSecurityBean.isIncludeHSTS(),
                configurationSecurityBean.isIncludeXContentTypeOptions(),
                configurationSecurityBean.isIncludeMozillaDirectives(),
                configurationSecurityBean.isIncludeXFrameOptions());
    }

    /**
     * Loading policies for Frame and Sandboxing will be dynamically defined : We need to know if context use Frame
     */
    private String defineCspPolicies() {
        final List<String> cspPolicies = new ArrayList<String>();

        // --Disable default source in order to avoid browser fallback loading using 'default-src' locations
        cspPolicies.add("default-src 'none'");

        // --Define loading policies for Plugins
        cspPolicies.add("object-src " + SELF_ORIGIN_LOCATION_REF);
        // --Define loading policies for Styles (CSS)
        cspPolicies.add("style-src " + SELF_ORIGIN_LOCATION_REF);
        // --Define loading policies for Images
        cspPolicies.add("img-src " + SELF_ORIGIN_LOCATION_REF);
        // --Define loading policies for Audios/Videos
        if (APP_USE_AUDIOS_OR_VIDEOS) {
            cspPolicies.add("media-src " + SELF_ORIGIN_LOCATION_REF);
        }
        // --Define loading policies for Fonts
        if (APP_USE_WEBFONTS) {
            cspPolicies.add("font-src " + SELF_ORIGIN_LOCATION_REF);
        }
        // --Define loading policies for Connection
        cspPolicies.add("connect-src " + SELF_ORIGIN_LOCATION_REF);

        // Target formating
        return cspPolicies.toString().replaceAll("(\\[|\\])", "").replaceAll(",", ";").trim();
    }

    /**
     * Method used to process the content security policy header
     *
     * @param httpRequest  the instance of {@link HttpServletRequest}
     * @param httpResponse the instance of {@link HttpServletResponse}
     */
    protected void processContentSecurityPolicy(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException {
        if (!StringUtils.isEmpty(httpRequest.getRemoteHost())) {
            MDC.put(LoggingMarkerMDC.MDC_REMOTE_HOST, httpRequest.getRemoteHost());
        }
        /* Add CSP policies to HTTP response */
        StringBuilder policiesBuilder = new StringBuilder(this.policies);

        // --Made available script nonce in view app layer
        final String scriptNonce = EidasParameterKeys.CSP_SCRIPT_NONCE.toString();
        if (null == httpRequest.getAttribute(scriptNonce)) {
            try {
                httpRequest.setAttribute(scriptNonce, ScriptNonce.getNonce());
            } catch (NoSuchAlgorithmException e) {
                throw new ServletException(e);
            }
        }
        policiesBuilder.append(";").append("script-src ").append("'nonce-" + httpRequest.getAttribute(scriptNonce) + "'");

        final String cspReportingUri = configurationSecurityBean.getCspReportingUri();
        if (configurationSecurityBean.getIsContentSecurityPolicyActive() && StringUtils.isNotBlank(cspReportingUri)) {
            policiesBuilder.append(";").append("report-uri ").append(cspReportingUri);
            httpResponse.setHeader(
                    "Report-To",
                    "{ " +
                            "\"group\": \"csp-group\", " +
                            "\"max_age\": 10886400, " +
                            "\"endpoints\": [ " +
                            "{ \"url\": \"" + cspReportingUri + "\" }" +
                            " ]" +
                            " }");
        }

        policiesBuilder.append(";").append("frame-ancestors 'none'");

        // Add policies to all HTTP headers
        for (String header : this.cspHeaders) {
            httpResponse.setHeader(header, policiesBuilder.toString());
            LOGGER.trace("Adding policy to header - " + policiesBuilder);
        }
    }

    public void populateResponseHeader(ServletRequest request, ServletResponse response) throws ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (configurationSecurityBean.getIsContentSecurityPolicyActive()) {
            processContentSecurityPolicy(httpRequest, httpResponse);
        }

        if (configurationSecurityBean.isIncludeXXssProtection()) {
            httpResponse.setHeader(X_XSS_PROTECTION_HEADER, X_XSS_PROTECTION_MODE_BLOCK);
        }

        if (configurationSecurityBean.isIncludeXContentTypeOptions()) {
            httpResponse.setHeader(X_CONTENT_TYPE_OPTIONS_HEADER, X_CONTENT_TYPE_OPTIONS_NO_SNIFF);
        }

        if (configurationSecurityBean.isIncludeXFrameOptions()) {
            httpResponse.setHeader(X_FRAME_OPTIONS_HEADER, X_FRAME_OPTIONS_SAME_ORIGIN);
        }

        if (configurationSecurityBean.isIncludeHSTS()) {
            httpResponse.setHeader(STRICT_TRANSPORT_SECURITY_HEADER, STRICT_TRANSPORT_SECURITY);
        }

        httpResponse.setHeader(HTTP_1_1_CACHE_CONTROL, HTTP_1_1_CACHE_CONTROL_NOCACHE); // HTTP 1.1.
        httpResponse.setHeader(HTTP_1_0_PRAGMA, HTTP_1_0_PRAGMA_NOCACHE); // HTTP 1.0.
        httpResponse.setHeader(PROXIES_EXPIRES, PROXIES_EXPIRES_0); // Proxies.
        httpResponse.setHeader(X_ROBOTS_TAG, X_ROBOTS_TAG_NOINDEX_NOFOLLOW); // disallow indexing

        if (response instanceof ExtendedServletResponseWrapper) {
            ((ExtendedServletResponseWrapper) response).setCSPHeaders(true);
        }
    }

}
