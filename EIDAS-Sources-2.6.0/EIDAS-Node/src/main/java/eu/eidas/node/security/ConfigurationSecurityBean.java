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

import org.springframework.context.MessageSource;

/**
 * Contains the security parameters configuration.
 * @author vanegdi
 */
public class ConfigurationSecurityBean {
    private boolean isContentSecurityPolicyActive;

    /** Host name used for the CSP directive report-uri */
   private String cspReportingUri;

    private boolean includeXXssProtection;

    private boolean includeXContentTypeOptions;

    private boolean includeXFrameOptions;

    private boolean includeHSTS;

    /**
     * Include CSP browser support test script into JSPs
     */
    private boolean includeMozillaDirectives;
    /**
     * Max requests per citizen.
     */
    private int ipMaxRequests;

    /**
     * Max requests per SP.
     */
    private int spMaxRequests;

    /**
     * Citizen timer threshold.
     */
    private int ipMaxTime;

    /**
     * SP timer threshold.
     */
    private int spMaxTime;

    /**
     * String containing all the trusted domains.
     */
    private String trustedDomains;

    /**
     * Method of validating the SP by domain.
     */
    private String validationMethod;

    private MessageSource messageSource;

    private boolean bypassValidation;

    public boolean getIsContentSecurityPolicyActive() {
        return isContentSecurityPolicyActive;
    }

    public void setIsContentSecurityPolicyActive(boolean isContentSecurityPolicyActive) {
        this.isContentSecurityPolicyActive = isContentSecurityPolicyActive;
    }

    public String getCspReportingUri() {
        return cspReportingUri;
    }

    public void setCspReportingUri(String cspReportingUri) {
        this.cspReportingUri = cspReportingUri;
    }

    public boolean isIncludeXXssProtection() {
        return includeXXssProtection;
    }

    public void setIncludeXXssProtection(boolean includeXXssProtection) {
        this.includeXXssProtection = includeXXssProtection;
    }

    public boolean isIncludeXContentTypeOptions() {
        return includeXContentTypeOptions;
    }

    public void setIncludeXContentTypeOptions(boolean includeXContentTypeOptions) {
        this.includeXContentTypeOptions = includeXContentTypeOptions;
    }

    public boolean isIncludeXFrameOptions() {
        return includeXFrameOptions;
    }

    public void setIncludeXFrameOptions(boolean includeXFrameOptions) {
        this.includeXFrameOptions = includeXFrameOptions;
    }

    public boolean isIncludeHSTS() {
        return includeHSTS;
    }

    public void setIncludeHSTS(boolean includeHSTS) {
        this.includeHSTS = includeHSTS;
    }

    public boolean isIncludeMozillaDirectives() {
        return includeMozillaDirectives;
    }

    public void setIncludeMozillaDirectives(boolean includeMozillaDirectives) {
        this.includeMozillaDirectives = includeMozillaDirectives;
    }

    public int getIpMaxRequests() {
        return ipMaxRequests;
    }

    public void setIpMaxRequests(int ipMaxRequests) {
        this.ipMaxRequests = ipMaxRequests;
    }

    public int getSpMaxRequests() {
        return spMaxRequests;
    }

    public void setSpMaxRequests(int spMaxRequests) {
        this.spMaxRequests = spMaxRequests;
    }

    public int getIpMaxTime() {
        return ipMaxTime;
    }

    public void setIpMaxTime(int ipMaxTime) {
        this.ipMaxTime = ipMaxTime;
    }

    public int getSpMaxTime() {
        return spMaxTime;
    }

    public void setSpMaxTime(int spMaxTime) {
        this.spMaxTime = spMaxTime;
    }

    public String getTrustedDomains() {
        return trustedDomains;
    }

    public void setTrustedDomains(String trustedDomains) {
        this.trustedDomains = trustedDomains;
    }

    public String getValidationMethod() {
        return validationMethod;
    }

    public void setValidationMethod(String validationMethod) {
        this.validationMethod = validationMethod;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setBypassValidation(boolean bypassValidation) {
        this.bypassValidation = bypassValidation;
    }

    public boolean getBypassValidation() {
        return bypassValidation;
    }
}
