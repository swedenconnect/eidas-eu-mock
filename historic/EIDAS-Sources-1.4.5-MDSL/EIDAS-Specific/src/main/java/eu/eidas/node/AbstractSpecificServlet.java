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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import eu.eidas.auth.commons.EIDASValues;


public abstract class AbstractSpecificServlet extends HttpServlet {
    private static final long serialVersionUID = -1223043917139819408L;

    /*Dedicated marker for the web events*/
    public static final Marker WEB_EVENT = MarkerFactory.getMarker("WEB_EVENT");
    public static final String MDC_SESSIONID = "sessionId";
    public static final String MDC_REMOTE_HOST = "remoteHost";

    abstract protected Logger getLogger();

    /**
     * Obtaining the application context
     *
     * @return applicationContext
     */
    protected ApplicationContext getApplicationContext() {
        return WebApplicationContextUtils.getWebApplicationContext(getServletContext());
    }

    /**
     * Encodes any given URL.
     *
     * @param url      The URL to be encoded.
     * @param request
     * @param response @return The encoded URL.
     */
    protected final String encodeURL(final String url, HttpServletRequest request, HttpServletResponse response) {

        if (request.getSession(false) == null) {
            // If the session doesn't exist, then we must create it.
            request.getSession();
        }
        return response.encodeURL(url);
    }

    /**
     * Sets HTTPOnly Header to prevent cookies from being accessed through
     * client-side script.
     */
    protected final void setHTTPOnlyHeader(HttpServletRequest request, HttpServletResponse response) {

        if (request.getSession() == null || request.getSession(false) == null) {
            // If the session doesn't exist, then we must create it.
            request.getSession();
            // We will set the value only if we didn't set it already.
            if (!response.containsHeader(EIDASValues.SETCOOKIE.toString())) {
                response.setHeader(EIDASValues.SETCOOKIE.toString(),
                        createHttpOnlyCookie(request));
            }
        }
    }

    /**
     * Creates the HttpOnly cookie string.
     *
     * @param request
     * @return The HttpOnly cookie.
     */
    private String createHttpOnlyCookie(HttpServletRequest request) {
        final StringBuilder strBuf = new StringBuilder();
        strBuf.append(EIDASValues.JSSESSION.toString());
        strBuf.append(EIDASValues.EQUAL.toString());
        strBuf.append(request.getSession().getId());
        strBuf.append(EIDASValues.SEMICOLON.toString());
        strBuf.append(EIDASValues.SPACE.toString());
        strBuf.append(EIDASValues.HTTP_ONLY.toString());
        return strBuf.toString();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!StringUtils.isEmpty(request.getRemoteHost())) {
            MDC.put(MDC_REMOTE_HOST, request.getRemoteHost());
        }
        MDC.put(MDC_SESSIONID, request.getSession().getId());
        getLogger().info(WEB_EVENT, "**** CALL to servlet " + this.getClass().getName()
                + "FROM " + request.getRemoteAddr()
                + "HTTP " + request.getMethod()
                + " SESSIONID " + request.getSession().getId() + "****");

        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(WEB_EVENT, "GET method invocation : possible spidering");
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(WEB_EVENT, "HEAD method invocation : possible spidering");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(WEB_EVENT, "POST method invocation : possible spidering");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(WEB_EVENT, "DELETE method invocation : possible spidering");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(WEB_EVENT, "PUT method invocation : possible spidering");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(WEB_EVENT, "OPTIONS method invocation : possible spidering");
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().warn(WEB_EVENT, "TRACE method invocation : possible spidering");
    }
}
