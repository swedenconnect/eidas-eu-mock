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

package eu.eidas.updater.servlet;

import eu.eidas.specificcommunication.SpecificCommunicationApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ContextLoader;

import javax.annotation.Nonnull;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Servlet that reloads the application context that allows update of external properties at runtime.
 *
 * @since 2.0
 */
@WebServlet(urlPatterns={"/updater/refresh"},
        name="nodeUpdater",
        displayName = "nodeUpdater",
        description = "Reloads application context and external properties")
public class UpdaterServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(UpdaterServlet.class.getName());

    private static final long serialVersionUID = -1822077159128167214L;

    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final ApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
        reloadApplicationContext(applicationContext, "Restarting node's application context...");

        final ApplicationContext specificCommunicationApplicationContext = SpecificCommunicationApplicationContextProvider.getApplicationContext();
        reloadApplicationContext(specificCommunicationApplicationContext, "Restarting specific communication application context...");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<pre>Refresh operation launched</pre>");
        out.close();
    }

    private void reloadApplicationContext(@Nonnull final ApplicationContext applicationContext, final String msg) {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableApplicationContext = ((ConfigurableApplicationContext) applicationContext);
            configurableApplicationContext.close();
            configurableApplicationContext.refresh();
            getLogger().debug(msg);
        }

    }
}
