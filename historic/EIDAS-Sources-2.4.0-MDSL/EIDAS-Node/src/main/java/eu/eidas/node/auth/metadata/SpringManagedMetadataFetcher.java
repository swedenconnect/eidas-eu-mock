/*
 * Copyright (c) 2019 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence
 */
package eu.eidas.node.auth.metadata;

import static eu.eidas.node.BeanProvider.getBean;

import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.metadata.impl.WhitelistingMetadataFetcher;
import eu.eidas.node.NodeBeanNames;

/**
 * Spring-Managed MetadataFetcher
 *
 * @since 1.1
 */
public final class SpringManagedMetadataFetcher extends WhitelistingMetadataFetcher {

	public SpringManagedMetadataFetcher(String whitelistURL, Boolean useWhitelist) {
		this.setWhitelistURL( whitelistURL );
		this.setUseWhitelist(useWhitelist);
	}

	public SpringManagedMetadataFetcher() {
	}

	@Override
	protected MetadataFetcherI getMetadataFetcher() {
		String beanName = NodeBeanNames.NODE_METADATA_FETCHER.toString();
        return getBean(MetadataFetcherI.class, beanName);
    }

}
