package eu.eidas.node.auth.metadata;

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;

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
    public EidasMetadataParametersI getEidasMetadata(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock)
            throws EIDASMetadataException {
        return metadataFetcher.getEidasMetadata(url, metadataSigner, metadataClock);
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
