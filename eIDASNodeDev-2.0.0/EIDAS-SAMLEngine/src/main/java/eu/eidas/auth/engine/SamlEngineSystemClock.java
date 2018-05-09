package eu.eidas.auth.engine;

import eu.eidas.auth.engine.metadata.MetadataClockI;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SamlEngineClock implementation that corresponds with the system clock.
 */
public class SamlEngineSystemClock implements SamlEngineClock, MetadataClockI {
    private static final Logger LOG = LoggerFactory.getLogger(SamlEngineSystemClock.class.getName());

    public DateTime getCurrentTime() {
        LOG.trace("getCurrentTime");
        return new DateTime(DateTimeZone.UTC);
    }
}