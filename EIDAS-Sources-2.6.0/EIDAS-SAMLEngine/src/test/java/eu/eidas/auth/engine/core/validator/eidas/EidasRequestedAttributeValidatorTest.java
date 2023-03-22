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
 *
 */

package eu.eidas.auth.engine.core.validator.eidas;

import eu.eidas.auth.engine.core.eidas.RequestedAttribute;
import eu.eidas.engine.exceptions.ValidationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Test class for {@link EidasRequestedAttributeValidator}
 */
public class EidasRequestedAttributeValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link EidasRequestedAttributeValidator#validate(org.opensaml.saml.saml2.metadata.RequestedAttribute)}
     * when an opensaml {@link RequestedAttribute} contains both Name and
     * Nameformat and is validated
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateOpenSaml() throws ValidationException {
        final EidasRequestedAttributeValidator validator = new EidasRequestedAttributeValidator();
        final RequestedAttribute requestedAttribute = createRequestedAttribute();
        final org.opensaml.saml.saml2.metadata.RequestedAttribute opensamlRequestedAttributeInterface = requestedAttribute;

        validator.validate(opensamlRequestedAttributeInterface);
    }

    /**
     * Test method for
     * {@link EidasRequestedAttributeValidator#validate(eu.eidas.auth.engine.core.eidas.RequestedAttribute)}
     * when an eidas engine {@link RequestedAttribute} contains both Name and
     * Nameformat and is validated
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidate() throws ValidationException {
        final EidasRequestedAttributeValidator validator = new EidasRequestedAttributeValidator();
        final RequestedAttribute requestedAttribute = createRequestedAttribute();

        validator.validate(requestedAttribute);
    }

    /**
     * Test method for
     * {@link EidasRequestedAttributeValidator#validate(eu.eidas.auth.engine.core.eidas.RequestedAttribute)}
     * when an eidas engine {@link RequestedAttribute} is missing Name and is validated
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateExceptionNameIsRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Name is required.");

        final EidasRequestedAttributeValidator validator = new EidasRequestedAttributeValidator();
        final RequestedAttribute requestedAttribute = createRequestedAttribute();
        requestedAttribute.setName(null);

        validator.validate(requestedAttribute);
    }

    /**
     * Test method for
     * {@link EidasRequestedAttributeValidator#validate(eu.eidas.auth.engine.core.eidas.RequestedAttribute)}
     * when an eidas engine {@link RequestedAttribute} is missing NameFormat and is validated
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateExceptionNameFormatIsRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("NameFormat is required.");

        final EidasRequestedAttributeValidator validator = new EidasRequestedAttributeValidator();
        final RequestedAttribute requestedAttribute = createRequestedAttribute();
        requestedAttribute.setNameFormat(null);

        validator.validate(requestedAttribute);
    }

    private RequestedAttribute createRequestedAttribute() {
        final RequestedAttribute requestedAttribute = new RequestedAttributeTestImpl(
                "name:space.uri",
                "elementLocalName",
                "NamSpacePrefix");
        requestedAttribute.setName("John");
        requestedAttribute.setNameFormat("uri:someNameFormat");
        return requestedAttribute;
    }

    class RequestedAttributeTestImpl extends eu.eidas.auth.engine.core.eidas.impl.RequestedAttributeImpl {

        protected RequestedAttributeTestImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
            super(namespaceURI, elementLocalName, namespacePrefix);
        }
    }

}