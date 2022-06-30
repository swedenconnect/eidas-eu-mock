package eu.eidas.auth.commons.attribute.impl;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;

/**
 * DateTime AttributeValue
 *
 * @since 1.1
 */
public final class DateTimeAttributeValue extends AbstractAttributeValue<DateTime> {

    public DateTimeAttributeValue(@Nonnull DateTime value) {
        super(value, false);
    }

    @Override
    public String toString() {
        return DateTimeAttributeValueMarshaller.printDateTime(getValue());
    }
}
