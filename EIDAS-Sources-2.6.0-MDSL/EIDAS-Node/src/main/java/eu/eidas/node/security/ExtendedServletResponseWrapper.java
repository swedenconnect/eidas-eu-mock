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

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * @author vargata
 * @since on 04/02/2016.
 */
public class ExtendedServletResponseWrapper extends HttpServletResponseWrapper  {
    /* This class instantized in CSP Servlet Filter and encapsulates Response original object.
     * Has explicit flag if CSP headers are set during the filtering.
     * used in internalError.jsp */

    private boolean cspHeadersPresent = false;

    public ExtendedServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    public boolean hasCSPHeaders() {
        return cspHeadersPresent;
    }

    public void setCSPHeaders(boolean cspHeadersPresent) {
        this.cspHeadersPresent = cspHeadersPresent;
    }

}
