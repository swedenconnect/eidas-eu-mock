/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
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