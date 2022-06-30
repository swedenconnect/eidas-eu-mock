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
 * limitations under the Licence.
 *
 */
package eu.eidas.node.utils;

import eu.eidas.node.logging.LoggingMarkerMDC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * SesionLogListener.
 * User: vanegdi
 */
public class SessionLogListener implements HttpSessionListener {
    private static final Logger LOG = LoggerFactory.getLogger(SessionLogListener.class);
    private final AtomicInteger activeSessions;

    // TODO use some metrics like codahale metrics
    public SessionLogListener(){
        super();
        activeSessions = new AtomicInteger();
    }
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions.incrementAndGet();
        MDC.put(LoggingMarkerMDC.MDC_SESSIONID, se.getSession().getId());
        LOG.debug(LoggingMarkerMDC.SECURITY_SUCCESS, "Session created, active # {}", activeSessions.intValue());
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessions.decrementAndGet();
        LOG.debug(LoggingMarkerMDC.SECURITY_SUCCESS, "Session {} destroyed, active # {}", se.getSession().getId(), activeSessions);
    }

    public int getTotalActiveSessions(){
        return activeSessions.intValue();
    }
}