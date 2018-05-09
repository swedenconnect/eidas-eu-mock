package eu.eidas.auth.commons;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * WebRequest
 *
 * @since 1.1
 */
public interface WebRequest {

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
    BindingMethod getMethod();

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
