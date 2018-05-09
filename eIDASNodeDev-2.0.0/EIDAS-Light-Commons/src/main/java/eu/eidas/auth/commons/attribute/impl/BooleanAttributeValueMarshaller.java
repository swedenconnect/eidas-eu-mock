package eu.eidas.auth.commons.attribute.impl;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;

/**
 * AttributeValueMarshaller for Boolean values.
 *
 * @since 1.1
 */
public final class BooleanAttributeValueMarshaller implements AttributeValueMarshaller<Boolean> {

    @Nonnull
    @Override
    public String marshal(@Nonnull AttributeValue<Boolean> value) {
        return value.getValue().toString();
    }

    @Nonnull
    @Override
    public AttributeValue<Boolean> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion) {
        return new BooleanAttributeValue(Boolean.valueOf(value));
    }
}
