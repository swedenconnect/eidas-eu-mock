package eu.eidas.auth.commons.attribute.impl;

import javax.annotation.Nonnull;

/**
 * Boolean AttributeValue
 *
 * @since 1.1
 */
public final class BooleanAttributeValue extends AbstractAttributeValue<Boolean> {

    public BooleanAttributeValue(@Nonnull Boolean value) {
        super(value, false);
    }
}
