package eu.eidas.auth.commons.protocol.eidas.impl;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;

/**
 * AttributeValueMarshaller for Gender values.
 *
 * @since 1.1
 */
public final class GenderAttributeValueMarshaller implements AttributeValueMarshaller<Gender> {

    @Nonnull
    @Override
    public String marshal(@Nonnull AttributeValue<Gender> value) {
        return value.getValue().getValue();
    }

    @Nonnull
    @Override
    public AttributeValue<Gender> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion)
            throws AttributeValueMarshallingException {
        Gender gender = Gender.fromString(value);
        if (null == gender) {
            throw new AttributeValueMarshallingException("Illegal gender value \"" + value + "\"");
        }
        return new GenderAttributeValue(gender);
    }
}
