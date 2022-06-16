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
import eu.eidas.auth.commons.io.ResourceLocator;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses the SAML engine configuration XML file using DOM.
 */
public final class DOMConfigurationParser {

    static final class InstanceTag {

        static final String TAG_NAME = "instance";

        private InstanceTag() {}

        static final class Attribute {

            static final String NAME = "name";
            private Attribute() {}
        }

        static final class ConfigurationTag {

            static final String TAG_NAME = "configuration";

            private ConfigurationTag() {}

            static final class Attribute {

                static final String NAME = "name";
                private Attribute() {}
            }

            static final class ParameterTag {

                static final String TAG_NAME = "parameter";

                private ParameterTag() {}

                static final class Attribute {

                    static final String NAME = "name";

                    static final String VALUE = "value";

                    private Attribute() {}
                }

            }

        }
    }

    public static final String DEFAULT_CONFIGURATION_FILE = "SamlEngine.xml";

    private static final Logger LOG = LoggerFactory.getLogger(DOMConfigurationParser.class);

    /**
     * Prevent instantiation.
     */
    private DOMConfigurationParser() {
    }

    /**
     * Read configuration.
     *
     * @param configurationFileName the default configuration file
     * @return the {@link Map} collection that contains {@link String} as
     * key and {@link InstanceEntry} as value.
     * @throws ProtocolEngineConfigurationException the EIDASSAML engine runtime exception
     */
    @Nonnull
    public static InstanceMap parseConfiguration(@Nonnull String configurationFileName)
            throws ProtocolEngineConfigurationException {
        Preconditions.checkNotNull(configurationFileName, "configurationFileName");
        LOG.debug("DOM parsing SAML engine configuration file: \"" + configurationFileName + "\"");
        try {
            // Load configuration file
            URL resource = ResourceLocator.getResource(configurationFileName);
            if (null == resource) {
                String message = "SAML engine configuration file \"" + configurationFileName + "\" cannot be found";
                LOG.error(message);
                throw new ProtocolEngineConfigurationException(message);
            } else {
                LOG.debug("SAML engine configuration file \"" + configurationFileName + "\" found at \""
                                  + resource.toExternalForm() + "\"");
            }
            return parseConfiguration(configurationFileName, resource.openStream());
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ProtocolEngineConfigurationException(ex);
        }
    }

    /**
     * Read configuration.
     *
     * @param configurationFileName the default configuration file
     * @param inputStream the default configuration file
     * @return the {@link Map} collection that contains {@link String} as
     * key and {@link InstanceEntry} as value.
     * @throws ProtocolEngineConfigurationException the EIDASSAML engine runtime exception
     */
    @Nonnull
    @SuppressWarnings("squid:S2095")
    public static InstanceMap parseConfiguration(@Nonnull String configurationFileName,
                                                 @Nonnull InputStream inputStream)
            throws ProtocolEngineConfigurationException {
        Preconditions.checkNotNull(configurationFileName, "configurationFileName");
        Preconditions.checkNotNull(inputStream, "inputStream");
        try {
            Document document;
            try (InputStream engineConf = inputStream) {
                document = DocumentBuilderFactoryUtil.parse(engineConf);
            }
            // Read instance
            NodeList instanceTags = document.getElementsByTagName(InstanceTag.TAG_NAME);

            Map<String, InstanceEntry> instanceEntries = new LinkedHashMap<String, InstanceEntry>();

            for (int i = 0, n = instanceTags.getLength(); i < n; i++) {
                Element instanceTag = (Element) instanceTags.item(i);
                InstanceEntry instanceEntry = parseInstanceEntry(configurationFileName, instanceTag);

                // Add to the list of configurations.
                InstanceEntry previous = instanceEntries.put(instanceEntry.getName(), instanceEntry);

                if (null != previous) {
                    String message = "Duplicate instance entry names \"" + instanceEntry.getName()
                            + "\" in SAML engine configuration file \"" + configurationFileName + "\"";
                    LOG.error(message);
                    throw new ProtocolEngineConfigurationException(message);
                }
            }

            return new InstanceMap(ImmutableMap.copyOf(instanceEntries));
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ProtocolEngineConfigurationException(ex);
        }
    }

    @Nonnull
    private static ConfigurationEntry parseConfigurationEntry(@Nonnull String configurationFileName,
                                                              @Nonnull String instanceName,
                                                              @Nonnull Element configurationTag)
            throws ProtocolEngineConfigurationException {
        String configurationName = configurationTag.getAttribute(InstanceTag.ConfigurationTag.Attribute.NAME);

        if (StringUtils.isBlank(configurationName)) {
            String message = "SAML engine configuration file \"" + configurationFileName
                    + "\" contains a blank configuration name for instance name \"" + instanceName + "\"";
            LOG.error(message);
            throw new ProtocolEngineConfigurationException(message);
        }

        // Set configuration name.
        String name = configurationName.trim();
        // Read every parameter for this configuration.
        ImmutableMap<String, String> parameters =
                parseParameters(configurationFileName, instanceName, configurationName, configurationTag);

        return new ConfigurationEntry(name, parameters);
    }

    /**
     * Read configuration.
     *
     * @return the {@link Map} collection that contains {@link String} as
     * key and {@link InstanceEntry} as value.
     * @throws ProtocolEngineConfigurationException the EIDASSAML engine runtime exception
     */
    public static InstanceMap parseDefaultConfiguration() throws ProtocolEngineConfigurationException {
        return parseConfiguration(DEFAULT_CONFIGURATION_FILE);
    }

