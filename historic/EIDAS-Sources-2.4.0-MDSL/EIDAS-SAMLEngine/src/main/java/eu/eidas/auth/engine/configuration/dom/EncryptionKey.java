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
 * EncryptionKey
 *
 * @since 1.1
 */
public enum EncryptionKey {

    DATA_ENCRYPTION_ALGORITHM("data.encryption.algorithm"),

    KEY_ENCRYPTION_ALGORITHM("key.encryption.algorithm"),

    KEY_ENCRYPTION_ALGORITHM_FOR_KEY_AGREEMENT("key.encryption.algorithm.key.wrapping"),

    ENCRYPTION_ALGORITHM_WHITE_LIST("encryption.algorithm.whitelist"),

    RESPONSE_ENCRYPTION_MANDATORY("response.encryption.mandatory"),
    ASSERTION_ENCRYPTION_WITH_KEY("assertion.encrypt.with.key.value"),

    CHECK_VALIDITY_PERIOD_PROPERTY(CertificateValidator.CHECK_VALIDITY_PERIOD_PROPERTY),

    DISALLOW_SELF_SIGNED_CERTIFICATE_PROPERTY(CertificateValidator.DISALLOW_SELF_SIGNED_CERTIFICATE_PROPERTY),

    /**
     * name of the parameter storing the JCA provider name
     */
    JCA_PROVIDER_NAME("jcaProviderName"),

    RESPONSE_DECRYPTION_ISSUER("responseDecryptionIssuer"),

    SERIAL_NUMBER("serialNumber"),

    RESPONSE_TO_POINT_ISSUER_PREFIX("responseToPointIssuer."),

    RESPONSE_TO_POINT_SERIAL_NUMBER_PREFIX("responseToPointSerialNumber."),

    ENCRYPTION_ACTIVATION("encryptionActivation"),

    ENCRYPTION_ACTIVATION_PATH("defaultPath"),

    ENCRYPT_TO_PREFIX("EncryptTo."),

    // put the ; on a separate line to make merges easier
    ;

    private static final EnumMapper<String, EncryptionKey> MAPPER =
            new EnumMapper<String, EncryptionKey>(new KeyAccessor<String, EncryptionKey>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull EncryptionKey encryptionKey) {
                    return encryptionKey.getKey();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static EncryptionKey fromString(@Nonnull String value) {
        return MAPPER.fromKey(value);
    }

    public static EnumMapper<String, EncryptionKey> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String key;

    EncryptionKey(@Nonnull String key) {
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
    public <T> T get(@Nonnull Map<EncryptionKey, T> map) {
        return map.get(this);
    }

    @Override
    public String toString() {
        return key;
    }
}
