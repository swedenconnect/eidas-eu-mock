/* 
*   Copyright (c) 2019 European Commission
*   Licensed under the EUPL, Version 1.2 or – as soon they will be
*   approved by the European Commission - subsequent versions of the
*    EUPL (the "Licence");
*    You may not use this work except in compliance with the Licence.
*    You may obtain a copy of the Licence at:
*    * https://joinup.ec.europa.eu/page/eupl-text-11-12
*    *
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the Licence is distributed on an "AS IS" basis,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the Licence for the specific language governing permissions and limitations under the Licence.
*/
package eu.eidas.node.security;

import eu.eidas.node.AbstractNodeServlet;
import eu.eidas.node.logging.LoggingMarkerMDC;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static eu.eidas.node.security.SecurityResponseHeaderHelper.CONTENT_SECURITY_POLICY_HEADER;

/**
 * @author mariger
 * @since on 12/11/2014.
 */
public class ContentSecurityPolicyReportServlet extends AbstractNodeServlet {
    private static final long serialVersionUID = -8979896410913801382L;

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ContentSecurityPolicyReportServlet.class.getName());

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {


        setHTTPOnlyHeaderToSession(false, request, response);

        String cspHeader = response.getHeader(CONTENT_SECURITY_POLICY_HEADER);
        if (!StringUtils.contains(cspHeader, "report-uri")) {
            // Blocking direct call to the Servlet uri when CSP directive is deactivated.
            response.sendError((HttpStatus.SC_FORBIDDEN));
            return;
        }

        String line;

        int read;
        final int MAX_SIZE = 200000;
        char[] buffer = new char[MAX_SIZE];

        try (BufferedReader reader = request.getReader()) {
            if ((read = reader.read(buffer, 0, MAX_SIZE)) != -1) {
                BufferedReader newReader = new BufferedReader(new StringReader(String.valueOf(buffer).substring(0, read)));
                while ((line = newReader.readLine()) != null) {
                    String stringToLog = String.format("%s BUSINESS EXCEPTION : Content security violation : %s",
                            LoggingMarkerMDC.SECURITY_WARNING, line);
                    LOG.info(stringToLog);
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage() + " {}", e);
        }
    }
}
