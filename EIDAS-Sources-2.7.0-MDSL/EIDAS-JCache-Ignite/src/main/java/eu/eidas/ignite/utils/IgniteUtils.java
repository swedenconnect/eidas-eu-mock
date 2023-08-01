/*
 * Copyright (c) 2020 by European Commission
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
        boolean isCheckUpdateIgniteVersionActive = IgniteSystemProperties
                .getBoolean(IgniteSystemProperties.IGNITE_UPDATE_NOTIFIER, false);
        if (!isCheckUpdateIgniteVersionActive) {
            System.setProperty(IgniteSystemProperties.IGNITE_UPDATE_NOTIFIER, Boolean.FALSE.toString());
        }
    }
}
