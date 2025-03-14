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
package eu.eidas.auth.commons.attribute;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * PersonType
 *
 * [Subject] &lt;---([Representation])---&gt; [Representative]
 * + Other
 *
 * @since 1.1
 */
public enum PersonType {

    /**
     * Requested natural person, subject.
     * Natural Person whom the transaction is about.
     */
    NATURAL_PERSON("NaturalPerson"),

    /**
     * Requested legal Person, subject.
     * Legal Entity whom the transaction is about.
     */
    LEGAL_PERSON("LegalPerson"),

    /**
     * Natural representative.
     * Natural person who is authenticating to represent another entity.
     */
    REPV_NATURAL_PERSON("RepresentativeNaturalPerson"),

    /**
     * Legal representative.
     * Legal person who is authenticating to represent another entity.
     */
    REPV_LEGAL_PERSON("RepresentativeLegalPerson"),

    /**
     * Representation of the subject
     * Relationship between subject and Representative.
     */
    REPRESENTATION("Representation"),

    /**
     * Other.
     * Attributes that exist unrelated to above entities.
     */
    OTHER("Other")
    ;


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
