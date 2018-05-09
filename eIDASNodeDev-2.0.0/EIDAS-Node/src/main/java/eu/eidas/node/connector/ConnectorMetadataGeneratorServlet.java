/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the
 * "NOTICE" text file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution.
 * Any derivative works that you distribute must include a readable
 * copy of the "NOTICE" text file.
 */

package eu.eidas.node.connector;

import eu.eidas.node.AbstractNodeServlet;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.utils.EidasNodeMetadataGenerator;
import eu.eidas.node.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * generates metadata used to communicate with the Connector.
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public class ConnectorMetadataGeneratorServlet extends AbstractNodeServlet {
    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConnectorMetadataGeneratorServlet.class.getName());

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String generatorName = NodeBeanNames.CONNECTOR_METADATA_GENERATOR.toString();
        EidasNodeMetadataGenerator generator = (EidasNodeMetadataGenerator)getApplicationContext().getBean(generatorName);
        PropertiesUtil.checkConnectorActive();
        if(PropertiesUtil.isMetadataEnabled()) {
            ConnectorControllerService controllerService = (ConnectorControllerService) getApplicationContext().getBean(
                    NodeBeanNames.EIDAS_CONNECTOR_CONTROLLER.toString());
            response.getOutputStream().print(generator.generateConnectorMetadata(controllerService.getConnectorService().getSamlService().getSamlEngine()));
        }else{
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
