/*
 *
 * #   Copyright (c) 2019 European Commission
 * #   Licensed under the EUPL, Version 1.2 or – as soon they will be
 * #   approved by the European Commission - subsequent versions of the
 * #    EUPL (the "Licence");
 * #    You may not use this work except in compliance with the Licence.
 * #    You may obtain a copy of the Licence at:
 * #    * https://joinup.ec.europa.eu/page/eupl-text-11-12
 * #    *
 * #    Unless required by applicable law or agreed to in writing, software
 * #    distributed under the Licence is distributed on an "AS IS" basis,
 * #    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * #    See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.eidas.auth.commons.attribute;

import com.google.common.collect.ImmutableSortedSet;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Test class for {@link AttributeSetPropertiesConverter}
 */
public class AttributeSetPropertiesConverterTest {

    private final static String NAME_URI = "http://eidas.europa.eu/attributes/naturalperson/AdditionalAttribute";
    private final static String ADDITIONAL_ATTRIBUTE = "AdditionalAttribute";
    private final static String NATURAL_PERSON = "NaturalPerson";
    private final static String NAMESPACE_URI= "http://www.w3.org/2001/XMLSchema";
    private final static String LOCAL_PART= "string";
    private final static String NAMESPACE_PREFIX = "xs";
    private final static String MARSHALLER_CLASS= "eu.eidas.auth.commons.attribute.impl.LiteralStringAttributeValueMarshaller";

    @Test
    /**
     * Test method for {@link AttributeSetPropertiesConverter#toAttributeSet(Properties)}
     * Builds an attribute definition with values containing characters that must be trimmed.
     * Must Succeed
     */
    public void toAttributeSet () {
        Properties properties = new Properties();
        properties.setProperty("1.NameUri", NAME_URI +"\n");
        properties.setProperty("1.FriendlyName", ADDITIONAL_ATTRIBUTE + "\n");
        properties.setProperty("1.PersonType", NATURAL_PERSON + "\n");
        properties.setProperty("1.Required", Boolean.FALSE + "\n");
        properties.setProperty("1.XmlType.NamespaceUri", NAMESPACE_URI + "\n");
        properties.setProperty("1.XmlType.LocalPart", LOCAL_PART + "\n");
        properties.setProperty("1.XmlType.NamespacePrefix", "xs\n");
        properties.setProperty("1.AttributeValueMarshaller", MARSHALLER_CLASS + "\n");

        ImmutableSortedSet immutableSortedSet = AttributeSetPropertiesConverter.toAttributeSet(properties);
        AttributeDefinition attributeDefinition = (AttributeDefinition) immutableSortedSet.first();
        assertAttributeDefinition(attributeDefinition);
    }

    private void assertAttributeDefinition(AttributeDefinition attributeDefinition) {
        assertThat(attributeDefinition.getNameUri().toString(), is(NAME_URI));
        assertThat(attributeDefinition.getFriendlyName(), is(ADDITIONAL_ATTRIBUTE));
        assertThat(attributeDefinition.getPersonType().toString(), is(NATURAL_PERSON));
        assertFalse(attributeDefinition.isRequired());
        assertThat(attributeDefinition.getXmlType().getNamespaceURI(), is(NAMESPACE_URI));
        assertThat(attributeDefinition.getXmlType().getLocalPart(), is(LOCAL_PART));
        assertThat(attributeDefinition.getXmlType().getPrefix(), is(NAMESPACE_PREFIX));

        AttributeValueMarshaller attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();
        assertNotNull(attributeValueMarshaller);
        assertEquals(MARSHALLER_CLASS, attributeValueMarshaller.getClass().getName());
    }
}