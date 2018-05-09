package eu.eidas.engine.test.simple.eidas;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EidasExtensionProcessorTest {

    private static final String TEST_ATTRIBUTE_FULL_NAME =
            "http://eidas.europa.eu/attributes/naturalperson/EidasAdditionalAttribute";

    private static final String TEST_ATTRIBUTE_INVALID = "urn:not_found";

    @Test
    public void testGetDynamicAtributeType() throws Exception {
        EidasProtocolProcessor eidasExtensionProcessor =
                new EidasProtocolProcessor(EidasSpec.REGISTRY, AttributeRegistries.fromFile("saml-engine-additional-attributes-TEMPLATE.xml", null), null, null, null);
        AttributeDefinition attributeDefinition =
                eidasExtensionProcessor.getAdditionalAttributes().getByName(TEST_ATTRIBUTE_FULL_NAME);
        assertNotNull(attributeDefinition);

        attributeDefinition = eidasExtensionProcessor.getAdditionalAttributes().getByName(TEST_ATTRIBUTE_INVALID);
        assertNull(attributeDefinition);
    }
}