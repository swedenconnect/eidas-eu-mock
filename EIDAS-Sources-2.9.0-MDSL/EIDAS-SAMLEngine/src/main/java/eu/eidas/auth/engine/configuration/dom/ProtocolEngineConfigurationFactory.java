/*
 * Copyright (c) 2024 by European Commission
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
 * limitations under the Licence.
 */
package eu.eidas.auth.engine.configuration.dom;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

/**
 * ProtocolEngineConfiguration Factory.
 */
public final class ProtocolEngineConfigurationFactory {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProtocolEngineConfigurationFactory.class);

    @Nonnull
    private static <T> T throwConfigurationException(@Nonnull String additionalInformation) throws ProtocolEngineConfigurationException {
        LOG.error(additionalInformation);
        throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR,
                additionalInformation);
    }

    @Nonnull
    private final SingletonAccessor<Map<String, ProtocolEngineConfiguration>> accessor;

    public ProtocolEngineConfigurationFactory(@Nonnull String configurationFileName,
                                              @Nullable String overrideFileName,
                                              @Nullable String defaultPath) {
        Preconditions.checkNotBlank(configurationFileName, "configurationFileName");
        accessor = new ReloadableProtocolConfigurationMap(configurationFileName, overrideFileName, defaultPath).getAccessor();
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
            throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR,
                    "Failed to read the configuration", e);
        }
        if (null == samlEngineConfiguration) {
            return throwConfigurationException("Instance : \"" + name + "\" does not exist.");
        }
        return samlEngineConfiguration;
    }

    @Nonnull
    public SingletonAccessor<Map<String, ProtocolEngineConfiguration>> getConfigurationMapAccessor() {
        return accessor;
    }
}
