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
package eu.eidas.engine.test.simple;

import eu.eidas.auth.engine.SamlEngineClock;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import org.joda.time.DateTime;

/**
 * A SamlEngineClock test implementation that can set its time relative to the system clock.
 */

public class SamlEngineTestClock implements SamlEngineClock, MetadataClockI {
    private long delta = 0;

    public DateTime getCurrentTime() {
        return new DateTime().plus(delta);
    }

    /**
     * Sets the delta time that this clock deviates from the system clock.
     *
     * @param deltaTime the delta time in milliseconds
     */
    public void setDelta(long deltaTime) {
        delta = deltaTime;
    }
}
