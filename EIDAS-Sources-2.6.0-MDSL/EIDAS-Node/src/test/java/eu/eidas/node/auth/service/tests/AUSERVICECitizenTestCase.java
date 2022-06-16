/*
 * Copyright (c) 2021 by European Commission
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

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.engine.DefaultProtocolEngineFactory;
import eu.eidas.node.auth.service.AUSERVICECitizen;
import eu.eidas.node.auth.service.AUSERVICESAML;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.auth.util.tests.TestingConstants;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Properties;

/**
 * Test class for {@link AUSERVICECitizen}.
 *
 */
@FixMethodOrder(MethodSorters.JVM)
public final class AUSERVICECitizenTestCase {

    private final AUSERVICECitizen auserviceCitizen = new AUSERVICECitizen();

    private final ImmutableAttributeMap EMPTY_IMMUTABLE_ATTR_MAP = new ImmutableAttributeMap.Builder().build();

    /**
     *  Mandatory attributes according to the 1.2 spec
     */
    private final ImmutableAttributeMap MANDATORY_ATTR_LIST = ImmutableAttributeMap.builder()
            .put(NaturalPersonSpec.Definitions.CURRENT_FAMILY_NAME)
            .put(NaturalPersonSpec.Definitions.CURRENT_GIVEN_NAME)
            .put(NaturalPersonSpec.Definitions.DATE_OF_BIRTH)
            .put(NaturalPersonSpec.Definitions.PERSON_IDENTIFIER)
            .build();


    @Before
    public void setup() {
        final AUSERVICEUtil serviceUtil = new AUSERVICEUtil();
        serviceUtil.setConfigs(new Properties());
        auserviceCitizen.setServiceUtil(serviceUtil);

        final AUSERVICESAML auservicesaml = new AUSERVICESAML();
        auservicesaml.setSamlEngineInstanceName(TestingConstants.SAML_INSTANCE_CONS.toString());
        auservicesaml.setNodeProtocolEngineFactory(DefaultProtocolEngineFactory.getInstance());
        auserviceCitizen.setSamlService(auservicesaml);
    }

    /**
     * Test method for
     * {@link AUSERVICECitizen#checkMandatoryAttributes(ImmutableAttributeMap)} .
     * Minimal mandatory attribute list should not throw exception
     * <p>
     * Must succeed
     */
    @Test
    public void testCheckMandatoryAttributes() {
        auserviceCitizen.checkMandatoryAttributes(MANDATORY_ATTR_LIST);
    }

    /**
     * Test method for
     * {@link AUSERVICECitizen#checkMandatoryAttributes(ImmutableAttributeMap)} .
     * Empty personal attribute list led to an unmodified attribute list.
     * <p>
     * Must fail
     */
    @Test(expected = EIDASServiceException.class)
    public void testUpdateAttributeListValuesEmptyAttrList() {
        auserviceCitizen.checkMandatoryAttributes(EMPTY_IMMUTABLE_ATTR_MAP);
    }

    /**
     * Test method for
     * {@link AUSERVICECitizen#checkRepresentativeAttributes(ImmutableAttributeMap)}
     * When attribute map contains representative attribute
     * <p>
     * Must fail
     */
    @Test(expected = EIDASServiceException.class)
    public void testcheckRepresentativeAttributesRepresentativePresent() {
        final AttributeDefinition identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        final AttributeDefinition repvIdentifier = EidasSpec.Definitions.REPV_PERSON_IDENTIFIER;
        final AttributeValue stringValue = new StringAttributeValue("1234 4321");

        final ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .put(repvIdentifier, stringValue)
                .build();

        auserviceCitizen.checkRepresentativeAttributes(attributesMap);
    }

    /**
     * Test method for
     * {@link AUSERVICECitizen#checkRepresentativeAttributes(ImmutableAttributeMap)}
     * When attribute map does not contain representative attribute
     * <p>
     * Must succeed
     */
    @Test
    public void testcheckRepresentativeAttributesRepresentativeAbsent() {
        final AttributeDefinition identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        final AttributeValue stringValue = new StringAttributeValue("1234 4321");

        final ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .build();

        auserviceCitizen.checkRepresentativeAttributes(attributesMap);
    }
}
