package eu.eidas.auth.commons.attribute;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableSortedSet;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * AttributeRegistryTest
 *
 * @since 1.1
 */
public final class AttributeRegistryTest {

    private static final String TEST_FILE = "src/test/resources/exampleAttributes.xml";

    private static final String TEST_FILE_WITH_SPACES = "src/test/resources/folder with spaces/exampleAttributes2.xml";

    private static final AttributeDefinition<String> CURRENT_FAMILY_NAME =
            new AttributeDefinition.Builder<String>()
                    .nameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
                    .friendlyName("FamilyName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .transliterationMandatory(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final AttributeDefinition<String> TOTALLY_WRONG_ATTRIBUTE =
            new AttributeDefinition.Builder<String>()
                    .nameUri("http://eidas.europa.eu/attributes/naturalperson/WrongAttribute")
                    .friendlyName("WrongAttribute")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .transliterationMandatory(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "WrongAttribute", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static AttributeRegistry attributeRegistry;

    @BeforeClass
    public static void startup() {
        attributeRegistry = AttributeRegistries.fromFile(TEST_FILE, null);
    }

    @SuppressWarnings({"PublicField"})
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetAttributes() throws Exception {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getAttributes();

        assertThat(attributes.size(), is(Integer.valueOf(20)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.TRUE));
    }

    @Test
    public void testGetByFriendlyName() throws Exception {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getByFriendlyName("FamilyName");

        assertThat(attributes.size(), is(Integer.valueOf(1)));
        assertThat(attributes.iterator().next(), is((Object) CURRENT_FAMILY_NAME));
    }

    @Test
    public void testGetByRequiredTrue() throws Exception {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getByRequired(true);

        assertThat(attributes.size(), is(Integer.valueOf(6)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.TRUE));
    }

    @Test
    public void testGetByRequiredFalse() throws Exception {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getByRequired(false);

        assertThat(attributes.size(), is(Integer.valueOf(14)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.FALSE));
    }

    @Test
    public void testGetByPersonTypeNatural() throws Exception {
        ImmutableSortedSet<AttributeDefinition<?>> attributes =
                attributeRegistry.getByPersonType(PersonType.NATURAL_PERSON);

        assertThat(attributes.size(), is(Integer.valueOf(9)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.TRUE));
    }

    @Test
    public void testGetByPersonTypeLegal() throws Exception {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getByPersonType(PersonType.LEGAL_PERSON);

        assertThat(attributes.size(), is(Integer.valueOf(11)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.FALSE));
    }

    @Test
    public void testGetByXmlType() throws Exception {
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getByXmlType(
                new QName("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType"));

        assertThat(attributes.size(), is(Integer.valueOf(1)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.TRUE));
    }

    @Test
    public void testWrongFile() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(startsWith("java.io.FileNotFoundException: File \"WrongFile.xml\" cannot be found from path: \""));
        thrown.expectMessage(endsWith("WrongFile.xml\""));

        AttributeRegistry attributeRegistry = AttributeRegistries.fromFile("WrongFile.xml", null);
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getAttributes();

        assertThat(attributes.size(), is(Integer.valueOf(20)));
    }

    @Test
    public void testGetAttributesFromFileWithSpacesInPath() throws Exception {

        AttributeRegistry attributeRegistry = AttributeRegistries.fromFile(TEST_FILE_WITH_SPACES, null);

        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getAttributes();

        assertThat(attributes.size(), is(Integer.valueOf(20)));
        assertThat(attributes.contains(CURRENT_FAMILY_NAME), is(Boolean.TRUE));
    }

    @Test
    public void testWrongAttributeCannotBeFound() throws Exception{
        ImmutableSortedSet<AttributeDefinition<?>> attributes = attributeRegistry.getAttributes();
        assertThat(attributes.contains(TOTALLY_WRONG_ATTRIBUTE), is(Boolean.FALSE));
    }
}
