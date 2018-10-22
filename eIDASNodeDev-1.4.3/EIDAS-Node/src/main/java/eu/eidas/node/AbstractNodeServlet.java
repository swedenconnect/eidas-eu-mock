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

package eu.eidas.node;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.LoggingUtil;

/**
 * Generic EidasNode servlet ancestor.
 *
 * @author vanegdi
 * @since 1.2.2
 */
public abstract class AbstractNodeServlet extends HttpServlet {

    /**
     * Abstract logging impl.
     * @return the concrete logger of implementing servlet
     */
	protected abstract Logger getLogger();

    /**
     * Obtaining the application context
     * @return Node applicationContext
     */
    protected final ApplicationContext getApplicationContext() {
        return ApplicationContextProvider.getApplicationContext();
    }

    /**
     * Method used to renew the http session in traditional web application.
     *
     * @return the new session Id
     */
    private String sessionIdRegenerationInWebApp(HttpServletRequest request) {
        request.getSession(false).invalidate();
        String currentSession = request.getSession(true).getId();
        // Servlet code to renew the session
        getLogger().debug(LoggingMarkerMDC.SECURITY_SUCCESS, "Session RENEWED SessionIdRegenerationInWebApp [domain : {}][path {}][sessionId {}]", request.getServerName(), getServletContext().getContextPath(),currentSession);
        return currentSession;
    }

    /**
     * Sets HTTPOnly Header on the session to prevent cookies from being accessed through
     * client-side script.
     *
     * @param renewSession indicates that the session cookie will be renewed
     */
    @SuppressWarnings("squid:S2254")  // it is just setting param to a cookie
    protected final void setHTTPOnlyHeaderToSession(final boolean renewSession, HttpServletRequest request, HttpServletResponse response) {
        if (request != null && request.getSession(false) != null) {
            // Renewing the session if necessary
            String currentSession;
            String messageLog;
            if (renewSession){
                currentSession = sessionIdRegenerationInWebApp(request);
                messageLog = "http session Renewed : {}";
            } else{
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

                    //TODO: when migrating to servlet 3, use the cookie interface calls below instead of writing the http header
                    //
//NOSONAR                        cookie.setMaxAge(0);
//NOSONAR                        cookie.setPath(getServletContext().getContextPath());
//NOSONAR                 cookie.setDomain(request.getServerName());
//NOSONAR                 cookie.setSecure(isSecure);
//NOSONAR                 cookie.setHttpOnly(true);
//NOSONAR                 response.addCookie(cookie);

                        // Create new one httpOnly
                        StringBuilder httpOnlyCookie = new StringBuilder(cookie.getName()).append(EIDASValues.EQUAL.toString()).append(cookie.getValue()).append(EIDASValues.SEMICOLON.toString()).append(" ")
                                .append(EIDASValues.DOMAIN.toString()).append(EIDASValues.EQUAL.toString()).append(request.getServerName()).append(EIDASValues.SEMICOLON.toString()).append(" ")
                                .append(EIDASValues.PATH.toString()).append(EIDASValues.EQUAL.toString()).append(getServletContext().getContextPath()).append(EIDASValues.SEMICOLON.toString()).append(" ")
                                .append(EIDASValues.HTTP_ONLY.toString()).append(EIDASValues.SEMICOLON.toString())
                                .append(isSecure ? EIDASValues.SECURE.toString()  : "");
                        response.setHeader(EIDASValues.SETCOOKIE.toString(), httpOnlyCookie.toString());
                    }
                }
            }
        } else {
            getLogger().warn(LoggingMarkerMDC.SECURITY_FAILURE, "Request or Session is null !");
        }
    }

    /**
     * Encodes any given URL.
     *
     * @param url The URL to be encoded.
     *
     * @return The encoded URL.
     */
    protected final String encodeURL(final String url, HttpServletResponse response) {
        return response.encodeURL(url);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LoggingUtil.logServletCall(request, this.getClass().getName(), getLogger());
        super.service(request, response);
    }



    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(LoggingMarkerMDC.WEB_EVENT, "GET method invocation : possible spidering");
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(LoggingMarkerMDC.WEB_EVENT, "HEAD method invocation : possible spidering");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(LoggingMarkerMDC.WEB_EVENT, "POST method invocation : possible spidering");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(LoggingMarkerMDC.WEB_EVENT, "DELETE method invocation : possible spidering");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(LoggingMarkerMDC.WEB_EVENT, "PUT method invocation : possible spidering");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(LoggingMarkerMDC.WEB_EVENT, "OPTIONS method invocation : possible spidering");
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(LoggingMarkerMDC.WEB_EVENT, "TRACE method invocation : possible spidering");
    }


    protected final boolean acceptsHttpRedirect(){
        Boolean acceptGet = ApplicationContextProvider.getNodeParameterBool(EidasParameterKeys.ALLOW_REDIRECT_BINDING.toString());
        return acceptGet!=null && acceptGet;
    }
}
