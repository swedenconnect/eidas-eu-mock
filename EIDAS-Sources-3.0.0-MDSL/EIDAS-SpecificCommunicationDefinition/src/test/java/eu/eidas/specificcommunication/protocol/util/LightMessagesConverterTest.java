/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.specificcommunication.protocol.util;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.specificcommunication.LightResponse;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.isA;

/**
 * Test class for {@link LightMessagesConverter}
 */
public class LightMessagesConverterTest {

    private LightMessagesConverter testConverter;
    private String personIdentifierUriString = "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier";
    private URI personIdentifierURI;
    private AttributeDefinition mockDefinition;
    private Collection mockRegistry;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws URISyntaxException {
         mockDefinition = new AttributeDefinition.Builder<String>()
                .nameUri(personIdentifierUriString)
                .friendlyName("PersonIdentifier")
                .personType(PersonType.NATURAL_PERSON)
                .required(true)
                .uniqueIdentifier(true)
                .xmlType("http://eidas.europa.eu/attributes/naturalperson", "PersonIdentifierType", "eidas-natural")
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();

        mockRegistry = Collections.singleton(mockDefinition);
        testConverter = new LightMessagesConverter();
        personIdentifierURI = new URI(personIdentifierUriString);
    }

    /**
     * Test method for
     * {@link LightMessagesConverter#getByName(URI, Collection)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetByName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AttributeDefinition returnedDefinition = invokeGetByName(personIdentifierURI);
        Assert.assertEquals(returnedDefinition, mockDefinition);
    }

    /**
     * Test method for
     * {@link LightMessagesConverter#getByName(URI, Collection)}
     * When nameUri argument is null
     * <p>
     * Must fail.
     */
    @Test
    public void testGetByNameWhenNameUriNull() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(SpecificCommunicationException.class));

        invokeGetByName(null);
    }

    /**
     * Test method for
     * {@link LightMessagesConverter#getByName(URI, Collection)}
     * When nameUri argument present in AttributeDefinition is null
     * <p>
     * Must fail.
     */
    @Test
    public void testGetByNameWhenDefinitionNameUriNull() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        expectedException.expectCause(isA(SpecificCommunicationException.class));

        Field nameUriField = AttributeDefinition.class.getDeclaredField("nameUri");
        nameUriField.setAccessible(true);
        nameUriField.set(mockDefinition, null);

        invokeGetByName(personIdentifierURI);
    }

    /**
     * Test method for
     * {@link LightMessagesConverter#getByName(URI, Collection)}
     * When nameUri points to an AttributeDefinition that is not present
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetByNameWhenDefinitionNotPresent() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, URISyntaxException {

        invokeGetByName(new URI("nonExistant"));
    }

    /**
     * Test method for
     * {@link LightMessagesConverter#convert(LightResponse.Status)}
     * When isFailure element is null
     * <p>
     * Must fail.
     */
    @Test
    public void testConvertWhenIsFailureIsNull() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(SpecificCommunicationException.class));

        final Method convertMethod = LightMessagesConverter.class.getDeclaredMethod("convert", LightResponse.Status.class);
        convertMethod.setAccessible(true);

        final LightResponse.Status status = new LightResponse.Status();
        convertMethod.invoke(testConverter, status);
    }

    private AttributeDefinition invokeGetByName(URI uri) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getByNameMethod = LightMessagesConverter.class.getDeclaredMethod("getByName", URI.class, Collection.class);
        getByNameMethod.setAccessible(true);
        AttributeDefinition result = (AttributeDefinition) getByNameMethod.invoke(testConverter, uri, mockRegistry);
        return result;
    }
}