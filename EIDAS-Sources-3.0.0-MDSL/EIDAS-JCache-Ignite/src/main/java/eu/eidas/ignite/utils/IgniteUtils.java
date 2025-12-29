/*
 * Copyright (c) 2025 by European Commission
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

package eu.eidas.ignite.utils;

import org.apache.ignite.IgniteSystemProperties;

import static org.apache.ignite.IgniteSystemProperties.IGNITE_DEV_ONLY_LOGGING_DISABLED;
import static org.apache.ignite.IgniteSystemProperties.IGNITE_ENABLE_OBJECT_INPUT_FILTER_AUTOCONFIGURATION;
import static org.apache.ignite.IgniteSystemProperties.IGNITE_MARSHALLER_WHITELIST;
import static org.apache.ignite.IgniteSystemProperties.IGNITE_UPDATE_NOTIFIER;

/**
 * Utility class for util method relative to Ignite cache
 *
 * @since 2.5
 */
public final class IgniteUtils {

    private IgniteUtils() {}

    /**
     * Deactivate the IgniteUpdateNotifier that checks if the version of Ignite should be updated.
     * Unless system property {@link IgniteSystemProperties#IGNITE_UPDATE_NOTIFIER} is specifically set to true.
     */
    public static void deactivateIgniteVersionNotifierByDefault() {
        if (!IgniteSystemProperties.getBoolean(IGNITE_UPDATE_NOTIFIER, false)) {
            System.setProperty(IGNITE_UPDATE_NOTIFIER, Boolean.FALSE.toString());
        }
    }

    public static void deactivateDeveloperLoggingByDefault() {
        if (!IgniteSystemProperties.getBoolean(IGNITE_DEV_ONLY_LOGGING_DISABLED, false)) {
            System.setProperty(IGNITE_DEV_ONLY_LOGGING_DISABLED, Boolean.TRUE.toString());
        }
    }

    /**
     * Configures a default whitelist file for Apache Ignite object deserialization
     * if the system property IGNITE_MARSHALLER_WHITELIST is not already set.
     * <p>
     * This method sets the system property to ignite-whitelist.txt, which is expected
     * to be available on the classpath (e.g. src/main/resources/ignite-whitelist.txt)
     * <p>
     * This approach allows modules to define a default whitelist without overriding
     * explicit configuration by the operator or system administrator
     */
    public static void configureIgniteWhitelistDefault() {
        if (!IgniteSystemProperties.getBoolean(IGNITE_MARSHALLER_WHITELIST, false)) {
            System.setProperty(IGNITE_MARSHALLER_WHITELIST, "ignite-whitelist.txt");
        }
        if (!IgniteSystemProperties.getBoolean(IGNITE_ENABLE_OBJECT_INPUT_FILTER_AUTOCONFIGURATION, false)) {
            System.setProperty(IGNITE_ENABLE_OBJECT_INPUT_FILTER_AUTOCONFIGURATION, Boolean.FALSE.toString());
        }
    }
}
