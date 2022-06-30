package eu.eidas.auth.commons.attribute.impl;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.attribute.AttributeValueTransliterator;

/**
 * String AttributeValue.
 *
 * @since 1.1
 */
public final class StringAttributeValue extends AbstractAttributeValue<String> {

    public StringAttributeValue(@Nonnull String value) {
        super(value, AttributeValueTransliterator.needsTransliteration(value));
    }

    /**
     * Constructor.
     *
     * @param value the value
     * @param isNonLatinScriptAlternateVersion whether the given value is a non-LatinScript alternate version of another
     * value in LatinScript.
     * @deprecated since 1.1.1, use {@link #StringAttributeValue(String)} instead.
     */
    @Deprecated
    public StringAttributeValue(@Nonnull String value, boolean isNonLatinScriptAlternateVersion) {
        this(value);
        if (isNonLatinScriptAlternateVersion() && !isNonLatinScriptAlternateVersion) {
            throw new IllegalArgumentException("Illegal argument: value \"" + value
                                                       + "\" is non LatinScript but isNonLatinScriptAlternateVersion = false");
        }
    }
}
