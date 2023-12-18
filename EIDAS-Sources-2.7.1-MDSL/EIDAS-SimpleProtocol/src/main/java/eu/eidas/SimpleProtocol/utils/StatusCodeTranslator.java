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

package eu.eidas.SimpleProtocol.utils;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Translates between supported Status Code in the eIDAS specification and the
 * simple status_code values.
 */
public enum StatusCodeTranslator {

    SUCCESS("success", "urn:oasis:names:tc:SAML:2.0:status:Success"),

    RESPONDER_FAILURE("responder failure", "urn:oasis:names:tc:SAML:2.0:status:Responder"),

    REQUESTER_FAILURE("requester failure", "urn:oasis:names:tc:SAML:2.0:status:Requester"),

    VERSION_MISMATCH("version mismatch", "urn:oasis:names:tc:SAML:2.0:status:VersionMismatch");


    public static final String SAML_STATUS_PREFIX = "urn:oasis:names:tc:SAML:2.0:status:";

    private static final EnumMapper<String, StatusCodeTranslator> URI_SMSSP_MAPPER =
            new EnumMapper<>(new KeyAccessor<String, StatusCodeTranslator>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull StatusCodeTranslator statusCodeTranslator) {
                    return statusCodeTranslator.stringSmsspStatusCode();
                }
            }, Canonicalizers.trimLowerCase(), values());


    private static final EnumMapper<String, StatusCodeTranslator> URI_EIDAS_MAPPER =
            new EnumMapper<>(new KeyAccessor<String, StatusCodeTranslator>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull StatusCodeTranslator statusCodeTranslator) {
                    return statusCodeTranslator.stringEidasStatusCode();
                }
            }, Canonicalizers.trimLowerCase(), values());


    @Nullable
    public static StatusCodeTranslator fromSmsspStatusCodeString(@Nonnull String smsspStatusCode) {
        return URI_SMSSP_MAPPER.fromKey(smsspStatusCode);
    }

    @Nullable
    public static StatusCodeTranslator fromEidasStatusCodeString(@Nonnull String eidasStatusCode) {
        return URI_EIDAS_MAPPER.fromKey(eidasStatusCode);
    }


    @Nonnull
    private final transient String smsspStatusCode;

    @Nonnull
    private final transient String eidasStatusCode;

    StatusCodeTranslator(@Nonnull String smsspNameIdPolicy, @Nonnull String eidasNameIdPolicy) {
        this.smsspStatusCode = smsspNameIdPolicy;
        this.eidasStatusCode = eidasNameIdPolicy;
    }

    @Nonnull
    public String stringSmsspStatusCode() {
        return smsspStatusCode;
    }

    @Nonnull
    public String stringEidasStatusCode() {
        return eidasStatusCode;
    }
}
