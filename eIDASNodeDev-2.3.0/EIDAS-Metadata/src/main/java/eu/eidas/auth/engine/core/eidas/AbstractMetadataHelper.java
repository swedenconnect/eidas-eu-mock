/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
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
