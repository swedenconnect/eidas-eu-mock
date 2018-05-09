package eu.eidas.auth.commons.attribute.impl;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import eu.eidas.auth.commons.attribute.AttributeValue;

import static org.junit.Assert.assertEquals;

/**
 * DateTimeAttributeValueMarshallerTest
 *
 * @since 1.1
 */
public final class DateTimeAttributeValueMarshallerTest {

    private static final DateTime SAMPLE_DATE_1970_UTC = new DateTime(0L, DateTimeZone.UTC);

    private final DateTimeAttributeValueMarshaller marshaller = new DateTimeAttributeValueMarshaller();

    @Test
    public void marshal() throws Exception {
        String value = marshaller.marshal(new AttributeValue<DateTime>() {
            @Override
            public DateTime getValue() {
                return SAMPLE_DATE_1970_UTC;
            }

            @Override
            public boolean isNonLatinScriptAlternateVersion() {
                return false;
            }
        });

        assertEquals("1970-01-01", value);
    }

    @Test
    public void unmarshal() throws Exception {

        AttributeValue<DateTime> value = marshaller.unmarshal("1970-01-01", false);

        assertEquals(SAMPLE_DATE_1970_UTC, value.getValue());

        assertEquals(value, new AttributeValue<DateTime>() {
            @Override
            public DateTime getValue() {
                return SAMPLE_DATE_1970_UTC;
            }

            @Override
            public boolean isNonLatinScriptAlternateVersion() {
                return false;
            }
        });

    }
}
