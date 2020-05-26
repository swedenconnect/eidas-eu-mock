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
package eu.eidas.auth.engine.core.impl;

import eu.eidas.auth.commons.EidasParameterKeys;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Provider;
import java.security.Security;

/**
 * BouncyCastle Provider Bootstrap.
 *
 * @since 1.1
 */
public final class BouncyCastleBootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(BouncyCastleBootstrap.class);
    protected static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();

    private BouncyCastleBootstrap() {
    }

    public static void bootstrap() {
        Security.addProvider(BOUNCY_CASTLE_PROVIDER);
    }

    /**
     * Aggressive method to overwrite the existing BouncyCastleProvider by our own instance.
     */
    public static void reInstallSecurityProvider() {
        // Mitigation measure against WebLogic java.security.NoSuchAlgorithmException: No such algorithm
        // Ensure a wrong version of BC is not installed:

        String value = System.getProperty(EidasParameterKeys.BLOCK_BOUNCY_CASTLE_PROVIDER_REINSTALL.toString());
        boolean blockingSecurityProviderReinstall = (Boolean.parseBoolean(value));
        if (blockingSecurityProviderReinstall) {
            LOG.info(createLogMessage(blockingSecurityProviderReinstall));
            return;
        }

        Provider provider = Security.getProvider(BOUNCY_CASTLE_PROVIDER.getName());
        if (provider != BOUNCY_CASTLE_PROVIDER) {
            LOG.info(createLogMessage(blockingSecurityProviderReinstall));
            Security.removeProvider(BOUNCY_CASTLE_PROVIDER.getName());
            Security.addProvider(BOUNCY_CASTLE_PROVIDER);
        }
    }

    private static String createLogMessage(boolean blockingSecurityProviderReinstall) {
        StringBuilder stringBuilder = new StringBuilder(BOUNCY_CASTLE_PROVIDER.toString()).
                append(": ");
        if (blockingSecurityProviderReinstall) {
            return stringBuilder.append("For activating reinstallation of JCE provider deactivate the needed system property ").
                    toString();
        } else {
            return stringBuilder.append("For blocking reinstallation of JCE provider activate the needed system property ").
                    toString();
        }
    }
}
