/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.engine.configuration.dom;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;
import eu.eidas.auth.engine.core.impl.CertificateValidator;

/**
 * SignatureKey
 *
 * @since 1.1
 */
public enum SignatureKey {

    SIGNATURE_ALGORITHM("signature.algorithm"),

    SIGNATURE_ALGORITHM_WHITE_LIST("signature.algorithm.whitelist"),

    CHECK_VALIDITY_PERIOD_PROPERTY(CertificateValidator.CHECK_VALIDITY_PERIOD_PROPERTY),

    DISALLOW_SELF_SIGNED_CERTIFICATE_PROPERTY(CertificateValidator.DISALLOW_SELF_SIGNED_CERTIFICATE_PROPERTY),

    ISSUER("issuer"),

    SERIAL_NUMBER("serialNumber"),

    METADATA_PREFIX("metadata."),

    RESPONSE_SIGN_ASSERTIONS("response.sign.assertions"),
    REQUEST_SIGN_WITH_KEY_VALUE("request.sign.with.key.value"),
    RESPONSE_SIGN_WITH_KEY_VALUE("response.sign.with.key.value"),

    // put the ; on a separate line to make merges easier
    ;

    private static final EnumMapper<String, SignatureKey> MAPPER =
            new EnumMapper<String, SignatureKey>(new KeyAccessor<String, SignatureKey>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull SignatureKey signatureKey) {
                    return signatureKey.getKey();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static SignatureKey fromString(@Nonnull String value) {
        return MAPPER.fromKey(value);
    }

    public static EnumMapper<String, SignatureKey> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String key;

    SignatureKey(@Nonnull String key) {
        this.key = key;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nullable
    public <T> T getAsString(@Nonnull Map<String, T> map) {
        return map.get(key);
    }

    @Nullable
    public <T> T get(@Nonnull Map<SignatureKey, T> map) {
        return map.get(this);
    }

    @Override
    public String toString() {
        return key;
    }
}
