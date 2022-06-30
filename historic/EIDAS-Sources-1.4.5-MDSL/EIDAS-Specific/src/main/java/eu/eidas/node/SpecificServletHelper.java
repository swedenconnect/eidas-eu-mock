package eu.eidas.node;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.validation.NormalParameterValidator;

/**
 * SpecificServletHelper
 *
 * @since 1.1
 */
public class SpecificServletHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificServletHelper.class);

    private SpecificServletHelper() {}

    /**
     * Creates a {@link java.util.Map} with all the parameters from the servletRequest, plus
     * the Remote Address, Remote Host, Local Address and Local Host. Then returns
     * the map.
     *
     * @return A map with the servletRequest's parameters, both the remote and
     * local addresses and the remote and local host.
     * @see java.util.Map
     */
    public static final Map<String, Object> getHttpRequestParameters(HttpServletRequest request) {

        final Map<String, Object> httpParameters = new HashMap<String, Object>();

        // iterate over the parameters
        for (final Object key : request.getParameterMap().keySet()) {
            final String parameterName = (String) key;
            httpParameters.put(parameterName, request.getParameter(parameterName));
        }

        // get the remote address, if the address came from a proxy server
        // then get the original address rather than the proxy address
        String remoteAddr = request.getRemoteAddr();
        if (request.getHeader(EidasParameterKeys.HTTP_X_FORWARDED_FOR.toString()) == null) {
            if (request.getHeader(EidasParameterKeys.X_FORWARDED_FOR.toString()) != null) {
                remoteAddr = request.getHeader(EidasParameterKeys.X_FORWARDED_FOR.toString());
            }
        } else {
            remoteAddr = request.getHeader(EidasParameterKeys.HTTP_X_FORWARDED_FOR.toString());
        }

        NormalParameterValidator.paramName(EidasParameterKeys.REMOTE_ADDR).paramValue(remoteAddr).validate();

        httpParameters.put(EidasParameterKeys.REMOTE_ADDR.toString(), remoteAddr);

        return httpParameters;
    }

    /**
     * Creates a {@link Map} with all the attributes and headers from the
     * servletRequest and then returns it.
     *
     * @param request
     * @return A map with the servletRequest's attributes.
     * @see Map
     */
    @SuppressWarnings("unchecked")
    public static final Map<String, Object> getHttpRequestAttributesHeaders(HttpServletRequest request) {

        final Map<String, Object> reqAttrHeaders = new HashMap<String, Object>();
        // Store servletRequest's attributes
        final Enumeration<String> attibuteNames = request.getAttributeNames();
        while (attibuteNames.hasMoreElements()) {
            final String attrName = attibuteNames.nextElement();
            LOGGER.trace("getHttpRequestAttributesHeader name {} val {} ", attrName, request.getAttribute(attrName));
            if (request.getAttribute(attrName) != null) {
                reqAttrHeaders.put(attrName, request.getAttribute(attrName));
            }
        }

        // Store servletRequest's headers
        final Enumeration<String> headerNames =
                request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            if (request.getHeader(headerName) != null) {
                reqAttrHeaders.put(headerName, request.getHeader(headerName));
            }
        }
        return reqAttrHeaders;
    }
}
