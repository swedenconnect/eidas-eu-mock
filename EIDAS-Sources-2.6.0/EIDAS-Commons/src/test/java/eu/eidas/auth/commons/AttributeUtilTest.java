/*
 * Copyright (c) 2020 by European Commission
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

package eu.eidas.auth.commons;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.commons.protocol.eidas.spec.RepresentativeNaturalPersonSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for the util methods of the {@link AttributeUtil} class
 */
public class AttributeUtilTest {

    /**
     * Test method for the {@link AttributeUtil#isGenderAttributeDefinition(AttributeDefinition)} method.
     * when the {@link NaturalPersonSpec.Definitions#GENDER} attribute is passed as a parameter.
     * <p>
     * Must succeed
     */
    @Test
    public void testIsGenderAttributeDefinitionWithNaturalPersonGender() {
        AttributeDefinition genderAttribute = NaturalPersonSpec.Definitions.GENDER;

        boolean actualIsGenderAttributeDefinition = AttributeUtil.isGenderAttributeDefinition(genderAttribute);

        Assert.assertTrue(actualIsGenderAttributeDefinition);
    }

    /**
     * Test method for the {@link AttributeUtil#isGenderAttributeDefinition(AttributeDefinition)}
     *
     * when the {@link RepresentativeNaturalPersonSpec.Definitions#GENDER} attribute is passed as a parameter.
     *
     * Must succeed
     */
    @Test
    public void testIsGenderAttributeDefinitionWithRepresentativeGender() {
        AttributeDefinition genderAttribute = RepresentativeNaturalPersonSpec.Definitions.GENDER;

        boolean actualIsGenderAttributeDefinition = AttributeUtil.isGenderAttributeDefinition(genderAttribute);

        Assert.assertTrue(actualIsGenderAttributeDefinition);
    }

    /**
     * Test method for the {@link AttributeUtil#isGenderAttributeDefinition(AttributeDefinition)}
     *
     * when the {@link NaturalPersonSpec.Definitions#PERSON_IDENTIFIER} attribute is passed as a parameter.
     *
     * Must succeed
     */
    @Test
    public void testIsGenderAttributeDefinitionWithIdentifierAttribute() {
        AttributeDefinition identifierAttribute = NaturalPersonSpec.Definitions.PERSON_IDENTIFIER;

        boolean actualIsGenderAttributeDefinition = AttributeUtil.isGenderAttributeDefinition(identifierAttribute);

        Assert.assertFalse(actualIsGenderAttributeDefinition);
    }
}
