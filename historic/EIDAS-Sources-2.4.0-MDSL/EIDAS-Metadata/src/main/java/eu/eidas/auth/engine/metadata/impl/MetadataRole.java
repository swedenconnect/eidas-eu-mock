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
package eu.eidas.auth.engine.metadata.impl;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum MetadataRole {
    IDP("IDP"),
    SP("SP");

    private static final EnumMapper<String, MetadataRole> MAPPER =
            new EnumMapper<>(new KeyAccessor<String, MetadataRole>() {
                @Nonnull
                @Override
                public String getKey(@Nonnull MetadataRole type) {
                    return type.getValue();
                }
            }, Canonicalizers.trimLowerCase(), values());
    @Nonnull
    private final transient String value;

    MetadataRole(@Nonnull String value) {
        this.value = value;
    }

    @Nullable
    public static MetadataRole fromString(@Nonnull String val) {
        return MAPPER.fromKey(val);
    }

    public static EnumMapper<String, MetadataRole> mapper() {
        return MAPPER;
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
