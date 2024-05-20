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

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum BindingMethod {

    GET("GET"),
    POST("POST");

    private static final EnumMapper<String, BindingMethod> MAPPER =
            new EnumMapper<String, BindingMethod>(new KeyAccessor<String, BindingMethod>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull BindingMethod stat) {
                    return stat.getValue();
                }
            }, Canonicalizers.trimUpperCase(), values());

    @Nullable
    public static BindingMethod fromString(@Nonnull String val) {
        return MAPPER.fromKey(val);
    }

    public static EnumMapper<String, BindingMethod> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String value;

    BindingMethod(@Nonnull String val) {
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
