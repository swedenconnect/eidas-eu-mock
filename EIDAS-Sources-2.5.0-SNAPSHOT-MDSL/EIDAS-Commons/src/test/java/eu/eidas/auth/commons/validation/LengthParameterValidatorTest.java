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

package eu.eidas.auth.commons.validation;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link eu.eidas.auth.commons.validation.LengthParameterValidator}
 */
public class LengthParameterValidatorTest {

    /**
     * Test validation of a valid length value for a parameter which maximum valid length is 10 chars.
     * Value is the String "valid"
     *
     * Must succeed
     */
    @Test
    public void testValidLengthParameter() {
        ValueValidator validator = LengthParameterValidator
                .Builder()
                .paramName("10chars.length")
                .build();

        boolean valid = validator.isValid("valid");
        Assert.assertTrue(valid);
    }

    /**
     * Test validation of a valid length value for a parameter which maximum valid length is 10.
     * Value is an integer value of 10.
     *
     * Must succeed
     */
    @Test
    public void testValidLengthParameterIntValue() {
        ValueValidator validator = LengthParameterValidator
                .Builder()
                .paramName("10chars.length")
                .build();

        boolean valid = validator.isValid(10);
        Assert.assertTrue(valid);
    }

    /**
     * Test validation of a valid length value for a parameter which maximum valid length is 10.
     * Value is a long value of 10.
     *
     * Must succeed
     */
    @Test
    public void testValidLengthParameterLongValue() {
        ValueValidator validator = LengthParameterValidator
                .Builder()
                .paramName("10chars.length")
                .build();

        boolean valid = validator.isValid(10L);
        Assert.assertTrue(valid);
    }

    /**
     * Test validation of a invalid length value for a parameter which maximum valid length is 10 chars.
     * Value is null
     *
     * Should not be valid
     */
    @Test
    public void testInvalidLengthParameterWithNullValue() {
        ValueValidator validator = LengthParameterValidator
                .Builder()
                .paramName("10chars.length")
                .build();

        boolean valid = validator.isValid(null);
        Assert.assertFalse(valid);
    }

    /**
     * Test validation of a valid length value for a parameter which maximum valid length is 10.
     * Value is an integer 11.
     *
     * Should not be valid
     */
    @Test
    public void testInvalidLengthParameterIntValue() {
        ValueValidator validator = LengthParameterValidator
                .Builder()
                .paramName("10chars.length")
                .build();

        boolean valid = validator.isValid(11);
        Assert.assertFalse(valid);
    }

    /**
     * Test validation of a valid length value for a parameter which maximum valid length is 10.
     * Value is an integer 11.
     *
     * Should not be valid
     */
    @Test
    public void testInvalidLengthParameterLongValue() {
        ValueValidator validator = LengthParameterValidator
                .Builder()
                .paramName("10chars.length")
                .build();

        boolean valid = validator.isValid(11L);
        Assert.assertFalse(valid);
    }

    /**
     * Test validation of a invalid length value for a parameter which maximum valid length is 10 chars.
     * Value is a String with a too long length
     *
     * Should not be valid
     */
    @Test
    public void testInvalidLengthParameter() {
        ValueValidator validator = LengthParameterValidator
                .Builder()
                .paramName("10chars.length")
                .build();

        boolean valid = validator.isValid("invalid parameter");
        Assert.assertFalse(valid);
    }

    /**
     * Test validation of a length parameter value of an Unsupported type.
     * Value is the Object class.
     *
     * Should throw an UnsupportedOperationException
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testLengthParameterValidationUnsupportedTypeOfValue() {
        ValueValidator validator = LengthParameterValidator
                .Builder()
                .paramName("10chars.length")
                .build();

        boolean valid = validator.isValid(Object.class);
        Assert.assertTrue(valid);
    }
}
