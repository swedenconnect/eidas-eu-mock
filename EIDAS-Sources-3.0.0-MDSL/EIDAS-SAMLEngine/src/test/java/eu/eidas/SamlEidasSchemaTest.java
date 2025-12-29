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
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.protocol.eidas.spec.LegalPersonSpec;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.commons.protocol.eidas.spec.RepresentativeLegalPersonSpec;
import eu.eidas.auth.commons.protocol.eidas.spec.RepresentativeNaturalPersonSpec;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test class for Saml Engine XSD files based on Common Module Repositories
 */
public class SamlEidasSchemaTest {

    /**
     * Test method for saml_eidas_natural_person.xsd
     * should define types used in {@link NaturalPersonSpec#REGISTRY}
     * <p>
     * Must succeed.
     */
    @Test
    public void testNaturalSchema() throws ParserConfigurationException, IOException, SAXException {
        xsdSchemaHasRepositorySchemaTypes("eidas/saml_eidas_natural_person.xsd", NaturalPersonSpec.REGISTRY);
    }

    /**
     * Test method for saml_eidas_natural_person.xsd
     * should define types used in {@link RepresentativeNaturalPersonSpec#REGISTRY}
     * <p>
     * Must succeed.
     */
    @Test
    public void testNaturalRepresentativeSchema() throws ParserConfigurationException, IOException, SAXException {
        xsdSchemaHasRepositorySchemaTypes("eidas/saml_eidas_representative_natural_person.xsd", RepresentativeNaturalPersonSpec.REGISTRY);
    }

    /**
     * Test method for saml_eidas_natural_person.xsd
     * should define types used in {@link LegalPersonSpec#REGISTRY}
     * <p>
     * Must succeed.
     */
    @Test
    public void testLegalSchema() throws ParserConfigurationException, IOException, SAXException {
        xsdSchemaHasRepositorySchemaTypes("eidas/saml_eidas_legal_person.xsd", LegalPersonSpec.REGISTRY);
    }

    /**
     * Test method for saml_eidas_natural_person.xsd
     * should define types used in {@link RepresentativeLegalPersonSpec#REGISTRY}
     * <p>
     * Must succeed.
     */
    @Test
    public void testLegalRepresentativeSchema() throws ParserConfigurationException, IOException, SAXException {
        xsdSchemaHasRepositorySchemaTypes("eidas/saml_eidas_representative_legal_person.xsd", RepresentativeLegalPersonSpec.REGISTRY);
    }

    private void xsdSchemaHasRepositorySchemaTypes(String fileOnContext, AttributeRegistry registry) throws ParserConfigurationException, IOException, SAXException {
        final List<Element> legalTypes = findAllTypesInFile(fileOnContext);
        final List<String> legalTypeNames = legalTypes.stream().map(type -> type.getAttribute("name")).collect(Collectors.toList());
        final List<String> missingXmlTypeDefinition = registry.getAttributes().stream()
                .map(AttributeDefinition::getXmlType)
                .map(QName::getLocalPart)
                .filter(localPart -> !legalTypeNames.contains(localPart))
                .collect(Collectors.toList());

        Assert.assertTrue(
                String.join("\n", String.format("No type definitions found for %s\" in %s", missingXmlTypeDefinition, fileOnContext)),
                missingXmlTypeDefinition.isEmpty()
        );
    }

    private List<Element> findAllTypesInFile(String fileOnContext) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileOnContext));

        final List<Element> typeDefinitions = new ArrayList<>();

        final NodeList simpleTypeNodes = document.getElementsByTagName("xsd:simpleType");
        for (int i = 0; i < simpleTypeNodes.getLength(); i++) {
            typeDefinitions.add((Element)simpleTypeNodes.item(i));
        }

        final NodeList complexTypeNodes = document.getElementsByTagName("xsd:complexType");
        for (int i = 0; i < complexTypeNodes.getLength(); i++) {
            typeDefinitions.add((Element)complexTypeNodes.item(i));
        }

        return typeDefinitions;
    }
}
