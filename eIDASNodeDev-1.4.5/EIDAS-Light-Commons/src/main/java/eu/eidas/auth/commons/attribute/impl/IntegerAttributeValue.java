package eu.eidas.auth.commons.attribute.impl;

import javax.annotation.Nonnull;

/**
 * Integer AttributeValue
 *
 * @since 1.1
 */
public final class IntegerAttributeValue extends AbstractAttributeValue<Integer> {

    public IntegerAttributeValue(@Nonnull Integer value) {
        super(value, false);
    }
}
