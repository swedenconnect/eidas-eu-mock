package eu.eidas.auth.engine.configuration;

import javax.annotation.Nonnull;

/**
 * ConfigurationAccessor using a fixed (frozen) configuration.
 * <p>
 * Remove this class in 1.2.
 *
 * @since 1.1
 * @deprecated since 1.1, use {@link FixedProtocolConfigurationAccessor} instead.
 */
@Deprecated
public final class FixedConfigurationAccessor implements ConfigurationAccessor {

    private final SamlEngineConfiguration configuration;

    public FixedConfigurationAccessor(SamlEngineConfiguration configuration) {
        this.configuration = configuration;
    }

    @Nonnull
    @Override
    public SamlEngineConfiguration get() {
        return configuration;
    }
}
