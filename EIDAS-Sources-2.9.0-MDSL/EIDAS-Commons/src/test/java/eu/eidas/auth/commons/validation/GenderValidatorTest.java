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

package eu.eidas.auth.commons.validation;

import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link GenderValidator}
 */
public class GenderValidatorTest {

    /**
     * Test method for
     * {@link GenderValidator#isValid(Gender)} must return true
     * when eidas protocol version is 1.2 and gender is Unspecified
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidGenderFemale() {
        ValueValidator<Gender> genderValidator = GenderValidator
                .Builder()
                .build();

        boolean actualIsValid = genderValidator.isValid(Gender.FEMALE);

        Assert.assertTrue(actualIsValid);
    }

    /**
     * Test method for
     * {@link GenderValidator#isValid(Gender)} must return true
     * when eidas protocol version is 1.2 and gender is Unspecified
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidGenderMale() {
        ValueValidator<Gender> genderValidator = GenderValidator
                .Builder()
                .build();

        boolean actualIsValid = genderValidator.isValid(Gender.MALE);

        Assert.assertTrue(actualIsValid);
    }


    /**
     * Test method for
     * {@link GenderValidator#isValid(Gender)} must return true
     * when eidas protocol version is 1.2 and gender is Unspecified
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidGenderUnspecified() {
        ValueValidator<Gender> genderValidator = GenderValidator
                .Builder()
                .build();

        boolean actualIsValid = genderValidator.isValid(Gender.UNSPECIFIED);

        Assert.assertTrue(actualIsValid);
    }
}
