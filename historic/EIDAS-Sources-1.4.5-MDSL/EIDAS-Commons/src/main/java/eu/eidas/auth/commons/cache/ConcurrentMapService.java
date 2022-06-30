package eu.eidas.auth.commons.cache;

import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public interface ConcurrentMapService {
    /**
     * Obtains the map cache
     * @return a concurrentMap
     */
    ConcurrentMap getConfiguredMapCache();

}
