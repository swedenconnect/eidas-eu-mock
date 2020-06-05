package eu.eidas.node.security;

import eu.eidas.node.AbstractNodeServlet;
import eu.eidas.node.logging.LoggingMarkerMDC;

import org.owasp.esapi.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

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
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String line;
    try {
      setHTTPOnlyHeaderToSession(false, request, response);
      BufferedReader reader = request.getReader();
      while ((line=reader.readLine()) != null){
        if (StringUtilities.notNullOrEmpty(line,false)){
          LOG.info(LoggingMarkerMDC.SECURITY_WARNING, "BUSINESS EXCEPTION : Content security violation : " + line);
        }
      }
    } catch (IOException e) {
      LOG.error(e.getMessage()+" {}", e);
    }
  }
}
