/*
 * Copyright (c) 2020 by European Commission
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

package eu.eidas.auth.engine.xml.opensaml;

import org.apache.commons.lang.StringUtils;

/**
 * Enum class to represent all the possible value of SAML consent in SAML V2.0
 */
public enum SAMLConsent {

    UNSPECIFIED ("urn:oasis:names:tc:SAML:2.0:consent:unspecified"),
    OBTAINED ("urn:oasis:names:tc:SAML:2.0:consent:obtained"),
    PRIOR ("urn:oasis:names:tc:SAML:2.0:consent:prior"),
    CURRENT_IMPLICIT ("urn:oasis:names:tc:SAML:2.0:consent:current-implicit"),
    CURRENT_EXPLICIT ("urn:oasis:names:tc:SAML:2.0:consent:current-explicit"),
    UNAVAILABLE ("urn:oasis:names:tc:SAML:2.0:consent:unavailable"),
    INAPPLICABLE ("urn:oasis:names:tc:SAML:2.0:consent:inapplicable")
    ;

    private String value;

    SAMLConsent(String value) {
        this.value = value;
    }

    /**
     * Get a SAMLConsent base on a given value.
     *
     * @param value the SAML consent value.
     * @return The SAML consent matching with the given value, or null if the given value is null or empty, or
     *          the {@link SAMLConsent#UNSPECIFIED} is no match found.
     */
    public static SAMLConsent fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        for (SAMLConsent consent: values()) {
            if (consent.getValue().equalsIgnoreCase(value)) {
                return consent;
            }
        }
        return null;
    }

    public String getValue() {
        return this.value;
    }

}
