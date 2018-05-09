/*
 * Copyright (c) 2017 by European Commission
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

package member_country_specific.specific.proxyservice.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Servlet that reloads the application context that allows update of external properties at runtime.
 *
 * @since 2.0
 */
@WebServlet(urlPatterns={"/spsupdater/refresh"},
        name="specificProxyServiceUpdater",
        displayName = "specificProxyServiceUpdater",
        description = "Reloads application context and external properties")
public class UpdaterServlet extends AbstractSpecificProxyServiceServlet {

    private static final Logger LOG = LoggerFactory.getLogger(UpdaterServlet.class.getName());

    private static final long serialVersionUID = -1822077159128167214L;

    /**
     * Abstract logging impl.
     *
     * @return the concrete logger of implementing servlet
     */
    @Override
    protected Logger getLogger() {
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOG.debug("Restarting specific proxy service application context...");
        ApplicationContext applicationContext = getApplicationContext();
        ((ConfigurableApplicationContext) applicationContext).close();
        ((ConfigurableApplicationContext) applicationContext).refresh();

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<pre>Refresh operation launched</pre>");
        out.close();
    }
}