    @Nonnull
    private static InstanceEntry parseInstanceEntry(@Nonnull String configurationFileName, @Nonnull Element instanceTag)
            throws ProtocolEngineConfigurationException {
        // read every configuration.
        String instanceName = instanceTag.getAttribute(InstanceTag.Attribute.NAME);

        if (StringUtils.isBlank(instanceName)) {
            String message =
                    "SAML engine configuration file \"" + configurationFileName + "\" contains a blank instance name";
            LOG.error(message);
            throw new ProtocolEngineConfigurationException(message);
        }
        instanceName = instanceName.trim();

        NodeList configurationTags = instanceTag.getElementsByTagName(InstanceTag.ConfigurationTag.TAG_NAME);

        Map<String, ConfigurationEntry> configurationEntries = new LinkedHashMap<>();

        for (int i = 0, n = configurationTags.getLength(); i < n; i++) {
            Element configurationTag = (Element) configurationTags.item(i);

            ConfigurationEntry configurationEntry =
                    parseConfigurationEntry(configurationFileName, instanceName, configurationTag);

            ConfigurationEntry previous = configurationEntries.put(configurationEntry.getName(), configurationEntry);
            if (null != previous) {
                String message = "Duplicate configuration entry names \"" + configurationEntry.getName()
                        + "\" in SAML engine configuration file \"" + configurationFileName + "\"";
                LOG.error(message);
                throw new ProtocolEngineConfigurationException(message);
            }
        }

        return new InstanceEntry(instanceName, ImmutableMap.copyOf(configurationEntries));
    }

    /**
     * Generate parameters.
     *
     * @param configurationFileName the default configuration file
     * @param instanceName the instance name
     * @param configurationTag the configuration node
     * @return the map< string, string>
     */
    @Nonnull
    private static ImmutableMap<String, String> parseParameters(@Nonnull String configurationFileName,
                                                                @Nonnull String instanceName,
                                                                @Nonnull String configurationName,
                                                                @Nonnull Element configurationTag)
            throws ProtocolEngineConfigurationException {
        Map<String, String> parameters = new LinkedHashMap<String, String>();

        NodeList parameterTags =
                configurationTag.getElementsByTagName(InstanceTag.ConfigurationTag.ParameterTag.TAG_NAME);

        for (int i = 0, n = parameterTags.getLength(); i < n; i++) {
            // for every parameter find, process.
            Element parameterTag = (Element) parameterTags.item(i);
            String parameterName = parameterTag.getAttribute(InstanceTag.ConfigurationTag.ParameterTag.Attribute.NAME);
            String parameterValue =
                    parameterTag.getAttribute(InstanceTag.ConfigurationTag.ParameterTag.Attribute.VALUE);

            // verify the content.
            if (StringUtils.isBlank(parameterName)) {
                String message = "SAML engine configuration file \"" + configurationFileName
                        + "\" contains a blank parameter name for configuration name \"" + configurationName
                        + "\" in instance name \"" + instanceName + "\"";
                LOG.error(message);
                throw new ProtocolEngineConfigurationException(message);
            }
            if (StringUtils.isBlank(parameterValue)) {
                String message =
                        "SAML engine configuration file \"" + configurationFileName + "\" contains parameter name \""
                                + parameterName + "\" with a blank value for configuration name \"" + configurationName
                                + "\" in instance name \"" + instanceName + "\"";
                LOG.error(message);
                throw new ProtocolEngineConfigurationException(message);
            }
            String previous = parameters.put(parameterName.trim(), parameterValue.trim());
            if (null != previous) {
                String message =
                        "Duplicate parameter entry names \"" + parameterName + "\" in SAML engine configuration file \""
                                + configurationFileName + "\" for configuration name \"" + configurationName
                                + "\" in instance name \"" + instanceName + "\"";
                LOG.error(message);
                throw new ProtocolEngineConfigurationException(message);
            }
        }
        return ImmutableMap.copyOf(parameters);
    }

    @Nonnull
    static ImmutableMap<String, String> validateParameters(@Nonnull String instanceName,
                                                           @Nonnull String configurationName,
                                                           @Nonnull Map<String, String> values)
            throws ProtocolEngineConfigurationException {
        Map<String, String> parameters = new LinkedHashMap<String, String>();

        for (final Map.Entry<String, String> entry : values.entrySet()) {
            String parameterName = entry.getKey();
            String parameterValue = entry.getValue();

            // verify the content.
            if (StringUtils.isBlank(parameterName)) {
                String message =
                        "SAML engine configuration file contains a blank parameter name for configuration name \""
                                + configurationName + "\" in instance name \"" + instanceName + "\"";
                LOG.error(message);
                throw new ProtocolEngineConfigurationException(message);
            }
            if (StringUtils.isBlank(parameterValue)) {
                String message = "SAML engine configuration file contains parameter name \"" + parameterName
                        + "\" with a blank value for configuration name \"" + configurationName
                        + "\" in instance name \"" + instanceName + "\"";
                LOG.error(message);
                throw new ProtocolEngineConfigurationException(message);
            }
            parameters.put(parameterName.trim(), parameterValue.trim());
        }

        return ImmutableMap.copyOf(parameters);
    }

}
