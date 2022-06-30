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

    ENCRYPTION_ALGORITHM_WHITE_LIST("encryption.algorithm.whitelist"),

    RESPONSE_ENCRYPTION_MANDATORY("response.encryption.mandatory"),

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
