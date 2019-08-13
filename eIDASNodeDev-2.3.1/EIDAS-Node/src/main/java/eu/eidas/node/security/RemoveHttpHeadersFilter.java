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
package eu.eidas.node.security;

import eu.eidas.node.logging.LoggingMarkerMDC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * This filter remove unwanted http headers sent by the server
 * <br>
 *
 * Purposes :
 *
 * Remove X-Powered-By header to avoid revealing information about software
 *
 * @since 1.2.0
 */
public class RemoveHttpHeadersFilter implements Filter {
    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveHttpHeadersFilter.class.getName());

    @Override
    public void init(FilterConfig fConfig) throws ServletException {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Init of RemoveHttpHeadersFilter filter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain fchain) throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            LOGGER.trace("RemoveHttpHeadersFilter FILTER for " + httpRequest.getServletPath());

            httpResponse.setHeader("X-Powered-By","");
            httpResponse.setHeader("Server","");

            fchain.doFilter(request, response);
        }catch(Exception e){
            LOGGER.info("ERROR : ",e.getMessage());
            LOGGER.debug("ERROR : ",e);
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Destroy of RemoveHttpHeadersFilter filter");
    }
}
