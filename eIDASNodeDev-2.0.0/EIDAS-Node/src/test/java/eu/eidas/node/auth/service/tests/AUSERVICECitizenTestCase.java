/*
 * Copyright (c) 2017 by European Commission
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.DefaultProtocolEngineFactory;
import eu.eidas.node.auth.service.*;
import eu.eidas.node.auth.util.tests.TestingConstants;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Properties;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Functional testing class to {@link AUSERVICECitizen}.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com
 * @version $Revision: $, $Date:$
 */
@FixMethodOrder(MethodSorters.JVM)
public final class AUSERVICECitizenTestCase {

    /**
     * Citizen Consent Object
     */
    private static ISERVICECitizenService AUSERVICECITIZEN = new AUSERVICECitizen();

    /**
     * Empty String[].
     */
    private static String[] EMPTY_STR_ARRAY = new String[0];

    private static WebRequest newEmptyWebRequest() {
        return new IncomingRequest(BindingMethod.POST, ImmutableMap.<String, ImmutableList<String>>of(),
                                   "127.0.0.1", null);
    }

    private static WebRequest newSingleParamWebRequest(String paramName, String paramValue) {
        return new IncomingRequest(BindingMethod.POST,
                                   ImmutableMap.<String, ImmutableList<String>>of(paramName,
                                                                                  ImmutableList.<String>of(paramValue)),
                                   "127.0.0.1", null);
    }

    private static WebRequest newWebRequest(String paramName1,
                                            String paramValue1,
                                            String paramName2,
                                            String paramValue2) {
        return new IncomingRequest(BindingMethod.POST,
                                   ImmutableMap.<String, ImmutableList<String>>of(paramName1,
                                                                                  ImmutableList.<String>of(paramValue1),
                                                                                  paramName2, ImmutableList.<String>of(
                                                   paramValue2)), "127.0.0.1", null);
    }

    /**
     * Empty parameters.
     */
    private static WebRequest EMPTY_PARAMETERS = newEmptyWebRequest();

    /**
     * Empty Personal Attribute List.
     */
    private static ImmutableAttributeMap EMPTY_ATTR_LIST = ImmutableAttributeMap.of();

    /**
     * Empty Immutable Attribute Map.
     */
    private static ImmutableAttributeMap EMPTY_IMMUTABLE_ATTR_MAP = new ImmutableAttributeMap.Builder().build();

    /**
     * Personal Attribute List with dummy attributes.
     */
    private static final ImmutableAttributeMap ATTR_LIST = ImmutableAttributeMap.builder().put(NaturalPersonSpec.Definitions.BIRTH_NAME, "FREDERIC").build();

    /**
     * EidasAuthenticationRequest object.
     */
    private static final StoredAuthenticationRequest AUTH_DATA = StoredAuthenticationRequest.builder().request(EidasAuthenticationRequest.builder().requestedAttributes(ATTR_LIST).destination(TestingConstants.REQUEST_DESTINATION_CONS.toString())
                    .issuer(TestingConstants.REQUEST_ISSUER_CONS.toString())
                    .assertionConsumerServiceURL(TestingConstants.ASSERTION_URL_CONS.toString())
                    .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                    .id(TestingConstants.REQUEST_ID_CONS.toString())
                    .build()).remoteIpAddress("127.0.0.1").build();

    /**
     * Dummy User IP.
     */
    private static String USER_IP = "10.10.10.10";

    /**
     * Initialising class variables.
     *
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void runBeforeClass() throws Exception {
        ISERVICESAMLService mockedServiceSAMLService = mock(ISERVICESAMLService.class);

        when(mockedServiceSAMLService.generateErrorAuthenticationResponse((IAuthenticationRequest) any(), anyString(),
                                                                          anyString(), anyString(), anyString(), anyString(),
                                                                          anyBoolean())).thenReturn(new byte[0]);

        when(mockedServiceSAMLService.updateRequest((IAuthenticationRequest) any(), (ImmutableAttributeMap) any())).thenReturn(AUTH_DATA.getRequest());

        when(mockedServiceSAMLService.getSamlEngine()).thenReturn(DefaultProtocolEngineFactory.getInstance()
                                                                          .getProtocolEngine(TestingConstants.SAML_INSTANCE_CONS.toString()));

        ((AUSERVICECitizen)AUSERVICECITIZEN).setSamlService(mockedServiceSAMLService);
    }

    private IAuthenticationRequest getFreshRequestWithAttrs() {
        EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder = EidasAuthenticationRequest.builder();
        eidasAuthenticationRequestBuilder.requestedAttributes(ATTR_LIST);
        return eidasAuthenticationRequestBuilder.build();
    }

    /**
     * Test method for {@link AUSERVICECitizen#checkMandatoryAttributes(ImmutableAttributeMap)} . Empty
     * personal attribute list led to an unmodified attribute list.
     */
    @Test(expected = EIDASServiceException.class)
    public void testUpdateAttributeListValuesEmptyAttrList() {
        AUSERVICECitizen auserviceCitizen = new AUSERVICECitizen();
        AUSERVICEUtil serviceUtil = new AUSERVICEUtil();
        serviceUtil.setConfigs(new Properties());
        auserviceCitizen.setServiceUtil(serviceUtil);
        AUSERVICESAML auservicesaml = new AUSERVICESAML();
        auservicesaml.setSamlEngineInstanceName(TestingConstants.SAML_INSTANCE_CONS.toString());
        auservicesaml.setNodeProtocolEngineFactory(DefaultProtocolEngineFactory.getInstance());
        auserviceCitizen.setSamlService(auservicesaml);
        auserviceCitizen.checkMandatoryAttributes(EMPTY_IMMUTABLE_ATTR_MAP);
    }


}
