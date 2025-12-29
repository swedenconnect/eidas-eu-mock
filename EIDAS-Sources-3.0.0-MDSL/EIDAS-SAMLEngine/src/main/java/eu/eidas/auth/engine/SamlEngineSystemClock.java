/*
 * Copyright (c) 2024 by European Commission
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
 * limitations under the Licence.
 */
package eu.eidas.auth.engine;

import eu.eidas.auth.engine.metadata.MetadataClockI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * A SamlEngineClock implementation that corresponds with the system clock.
 */
public class SamlEngineSystemClock implements SamlEngineClock, MetadataClockI {
    private static final Logger LOG = LoggerFactory.getLogger(SamlEngineSystemClock.class.getName());

    public ZonedDateTime getCurrentTime() {
        LOG.trace("getCurrentTime");
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
}