package eu.eidas.engine.test.simple.eidas;

import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import org.junit.Test;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.engine.core.eidas.EidasExtensionProcessor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EidasExtensionProcessorTest {

    private static final String TEST_ATTRIBUTE_FULL_NAME =
            "http://eidas.europa.eu/attributes/naturalperson/EidasAdditionalAttribute";

    private static final String TEST_ATTRIBUTE_INVALID = "urn:not_found";

    @Test
    public void testGetDynamicAtributeType() throws Exception {
        EidasExtensionProcessor eidasExtensionProcessor =
                new EidasExtensionProcessor(EidasSpec.REGISTRY, AttributeRegistries.fromFile("saml-engine-additional-attributes-TEMPLATE.xml", null), null, null);
        AttributeDefinition attributeDefinition =
                eidasExtensionProcessor.getAdditionalAttributes().getByName(TEST_ATTRIBUTE_FULL_NAME);
        assertNotNull(attributeDefinition);

        attributeDefinition = eidasExtensionProcessor.getAdditionalAttributes().getByName(TEST_ATTRIBUTE_INVALID);
        assertNull(attributeDefinition);
    }
}