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

package eu.eidas.auth.engine.core.validator.eidas;

import eu.eidas.engine.exceptions.ValidationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.Extensions;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for the {@link ExtensionsSchemaValidator}
 */
public class ExtensionsSchemaValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link ExtensionsSchemaValidator#validate(Extensions)}
     * when unknownXMLObjects is null
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateWithUnknownXMLObjectsNull() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Extension element is empty or not exist.");

        final ExtensionsSchemaValidator extensionsSchemaValidator = new ExtensionsSchemaValidator();
        final Extensions mockExtensions = Mockito.mock(Extensions.class);
        Mockito.when(mockExtensions.getUnknownXMLObjects()).thenReturn(null);
        extensionsSchemaValidator.validate(mockExtensions);
    }

    /**
     * Test method for
     * {@link ExtensionsSchemaValidator#validate(Extensions)}
     * when element of XMLObjects is not instanceof XSAnyImpl
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateWithElementNotInstanceOfXSAnyImpl() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Extensions element is not valid: null");

        final ExtensionsSchemaValidator extensionsSchemaValidator = new ExtensionsSchemaValidator();
        final List<XMLObject> xmlObjectList = new ArrayList<>();

        final Extensions mockExtensions = Mockito.mock(Extensions.class);
        final XSAnyImpl mockXSAnyImpl = Mockito.mock(XSAnyImpl.class);

        xmlObjectList.add(mockXSAnyImpl);
        Mockito.when(mockExtensions.getUnknownXMLObjects()).thenReturn(xmlObjectList);

        extensionsSchemaValidator.validate(mockExtensions);
    }

}