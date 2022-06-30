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
