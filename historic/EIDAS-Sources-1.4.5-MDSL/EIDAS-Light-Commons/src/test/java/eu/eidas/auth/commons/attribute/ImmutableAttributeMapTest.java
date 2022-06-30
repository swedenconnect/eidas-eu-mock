package eu.eidas.auth.commons.attribute;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;

import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * ImmutableAttributeMapTest
 *
 * @since 1.1
 */
public final class ImmutableAttributeMapTest {

    private static final AttributeDefinition<String> CURRENT_FAMILY_NAME =
            new AttributeDefinition.Builder<String>().nameUri(
                    "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
                    .friendlyName("FamilyName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType",
                             "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .transliterationMandatory(true)
                    .build();

    private static final AttributeDefinition<String> CURRENT_GIVEN_NAME =
            new AttributeDefinition.Builder<String>().nameUri(
                    "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName")
                    .friendlyName("FirstName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentGivenNameType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final AttributeDefinition<String> COLLIDING_GIVEN_NAME =
            new AttributeDefinition.Builder<String>().nameUri("https://sector-specific.eu/firstname")
                    .friendlyName("FirstName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("https://sector-specific.eu", "FirstNameType", "sector-specific")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final AttributeDefinition<String> DUPLICATE_GIVEN_NAME =
            new AttributeDefinition.Builder<String>().nameUri(
                    "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName")
                    .friendlyName("FirstName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("https://sector-specific.eu", "FirstNameType", "sector-specific")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    ImmutableAttributeMap newMap() {
        return new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker", false))
                .put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude", false))
                .build();
    }

    ImmutableAttributeMap newMapWithCollision() {
        return new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker", false))
                .put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude", false))
                .put(COLLIDING_GIVEN_NAME, new StringAttributeValue("Jean-Pierre", false))
                .build();
    }

    ImmutableAttributeMap newMapWithoutValues() {
        return new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker", false))
                .put(CURRENT_GIVEN_NAME)
                .build();
    }

    ImmutableAttributeMap newMapWithTwoValues() {
        return new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker", false))
                .put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude", false),
                     new StringAttributeValue("Jean-Pierre", false))
                .build();
    }

    @SuppressWarnings({"PublicField"})
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testBuilderDuplicateNameUri() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Non-unique attribute name URIs for 2 attributes: ");

        new ImmutableAttributeMap.Builder().put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude", false))
                .put(DUPLICATE_GIVEN_NAME, new StringAttributeValue("Jean-Pierre", false))
                .build();
    }

    @Test
    public void testGetAttributeMap() throws Exception {
        Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> map = new HashMap<>();
        map.put(CURRENT_FAMILY_NAME, Collections.singleton(new StringAttributeValue("Juncker", false)));
        map.put(CURRENT_GIVEN_NAME, Collections.singleton(new StringAttributeValue("Jean-Claude", false)));
        assertThat(newMap().getAttributeMap(), is((Map) map));
    }

    @Test
    public void testGetDefinitionByNameUriString() throws Exception {
        assertThat(newMap().getDefinitionByNameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName"),
                   is((Object) CURRENT_FAMILY_NAME));
        assertThat(newMap().getDefinitionByNameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName"),
                   is((Object) CURRENT_GIVEN_NAME));
        assertThat(newMap().getDefinitionByNameUri("http://www.unknown.eu/Unknown"), is(nullValue()));
    }

    @Test
    public void testGetDefinitionByNameUri() throws Exception {
        assertThat(newMap().getDefinitionByNameUri(
                new URI("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")),
                   is((Object) CURRENT_FAMILY_NAME));
        assertThat(newMap().getDefinitionByNameUri(
                new URI("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName")),
                   is((Object) CURRENT_GIVEN_NAME));
        assertThat(newMap().getDefinitionByNameUri(new URI("http://www.unknown.eu/Unknown")), is((Object) null));
    }

    @Test
    public void testGetDefinitionsByFriendlyName() throws Exception {
        assertThat(newMap().getDefinitionsByFriendlyName("FamilyName"),
                   is((Object) ImmutableSet.of(CURRENT_FAMILY_NAME)));
        assertThat(newMap().getDefinitionsByFriendlyName("FirstName"),
                   is((Object) ImmutableSet.of(CURRENT_GIVEN_NAME)));
        assertThat(newMap().getDefinitionsByFriendlyName("Unknown"), is(nullValue()));
        assertThat(newMapWithCollision().getDefinitionsByFriendlyName("FirstName"),
                   is((Object) ImmutableSet.of(CURRENT_GIVEN_NAME, COLLIDING_GIVEN_NAME)));

    }

    @Test
    public void testValues() throws Exception {
        assertThat(newMap().getAttributeValues(CURRENT_GIVEN_NAME), is(not(nullValue())));
        assertThat(newMap().getAttributeValues(CURRENT_GIVEN_NAME),
                   contains((Object) new StringAttributeValue("Jean-Claude", false)));

        assertThat(newMapWithTwoValues().getAttributeValues(CURRENT_GIVEN_NAME), is(not(nullValue())));
        assertThat(newMapWithTwoValues().getAttributeValues(CURRENT_GIVEN_NAME),
                   contains((Object) new StringAttributeValue("Jean-Claude", false),
                            (Object) new StringAttributeValue("Jean-Pierre", false)));

        assertThat(newMapWithoutValues().getAttributeValues(CURRENT_GIVEN_NAME),
                   either(hasSize(0)).or(is(nullValue())));
    }

    @Test
    public void testFirstValue() throws Exception {
        assertThat(newMap().getFirstAttributeValue(CURRENT_GIVEN_NAME),
                   is((Object) new StringAttributeValue("Jean-Claude", false)));
        assertThat(newMapWithTwoValues().getFirstAttributeValue(CURRENT_GIVEN_NAME),
                   is((Object) new StringAttributeValue("Jean-Claude", false)));
        assertThat(newMapWithoutValues().getFirstAttributeValue(CURRENT_GIVEN_NAME), is(nullValue()));
    }

    @Test
    public void testGetValuesByFriendlyName() throws Exception {
        assertThat(newMap().getAttributeValuesByFriendlyName("FamilyName"),
                   is((Object) ImmutableAttributeMap.of(CURRENT_FAMILY_NAME,
                                                        ImmutableSet.of(new StringAttributeValue("Juncker", false)))));
        assertThat(newMap().getAttributeValuesByFriendlyName("FirstName"),
                   is((Object) ImmutableAttributeMap.of(CURRENT_GIVEN_NAME, ImmutableSet.of(
                           new StringAttributeValue("Jean-Claude", false)))));
        assertThat(newMap().getAttributeValuesByFriendlyName("Unknown"), is((ImmutableAttributeMap) null));
        assertThat(newMapWithCollision().getAttributeValuesByFriendlyName("FirstName"),
                   is((Object) ImmutableAttributeMap.copyOf((Map) ImmutableMap.of(CURRENT_GIVEN_NAME, ImmutableSet.of(
                           new StringAttributeValue("Jean-Claude", false)), COLLIDING_GIVEN_NAME, ImmutableSet.of(
                           new StringAttributeValue("Jean-Pierre", false))))));
    }

    @Test
    public void testGetValuesByName() throws Exception {
        assertThat(newMap().getAttributeValuesByNameUri(
                "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName"),
                   is((Object) ImmutableSet.of(new StringAttributeValue("Juncker", false))));
        assertThat(newMap().getAttributeValuesByNameUri(
                "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName"),
                   is((Object) ImmutableSet.of(new StringAttributeValue("Jean-Claude", false))));
        assertThat(newMap().getAttributeValuesByNameUri("http://www.unknown.eu/Unknown"), is((ImmutableSet) null));
    }

    @Test
    public void testGetValuesByNameUri() throws Exception {
        assertThat(newMap().getAttributeValuesByNameUri(
                new URI("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")),
                   is((Object) ImmutableSet.of(new StringAttributeValue("Juncker", false))));
        assertThat(newMap().getAttributeValuesByNameUri(
                new URI("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName")),
                   is((Object) ImmutableSet.of(new StringAttributeValue("Jean-Claude", false))));
        assertThat(newMap().getAttributeValuesByNameUri(new URI("http://www.unknown.eu/Unknown")),
                   is((ImmutableSet) null));
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(newMap(), newMap());
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals(newMap().hashCode(), newMap().hashCode());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals(
                "{AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName', friendlyName='FamilyName', personType=NaturalPerson, required=true, transliterationMandatory=true, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentFamilyNameType', attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[Juncker], AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName', friendlyName='FirstName', personType=NaturalPerson, required=true, transliterationMandatory=false, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentGivenNameType', attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[Jean-Claude]}",
                newMap().toString());
    }

    @Test
    public void testSerialization() throws Exception {
        ImmutableAttributeMap attributeMap = newMap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(attributeMap);
        oos.close();
        byte[] bytes = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        ImmutableAttributeMap result = (ImmutableAttributeMap) ois.readObject();

        assertEquals(2, result.getAttributeMap().size());
        assertEquals(attributeMap, result);
    }

    @Test
    public void testEmptyWithBuild() throws Exception {
        ImmutableAttributeMap empty = ImmutableAttributeMap.builder().build();
        assertTrue(empty.isEmpty());
    }

    @Test
    public void testEmptyWithOf() throws Exception {
        ImmutableAttributeMap empty = ImmutableAttributeMap.of();
        assertTrue(empty.isEmpty());
    }
}
