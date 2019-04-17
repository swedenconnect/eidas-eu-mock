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
