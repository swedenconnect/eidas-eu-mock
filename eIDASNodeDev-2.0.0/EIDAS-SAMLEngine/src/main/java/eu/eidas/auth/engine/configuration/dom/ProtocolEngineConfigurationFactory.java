package eu.eidas.auth.engine.configuration.dom;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.commons.io.SingletonAccessors;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.samlengineconfig.CertificateConfigurationManager;
import eu.eidas.util.Preconditions;

/**
 * ProtocolEngineConfiguration Factory.
 *
 * @since 1.1
 */
public final class ProtocolEngineConfigurationFactory {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProtocolEngineConfigurationFactory.class);

    @Nonnull
    private static InstanceMap adaptToInstanceMap(@Nonnull CertificateConfigurationManager configManager)
            throws ProtocolEngineConfigurationException {
        if (isActiveConfigurationManager(configManager)) {
            return ConfigurationAdapter.adapt(configManager);
        }
        throw new ProtocolEngineConfigurationException(
                EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode()),
                EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage()),
                "The given CertificateConfigurationManager is null or not active.");
    }

    @Nonnull
    public static ImmutableMap<String, ProtocolEngineConfiguration> getConfigurationMap(
            @Nonnull CertificateConfigurationManager configManager) throws ProtocolEngineConfigurationException {
        return DOMConfigurator.getProtocolConfigurationMap(adaptToInstanceMap(configManager));
    }

    private static boolean isActiveConfigurationManager(@Nullable CertificateConfigurationManager configManager) {
        return null != configManager && configManager.isActive();
    }

    @Nonnull
    private static <T> T throwConfigurationException(@Nonnull String message) throws ProtocolEngineConfigurationException {
        LOG.error(message);
        throw new ProtocolEngineConfigurationException(
                EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode()),
                EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage()), message);
    }

    @Nonnull
    private final SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> accessor;

    public ProtocolEngineConfigurationFactory(@Nonnull String configurationFileName,
                                              @Nullable String overrideFileName,
                                              @Nullable String defaultPath) {
        Preconditions.checkNotBlank(configurationFileName, "configurationFileName");
        accessor = new ReloadableProtocolConfigurationMap(configurationFileName, overrideFileName, defaultPath).getAccessor();
    }

    public ProtocolEngineConfigurationFactory(@Nonnull CertificateConfigurationManager configManager)
            throws ProtocolEngineConfigurationException {
        Preconditions.checkNotNull(configManager, "configManager");
        accessor = SingletonAccessors.immutableAccessor(getConfigurationMap(configManager));
    }

    /**
     * Returns the corresponding configuration.
     *
     * @param instanceName the name of the instance
     */
    @Nonnull
    public ProtocolEngineConfiguration getConfiguration(@Nonnull String instanceName)
            throws ProtocolEngineConfigurationException {
        if (StringUtils.isBlank(instanceName)) {
            return throwConfigurationException("instanceName cannot be null or empty.");
        }
        String name = instanceName.trim();
        ProtocolEngineConfiguration samlEngineConfiguration;
        try {
            samlEngineConfiguration = accessor.get().get(name);
        } catch (IOException e) {
            throw new ProtocolEngineConfigurationException(
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode()),
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage()), e);
        }
        if (null == samlEngineConfiguration) {
            return throwConfigurationException("Instance : \"" + name + "\" does not exist.");
        }
        return samlEngineConfiguration;
    }

    @Nonnull
    public SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> getConfigurationMapAccessor() {
        return accessor;
    }
}
