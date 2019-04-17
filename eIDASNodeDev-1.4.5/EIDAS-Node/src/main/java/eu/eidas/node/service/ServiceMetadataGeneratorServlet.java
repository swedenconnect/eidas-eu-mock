/*
 * Copyright (c) 2016 by European Commission
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
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

package eu.eidas.node.service;

import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.connector.AbstractConnectorServlet;
import eu.eidas.node.utils.EidasNodeMetadataGenerator;
import eu.eidas.node.utils.PropertiesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * generates metadata used to communicate with the ProxyService.
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public class ServiceMetadataGeneratorServlet extends AbstractConnectorServlet{
    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServiceMetadataGeneratorServlet.class.getName());
    //TODO: ProxyService requester metadata generator belongs in fact to the Specific module  
    private static final String REQUESTER_METADATA_URL="/ServiceRequesterMetadata";

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    //ProxyService presents itself as both an IdP (to the Connectors) and as an SP (to IdP)

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String generatorName=request.getServletPath().startsWith(REQUESTER_METADATA_URL)?NodeBeanNames.SERVICE_AS_REQUESTER_METADATA_GENERATOR.toString():NodeBeanNames.SERVICE_METADATA_GENERATOR.toString();
        EidasNodeMetadataGenerator generator = (EidasNodeMetadataGenerator)getApplicationContext().getBean(generatorName);
        PropertiesUtil.checkProxyServiceActive();
        if(PropertiesUtil.isMetadataEnabled()) {
            response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(generator.generateServiceMetadata());
        }else{
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
