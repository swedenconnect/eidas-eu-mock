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
 * limitations under the Licence.
 *
 */
package eu.eidas.auth.commons;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This enum class contains the SAML Token Sub Status Code.
 */
public enum EIDASSubStatusCode {

    /**
     * URI for AuthnFailed status code.
     */
    AUTHN_FAILED_URI("urn:oasis:names:tc:SAML:2.0:status:AuthnFailed"),

    /**
     * URI for InvalidAttrNameOrValue status code.
     */
    INVALID_ATTR_NAME_VALUE_URI("urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue"),

    /**
     * URI for InvalidNameIDPolicy status code.
     */
    INVALID_NAMEID_POLICY_URI("urn:oasis:names:tc:SAML:2.0:status:InvalidNameIDPolicy"),

    /**
     * URI for VersionMismatch status code.
     */
    VERSION_MISMATCH_URI("urn:oasis:names:tc:SAML:2.0:status:VersionMismatch"),

    /**
     * URI for RequestDenied status code.
     */
    REQUEST_DENIED_URI("urn:oasis:names:tc:SAML:2.0:status:RequestDenied"),

    ;

    /**
     * Represents the constant's value.
     */
    @Nonnull
    private final transient String value;

    private static final EnumMapper<String, EIDASSubStatusCode> MAPPER =
            new EnumMapper<String, EIDASSubStatusCode>(new KeyAccessor<String, EIDASSubStatusCode>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull EIDASSubStatusCode eidasSubStatusCode) {
                    return eidasSubStatusCode.getValue();
                }
            }, Canonicalizers.trimLowerCase(), values());

    /**
     * Solo Constructor.
     *
     * @param val The Constant value.
     */
    EIDASSubStatusCode(@Nonnull final String val) {
        value = val;
    }

    @Nullable
    public static EIDASSubStatusCode fromString(@Nonnull String val) {
        return MAPPER.fromKey(val);
    }

    public static EnumMapper<String, EIDASSubStatusCode> mapper() {
        return MAPPER;
    }


    @Nonnull
    public String getValue() {
        return value;
    }

    /**
     * Return the Constant Value.
     *
     * @return The constant value.
     */
    @Nonnull
    @Override
    public String toString() {
        return value;
    }
}
