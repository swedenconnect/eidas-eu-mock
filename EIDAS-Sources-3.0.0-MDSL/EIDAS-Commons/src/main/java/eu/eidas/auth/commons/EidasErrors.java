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

package eu.eidas.auth.commons;

import eu.eidas.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Class to load the properties only once from a file and copy the key/values
 * into an {@link Map}.
 * <p>
 * The purpose it disallow the loading of new or additional properties
 * at runtime and therefore only allowing retrieval of values initially loaded.
 */
public final class EidasErrors {

    private static final Map<String, String> properties;

    /**
     * Path and name of the EIDAS properties file.
     */
    private static final String PROPERTIES_FILENAME = "eidasErrors.properties";

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasErrors.class);

    static {
        properties = EidasErrors.loadProperties();
    }

    private EidasErrors() {
    }

    /**
     * Method that loads the properties from a file into an {@link Map}. If the properties could not be loaded
     * the method will suppress the exception and log it as an error.
     *
     * @return the {@link Map} which contains the properties loaded
     */
    private static Map<String, String> loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = EidasErrors.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME)) {
            properties.load(inputStream);
        } catch (IOException e) {
            LOG.error("An error occurred when trying to load properties file: " + PROPERTIES_FILENAME, e);
        }

        return Map.<String, String>copyOf((Map) properties);
    }

    /**
     * Method to retrieve the value from the {@link Map} that corresponds to a key.
     *
     * @param key the key for the value
     * @return the value corresponding to the key
     */
    @Nullable
    public static String get(@Nullable final String key) {
        Preconditions.checkNotNull(key, "key");
        return properties.get(key);
    }

}