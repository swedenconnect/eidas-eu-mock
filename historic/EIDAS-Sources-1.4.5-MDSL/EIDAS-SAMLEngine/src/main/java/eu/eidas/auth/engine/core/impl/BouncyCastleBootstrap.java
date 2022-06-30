package eu.eidas.auth.engine.core.impl;

import java.security.Provider;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * BouncyCastle Provider Bootstrap.
 *
 * @since 1.1
 */
public final class BouncyCastleBootstrap {

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
        Provider provider = Security.getProvider(BOUNCY_CASTLE_PROVIDER.getName());
        if (provider != BOUNCY_CASTLE_PROVIDER) {
            Security.removeProvider(BOUNCY_CASTLE_PROVIDER.getName());
            Security.addProvider(BOUNCY_CASTLE_PROVIDER);
        }
    }
}
