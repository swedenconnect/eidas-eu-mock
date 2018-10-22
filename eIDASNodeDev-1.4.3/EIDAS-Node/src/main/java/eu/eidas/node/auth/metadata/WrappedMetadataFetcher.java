package eu.eidas.node.auth.metadata;

import javax.annotation.Nonnull;

import org.opensaml.saml2.metadata.EntityDescriptor;

import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;

/**
 * Decorated MetadataFetcher.
 *
 * @since 1.1
 */
public final class WrappedMetadataFetcher implements MetadataFetcherI {

    @Nonnull
    private volatile MetadataFetcherI metadataFetcher;

    @Override
    @Nonnull
    public EntityDescriptor getEntityDescriptor(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner)
            throws EIDASSAMLEngineException {
        return metadataFetcher.getEntityDescriptor(url, metadataSigner);
    }

    @Nonnull
    public MetadataFetcherI getMetadataFetcher() {
        return metadataFetcher;
    }

    public void setMetadataFetcher(@Nonnull MetadataFetcherI metadataFetcher) {
        Preconditions.checkNotNull(metadataFetcher, "metadataFetcher");
        this.metadataFetcher = metadataFetcher;
    }
}
