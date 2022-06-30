package eu.eidas.auth.engine.configuration;

import javax.annotation.Nonnull;

/**
 * ProtocolConfigurationAccessor using a fixed (frozen) configuration.
 *
 * @since 1.1
 */
public final class FixedProtocolConfigurationAccessor implements ProtocolConfigurationAccessor {

    private final ProtocolEngineConfiguration configuration;

    public FixedProtocolConfigurationAccessor(ProtocolEngineConfiguration configuration) {
        this.configuration = configuration;
    }

    @Nonnull
    @Override
    public ProtocolEngineConfiguration get() {
        return configuration;
    }
}
