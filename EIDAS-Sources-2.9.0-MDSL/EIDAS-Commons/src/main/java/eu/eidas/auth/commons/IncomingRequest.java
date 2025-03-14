/*
 * Copyright (c) 2024 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.eidas.auth.commons;

import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.owasp.encoder.Encode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An incoming Web Request.
 *
 * @since 1.1
 */
public final class IncomingRequest implements WebRequest {

    @Nonnull
    private final BindingMethod method;

    @Nonnull
    private final Map<String, List<String>> parameterMap;

    @Nonnull
    private final String remoteIpAddress;

    @Nullable
    private final String relayState;

    @Nonnull
    private final RequestState requestState = new IncomingRequestState();

    public IncomingRequest(@Nonnull HttpServletRequest request) {
        Preconditions.checkNotNull(request, "request");
        String httpMethod = request.getMethod();
        BindingMethod webMethod = BindingMethod.fromString(httpMethod);
        parameterMap = Map.copyOf(newParameterMap(request));
        method = webMethod;
        remoteIpAddress = getRemoteAddress(request);
        relayState = getRelayStateFromRequest(request);
        if (webMethod == null) {
            throw new IllegalArgumentException("HTTP method \"" + httpMethod + "\" is not supported");
        }
    }

    public IncomingRequest(@Nonnull BindingMethod webMethod,
                           @Nonnull Map<String, List<String>> parameters,
                           @Nonnull String remoteIpAddr,
                           @Nullable String relaySt) {
        method = webMethod;
        parameterMap = Map.copyOf(parameters);
        remoteIpAddress = remoteIpAddr;
        relayState = relaySt;
    }

    public IncomingRequest(@Nonnull BindingMethod webMethod,
                           @Nonnull List<Map.Entry<String, String[]>> parameterEntries,
                           @Nonnull String remoteIpAddress,
                           @Nullable String relayState) {
        this(webMethod, convertEntriesToMap(parameterEntries), remoteIpAddress, relayState);
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
        String xForwardedFor = request.getHeader(EidasParameterKeys.X_FORWARDED_FOR.toString());
        if (null != xForwardedFor) {
            remoteAddr = xForwardedFor;
        } else {
            remoteAddr = request.getRemoteAddr();
        }

        return remoteAddr;
    }

    /**
     * Returns RelayState if supplied in request
     *
     * @param request the current request.
     * @return the last RelayState value in the list of values associated with {link EidasParameterKeys.RELAY_STATE} Http parameter
     */
    @Nonnull
    public static String getRelayStateFromRequest(@Nonnull HttpServletRequest request) {

        Map<String, List<String>> parameterMap = newParameterMap(request.getParameterMap());
        List<String> parameterValues = parameterMap.get(EidasParameterKeys.RELAY_STATE.toString());

        if (null == parameterValues) {
            return StringUtils.EMPTY;
        }
        if (parameterValues.isEmpty()) {
            return StringUtils.EMPTY;
        }
        int size = parameterValues.size();
        String relayState = parameterValues.get(size - 1);
        return encodeForHtmlAttribute(relayState);
    }


    /**
     * Returns an immutable Map of request parameters associated with their corresponding values.
     *
     * @param request the current request
     * @return an immutable Map of request parameters associated with their corresponding values.
     * @since 1.1
     */
    @Nonnull
    private static Map<String, List<String>> newParameterMap(@Nonnull HttpServletRequest request) {
        Preconditions.checkNotNull(request, "request");
        //noinspection unchecked
        return newParameterMap((Map<String, String[]>) request.getParameterMap());
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    private static Map<String, List<String>> newParameterMap(
            @Nonnull Map<String, String[]> parameterMap) {
        Preconditions.checkNotNull(parameterMap, "parameterMap");
        Map<String, List<String>> map = new LinkedHashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String parameterName = entry.getKey();
            String[] values = entry.getValue();
            List<String> valueList = new ArrayList<>();
            if (values != null && values.length > 0) {
                Collections.addAll(valueList, values);
            } else {
                valueList.add(StringUtils.EMPTY);
            }
            map.put(parameterName, valueList);
        }
        return map;
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
        List<String> parameterValues = getParameterValues(parameter);
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
        List<String> parameterValues = getParameterValues(parameter);
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

    /**
     * Converts a list of map entries with array values to a map with list values.
     *
     * @param entries the list of map entries where each entry has a key and an array of strings as its value
     * @return a map where each key is associated with a list of strings
     * @throws NullPointerException if the entries list is null
     */
    private static Map<String, List<String>> convertEntriesToMap(@Nonnull List<Map.Entry<String, String[]>> entries) {
        Map<String, List<String>> map = new HashMap<>();
        for (Map.Entry<String, String[]> entry : entries) {
            map.put(entry.getKey(), List.of(entry.getValue()));
        }
        return map;
    }

    @Override
    @Nonnull
    public BindingMethod getMethod() {
        return method;
    }

    @Override
    @Nonnull
    public Map<String, List<String>> getParameterMap() {
        return parameterMap;
    }

    @Override
    @Nullable
    public List<String> getParameterValues(@Nonnull String parameter) {
        Preconditions.checkNotNull(parameter, "parameter");
        return parameterMap.get(parameter);
    }

    @Override
    @Nullable
    public List<String> getParameterValues(@Nonnull EidasParameterKeys parameter) {
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
