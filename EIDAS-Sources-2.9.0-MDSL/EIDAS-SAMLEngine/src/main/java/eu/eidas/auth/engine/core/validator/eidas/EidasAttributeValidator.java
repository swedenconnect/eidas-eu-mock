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
package eu.eidas.auth.engine.core.validator.eidas;

import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.engine.exceptions.ValidationException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.saml2.core.Attribute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validates the attributes and values.
 *
 * the validation eIdentifier (PERSON_IDENTIFIER &amp; LEGAL_PERSON_IDENTIFIER ) are reported to the connector validation
 */
public final class EidasAttributeValidator extends AttributeSchemaValidator {

    /**
     * Constructor
     */
    public EidasAttributeValidator() {

        super();
    }

    @Override
    public void validate(Attribute attr) throws ValidationException {

        super.validate(attr);

        if (attr.getName() == null) {
            throw new ValidationException("Name is required.");
        }

        if (attr.getNameFormat() == null) {
            throw new ValidationException("NameFormat is required.");
        }

        if (!attr.getAttributeValues().isEmpty()) {
            //validate individual attributes if present
            XMLObject attrValueObject = attr.getAttributeValues().get(0);

            // validates only the strings
            if ((attrValueObject instanceof XSAny)) {
                final String value = ((XSAny) attr.getAttributeValues().get(0)).getTextContent();
                final String attrName = attr.getName();

                if (value == null) {
                    throw new ValidationException(attrName + " is required.");
                }

                final String gender = EidasSpec.Definitions.GENDER.getNameUri().toASCIIString();
                final String representativeGender = EidasSpec.Definitions.REPV_GENDER.getNameUri().toASCIIString();

                // validate gender
                if (attrName.equals(gender)) {
                    if(Gender.fromString(value) == null) {
                        throw new ValidationException(gender + " has incorrect format.");
                    }
                }

                // validate representative gender
                if (attrName.equals(representativeGender)) {
                    if(Gender.fromString(value) == null) {
                        throw new ValidationException(representativeGender + " has incorrect format.");
                    }
                }

                //validate dateOfBirth
                if (attrName.equals(EidasSpec.Definitions.DATE_OF_BIRTH.getNameUri().toASCIIString())) {
                    verifyDate(value);
                }
            }
        }

    }

    private void validateAttributeValueFormat(String value,
                                              String currentAttrName,
                                              String attrNameToTest,
                                              String pattern) throws ValidationException {
        if (currentAttrName.equals(attrNameToTest) && !Pattern.matches(pattern, value)) {
            throw new ValidationException(attrNameToTest + " has incorrect format.");
        }

    }

    private static void verifyDate(String nodeDate) throws ValidationException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate.parse(nodeDate, fmt);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Date has wrong format: " + e.getMessage(), new IllegalArgumentException(e));
        }
    }

}
