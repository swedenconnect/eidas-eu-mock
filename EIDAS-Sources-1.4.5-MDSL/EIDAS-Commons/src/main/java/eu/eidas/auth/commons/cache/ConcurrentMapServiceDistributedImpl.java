package eu.eidas.auth.commons.cache;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

/**
 * Hazelcast Distributed hashMap implementation of the cache provider.
 */
public class ConcurrentMapServiceDistributedImpl implements ConcurrentMapService {
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentMapServiceDistributedImpl.class.getName());

    protected String cacheName;
    protected HazelcastInstanceInitializer hazelcastInstanceInitializer;

    @Override
    public ConcurrentMap getConfiguredMapCache() {
        if (getCacheName() == null) {
            throw new InvalidParameterEIDASException("Distributed Cache Configuration mismatch");
        }
        HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(hazelcastInstanceInitializer.getHazelcastInstanceName());
        return instance.getMap(getCacheName());
    }

    public String getCacheName() {
        return cacheName;
    }
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public HazelcastInstanceInitializer getHazelcastInstanceInitializer() {
        return hazelcastInstanceInitializer;
    }

    public void setHazelcastInstanceInitializer(HazelcastInstanceInitializer hazelcastInstanceInitializer) {
        this.hazelcastInstanceInitializer = hazelcastInstanceInitializer;
    }

}
