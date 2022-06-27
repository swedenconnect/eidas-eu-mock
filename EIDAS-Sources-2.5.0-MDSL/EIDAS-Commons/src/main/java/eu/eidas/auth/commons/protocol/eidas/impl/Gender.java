/*
 * Copyright (c) 2020 by European Commission
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
package eu.eidas.auth.commons.protocol.eidas.impl;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Gender as per the eIDAS spec.
 * <br>
 * <pre>
 *     &lt;xsd:simpleType name="GenderType"&gt;
 *         &lt;xsd:annotation&gt;
 *             &lt;xsd:documentation&gt; Gender of the natural person.&lt;/xsd:documentation&gt;
 *         &lt;/xsd:annotation&gt;
 *         &lt;xsd:restriction base="xsd:string"&gt;
 *             &lt;xsd:enumeration value="Male"/&gt;
 *             &lt;xsd:enumeration value="Female"/&gt;
 *             &lt;xsd:enumeration value="Unspecified"/&gt;
 *         &lt;/xsd:restriction&gt;
 *     &lt;/xsd:simpleType&gt;
 * </pre>
 *
 * @since 1.1
 */
public enum Gender {

    MALE("Male"),

    FEMALE("Female"),

    UNSPECIFIED("Unspecified"),

    // Technical specifications 1.1
    NOT_SPECIFIED("Not Specified")
    ;

    private static final EnumMapper<String, Gender> MAPPER =
            new EnumMapper<String, Gender>(new KeyAccessor<String, Gender>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull Gender gender) {
                    return gender.getValue();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static Gender fromString(@Nonnull String val) {
        return MAPPER.fromKey(val);
    }

    public static EnumMapper<String, Gender> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String value;

    Gender(@Nonnull String value) {
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
