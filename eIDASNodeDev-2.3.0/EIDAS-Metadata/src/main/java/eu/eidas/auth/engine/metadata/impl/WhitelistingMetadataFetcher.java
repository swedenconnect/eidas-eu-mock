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
package eu.eidas.auth.engine.metadata.impl;


import java.util.Collection;

import javax.annotation.Nonnull;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataRuntimeException;
import eu.eidas.util.WhitelistUtil;

/**
 * Managed MetadataFetcher with whitelisting functionality
 *
 * @since 1.1
 */
@Component
@Scope("prototype")
public abstract class WhitelistingMetadataFetcher implements MetadataFetcherI {
	
	private Collection<String> whitelistURL;
	private boolean useWhitelist;

	public WhitelistingMetadataFetcher(
			String whitelistURL, 
			Boolean useWhitelist) {
		this.setWhitelistURL( whitelistURL );
		this.setUseWhitelist(useWhitelist);
	}

	public WhitelistingMetadataFetcher() {
	}

	protected final Collection<String> getWhitelistURL() {
		return whitelistURL;
	}

	protected final void setWhitelistURL(String whitelist) {
		Collection<String> whitelistURL;
		whitelistURL=WhitelistUtil.metadataWhitelist(whitelist);
		this.whitelistURL = whitelistURL;
	}

	protected final boolean mustUseWhitelist() {
		return useWhitelist;
	}

	protected final void setUseWhitelist(boolean useWhitelist) {
		this.useWhitelist = useWhitelist;
	}

	protected final void checkWhitelisted(String url){
		if (mustUseWhitelist() && !WhitelistUtil.isWhitelisted(url, getWhitelistURL())) {
	        throw new EIDASMetadataRuntimeException("URL: "+url+" , metadata not in whitelist: ");
	    }
	}

	protected abstract MetadataFetcherI getMetadataFetcher();

    @Nonnull
    public final EidasMetadataParametersI getEidasMetadata(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock)
            throws EIDASMetadataException {
        checkWhitelisted(url);
        return getMetadataFetcher().getEidasMetadata(url, metadataSigner, metadataClock);
    }
}
