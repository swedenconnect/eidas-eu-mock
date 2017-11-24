package eu.eidas.node.security;

import eu.eidas.node.logging.LoggingMarkerMDC;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import java.io.IOException;
import java.rmi.AccessException;
import java.security.SecureRandom;

/**
 * Token class used to include a anti CSRF cryptographically random token with form submission.
 * See https://www.owasp.org/index.php/Cross-Site_Request_Forgery_%28CSRF%29_Prevention_Cheat_Sheet
 *
 * @author vanegdi
 * @since 1.2.2
 */
public class Token extends TagSupport {
    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Token.class.getName());
    public static final int SIZE_TWENTY = 20;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final String TOKEN_NAME = "token";

    private String getBase64CrytographicallyRandomToken(int size) {
        byte[] randomBytes = new byte[size];

        secureRandom.nextBytes(randomBytes);


        String pseudoRandomString = Base64.encodeBase64String(randomBytes).replaceAll("\r", "").replaceAll("\n", "").replace('+', '_').replace('/', '-').replace('=', '$');

        return pseudoRandomString;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            ServletRequest servletRequest = pageContext.getRequest();
            HttpSession httpSession = ((HttpServletRequest) servletRequest).getSession();
            // Check if a token already exists in session
            String token = (String) httpSession.getAttribute(TOKEN_NAME);
            if (StringUtils.isEmpty(token)) {
                token = getBase64CrytographicallyRandomToken(SIZE_TWENTY);
                httpSession.setAttribute(TOKEN_NAME, token);
                LOGGER.debug("Generating " + token + " for " + servletRequest.getRemoteAddr());
            }
            // Building the output tag
            JspWriter jspWriter = pageContext.getOut();
            jspWriter.println();
            jspWriter.print("<input type=\"hidden\" ");
            jspWriter.print(" name=\"" + TOKEN_NAME + "\" ");
            jspWriter.print(" value=\"" + token + "\" />");
        } catch (IOException ex) {
            LOGGER.info("Error in doStartTag {}", ex.getMessage());
            LOGGER.debug("Error in doStartTag {}", ex);
        }
        return SKIP_BODY;
    }

    public static void checkToken(HttpServletRequest httpServletRequest) throws AccessException {
        HttpSession httpSession = httpServletRequest.getSession();
        // Obtaining the session token
        String sessionToken = (String) httpSession.getAttribute(TOKEN_NAME);
        // Obtaining the request token
        String requestToken = (String) httpServletRequest.getParameter(TOKEN_NAME);
        // Check matching values
        if (StringUtils.isEmpty(sessionToken) || StringUtils.isEmpty(requestToken) || !sessionToken.equals(requestToken)) {
            LOGGER.info(LoggingMarkerMDC.SECURITY_WARNING, "CSRF Token is missing or invalid (Session/request) " + sessionToken + "/" + requestToken);
            throw new AccessException("CSRF Token is missing");
        }
    }
}
