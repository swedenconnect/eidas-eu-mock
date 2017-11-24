package eu.eidas.auth.engine.configuration;

import javax.annotation.Nonnull;

/**
 * Configuration Accessor.
 * <p>
 * Remove this class in 1.2.
 *
 * @deprecated since 1.1, use {@link ProtocolConfigurationAccessor} instead.
 */
@Deprecated
public interface ConfigurationAccessor {

    @Nonnull
    SamlEngineConfiguration get() throws SamlEngineConfigurationException;
}
