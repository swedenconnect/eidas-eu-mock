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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * This enum class contains the SAML Token Status Code.
 */
public enum EIDASStatusCode {

    /**
     * URI for Requester status code.
     */
    REQUESTER_URI("urn:oasis:names:tc:SAML:2.0:status:Requester"),

    /**
     * URI for Responder status code.
     */
    RESPONDER_URI("urn:oasis:names:tc:SAML:2.0:status:Responder"),

    /**
     * URI for Success status code.
     */
    SUCCESS_URI("urn:oasis:names:tc:SAML:2.0:status:Success"),

    // put the ; on a separate line to make merges easier
    ;

    /**
     * Represents the constant's value.
     */
    @Nonnull
    private final transient String value;

    private static final EnumMapper<String, EIDASStatusCode> MAPPER =
            new EnumMapper<String, EIDASStatusCode>(new KeyAccessor<String, EIDASStatusCode>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull EIDASStatusCode eidasStatusCode) {
                    return eidasStatusCode.getValue();
                }
            }, Canonicalizers.trimLowerCase(), values());

    /**
     * Solo Constructor.
     *
     * @param val The Constant value.
     */
    EIDASStatusCode(@Nonnull String val) {
        value = val;
    }

    @Nullable
    public static EIDASStatusCode fromString(@Nonnull String val) {
        return MAPPER.fromKey(val);
    }

    public static EnumMapper<String, EIDASStatusCode> mapper() {
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
