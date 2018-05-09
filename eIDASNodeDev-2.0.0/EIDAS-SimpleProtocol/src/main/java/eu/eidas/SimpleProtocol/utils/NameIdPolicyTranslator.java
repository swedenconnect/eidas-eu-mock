/*
 * Copyright (c) 2017 by European Commission
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
 * Translates between supported NameIDPolicy in the eIDAS specification and the
 * simple name_id_policy values.
 */
public enum NameIdPolicyTranslator {

    PERSISTENT("persistent", "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"),

    TRANSIENT("transient", "urn:oasis:names:tc:SAML:2.0:nameid-format:transient"),

    UNSPECIFIED("unspecified", "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");

    private static final EnumMapper<String, NameIdPolicyTranslator> URI_SMSSP_MAPPER =
            new EnumMapper<>(new KeyAccessor<String, NameIdPolicyTranslator>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull NameIdPolicyTranslator nameIdPolicyTranslator) {
                    return nameIdPolicyTranslator.stringSmsspNameIdPolicy();
                }
            }, Canonicalizers.trimLowerCase(), values());


    private static final EnumMapper<String, NameIdPolicyTranslator> URI_EIDAS_MAPPER =
            new EnumMapper<>(new KeyAccessor<String, NameIdPolicyTranslator>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull NameIdPolicyTranslator nameIdPolicyTranslator) {
                    return nameIdPolicyTranslator.stringEidasNameIdPolicy();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static NameIdPolicyTranslator fromSmsspNameIdPolicyString(@Nonnull String smsspNameIdPolicy) {
        return URI_SMSSP_MAPPER.fromKey(smsspNameIdPolicy);
    }

    @Nullable
    public static NameIdPolicyTranslator fromEidasNameIdPolicyString(@Nonnull String eidasNameIdPolicy) {
        return URI_EIDAS_MAPPER.fromKey(eidasNameIdPolicy);
    }


    @Nonnull
    private final transient String smsspNameIdPolicy;

    @Nonnull
    private final transient String eidasNameIdPolicy;

    NameIdPolicyTranslator(@Nonnull String smsspNameIdPolicy, @Nonnull String eidasNameIdPolicy) {
        this.smsspNameIdPolicy = smsspNameIdPolicy;
        this.eidasNameIdPolicy = eidasNameIdPolicy;
    }

    @Nonnull
    public String stringSmsspNameIdPolicy() {
        return smsspNameIdPolicy;
    }

    @Nonnull
    public String stringEidasNameIdPolicy() {
        return eidasNameIdPolicy;
    }
}
