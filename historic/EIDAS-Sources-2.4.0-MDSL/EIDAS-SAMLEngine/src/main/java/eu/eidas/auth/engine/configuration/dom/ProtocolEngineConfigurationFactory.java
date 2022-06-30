/*
 * Copyright (c) 2019 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence
 */
package eu.eidas.auth.engine.configuration.dom;

import com.google.common.collect.ImmutableMap;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.commons.io.SingletonAccessors;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.samlengineconfig.CertificateConfigurationManager;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

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
     * @return a {@link ProtocolEngineConfiguration}
     * @throws ProtocolEngineConfigurationException the configuration exception
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
