package eu.eidas.auth.engine.configuration.dom;

import java.security.KeyStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class KeyContainerEntry {

    private final KeyStore.PrivateKeyEntry privateKeyEntry;
    private final String keyProvider;

    public KeyContainerEntry(@Nonnull final KeyStore.PrivateKeyEntry privateKeyEntry) {
        this(privateKeyEntry, null);
    }

    public KeyContainerEntry(@Nonnull final KeyStore.PrivateKeyEntry privateKeyEntry,
                             @Nullable final String keyProvider) {
        this.privateKeyEntry = privateKeyEntry;
        this.keyProvider = keyProvider;
    }

    @Nonnull
    public KeyStore.PrivateKeyEntry getPrivateKeyEntry() {
        return this.privateKeyEntry;
    }

    @Nullable
    public String getKeyProvider() {
        return this.keyProvider;
    }
}
