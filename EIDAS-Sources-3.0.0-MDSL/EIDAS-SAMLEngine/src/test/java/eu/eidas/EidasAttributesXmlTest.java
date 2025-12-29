/*
 * Copyright (c) 2023 by European Commission
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

package eu.eidas;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.protocol.eidas.spec.LegalPersonSpec;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.commons.protocol.eidas.spec.RepresentativeLegalPersonSpec;
import eu.eidas.auth.commons.protocol.eidas.spec.RepresentativeNaturalPersonSpec;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Test class for test/resources/eidasAttributes/eidas-attributes.xml,
 * this file should serve as a reference toward the demo configuration.
 */
public class EidasAttributesXmlTest {


    /**
     * Test method for test/resources/eidasAttributes/eidas-attributes.xml against {@link NaturalPersonSpec#REGISTRY}
     * <p>
     * Must succeed.
     */
    @Test
    public void TestNaturalPersonXmlAttributes() {
        final AttributeRegistry xmlEidasAttributeRegistry = AttributeRegistries.fromFiles("eidasAttributes/eidas-attributes.xml", null);

        compareAttributeRegistries(NaturalPersonSpec.REGISTRY, xmlEidasAttributeRegistry);
    }

    /**
     * Test method for test/resources/eidasAttributes/eidas-attributes.xml against {@link RepresentativeNaturalPersonSpec#REGISTRY}
     * <p>
     * Must succeed.
     */
    @Test
    public void TestRepresentativeNaturalPersonXmlAttributes() {
        final AttributeRegistry xmlEidasAttributeRegistry = AttributeRegistries.fromFiles("eidasAttributes/eidas-attributes.xml", null);

        compareAttributeRegistries(RepresentativeNaturalPersonSpec.REGISTRY, xmlEidasAttributeRegistry);
    }

    /**
     * Test method for test/resources/eidasAttributes/eidas-attributes.xml against {@link LegalPersonSpec#REGISTRY}
     * <p>
     * Must succeed.
     */
    @Test
    public void TestLegalPersonSpecXmlAttributes() {
        final AttributeRegistry xmlEidasAttributeRegistry = AttributeRegistries.fromFiles("eidasAttributes/eidas-attributes.xml", null);

        compareAttributeRegistries(LegalPersonSpec.REGISTRY, xmlEidasAttributeRegistry);
    }

    /**
     * Test method for test/resources/eidasAttributes/eidas-attributes.xml against {@link RepresentativeLegalPersonSpec#REGISTRY}
     * <p>
     * Must succeed.
     */
    @Test
    public void TestRepresentativeLegalPersonSpecXmlAttributes() {
        final AttributeRegistry xmlEidasAttributeRegistry = AttributeRegistries.fromFiles("eidasAttributes/eidas-attributes.xml", null);

        compareAttributeRegistries(RepresentativeLegalPersonSpec.REGISTRY, xmlEidasAttributeRegistry);
    }




    private static void compareAttributeRegistries(AttributeRegistry expectedAttributeRegistry, AttributeRegistry actualAttributeRegistry) {
        final List<String> missingProperty = expectedAttributeRegistry.getAttributes().stream()
                .map(AttributeDefinition::getFriendlyName)
                .filter(friendlyName -> actualAttributeRegistry.getByFriendlyName(friendlyName).size() != 1)
                .collect(Collectors.toList());

        Assert.assertTrue(
                String.join("\n", String.format("No property for %s\"", missingProperty)),
                missingProperty.isEmpty()
        );

        for (AttributeDefinition attributeDefinition : expectedAttributeRegistry.getAttributes()) {
            Assert.assertEquals(attributeDefinition, actualAttributeRegistry.getByFriendlyName(attributeDefinition.getFriendlyName()).first());
        }
    }
}
