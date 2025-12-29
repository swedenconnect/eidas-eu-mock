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

package eu.eidas.auth.commons.protocol.eidas.spec;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.PersonType;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * Test class for {@link RepresentativeLegalPersonSpec}
 */
public class RepresentativeLegalPersonSpecTest {

    /**
     * Test method for {@link RepresentativeLegalPersonSpec#REGISTRY}
     * {@link AttributeDefinition} should be derived from {@link LegalPersonSpec#REGISTRY} as described in
     * eIDAS SAML Attribute profile // 2.8. NATURAL AND LEGAL PERSON REPRESENTATIVE
     * <p>
     * Must succeed.
     */
    @Test
    public void legalRepresentativeHasSameAttributesThenLegal() {
        final List<String> missingRepresentative = LegalPersonSpec.REGISTRY.getAttributes().stream()
                .map(AttributeDefinition::getFriendlyName)
                .filter(friendlyName -> RepresentativeLegalPersonSpec.REGISTRY.getByFriendlyName("Representative" + friendlyName).size() != 1)
                .collect(Collectors.toList());

        Assert.assertTrue(
                String.join("\n", String.format("No representative for %s\"", missingRepresentative)),
                missingRepresentative.isEmpty()
        );

        for (AttributeDefinition attributeDefinition : LegalPersonSpec.REGISTRY.getAttributes()) {
            final AttributeDefinition<Object> expectedRepresentation = AttributeDefinition.builder()
                    .nameUri(attributeDefinition.getNameUri().toString().replace(
                            LegalPersonSpec.Namespace.URI,
                            RepresentativeLegalPersonSpec.Namespace.URI
                    ))
                    .friendlyName("Representative" + attributeDefinition.getFriendlyName())
                    .personType(PersonType.REPV_LEGAL_PERSON)
                    .xmlType(
                            RepresentativeLegalPersonSpec.Namespace.URI,
                            attributeDefinition.getXmlType().getLocalPart(),
                            RepresentativeLegalPersonSpec.Namespace.PREFIX
                    )
                    .attributeValueMarshaller((AttributeValueMarshaller<Object>) attributeDefinition.getAttributeValueMarshaller())
                    .required(attributeDefinition.isRequired())
                    .uniqueIdentifier(attributeDefinition.isUniqueIdentifier())
                    .transliterationMandatory(attributeDefinition.isTransliterationMandatory())
                    .build();

            final SortedSet<AttributeDefinition<?>> listOfRepresentativesMatchingAttributeDef =
                    RepresentativeLegalPersonSpec.REGISTRY.getByFriendlyName("Representative" + attributeDefinition.getFriendlyName());

            final AttributeDefinition<?> actualRepresentation = listOfRepresentativesMatchingAttributeDef.first();
            Assert.assertEquals(expectedRepresentation, actualRepresentation);
        }
    }
}