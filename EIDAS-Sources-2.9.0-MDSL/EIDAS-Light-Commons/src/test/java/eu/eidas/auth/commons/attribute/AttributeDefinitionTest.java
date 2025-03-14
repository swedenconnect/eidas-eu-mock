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
package eu.eidas.auth.commons.attribute;

import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

/**
 Test class for {@link AttributeDefinition}
 */
public final class AttributeDefinitionTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final AttributeDefinition<String> CURRENT_FAMILY_NAME =
            new AttributeDefinition.Builder<String>()
                    .nameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
                    .friendlyName("FamilyName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final AttributeDefinition<String> CURRENT_GIVEN_NAME =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName")
                    .friendlyName("FirstName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(false)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentGivenNameType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final AttributeDefinition<String> PERSON_IDENTIFIER =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier")
                    .friendlyName("PersonIdentifier")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .uniqueIdentifier(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "PersonIdentifierType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    @Test
    public void testGetFriendlyName() throws Exception {
        assertThat(CURRENT_FAMILY_NAME.getFriendlyName(), is("FamilyName"));
        assertThat(CURRENT_GIVEN_NAME.getFriendlyName(), is("FirstName"));
    }

    @Test
    public void testGetName() throws Exception {
        assertThat(CURRENT_FAMILY_NAME.getNameUri(),
                   is(new URI("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")));
        assertThat(CURRENT_GIVEN_NAME.getNameUri(),
                   is(new URI("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName")));
    }

    @Test
    public void testIsRequired() throws Exception {
        assertThat(Boolean.valueOf(CURRENT_FAMILY_NAME.isRequired()), is(Boolean.TRUE));
        assertThat(Boolean.valueOf(CURRENT_GIVEN_NAME.isRequired()), is(Boolean.FALSE));
    }

    @Test
    public void testIsUniqueIdentifier() throws Exception {
        assertThat(Boolean.valueOf(PERSON_IDENTIFIER.isUniqueIdentifier()), is(Boolean.TRUE));
        assertThat(Boolean.valueOf(CURRENT_GIVEN_NAME.isUniqueIdentifier()), is(Boolean.FALSE));
    }

    @Test
    public void testGetPersonType() throws Exception {
        assertThat(CURRENT_FAMILY_NAME.getPersonType(), is(PersonType.NATURAL_PERSON));
    }

    @Test
    public void testGetXmlType() throws Exception {
        assertThat(CURRENT_FAMILY_NAME.getXmlType(),
                   is(new QName("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType")));
        assertThat(CURRENT_GIVEN_NAME.getXmlType(),
                   is(new QName("http://eidas.europa.eu/attributes/naturalperson", "CurrentGivenNameType")));
    }

    @Test
    public void testCompareTo() throws Exception {
        assertThat(Integer.valueOf(CURRENT_FAMILY_NAME.compareTo(CURRENT_GIVEN_NAME)), is(Integer.valueOf(-1)));
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(CURRENT_FAMILY_NAME, new AttributeDefinition.Builder<String>().nameUri(
                "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
                .friendlyName("FamilyName")
                .personType(PersonType.NATURAL_PERSON)
                .required(true)
                .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType", "eidas-natural")
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build());
    }

    @Test
    public void testNotEquals() throws Exception {
        assertNotEquals(CURRENT_FAMILY_NAME, CURRENT_GIVEN_NAME);
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals(CURRENT_FAMILY_NAME.hashCode(), new AttributeDefinition.Builder<String>().nameUri(
                "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
                .friendlyName("FamilyName")
                .personType(PersonType.NATURAL_PERSON)
                .required(true)
                .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType", "eidas-natural")
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build().hashCode());
    }

    @Test
    public void testHashCodeDistinct() throws Exception {
        assertNotEquals(CURRENT_FAMILY_NAME.hashCode(), CURRENT_GIVEN_NAME.hashCode());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName', friendlyName='FamilyName', personType=NaturalPerson, required=true, transliterationMandatory=false, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentFamilyNameType', attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}",
                     CURRENT_FAMILY_NAME.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(PERSON_IDENTIFIER);
        oos.close();
        byte[] bytes = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        AttributeDefinition<?> result = (AttributeDefinition<?>) ois.readObject();

        assertEquals(PERSON_IDENTIFIER, result);
    }

    /**
     * Test method for
     * {@link AttributeDefinition.Builder#nameUri(URI)}
     * when nameUri is invalid,
     * when {@link IllegalArgumentException} is thrown inside {@link AttributeDefinition.Builder#nameUri(URI)}
     * <p>
     * Must fail.
     */
    @Test
    public void testBuilderValidateInvalidNameUri() {
        final String invalidNameUri = "::";
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid name URI \"" + invalidNameUri + "\": ");

        new AttributeDefinition.Builder<String>()
                .nameUri(invalidNameUri)
                .friendlyName("FamilyName")
                .personType(PersonType.NATURAL_PERSON)
                .required(true)
                .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType", "eidas-natural")
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();
    }

    /**
     * Test method for
     * {@link AttributeDefinition.Builder#validate()}
     * when nameUri is relative,
     * when {@link IllegalArgumentException} is thrown inside {@link AttributeDefinition.Builder#validate()}
     * <p>
     * Must fail.
     */
    @Test
    public void testBuilderValidateRelativeNameUri() {
        final String relativeNameUri = "/attributes/naturalperson/CurrentFamilyName";
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("name must be an absolute URI, but was: \"" + relativeNameUri + "\"");

        new AttributeDefinition.Builder<String>()
            .nameUri(relativeNameUri)
            .friendlyName("FamilyName")
            .personType(PersonType.NATURAL_PERSON)
            .required(true)
            .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType", "eidas-natural")
            .attributeValueMarshaller(new StringAttributeValueMarshaller())
            .build();
    }

    /**
     * Test method for
     * {@link AttributeDefinition.Builder#validate()}
     * when xmlNamespaceUri is relative,
     * when {@link IllegalArgumentException} is thrown inside {@link AttributeDefinition.Builder#validate()}
     * <p>
     * Must fail.
     */
    @Test
    public void testBuilderValidateRelativeXmlNamespaceUri() {
        final String relativeXmlNamespaceUri = "eidas.europa.eu/attributes/naturalperson";
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("XML personType NamespaceURI must be an absolute URI, but was: \"" + relativeXmlNamespaceUri + "\"");

        new AttributeDefinition.Builder<String>()
                .nameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
                .friendlyName("FamilyName")
                .personType(PersonType.NATURAL_PERSON)
                .required(true)
                .xmlType(relativeXmlNamespaceUri, "CurrentFamilyNameType", "eidas-natural")
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();
    }

    /**
     * Test method for
     * {@link AttributeDefinition.Builder#validate()}
     * when xmlNamespaceUri is invalid, validate should throw an IllegalArgumentException
     * <p>
     * Must fail.
     */
    @Test
    public void testBuilderValidateInvalidXmlNamespaceUri() {
        final String invalidXmlNamespaceUri = "::";
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid XML personType NamespaceURI \"" + invalidXmlNamespaceUri + "\": ");

        new AttributeDefinition.Builder<String>()
                .nameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
                .friendlyName("FamilyName")
                .personType(PersonType.NATURAL_PERSON)
                .required(true)
                .xmlType(invalidXmlNamespaceUri, "CurrentFamilyNameType", "eidas-natural")
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();
    }
}
