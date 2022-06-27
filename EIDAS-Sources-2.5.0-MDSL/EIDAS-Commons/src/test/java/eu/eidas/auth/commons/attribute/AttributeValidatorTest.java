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

package eu.eidas.auth.commons.attribute;

import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.validation.LengthParameterValidator;
import eu.eidas.auth.commons.validation.PatternValidator;
import eu.eidas.auth.commons.validation.ValueValidator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;

/**
 * Test class for {@link eu.eidas.auth.commons.attribute.AttributeValidator}
 */
public class AttributeValidatorTest {

    private static final String VALIDATOR_FIELD_NAME = "validator";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test for attribute validator for empty attribute value.
     *
     * Should throw an {@link InvalidParameterEIDASException}.
     */
    @Test
    public void testAttributeValidatorOnEmptyValue() {
        expectedException.expect(InvalidParameterEIDASException.class);

        AttributeValue attrValue = Mockito.mock(AttributeValue.class);
        Mockito.when(attrValue.getValue()).thenReturn("");

        AttributeValidator validator = AttributeValidator.of(EidasSpec.Definitions.BIRTH_NAME);
        validator.validate(attrValue);

        verifyValidatorType(LengthParameterValidator.class, validator);
    }

    /**
     * Test for attribute validator for null attribute value.
     *
     * Should throw an {@link InvalidParameterEIDASException}.
     */
    @Test
    public void testAttributeValidatorOnNullValue() {
        expectedException.expect(InvalidParameterEIDASException.class);

        AttributeValue attrValue = Mockito.mock(AttributeValue.class);
        Mockito.when(attrValue.getValue()).thenReturn(null);

        AttributeValidator validator = AttributeValidator.of(EidasSpec.Definitions.BIRTH_NAME);
        validator.validate(attrValue);

        verifyValidatorType(LengthParameterValidator.class, validator);
    }

    /**
     * Test for attribute validator for valid attribute value based on its length.
     *
     * Must succeed
     */
    @Test
    public void testAttributeValidatorOnValidLength() {
        AttributeValue attrValue = Mockito.mock(AttributeValue.class);
        Mockito.when(attrValue.getValue()).thenReturn("short value");

        AttributeValidator validator = AttributeValidator.of(EidasSpec.Definitions.BIRTH_NAME);
        validator.validate(attrValue);

        verifyValidatorType(LengthParameterValidator.class, validator);
    }

    /**
     * Test for attribute validator for an attribute value
     * that exceeds a maximum set in property max.attrValue.size {@see eidasParameters.properties}.
     * <p>
     * Should throw an {@link InvalidParameterEIDASException}.
     */
    @Test
    public void testAttributeValidatorOnTooLongLength() {
        expectedException.expect(InvalidParameterEIDASException.class);

        // Creates a string with 129 'c'.
        String tooLongValue = String.join("", Collections.nCopies(129, "c"));
        AttributeValue attrValue = Mockito.mock(AttributeValue.class);
        Mockito.when(attrValue.getValue()).thenReturn(tooLongValue);

        AttributeValidator validator = AttributeValidator.of(EidasSpec.Definitions.BIRTH_NAME);
        validator.validate(attrValue);

        verifyValidatorType(LengthParameterValidator.class, validator);
    }

    /**
     * Test for attribute validator for valid value of UID.
     *
     * Unique Identifier (UID)
     *  must be maximum 256 characters long.
     *  must not have whitespaces.
     *
     * Must succeed
     */
    @Test
    public void testAttributeValidatorOnValidEUID() {
        String validEUID = "test1234567890";
        AttributeValue attributeValue = Mockito.mock(AttributeValue.class);
        Mockito.when(attributeValue.getValue()).thenReturn(validEUID);

        AttributeValidator attributeValidator = AttributeValidator.of(EidasSpec.Definitions.PERSON_IDENTIFIER);
        attributeValidator.validate(attributeValue);

        verifyValidatorType(PatternValidator.class, attributeValidator);
    }

    /**
     * Test for attribute validator for an UID containing whitespaces (invalid value).
     *
     * Unique Identifier (UID)
     *  must be maximum 256 characters long.
     *  must not have whitespaces.
     *
     * Should throw an {@link InvalidParameterEIDASException}
     */
    @Test
    public void testAttributeValidatorOnInvalidPersonIdentifierValueForPattern() {
        expectedException.expect(InvalidParameterEIDASException.class);

        String invalidEUID = "test 1234567890";
        AttributeValue attributeValue = Mockito.mock(AttributeValue.class);
        Mockito.when(attributeValue.getValue()).thenReturn(invalidEUID);

        AttributeValidator attributeValidator = AttributeValidator.of(EidasSpec.Definitions.PERSON_IDENTIFIER);
        attributeValidator.validate(attributeValue);

        verifyValidatorType(PatternValidator.class, attributeValidator);
    }

