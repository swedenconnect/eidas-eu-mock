package eu.eidas.auth.engine;

import org.joda.time.DateTime;

/**
 * Interface responsible for obtaining the system time.
 */
public interface SamlEngineClock {

    /**
     * Obtains the current time as determined by the clock.
     *
     * @return a DateTime instance representing the current time
     */
    DateTime getCurrentTime();
}
