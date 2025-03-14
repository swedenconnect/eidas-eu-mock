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
package eu.eidas.auth.commons.attribute;

import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
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
        return new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker"))
                .put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude"))
                .build();
    }

    ImmutableAttributeMap newMapWithCollision() {
        return new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker"))
                .put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude"))
                .put(COLLIDING_GIVEN_NAME, new StringAttributeValue("Jean-Pierre"))
                .build();
    }

    ImmutableAttributeMap newMapWithoutValues() {
        return new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker"))
                .put(CURRENT_GIVEN_NAME)
                .build();
    }

    ImmutableAttributeMap newMapWithTwoValues() {
        return new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker"))
                .put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude"),
                     new StringAttributeValue("Jean-Pierre"))
                .build();
    }

    @SuppressWarnings({"PublicField"})
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testBuilderDuplicateNameUri() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Non-unique attribute name URIs for 2 attributes: ");

        new ImmutableAttributeMap.Builder().put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude"))
                .put(DUPLICATE_GIVEN_NAME, new StringAttributeValue("Jean-Pierre"))
                .build();
    }

    @Test
    public void testGetAttributeMap() throws Exception {
        Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> map = new HashMap<>();
        map.put(CURRENT_FAMILY_NAME, Collections.singleton(new StringAttributeValue("Juncker")));
        map.put(CURRENT_GIVEN_NAME, Collections.singleton(new StringAttributeValue("Jean-Claude")));
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
    public void testGetDefinitionsByFriendlyName() {
        assertThat(newMap().getDefinitionsByFriendlyName("FamilyName"),
                   is((Object) Set.of(CURRENT_FAMILY_NAME)));
        assertThat(newMap().getDefinitionsByFriendlyName("FirstName"),
                   is((Object) Set.of(CURRENT_GIVEN_NAME)));
        assertThat(newMap().getDefinitionsByFriendlyName("Unknown"), is(nullValue()));
        assertThat(newMapWithCollision().getDefinitionsByFriendlyName("FirstName"),
                   is((Object) Set.of(CURRENT_GIVEN_NAME, COLLIDING_GIVEN_NAME)));

    }

    @Test
    public void testValues() throws Exception {
        assertThat(newMap().getAttributeValues(CURRENT_GIVEN_NAME), is(not(nullValue())));
        assertThat(newMap().getAttributeValues(CURRENT_GIVEN_NAME),
                   contains((Object) new StringAttributeValue("Jean-Claude")));

        assertThat(newMapWithTwoValues().getAttributeValues(CURRENT_GIVEN_NAME), is(not(nullValue())));
        assertThat(newMapWithTwoValues().getAttributeValues(CURRENT_GIVEN_NAME),
                   contains((Object) new StringAttributeValue("Jean-Claude"),
                            (Object) new StringAttributeValue("Jean-Pierre")));

        assertThat(newMapWithoutValues().getAttributeValues(CURRENT_GIVEN_NAME),
                   either(hasSize(0)).or(is(nullValue())));
    }

    @Test
    public void testFirstValue() throws Exception {
        assertThat(newMap().getFirstAttributeValue(CURRENT_GIVEN_NAME),
                   is((Object) new StringAttributeValue("Jean-Claude")));
        assertThat(newMapWithTwoValues().getFirstAttributeValue(CURRENT_GIVEN_NAME),
                   is((Object) new StringAttributeValue("Jean-Claude")));
        assertThat(newMapWithoutValues().getFirstAttributeValue(CURRENT_GIVEN_NAME), is(nullValue()));
    }

    @Test
    public void testGetValuesByFriendlyName() {
        assertThat(newMap().getAttributeValuesByFriendlyName("FamilyName"),
                is((Object) ImmutableAttributeMap.of(CURRENT_FAMILY_NAME,
                        Set.of(new StringAttributeValue("Juncker")))));
        assertThat(newMap().getAttributeValuesByFriendlyName("FirstName"),
                is((Object) ImmutableAttributeMap.of(CURRENT_GIVEN_NAME, Set.of(
                        new StringAttributeValue("Jean-Claude")))));
        assertThat(newMap().getAttributeValuesByFriendlyName("Unknown"), is((ImmutableAttributeMap) null));
        assertThat(newMapWithCollision().getAttributeValuesByFriendlyName("FirstName"),
                is((Object) ImmutableAttributeMap.copyOf(Map.of(CURRENT_GIVEN_NAME, Set.of(
                        new StringAttributeValue("Jean-Claude")), COLLIDING_GIVEN_NAME, Set.of(
                        new StringAttributeValue("Jean-Pierre"))))));
    }

    @Test
    public void testGetValuesByName() {
        assertThat(newMap().getAttributeValuesByNameUri(
                        "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName"),
                is((Object) Set.of(new StringAttributeValue("Juncker"))));
        assertThat(newMap().getAttributeValuesByNameUri(
                        "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName"),
                is((Object) Set.of(new StringAttributeValue("Jean-Claude"))));
        assertThat(newMap().getAttributeValuesByNameUri("http://www.unknown.eu/Unknown"), is((Set) null));
    }

    @Test
    public void testGetValuesByNameUri() throws Exception {
        assertThat(newMap().getAttributeValuesByNameUri(
                        new URI("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")),
                is((Object) Set.of(new StringAttributeValue("Juncker"))));
        assertThat(newMap().getAttributeValuesByNameUri(
                        new URI("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName")),
                is((Object) Set.of(new StringAttributeValue("Jean-Claude"))));
        assertThat(newMap().getAttributeValuesByNameUri(new URI("http://www.unknown.eu/Unknown")),
                is((Set) null));
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
