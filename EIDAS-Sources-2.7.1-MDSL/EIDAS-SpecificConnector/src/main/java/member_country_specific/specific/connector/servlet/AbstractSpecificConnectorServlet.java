/*
 * Copyright (c) 2022 by European Commission
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

package member_country_specific.specific.connector.servlet;

import member_country_specific.specific.connector.logging.LoggingMarkerMDC;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;

import member_country_specific.specific.connector.SpecificConnectorApplicationContextProvider;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Specific Connector servlet ancestor.
 */
public abstract class AbstractSpecificConnectorServlet extends HttpServlet {

    /**
     * Abstract logging impl.
     *
     * @return the concrete logger of implementing servlet
     */
    protected abstract Logger getLogger();

    /**
     * Obtaining the application context
     *
     * @return Node applicationContext
     */
    protected final ApplicationContext getApplicationContext() {
        return SpecificConnectorApplicationContextProvider.getApplicationContext();
    }

    /**
     * Sets HTTPOnly Header on the session to prevent cookies from being accessed through
     * client-side script.
     *
     * @param renewSession indicates that the session cookie will be renewed
     * @param request      the instance of {@link HttpServletRequest}
     * @param response     the instance of {@link HttpServletResponse}
     */
    @SuppressWarnings("squid:S2254")  // it is just setting param to a cookie
    protected final void setHTTPOnlyHeaderToSession(final boolean renewSession, HttpServletRequest request, HttpServletResponse response) {
        if (request != null && request.getSession(false) != null) {
            // Renewing the session if necessary
            String currentSession;
            String messageLog;
            if (renewSession) {
                currentSession = sessionIdRegenerationInWebApp(request);
                messageLog = "http session Renewed : {}";
            } else {
                currentSession = request.getSession().getId();
                messageLog = "http session obtained from request : {}";
            }
            MDC.put(LoggingMarkerMDC.MDC_SESSIONID, currentSession);
            getLogger().info(LoggingMarkerMDC.SECURITY_SUCCESS, messageLog, currentSession);
            // changing session cookie to http only cookie
            if (request.getCookies() != null && request.isRequestedSessionIdFromCookie()) {
                //Session Id requested by the client, obtained from the cookie
                final String requestedSessionId = request.getRequestedSessionId();
                for (Cookie cookie : request.getCookies()) {
                    getLogger().debug("Treating cookie [domain][path][name][value] : [{}][{}][{}][{}]",
                            cookie.getName(),
                            cookie.getPath(),
                            cookie.getName(),
                            cookie.getValue());
                    if (currentSession.equals(requestedSessionId)) {
                        // Removes old version
                        boolean isSecure = request.isSecure();
                        getLogger().debug("Cookie==session : Remove and replacing with HttpOnly {}", cookie.toString());
                        getLogger().debug("Is using SSL?", isSecure);

                        cookie.setMaxAge(0);
                        cookie.setPath(getServletContext().getContextPath());
                        cookie.setDomain(request.getServerName());
                        cookie.setSecure(isSecure);
                        cookie.setHttpOnly(true);
                        response.addCookie(cookie);
                    }
                }
            }
        } else {
            getLogger().warn(LoggingMarkerMDC.SECURITY_FAILURE, "Request or Session is null !");
        }
    }

    /**
     * Method used to renew the http session in traditional web application.
     *
     * @return the new session Id
     */
    private String sessionIdRegenerationInWebApp(HttpServletRequest request) {
        request.getSession(false).invalidate();
        String currentSession = request.getSession(true).getId();
        getLogger().debug(LoggingMarkerMDC.SECURITY_SUCCESS, "Session RENEWED SessionIdRegenerationInWebApp [domain : {}][path {}][sessionId {}]", request.getServerName(), getServletContext().getContextPath(), currentSession);
        return currentSession;
    }

}
