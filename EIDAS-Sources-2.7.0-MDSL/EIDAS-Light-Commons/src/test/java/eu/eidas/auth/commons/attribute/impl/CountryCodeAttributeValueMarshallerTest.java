/*
 * Copyright (c) 2023 by European Commission
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
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link CountryCodeAttributeValueMarshaller}
 */
public class CountryCodeAttributeValueMarshallerTest {
    private final CountryCodeAttributeValueMarshaller marshaller = new CountryCodeAttributeValueMarshaller();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for {@link CountryCodeAttributeValueMarshaller#marshal(AttributeValue)}
     * must succeed
     */
    @Test
    public void marshal() {
        String value = "BE";
        CountryCodeAttributeValue attributeValue = new CountryCodeAttributeValue("BE");


        assertEquals(value, marshaller.marshal(attributeValue));
    }

    /**
     * Test method for {@link CountryCodeAttributeValueMarshaller#unmarshal(String, boolean)
     * when country code has a valid format
     * must succeed
     */
    @Test
    public void unmarshalCorrectFormat() throws Exception {
        String value = "BE";
        CountryCodeAttributeValue referenceAttributeValue = new CountryCodeAttributeValue(value);
        CountryCodeAttributeValue attributeValue =
                (CountryCodeAttributeValue) marshaller.unmarshal(value, false);

        assertEquals(referenceAttributeValue, attributeValue);
    }

    /**
     * Test method for {@link CountryCodeAttributeValueMarshaller#unmarshal(String, boolean)
     * when country code has an invalid format
     * must fail
     */
    @Test
    public void unmarshalIncorrectFormat() throws Exception {
        expectedException.expect(AttributeValueMarshallingException.class);
        String value = "BEE";
        marshaller.unmarshal(value, false);
    }
}
