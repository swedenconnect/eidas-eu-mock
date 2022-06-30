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
@Deprecated
public final class WrappedMetadataFetcher implements MetadataFetcherI {
    public WrappedMetadataFetcher() {
    }

    public WrappedMetadataFetcher(String s, Boolean b) {
    }

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
