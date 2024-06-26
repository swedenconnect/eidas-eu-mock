/*
 * Copyright (c) 2023 by European Commission
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

package eu.eidas.logging.utils.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.ArrayList;
import java.util.List;

/**
 * The Level filter to be used in the tests.
 */
public class LevelFilter extends Filter<ILoggingEvent> {

    private List<Level> levels = new ArrayList<>();

    /**
     * Constructor that takes as parameter the {@link Level}
     *
     * @param levels: logging levels handled by the filter.
     */
    public LevelFilter(Level... levels) {
        if (levels != null) {
            for (Level level: levels) {
                this.levels.add(level);
            }
        }
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (levels.contains(event.getLevel())) {
            return FilterReply.ACCEPT;
        } else {
            return FilterReply.DENY;
        }
    }
}
