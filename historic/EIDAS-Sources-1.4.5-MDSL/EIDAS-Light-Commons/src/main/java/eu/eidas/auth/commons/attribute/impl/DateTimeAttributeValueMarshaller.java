package eu.eidas.auth.commons.attribute.impl;

import java.util.Locale;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;

/**
 * AttributeValueMarshaller for DateTime values.
 *
 * @since 1.1
 */
public final class DateTimeAttributeValueMarshaller implements AttributeValueMarshaller<DateTime> {

    private static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd")
            .withChronology(ISOChronology.getInstance())
            .withLocale(Locale.ENGLISH)
            .withZoneUTC();

    public static String printDateTime(@Nonnull DateTime dateTime) {
        return FORMAT.print(dateTime);
    }

    @Nonnull
    @Override
    public String marshal(@Nonnull AttributeValue<DateTime> value) {
        return printDateTime(value.getValue());
    }

    @Nonnull
    @Override
    public AttributeValue<DateTime> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion)
            throws AttributeValueMarshallingException {
        try {
            return new DateTimeAttributeValue(FORMAT.parseDateTime(value));
        } catch (IllegalArgumentException iae) {
            throw new AttributeValueMarshallingException(iae);
        }
    }
}
