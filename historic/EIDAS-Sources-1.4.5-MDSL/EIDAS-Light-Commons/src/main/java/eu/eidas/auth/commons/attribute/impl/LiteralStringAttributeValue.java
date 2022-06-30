package eu.eidas.auth.commons.attribute.impl;

import javax.annotation.Nonnull;

/**
 * An {@link eu.eidas.auth.commons.attribute.AttributeValue} for "literal" {@code String}s which must not be
 * transliterated.
 * <p>
 * <em>Note</em>: using this implementation for an {@link eu.eidas.auth.commons.attribute.AttributeDefinition} where the
 * {@link eu.eidas.auth.commons.attribute.AttributeDefinition#isTransliterationMandatory()} is {@code true} and the
 * supplied value is not in LatinScript would thow an exception in {@link eu.eidas.auth.commons.attribute.ImmutableAttributeMap.Builder#build()}.
 *
 * @since 1.1.1
 */
public final class LiteralStringAttributeValue extends AbstractAttributeValue<String> {

    public LiteralStringAttributeValue(@Nonnull String value) {
        super(value, false);
    }
}
