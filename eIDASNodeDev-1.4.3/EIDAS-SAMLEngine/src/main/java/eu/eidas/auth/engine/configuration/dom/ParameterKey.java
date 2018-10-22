package eu.eidas.auth.engine.configuration.dom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * Parameter Key.
 *
 * @since 1.1
 */
public enum ParameterKey {

    CLASS("class"),

    FILE_CONFIGURATION("fileConfiguration"),

    CORE_ATTRIBUTE_REGISTRY_FILE("coreAttributeRegistryFile"),

    ADDITIONAL_ATTRIBUTE_REGISTRY_FILE("additionalAttributeRegistryFile"),

    METADATA_FETCHER_CLASS("metadataFetcherClass"),

    // put the ; on a separate line to make merges easier
    ;

    private static final EnumMapper<String, ParameterKey> MAPPER =
            new EnumMapper<String, ParameterKey>(new KeyAccessor<String, ParameterKey>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull ParameterKey configurationKey) {
                    return configurationKey.getKey();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static ParameterKey fromString(@Nonnull String value) {
        return MAPPER.fromKey(value);
    }

    public static EnumMapper<String, ParameterKey> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String key;

    ParameterKey(@Nonnull String key) {
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
