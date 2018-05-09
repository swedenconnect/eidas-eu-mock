package eu.eidas.auth.commons.attribute.impl;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;

/**
 * AttributeValueMarshaller for String values.
 *
 * @since 1.1
 */
public final class StringAttributeValueMarshaller implements AttributeValueMarshaller<String> {

    @Nonnull
    @Override
    public String marshal(@Nonnull AttributeValue<String> value) {
        return value.getValue();
    }

    @Nonnull
    @Override
    public AttributeValue<String> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion) {
        return new StringAttributeValue(value, isNonLatinScriptAlternateVersion);
    }
}
