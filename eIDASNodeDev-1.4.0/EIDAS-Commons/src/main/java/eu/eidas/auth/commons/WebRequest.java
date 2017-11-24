package eu.eidas.auth.commons;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * WebRequest
 *
 * @since 1.1
 */
public interface WebRequest {

    enum Method {

        GET("GET"),
        POST("POST");

        private static final EnumMapper<String, IncomingRequest.Method> MAPPER =
                new EnumMapper<String, IncomingRequest.Method>(new KeyAccessor<String, IncomingRequest.Method>() {

                    @Nonnull
                    @Override
                    public String getKey(@Nonnull Method stat) {
                        return stat.getValue();
                    }
                }, Canonicalizers.trimUpperCase(), values());

        @Nullable
        public static Method fromString(@Nonnull String val) {
            return MAPPER.fromKey(val);
        }

        public static EnumMapper<String, IncomingRequest.Method> mapper() {
            return MAPPER;
        }

        @Nonnull
        private final transient String value;

        Method(@Nonnull String val) {
            value = val;
        }

        @Nonnull
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Nullable
    String getEncodedFirstParameterValue(@Nonnull EidasParameterKeys parameter);

    @Nullable
    String getEncodedFirstParameterValue(@Nonnull String parameter);

    @Nullable
    String getEncodedLastParameterValue(@Nonnull EidasParameterKeys parameter);

    @Nullable
    String getEncodedLastParameterValue(@Nonnull String parameter);

    @Nullable
    String getFirstParameterValue(@Nonnull String parameter);

    @Nullable
    String getFirstParameterValue(@Nonnull EidasParameterKeys parameter);

    @Nullable
    String getLastParameterValue(@Nonnull String parameter);

    @Nullable
    String getLastParameterValue(@Nonnull EidasParameterKeys parameter);

    @Nonnull
    Method getMethod();

    @Nonnull
    ImmutableMap<String, ImmutableList<String>> getParameterMap();

    @Nullable
    ImmutableList<String> getParameterValues(@Nonnull String parameter);

    @Nullable
    ImmutableList<String> getParameterValues(@Nonnull EidasParameterKeys parameter);

    @Nonnull
    String getRemoteIpAddress();

    @Nonnull
    RequestState getRequestState();

    @Nonnull
    String getRelayState();

}
