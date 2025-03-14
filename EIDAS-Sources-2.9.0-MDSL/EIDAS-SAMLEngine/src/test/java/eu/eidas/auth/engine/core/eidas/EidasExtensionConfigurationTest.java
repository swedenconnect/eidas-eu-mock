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
package eu.eidas.auth.engine.core.eidas;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.impl.AbstractProtocolSigner;
import org.junit.Assert;
import org.junit.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;

import javax.xml.namespace.QName;
import java.util.SortedSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link EidasExtensionConfiguration}
 */
public class EidasExtensionConfigurationTest {

    public static final String EIDAS_ATTRIBUTES_FILENAME = "eidas-attributes.xml";
    public static final String EIDAS_ATTRIBUTES_PATH = "src/test/resources/eidasAttributes/";

    /**
     * Test method for {@link EidasExtensionConfiguration#configureExtension(ProtocolProcessorI)}
     *
     * Must succeed
     */
    @Test
    public void configureExtension() {
        EidasProtocolProcessor mockEidasProtocolProcessor = mock(EidasProtocolProcessor.class);
        AbstractProtocolSigner mockProtocolSigner = mock(AbstractProtocolSigner.class);
        AttributeRegistry attributeRegistry = AttributeRegistries.fromFile(EIDAS_ATTRIBUTES_FILENAME, EIDAS_ATTRIBUTES_PATH);
        SortedSet<AttributeDefinition<?>> attributeDefinitions = attributeRegistry.getAttributes();
        when(mockEidasProtocolProcessor.getAllSupportedAttributes()).thenReturn(attributeDefinitions);

        EidasExtensionConfiguration.configureExtension(mockEidasProtocolProcessor,mockProtocolSigner);

        Marshaller marshallerFirstAttributeDefinition = getMarshallerFromXMLObjectProviderRegistrySupport(attributeDefinitions.first().getXmlType());
        Assert.assertNotNull(marshallerFirstAttributeDefinition);

        Marshaller marshallerLastAttributeDefinition = getMarshallerFromXMLObjectProviderRegistrySupport(attributeDefinitions.last().getXmlType());
        Assert.assertNotNull(marshallerLastAttributeDefinition);

        Marshaller marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(DigestMethod.DEF_ELEMENT_NAME);
        Assert.assertNotNull(marshaller);
    }

    private Marshaller getMarshallerFromXMLObjectProviderRegistrySupport(QName attributeDefinitions) {
        return XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(attributeDefinitions);
    }
}