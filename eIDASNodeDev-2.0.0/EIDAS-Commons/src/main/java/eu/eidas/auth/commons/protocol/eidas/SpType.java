/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.auth.commons.protocol.eidas;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;
import eu.eidas.util.Preconditions;

/**
 * Supported SpType as per the eIDAS specification.
 */
public enum SpType {

    PUBLIC("public"),

    PRIVATE("private");

    private static final EnumMapper<String, SpType> MAPPER =
            new EnumMapper<String, SpType>(new KeyAccessor<String, SpType>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull SpType spType) {
                    return spType.getValue();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static SpType fromString(@Nonnull String val) {
        return MAPPER.fromKey(val);
    }

    public static EnumMapper<String, SpType> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String value;

    SpType(@Nonnull String value) {
        Preconditions.checkNotBlank(value, "value");
        this.value = value;
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
