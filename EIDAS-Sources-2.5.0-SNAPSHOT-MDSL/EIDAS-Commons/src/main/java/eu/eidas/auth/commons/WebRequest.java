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
