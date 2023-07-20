/*
 * Copyright (c) 2021 by European Commission
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

package eu.eidas.auth.commons.protocol.eidas.impl;

import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test class for {@link GenderAttributeValueMarshaller}
 */
public class GenderAttributeValueMarshallerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    /**
     * Test method for
     * {@link GenderAttributeValueMarshaller#marshal(AttributeValue)}
     * when {@link AttributeValue} is a valid one
     * <p>
     * Must succeed.
     */
    @Test
    public void testMarshalWithGenderValue() {
        final GenderAttributeValueMarshaller genderAttributeValueMarshaller = new GenderAttributeValueMarshaller();
        final String expectedValue = "Female";
        final Gender gender = Gender.fromString(expectedValue);
        final GenderAttributeValue genderAttributeValue = new GenderAttributeValue(gender);
        final String actualValue = genderAttributeValueMarshaller.marshal(genderAttributeValue);

        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test method for
     * {@link GenderAttributeValueMarshaller#unmarshal(String, boolean)}
     * when gender is null and
     * isNonLatinScriptAlternateVersion is false
     * <p>
     * Must fail.
     */
    @Test
    public void testUnmarshalWithGenderValueNull() throws AttributeValueMarshallingException {
        expectedException.expect(AttributeValueMarshallingException.class);

        final GenderAttributeValueMarshaller genderAttributeValueMarshaller = new GenderAttributeValueMarshaller();
        genderAttributeValueMarshaller.unmarshal(null, false);
    }

    /**
     * Test method for
     * {@link GenderAttributeValueMarshaller#unmarshal(String, boolean)}
     * when gender is {@link Gender#MALE} and
     * isNonLatinScriptAlternateVersion is false
     * <p>
     * Must succeed.
     */
    @Test
    public void testUnmarshalWithGenderValueNull2() throws AttributeValueMarshallingException {
        final GenderAttributeValueMarshaller genderAttributeValueMarshaller = new GenderAttributeValueMarshaller();
        AttributeValue<Gender> attributeValue =  genderAttributeValueMarshaller.unmarshal("Male", false);

        Assert.assertNotNull(attributeValue);
        Assert.assertTrue(attributeValue instanceof GenderAttributeValue);

    }

}