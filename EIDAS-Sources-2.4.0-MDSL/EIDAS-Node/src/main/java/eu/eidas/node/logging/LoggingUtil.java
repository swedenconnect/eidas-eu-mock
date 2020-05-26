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

package eu.eidas.node.logging;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static eu.eidas.node.logging.AbstractLogger.NOT_APPLICABLE;

/**
 * Central class used to associate logging marker to the functionality.
 */
public class LoggingUtil {

    private LoggingUtil(){
    }
    public static void logServletCall(HttpServletRequest request, final String className, final Logger logger){
        String sessionId = NOT_APPLICABLE;
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
