package eu.eidas.auth.commons;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;
import org.owasp.encoder.Encode;

import eu.eidas.util.Preconditions;

/**
 * An incoming Web Request.
 *
 * @since 1.1
 */
public final class IncomingRequest implements WebRequest {

    @Nonnull
    private final Method method;

    @Nonnull
    private final ImmutableMap<String, ImmutableList<String>> parameterMap;

    @Nonnull
    private final String remoteIpAddress;

    @Nullable
    private final String relayState;

    @Nonnull
    private final RequestState requestState = new IncomingRequestState();

    public IncomingRequest(@Nonnull HttpServletRequest request) {
        Preconditions.checkNotNull(request, "request");
        String httpMethod = request.getMethod();
        Method webMethod = Method.fromString(httpMethod);
        parameterMap = newParameterMap(request);
        method = webMethod;
        remoteIpAddress = getRemoteAddress(request);
        relayState = getRelayStateFromRequest(request);
        if (webMethod == null) {
            throw new IllegalArgumentException("HTTP method \"" + httpMethod + "\" is not supported");
        }
    }

    public IncomingRequest(@Nonnull Method webMethod,
                           @Nonnull ImmutableMap<String, ImmutableList<String>> parameters,
                           @Nonnull String remoteIpAddr,
                           @Nullable String relaySt) {
        method = webMethod;
        parameterMap = parameters;
        remoteIpAddress = remoteIpAddr;
        relayState = relaySt;
    }

    public IncomingRequest(@Nonnull Method webMethod,
                           @Nonnull Map<String, String[]> parameterMap,
                           @Nonnull String remoteIpAddress,
                           @Nullable String relayState) {
        this(webMethod, newParameterMap(parameterMap), remoteIpAddress, relayState);
    }

    @Nullable
    private static String encodeForHtmlAttribute(@Nullable String value) {
        if (null == value) {
            return null;
        }
        if (StringUtils.isBlank(value)) {
            return StringUtils.EMPTY;
        }
        return Encode.forHtmlAttribute(value);
    }

    /**
     * Returns the remote address from the given request taking into account reverse proxy <em>X-FORWARDED-FOR</em>
     * headers.
     *
     * @param request the current request.
     * @return the remote address from the given request taking into account reverse proxy <em>X-FORWARDED-FOR</em>
     * headers.
     * @since 1.1
     */
    @Nonnull
    public static String getRemoteAddress(@Nonnull HttpServletRequest request) {
        // Get the remote address, if the address came from a reverse proxy server
        // then get the original address rather than the reverse proxy address
        final String remoteAddr;
        String xForwardedFor = request.getHeader(EidasParameterKeys.HTTP_X_FORWARDED_FOR.toString());
        if (null != xForwardedFor) {
            remoteAddr = xForwardedFor;
        } else {
            xForwardedFor = request.getHeader(EidasParameterKeys.X_FORWARDED_FOR.toString());
            if (null != xForwardedFor) {
                remoteAddr = xForwardedFor;
            } else {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    /**
     * Returns RelayState if supplied in request
     *
     * @param request the current request.
     * @return the remote address from the given request taking into account reverse proxy <em>X-FORWARDED-FOR</em>
     * headers.
     * @since 1.1
     */
    @Nonnull
    public static String getRelayStateFromRequest(@Nonnull HttpServletRequest request) {
        final String relayState = request.getParameter(EidasParameterKeys.RELAY_STATE.toString());
        return relayState;
    }


    /**
     * Returns an immutable Map of request parameters associated with their corresponding values.
     *
     * @param request the current request
     * @return an immutable Map of request parameters associated with their corresponding values.
     * @since 1.1
     */
    @Nonnull
    private static ImmutableMap<String, ImmutableList<String>> newParameterMap(@Nonnull HttpServletRequest request) {
        Preconditions.checkNotNull(request, "request");
        //noinspection unchecked
        return newParameterMap((Map<String, String[]>) request.getParameterMap());
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    private static ImmutableMap<String, ImmutableList<String>> newParameterMap(
            @Nonnull Map<String, String[]> parameterMap) {
        Preconditions.checkNotNull(parameterMap, "parameterMap");
        ImmutableMap.Builder<String, ImmutableList<String>> mapBuilder =
                new ImmutableMap.Builder<String, ImmutableList<String>>();

        for (final Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String parameterName = entry.getKey();
            String[] values = entry.getValue();
            ImmutableList.Builder<String> listBuilder = new ImmutableList.Builder<String>();
            if (null != values && values.length != 0) {
                for (final String value : values) {
                    listBuilder.add(value);
                }
            } else {
                listBuilder.add(StringUtils.EMPTY);
            }
            mapBuilder.put(parameterName, listBuilder.build());
        }
        return mapBuilder.build();
    }

    @Override
    @Nullable
    public String getEncodedFirstParameterValue(@Nonnull EidasParameterKeys parameter) {
        String value = getFirstParameterValue(parameter);
        return encodeForHtmlAttribute(value);
    }

    @Override
    @Nullable
    public String getEncodedFirstParameterValue(@Nonnull String parameter) {
        String value = getFirstParameterValue(parameter);
        return encodeForHtmlAttribute(value);
    }

    @Override
    @Nullable
    public String getEncodedLastParameterValue(@Nonnull EidasParameterKeys parameter) {
        String value = getLastParameterValue(parameter);
        return encodeForHtmlAttribute(value);
    }

    @Override
    @Nullable
    public String getEncodedLastParameterValue(@Nonnull String parameter) {
        String value = getLastParameterValue(parameter);
        return encodeForHtmlAttribute(value);
    }

    @Override
    @Nullable
    public String getFirstParameterValue(@Nonnull String parameter) {
        ImmutableList<String> parameterValues = getParameterValues(parameter);
        if (null == parameterValues) {
            return null;
        }
        if (parameterValues.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return parameterValues.get(0);
    }

    @Override
    @Nullable
    public String getFirstParameterValue(@Nonnull EidasParameterKeys parameter) {
        Preconditions.checkNotNull(parameter, "parameter");
        return getFirstParameterValue(parameter.toString());
    }

    @Override
    @Nullable
    public String getLastParameterValue(@Nonnull String parameter) {
        ImmutableList<String> parameterValues = getParameterValues(parameter);
        if (null == parameterValues) {
            return null;
        }
        if (parameterValues.isEmpty()) {
            return StringUtils.EMPTY;
        }
        int size = parameterValues.size();
        return parameterValues.get(size - 1);
    }

    @Override
    @Nullable
    public String getLastParameterValue(@Nonnull EidasParameterKeys parameter) {
        Preconditions.checkNotNull(parameter, "parameter");
        return getLastParameterValue(parameter.toString());
    }

    @Override
    @Nonnull
    public Method getMethod() {
        return method;
    }

    @Override
    @Nonnull
    public ImmutableMap<String, ImmutableList<String>> getParameterMap() {
        return parameterMap;
    }

    @Override
    @Nullable
    public ImmutableList<String> getParameterValues(@Nonnull String parameter) {
        Preconditions.checkNotNull(parameter, "parameter");
        return parameterMap.get(parameter);
    }

    @Override
    @Nullable
    public ImmutableList<String> getParameterValues(@Nonnull EidasParameterKeys parameter) {
        Preconditions.checkNotNull(parameter, "parameter");
        return getParameterValues(parameter.toString());
    }

    @Override
    @Nonnull
    public String getRemoteIpAddress() {
        return remoteIpAddress;
    }

    @Nonnull
    @Override
    public RequestState getRequestState() {
        return requestState;
    }

    @Override
    @Nonnull
    public String getRelayState() {
        return relayState;
    }

}
