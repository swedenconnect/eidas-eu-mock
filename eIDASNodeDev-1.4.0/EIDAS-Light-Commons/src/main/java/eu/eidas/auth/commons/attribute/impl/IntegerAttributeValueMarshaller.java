package eu.eidas.auth.commons.attribute.impl;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;

/**
 * AttributeValueMarshaller for Integer values.
 *
 * @since 1.1
 */
public final class IntegerAttributeValueMarshaller implements AttributeValueMarshaller<Integer> {

    @Nonnull
    @Override
    public String marshal(@Nonnull AttributeValue<Integer> value) {
        return value.getValue().toString();
    }

    @Nonnull
    @Override
    public AttributeValue<Integer> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion)
            throws AttributeValueMarshallingException {
        try {
            return new IntegerAttributeValue(Integer.valueOf(value));
        } catch (NumberFormatException nfe) {
            throw new AttributeValueMarshallingException(nfe);
        }
    }
}
