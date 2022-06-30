package eu.eidas.auth.engine.configuration;

import javax.annotation.Nonnull;

/**
 * ProtocolEngineConfiguration Accessor
 *
 * @since 1.1
 */
public interface ProtocolConfigurationAccessor {

    @Nonnull
    ProtocolEngineConfiguration get() throws SamlEngineConfigurationException;
}
