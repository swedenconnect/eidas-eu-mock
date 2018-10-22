package eu.eidas.auth.commons.protocol.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * SAML Name ID Format.
 *
 * @since 1.1
 */
public enum SamlNameIdFormat {

    PERSISTENT("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"),

    TRANSIENT("urn:oasis:names:tc:SAML:2.0:nameid-format:transient"),

    UNSPECIFIED("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");

    private static final EnumMapper<String, SamlNameIdFormat> MAPPER =
            new EnumMapper<String, SamlNameIdFormat>(new KeyAccessor<String, SamlNameIdFormat>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull SamlNameIdFormat samlNameIdFormat) {
                    return samlNameIdFormat.getNameIdFormat();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static SamlNameIdFormat fromString(@Nonnull String value) {
        return MAPPER.fromKey(value);
    }

    public static EnumMapper<String, SamlNameIdFormat> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String nameIdFormat;

    SamlNameIdFormat(@Nonnull String nameIdFormat) {
        this.nameIdFormat = nameIdFormat;
    }

    @Nonnull
    public String getNameIdFormat() {
        return nameIdFormat;
    }

    @Override
    public String toString() {
        return nameIdFormat;
    }
}
