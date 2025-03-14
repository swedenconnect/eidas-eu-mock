/*
 * Copyright (c) 2024 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.eidas.auth.commons.attribute.impl;

import eu.eidas.auth.commons.attribute.AttributeValue;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

/**
 * DateTimeAttributeValueMarshallerTest
 *
 * @since 1.1
 */
public final class DateTimeAttributeValueMarshallerTest {

    private static final LocalDate SAMPLE_DATE_1970 = LocalDate.ofEpochDay(0);

    private final DateTimeAttributeValueMarshaller marshaller = new DateTimeAttributeValueMarshaller();

    @Test
    public void marshal() {
        String value = marshaller.marshal(new AttributeValue<>() {
            @Override
            public LocalDate getValue() {
                return SAMPLE_DATE_1970;
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

        AttributeValue<LocalDate> value = marshaller.unmarshal("1970-01-01", false);

        assertEquals(SAMPLE_DATE_1970, value.getValue());

        assertEquals(value, new AttributeValue<LocalDate>() {
            @Override
            public LocalDate getValue() {
                return SAMPLE_DATE_1970;
            }

            @Override
            public boolean isNonLatinScriptAlternateVersion() {
                return false;
            }
        });

    }
}
