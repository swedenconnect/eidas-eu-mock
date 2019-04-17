/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.node.logging;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Central class used to associate logging marker to the functionality.
 */
public class LoggingUtil {

    private LoggingUtil(){
    }
    public static void logServletCall(HttpServletRequest request, final String className, final Logger logger){
        String sessionId = "N/A";
        HttpSession session = request.getSession(false);
        if (null != session) {
            sessionId = session.getId();
            MDC.put(LoggingMarkerMDC.MDC_SESSIONID, sessionId);
        }
        if (!StringUtils.isEmpty(request.getRemoteHost())) {
            MDC.put(LoggingMarkerMDC.MDC_REMOTE_HOST, request.getRemoteHost());
        }
        logger.info(LoggingMarkerMDC.WEB_EVENT, "**** CALL to servlet " + className
                + " FROM " + request.getRemoteAddr()
                + " HTTP " + request.getMethod()
                + " SESSIONID " + sessionId + "****");
    }

}
