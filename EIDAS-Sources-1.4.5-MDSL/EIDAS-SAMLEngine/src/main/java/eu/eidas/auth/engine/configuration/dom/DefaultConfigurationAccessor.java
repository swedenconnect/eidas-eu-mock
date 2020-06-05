package eu.eidas.auth.engine.configuration.dom;

import javax.annotation.Nonnull;

import eu.eidas.auth.engine.configuration.ConfigurationAccessor;
import eu.eidas.auth.engine.configuration.SamlEngineConfiguration;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.util.Preconditions;

/**
 * Default ConfigurationAccessor
 * <p>
 * Remove this class in 1.2.
 *
 * @since 1.1
 * @deprecated since 1.1
 */
@Deprecated
public final class DefaultConfigurationAccessor implements ConfigurationAccessor {

    private final String instanceName;

    public DefaultConfigurationAccessor(String instanceName) {
        Preconditions.checkNotBlank(instanceName, "instanceName");
        this.instanceName = instanceName;
    }

    @Nonnull
    @Override
    public SamlEngineConfiguration get() throws SamlEngineConfigurationException {
        return DefaultSamlEngineConfigurationFactory.getDefaultConfiguration(instanceName);
    }
}
