/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.engine.configuration.dom;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.samlengineconfig.CertificateConfigurationManager;
import eu.eidas.samlengineconfig.ConfigurationParameter;
import eu.eidas.samlengineconfig.EngineInstance;
import eu.eidas.samlengineconfig.InstanceConfiguration;
import eu.eidas.samlengineconfig.PropsParameter;
import eu.eidas.samlengineconfig.StringParameter;
import eu.eidas.util.Preconditions;

/**
 * Adapts the 2 models of Configurations.
 *
 * @since 1.1
 */
public final class ConfigurationAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationAdapter.class);

    @Nonnull
    public static InstanceMap adapt(@Nonnull CertificateConfigurationManager configManager)
            throws ProtocolEngineConfigurationException {
        Preconditions.checkNotNull(configManager, "configManager");
        if (configManager.isActive()) {
            Map<String, InstanceEntry> instanceMap = new LinkedHashMap<>();
            Map<String, EngineInstance> config = configManager.getConfiguration();
            for (Map.Entry<String, EngineInstance> entry : config.entrySet()) {
                String instanceName = entry.getKey();
                EngineInstance value = entry.getValue();
                String valueName = value.getName();
                if (StringUtils.isBlank(instanceName) || StringUtils.isBlank(valueName)) {
                    String message = "SAML engine configuration contains a blank instance name";
                    LOG.error(message);
                    throw new ProtocolEngineConfigurationException(message);
                }
                if (!instanceName.equals(valueName)) {
                    String message = "SAML engine configuration contains mismatched instance names: \"" + instanceName
                            + "\" and \"" + valueName + "\"";
                    LOG.error(message);
                    throw new ProtocolEngineConfigurationException(message);
                }

                instanceMap.put(instanceName, toInstanceEntry(value));
            }
            return new InstanceMap(ImmutableMap.copyOf(instanceMap));
        }
        String message =
                "SAML engine configuration cannot be obtained from an inactive configManager: " + configManager;
        LOG.error(message);
        throw new ProtocolEngineConfigurationException(message);
    }

    @Nonnull
    private static ConfigurationEntry toConfigurationEntry(@Nonnull String instanceName,
                                                           @Nonnull String configurationName,
                                                           @Nonnull Map<String, String> parameters)
            throws ProtocolEngineConfigurationException {
        ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<>();
        for (final Map.Entry<String, String> entry : parameters.entrySet()) {
            String parameterName = entry.getKey();
            String parameterValue = entry.getValue();
            if (StringUtils.isBlank(parameterName)) {
                String message = "SAML engine configuration contains a blank parameter name for configuration name \""
                        + configurationName + "\" in instance name \"" + instanceName + "\"";
                LOG.error(message);
                throw new ProtocolEngineConfigurationException(message);
            }
            if (StringUtils.isBlank(parameterValue)) {
                String message = "SAML engine configuration contains parameter name \"" + parameterName
                        + "\" with a blank value for configuration name \"" + configurationName
                        + "\" in instance name \"" + instanceName + "\"";
                LOG.error(message);
                throw new ProtocolEngineConfigurationException(message);
            }
            mapBuilder.put(parameterName, parameterValue);
        }
        return new ConfigurationEntry(configurationName, mapBuilder.build());
    }

    @Nonnull
    private static ConfigurationEntry toConfigurationEntry(@Nonnull String instanceName,
                                                           @Nonnull String configurationName,
                                                           @Nonnull Properties properties)
            throws ProtocolEngineConfigurationException {
        return toConfigurationEntry(instanceName, configurationName, Maps.fromProperties(properties));
    }

    @Nonnull
    private static ConfigurationEntry toConfigurationEntry(@Nonnull String instanceName,
                                                           @Nonnull String configurationName,
                                                           @Nonnull List<ConfigurationParameter> parameterList)
            throws ProtocolEngineConfigurationException {
        ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<>();
        for (final ConfigurationParameter configurationParameter : parameterList) {
            String parameterName = configurationParameter.getName();
            if (configurationParameter instanceof StringParameter) {
                String parameterValue = (String) configurationParameter.getValue();
                mapBuilder.put(parameterName, parameterValue);
            } else {
                String message = "SAML engine configuration contains parameter name \"" + parameterName
                        + "\" which is not a StringParameter (" + configurationParameter.getClass().getName()
                        + ") for configuration name \"" + configurationName + "\" in instance name \"" + instanceName
                        + "\"";
                LOG.error(message);
                throw new ProtocolEngineConfigurationException(message);
            }
        }
        return toConfigurationEntry(instanceName, configurationName, mapBuilder.build());
    }

    @Nonnull
    private static InstanceEntry toInstanceEntry(@Nonnull EngineInstance ei) throws ProtocolEngineConfigurationException {
        String instanceName = ei.getName();
        Map<String, ConfigurationEntry> instanceEntry = new LinkedHashMap<>();
        for (InstanceConfiguration configuration : ei.getConfigurations()) {
            String configurationName = configuration.getName();
            if (StringUtils.isBlank(configurationName)) {
                String message = "SAML engine configuration contains a blank configuration name for instance name \""
                        + instanceName + "\"";
                LOG.error(message);
                throw new ProtocolEngineConfigurationException(message);
            }
            if (ConfigurationKey.mapper().matches(configurationName, ConfigurationKey.SAML_ENGINE_CONFIGURATION)) {
                for (ConfigurationParameter cp : configuration.getParameters()) {
                    if (ParameterKey.mapper().matches(cp.getName(), ParameterKey.FILE_CONFIGURATION)
                            && cp instanceof PropsParameter) {
                        Properties properties = (Properties) cp.getValue();
                        instanceEntry.put(configurationName,
                                          toConfigurationEntry(instanceName, configurationName, properties));
                    }
                }
            } else {
                instanceEntry.put(configurationName,
                                  toConfigurationEntry(instanceName, configurationName, configuration.getParameters()));
            }
        }
        return new InstanceEntry(instanceName, ImmutableMap.copyOf(instanceEntry));
    }

    private ConfigurationAdapter() {
    }
}
