package eu.eidas.auth.engine.configuration.dom;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * Configuration Keys pertaining to KeyStores.
 *
 * @since 1.1
 */
public enum KeyStoreKey {

    KEYSTORE_PATH("keyStorePath"),

    KEYSTORE_TYPE("keyStoreType"),

    /**
     * The JCA provider name
     */
    KEYSTORE_PROVIDER("keyStoreProvider"),

    KEYSTORE_PASSWORD("keyStorePassword"),

    KEY_ALIAS("keyAlias"),

    KEY_PASSWORD("keyPassword"),

    // put the ; on a separate line to make merges easier
    ;

    private static final EnumMapper<String, KeyStoreKey> MAPPER =
            new EnumMapper<String, KeyStoreKey>(new KeyAccessor<String, KeyStoreKey>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull KeyStoreKey keyStoreKey) {
                    return keyStoreKey.getKey();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static KeyStoreKey fromString(@Nonnull String value) {
        return MAPPER.fromKey(value);
    }

    public static EnumMapper<String, KeyStoreKey> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String key;

    KeyStoreKey(@Nonnull String key) {
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
    public <T> T get(@Nonnull Map<KeyStoreKey, T> map) {
        return map.get(this);
    }

    @Override
    public String toString() {
        return key;
    }
}
