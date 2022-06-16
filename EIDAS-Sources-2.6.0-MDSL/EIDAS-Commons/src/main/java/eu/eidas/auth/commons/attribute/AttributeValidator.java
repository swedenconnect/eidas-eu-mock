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

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.validation.LengthParameterValidator;
import eu.eidas.auth.commons.validation.PatternValidator;
import eu.eidas.auth.commons.validation.ValueValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * Validators for attributes
 */
public class AttributeValidator {

    /**
     * Unique Identifier (UID) validation pattern.
     * The UID must not contain spaces
     * The UID must not exceed 256 characters
     *
     * UID can be prefixed with nationality and requester country codes
     */
    private static final Pattern UID_VALIDATION_PATTERN = Pattern.compile("^(([A-Z]{2}[/]){2})?[\\S]{0,256}$");
    private static final Logger LOG = LoggerFactory.getLogger(AttributeValidator.class);

    private AttributeDefinition<?> definition;
    private ValueValidator validator;

    /**
     * private constructor.
     * {@link AttributeValidator#of(AttributeDefinition)}
     *
     * @param definition the attribute definition of the attribute value that should be validated
     * @param validator  the validator type to validate the attribute value.
     */
    private AttributeValidator(AttributeDefinition<?> definition, ValueValidator validator) {
        this.definition = definition;
        this.validator = validator;
    }

    /**
     * AttributeValidator Selection based on the attributeDefinition to validate.
     * @param attributeDefinition the attribute definition of the attribute to validate
     * @return the AttributeValidator associate with the given attributeDefinition.
     */
    public static AttributeValidator of(@Nonnull AttributeDefinition<?> attributeDefinition) {
        ValueValidator validator;
        if (attributeDefinition.isUniqueIdentifier()) {
            validator = PatternValidator.Builder()
                    .pattern(UID_VALIDATION_PATTERN)
                    .build();
        } else {
            validator = LengthParameterValidator.Builder()
                    .paramName(EidasParameterKeys.ATTRIBUTE_VALUE)
                    .build();
        }
        return new AttributeValidator(attributeDefinition, validator);
    }

    /**
     * Apply the validation of this validator on the given attribute value.
     * This will throw a InvalidParameterException if the value is invalid.
     * @param attributeValue the attributeValue to validate.
     */
    public void validate(AttributeValue<?> attributeValue) {
        String value = marshallAttribute(attributeValue);
        if (!validator.isValid(value)) {
            String errorMessage = "Invalid value for attribute " + definition.getFriendlyName();
            LOG.error(errorMessage);
            throw new InvalidParameterEIDASException(EidasErrorKey.INVALID_ATTRIBUTE_VALUE.errorCode(), errorMessage);
        }
    }

    private String marshallAttribute(AttributeValue attributeValue) {
        AttributeValueMarshaller<?> attributeValueMarshaller = definition.getAttributeValueMarshaller();
        try {
            return attributeValueMarshaller.marshal(attributeValue);
        } catch (AttributeValueMarshallingException e) {
            String errorMessage = "Invalid value for attribute " + definition.getFriendlyName()
                    + ". AttributeValue could not be marshalled.";
            LOG.error(errorMessage);
            throw new InvalidParameterEIDASException(EidasErrorKey.INVALID_ATTRIBUTE_VALUE.errorCode(), errorMessage, e);
        }
    }
}
