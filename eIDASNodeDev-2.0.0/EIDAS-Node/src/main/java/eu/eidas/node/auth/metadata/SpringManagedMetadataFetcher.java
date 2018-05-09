package eu.eidas.node.auth.metadata;

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.node.ApplicationContextProvider;

import javax.annotation.Nonnull;

/**
 * Spring-Managed MetadataFetcher
 *
 * @since 1.1
 */
public final class SpringManagedMetadataFetcher implements MetadataFetcherI {

    public MetadataFetcherI getMetadataFetcher() {
        return ApplicationContextProvider.getApplicationContext().getBean(MetadataFetcherI.class);
    }

    @Override
    @Nonnull
    public EidasMetadataParametersI getEidasMetadata(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock)
            throws EIDASMetadataException {
        return getMetadataFetcher().getEidasMetadata(url, metadataSigner, metadataClock);
    }
}
