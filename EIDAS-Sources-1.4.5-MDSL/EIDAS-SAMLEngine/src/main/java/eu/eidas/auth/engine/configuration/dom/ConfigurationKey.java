package eu.eidas.auth.engine.configuration.dom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * ConfigurationKey
 *
 * @since 1.1
 */
public enum ConfigurationKey {

    SAML_ENGINE_CONFIGURATION("SamlEngineConf"),

    SIGNATURE_CONFIGURATION("SignatureConf"),

    ENCRYPTION_CONFIGURATION("EncryptionConf"),

    /**
     * @deprecated since 1.1, use {@link #PROTOCOL_PROCESSOR_CONFIGURATION} instead.
     */
    @Deprecated
    EXTENSION_PROCESSOR_CONFIGURATION("ExtensionProcessorConf"),

    PROTOCOL_PROCESSOR_CONFIGURATION("ProtocolProcessorConf"),

    CLOCK_CONFIGURATION("ClockConf"),

    // put the ; on a separate line to make merges easier
    ;

    private static final EnumMapper<String, ConfigurationKey> MAPPER =
            new EnumMapper<String, ConfigurationKey>(new KeyAccessor<String, ConfigurationKey>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull ConfigurationKey configurationKey) {
                    return configurationKey.getKey();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static ConfigurationKey fromString(@Nonnull String value) {
        return MAPPER.fromKey(value);
    }

    public static EnumMapper<String, ConfigurationKey> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String key;

    ConfigurationKey(@Nonnull String key) {
        this.key = key;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key;
    }
}
