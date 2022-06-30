package eu.eidas.auth.commons.attribute;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

import javax.xml.namespace.QName;

import org.junit.Test;

import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

/**
 * AttributeDefinitionTest
 *
 * @since 1.1
 */
public final class AttributeDefinitionTest {

    private static final AttributeDefinition<String> CURRENT_FAMILY_NAME =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
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
}