    /**
     * Test for attribute validator for an UID containing whitespaces (invalid value).
     *
     * Unique Identifier (UID)
     *  must be maximum 256 characters long.
     *  must not have whitespaces.
     *
     * Should throw an {@link InvalidParameterEIDASException}
     */
    @Test
    public void testAttributeValidatorOnInvalidLegalIdentifierValueForPattern() {
        expectedException.expect(InvalidParameterEIDASException.class);

        String invalidEUID = "test 1234567890";
        AttributeValue attributeValue = Mockito.mock(AttributeValue.class);
        Mockito.when(attributeValue.getValue()).thenReturn(invalidEUID);

        AttributeValidator attributeValidator = AttributeValidator.of(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER);
        attributeValidator.validate(attributeValue);

        verifyValidatorType(PatternValidator.class, attributeValidator);
    }

    /**
     * Test for attribute validator for an UID containing whitespaces (invalid value).
     *
     * Unique Identifier (UID)
     *  must be maximum 256 characters long.
     *  must not have whitespaces.
     *
     * Should throw an {@link InvalidParameterEIDASException}
     */
    @Test
    public void testAttributeValidatorOnInvalidRepresentativePersonIdentifierValueForPattern() {
        expectedException.expect(InvalidParameterEIDASException.class);

        String invalidEUID = "test 1234567890";
        AttributeValue attributeValue = Mockito.mock(AttributeValue.class);
        Mockito.when(attributeValue.getValue()).thenReturn(invalidEUID);

        AttributeValidator attributeValidator = AttributeValidator.of(EidasSpec.Definitions.REPV_PERSON_IDENTIFIER);
        attributeValidator.validate(attributeValue);

        verifyValidatorType(PatternValidator.class, attributeValidator);
    }

    /**
     * Test for attribute validator for an UID containing whitespaces (invalid value).
     *
     * Unique Identifier (UID)
     *  must be maximum 256 characters long.
     *  must not have whitespaces.
     *
     * Should throw an {@link InvalidParameterEIDASException}
     */
    @Test
    public void testAttributeValidatorOnInvalidRepresentativeLegalIdentifierValueForPattern() {
        expectedException.expect(InvalidParameterEIDASException.class);

        String invalidEUID = "test 1234567890";
        AttributeValue attributeValue = Mockito.mock(AttributeValue.class);
        Mockito.when(attributeValue.getValue()).thenReturn(invalidEUID);

        AttributeValidator attributeValidator = AttributeValidator.of(EidasSpec.Definitions.REPV_LEGAL_PERSON_IDENTIFIER);
        attributeValidator.validate(attributeValue);

        verifyValidatorType(PatternValidator.class, attributeValidator);
    }

    /**
     * Test for attribute validator for a too long UID (invalid value).
     *
     * Unique Identifier (UID)
     *  must be maximum 256 characters long.
     *  must not have whitespaces.
     *
     * Should throw an {@link InvalidParameterEIDASException}
     */
    @Test
    public void testAttributeValidatorOnValueTooLongForPattern() {
        expectedException.expect(InvalidParameterEIDASException.class);

        // Creates a string with 257 'c'.
        String invalidEUID = String.join("", Collections.nCopies(257, "c"));
        AttributeValue attributeValue = Mockito.mock(AttributeValue.class);
        Mockito.when(attributeValue.getValue()).thenReturn(invalidEUID);

        AttributeValidator attributeValidator = AttributeValidator.of(EidasSpec.Definitions.PERSON_IDENTIFIER);
        attributeValidator.validate(attributeValue);

        verifyValidatorType(PatternValidator.class, attributeValidator);
    }

    /**
     * Test for attribute validator for a too long UID (invalid value).
     *
     * Unique Identifier (UID)
     *  must be maximum 256 characters long.
     *  must not have whitespaces.
     *  can be prefixed with the nationality and destination country code as ISO 3166-1 alpha-2 code
     *  separated by /
     *
     * Should throw an {@link InvalidParameterEIDASException}
     */
    @Test
    public void testPatternAttributeValidatorWithIdentifierPrefix() {
        // Creates a string with AA/AA/ and 256 'c'.
        String prefixEUID = "AA/AA/" + String.join("", Collections.nCopies(256, "c"));
        AttributeValue attributeValue = Mockito.mock(AttributeValue.class);
        Mockito.when(attributeValue.getValue()).thenReturn(prefixEUID);

        AttributeValidator attributeValidator = AttributeValidator.of(EidasSpec.Definitions.PERSON_IDENTIFIER);
        attributeValidator.validate(attributeValue);

        verifyValidatorType(PatternValidator.class, attributeValidator);
    }

    private void verifyValidatorType(Class<? extends ValueValidator> expectedValidatorType, AttributeValidator validator) {
        ValueValidator actualValidator = getParameterValidator(validator);
        Assert.assertEquals(expectedValidatorType, actualValidator.getClass());
    }

    private ValueValidator getParameterValidator(AttributeValidator validator) {
        try {
            Field parameterValidatorField = AttributeValidator.class.getDeclaredField(VALIDATOR_FIELD_NAME);
            parameterValidatorField.setAccessible(true);
            return (ValueValidator) parameterValidatorField.get(validator);
        } catch (Exception e) {
            throw new AssertionError("Validator field couldn't be retrieved", e);
        }
    }

}
