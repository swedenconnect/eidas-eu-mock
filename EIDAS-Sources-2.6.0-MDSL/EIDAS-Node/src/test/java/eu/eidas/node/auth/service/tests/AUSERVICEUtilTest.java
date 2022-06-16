/*
 * Copyright (c) 2020 by European Commission
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

package eu.eidas.node.auth.service.tests;


import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.Set;

/**
 * Unit test class for {@link AUSERVICEUtil}.
 */
public class AUSERVICEUtilTest {

    private AUSERVICEUtil auserviceUtil;

    private Properties configs;

    /**
     * Initialize the CONFIGS properties for each test to avoid
     * inherited configurations
     */
    @Before
    public void initialize(){
        auserviceUtil = new AUSERVICEUtil();

        configs = new Properties();
        auserviceUtil.setConfigs(configs);
    }

    /**
     * Test method for {@link AUSERVICEUtil#getUnsupportedAttributes()}
     * When {@link EidasParameterKeys#UNSUPPORTED_ATTRIBUTES} parameter is filled in.
     *
     * Must succeed
     */
    @Test
    public void testGetProcessableAttributesWithUnsupportedAttributesParameter() {
        String unsupportedAttributes = EidasSpec.Definitions.BIRTH_NAME.getNameUri().toASCIIString() + ";"
                + EidasSpec.Definitions.LEGAL_NAME.getNameUri().toASCIIString();

        configs.setProperty(EidasParameterKeys.UNSUPPORTED_ATTRIBUTES.toString(), unsupportedAttributes);

        Set<String> result = auserviceUtil.getUnsupportedAttributes();

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains(EidasSpec.Definitions.BIRTH_NAME.getNameUri().toASCIIString()));
        Assert.assertTrue(result.contains(EidasSpec.Definitions.LEGAL_NAME.getNameUri().toASCIIString()));
    }

    /**
     * Test method for {@link AUSERVICEUtil#getUnsupportedAttributes()}
     * When {@link EidasParameterKeys#UNSUPPORTED_ATTRIBUTES} parameter is left empty.
     *
     * Must succeed
     */
    @Test
    public void testGetProcessableAttributesWithEmptyUnsupportedAttributesParameter() {
        configs.setProperty(EidasParameterKeys.UNSUPPORTED_ATTRIBUTES.toString(), "");

        Set<String> result = auserviceUtil.getUnsupportedAttributes();

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    /**
     * Test method for {@link AUSERVICEUtil#getUnsupportedAttributes()}
     * When {@link EidasParameterKeys#UNSUPPORTED_ATTRIBUTES} parameter is not defined.
     *
     * Must succeed
     */
    @Test
    public void testGetProcessableAttributesWithoutUnsupportedAttributesParameter() {
        Set<String> result = auserviceUtil.getUnsupportedAttributes();

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }
}
