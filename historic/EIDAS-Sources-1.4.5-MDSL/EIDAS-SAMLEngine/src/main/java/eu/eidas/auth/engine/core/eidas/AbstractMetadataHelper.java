package eu.eidas.auth.engine.core.eidas;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang.StringUtils;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;

import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;

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

    AbstractMetadataHelper(@Nonnull MetadataFetcherI metadataFetcher, @Nonnull MetadataSignerI metadataSigner) {
        Preconditions.checkNotNull(metadataFetcher, "metadataFetcher");
        Preconditions.checkNotNull(metadataSigner, "metadataSigner");
        this.metadataFetcher = metadataFetcher;
        this.metadataSigner = metadataSigner;
    }

    @Nullable
    EntityDescriptor getEntityDescriptor(@Nullable String metadataUrl) throws EIDASSAMLEngineException {
        if (StringUtils.isNotBlank(metadataUrl)) {
            return metadataFetcher.getEntityDescriptor(metadataUrl, metadataSigner);
        }
        return null;
    }

    @Nullable
    public IDPSSODescriptor getIDPSSODescriptor(@Nullable String metadataUrl) throws EIDASSAMLEngineException {
        EntityDescriptor entityDescriptor = getEntityDescriptor(metadataUrl);
        if (null == entityDescriptor) {
            return null;
        }
        return MetadataUtil.getIDPSSODescriptor(entityDescriptor);
    }

    @Nullable
    public SPSSODescriptor getSPSSODescriptor(@Nullable String metadataUrl) throws EIDASSAMLEngineException {
        EntityDescriptor entityDescriptor = getEntityDescriptor(metadataUrl);
        if (null == entityDescriptor) {
            return null;
        }
        return MetadataUtil.getSPSSODescriptor(entityDescriptor);
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
