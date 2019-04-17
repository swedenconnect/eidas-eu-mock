package eu.eidas.auth.commons.tx;

import eu.eidas.auth.commons.cache.ConcurrentMapService;

/**
 * Default implementation of the CorrelationMap for specific LightRequest instances.
 *
 * @since 1.1
 */
public final class StoredLightRequestCorrelationMap extends AbstractCorrelationMap<StoredLightRequest> {

    public StoredLightRequestCorrelationMap(final ConcurrentMapService concurrentMapService) {
        super(concurrentMapService);
    }
}
