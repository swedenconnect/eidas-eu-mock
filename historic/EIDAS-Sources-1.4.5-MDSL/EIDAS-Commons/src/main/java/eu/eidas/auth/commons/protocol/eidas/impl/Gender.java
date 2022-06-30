package eu.eidas.auth.commons.protocol.eidas.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * Gender as per the eIDAS spec.
 * <p>
 * <pre>
 *     <xsd:simpleType name="GenderType">
 *         <xsd:annotation>
 *             <xsd:documentation> Gender of the natural person.</xsd:documentation>
 *         </xsd:annotation>
 *         <xsd:restriction base="xsd:string">
 *             <xsd:enumeration value="Male"/>
 *             <xsd:enumeration value="Female"/>
 *             <xsd:enumeration value="Unspecified"/>
 *         </xsd:restriction>
 *     </xsd:simpleType>
 * </pre>
 *
 * @since 1.1
 */
public enum Gender {

    MALE("Male"),

    FEMALE("Female"),

    UNSPECIFIED("Unspecified"),

    //TODO "Not Specified" is a temporary allowed value to avoid interoperability issues, it will be removed in future -->
    NOT_SPECIFIED("Not Specified");

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
