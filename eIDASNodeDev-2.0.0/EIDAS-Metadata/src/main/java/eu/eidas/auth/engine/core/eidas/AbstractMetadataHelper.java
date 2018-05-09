package eu.eidas.auth.engine.core.eidas;

import com.google.common.annotations.VisibleForTesting;
import eu.eidas.auth.engine.metadata.*;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract MetadataHelper
 *
 * @since 1.1
 */
abstract class AbstractMetadataHelper {

    @Nonnull
    private final MetadataFetcherI metadataFetcher;

    @Nonnull
    private final MetadataSignerI metadataSigner;

    @Nonnull
    private final MetadataClockI metadataClock;

    AbstractMetadataHelper(@Nonnull MetadataFetcherI metadataFetcher, @Nonnull MetadataSignerI metadataSigner, @Nonnull MetadataClockI metadataClock) {
        Preconditions.checkNotNull(metadataFetcher, "metadataFetcher");
        Preconditions.checkNotNull(metadataSigner, "metadataSigner");
        Preconditions.checkNotNull(metadataClock, "metadataClock");
        this.metadataFetcher = metadataFetcher;
        this.metadataSigner = metadataSigner;
        this.metadataClock = metadataClock;
    }

    @Nullable
    EidasMetadataParametersI getEntityDescriptor(@Nullable String metadataUrl) throws EIDASMetadataException {
        if (StringUtils.isNotBlank(metadataUrl)) {
            return metadataFetcher.getEidasMetadata(metadataUrl, metadataSigner, metadataClock);
        }
        return null;
    }

    @Nullable
    public EidasMetadataRoleParametersI getIDPSSODescriptor(@Nullable String metadataUrl) throws EIDASMetadataException {
        EidasMetadataParametersI entityParameters = getEntityDescriptor(metadataUrl);
        return MetadataUtil.getIDPRoleDescriptor(entityParameters);
    }

    @Nullable
    public EidasMetadataRoleParametersI getSPSSODescriptor(@Nullable String metadataUrl) throws EIDASMetadataException {
        EidasMetadataParametersI entityParameters = getEntityDescriptor(metadataUrl);
        return MetadataUtil.getSPRoleDescriptor(entityParameters);
    }

    @VisibleForTesting
    @Nonnull
    public MetadataFetcherI getMetadataFetcher() {
        return metadataFetcher;
    }

    @VisibleForTesting
    @Nonnull
    public MetadataSignerI getMetadataSigner() {
        return metadataSigner;
    }
}
