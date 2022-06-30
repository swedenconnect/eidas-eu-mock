/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */

package eu.eidas.node.utils.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Utils class for LoggerFilter classes.
 */
public class LoggerTestUtils {

    /**
     * Created and starts a {@link ListAppender}
     *
     * @param levelFilter the instance of the {@link LevelFilter}
     * @return the instance of  {@link ListAppender}
     */
    public static ListAppender<ILoggingEvent> createStartListAppender(LevelFilter levelFilter) {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.addFilter(levelFilter);
        listAppender.start();

        return listAppender;
    }

    private LoggerTestUtils(){}
}
