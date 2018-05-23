package se.swedenconnect.eidas.test.cef20demohub.process;

import javax.servlet.http.HttpServletRequest;

public class StaticUtils {

    /**
     * Retrieves the original client IP-adress. Chooses the X-FORWARDED-FOR adress if present or
     * Otherwise chooses the originating ip address.
     * @param request The HttpServlet request
     * @return The remote ip address
     */
    public static final String getRemoteIpAdress(HttpServletRequest request){
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
