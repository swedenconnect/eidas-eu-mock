/*
 * Copyright (c) 2017 by European Commission
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

import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.engine.exceptions.ValidationException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.saml2.core.Attribute;

import java.util.regex.Pattern;

/**
 * Validates the attributes and values.
 *
 * the validation eIdentifier (PERSON_IDENTIFIER & LEGAL_PERSON_IDENTIFIER ) are reported to the connector validation
 */
public final class EidasAttributeValidator extends AttributeSchemaValidator {

    private static final String PATTERN_GENDER_EIDAS = "^(?:Male|Female|Not specified)$";

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
                String value = ((XSAny) attr.getAttributeValues().get(0)).getTextContent();
                String attrName = attr.getName();

                //validate gender
                //TODO resolv EID-582 & EID-587
                /*if (attrName.equals(EidasSpec.Definitions.GENDER.getNameUri().toASCIIString())) {
                    validateAttributeValueFormat(value, attrName, EidasSpec.Definitions.GENDER.getNameUri().toASCIIString(),
                            PATTERN_GENDER_EIDAS);
                }
                if (attrName.equals(EidasSpec.Definitions.REPV_GENDER.getNameUri().toASCIIString())) {
                    validateAttributeValueFormat(value, attrName, EidasSpec.Definitions.REPV_GENDER.getNameUri().toASCIIString(),
                            PATTERN_GENDER_EIDAS);
                }*/


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
        DateTimeFormatter fmt;
        fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        try {
            fmt.parseDateTime(nodeDate);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Date has wrong format  {}", e);
        }

    }
}
