package eu.eidas.auth.commons.protocol.eidas.impl;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.attribute.impl.AbstractAttributeValue;

/**
 * eIDAS Gender AttributeValue
 *
 * @since 1.1
 */
public final class GenderAttributeValue extends AbstractAttributeValue<Gender> {

    public GenderAttributeValue(@Nonnull Gender value) {
        super(value, true);
    }
}
