/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
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
package eu.eidas.auth.commons.attribute;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * PersonType
 *
 * @since 1.1
 */
public enum PersonType {

    NATURAL_PERSON("NaturalPerson"),
    LEGAL_PERSON("LegalPerson"),
    REPV_NATURAL_PERSON("RepresentativeNaturalPerson"),
    REPV_LEGAL_PERSON("RepresentativeLegalPerson");

    private static final EnumMapper<String, PersonType> MAPPER =
            new EnumMapper<String, PersonType>(new KeyAccessor<String, PersonType>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull PersonType type) {
                    return type.getValue();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static PersonType fromString(@Nonnull String val) {
        return MAPPER.fromKey(val);
    }

    public static EnumMapper<String, PersonType> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String value;

    PersonType(@Nonnull String value) {
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
