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
package eu.eidas.auth.commons.tx;

import eu.eidas.auth.commons.cache.ConcurrentMapService;

/**
 * Represents an eIDAS transaction between the ServiceProvider and the eIDAS Connector.
 * <p>
 * This class is used to correlate incoming requests sent by the ServiceProvider to asynchronous responses received from
 * eIDAS ProxyServices.
 *
 * @since 1.1
 */
public final class StoredAuthenticationRequestCorrelationMap extends AbstractCorrelationMap<StoredAuthenticationRequest> {

    public StoredAuthenticationRequestCorrelationMap(final ConcurrentMapService concurrentMapService) {
        super(concurrentMapService);
    }
}
