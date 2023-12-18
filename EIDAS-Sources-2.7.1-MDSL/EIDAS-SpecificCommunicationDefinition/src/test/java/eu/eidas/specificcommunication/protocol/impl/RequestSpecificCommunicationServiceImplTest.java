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

package eu.eidas.specificcommunication.protocol.impl;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.HelperUtil;
import eu.eidas.specificcommunication.protocol.LightRequestTestHelper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.AbstractCollection;
import java.util.Collection;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:specificCommunicationDefinitionLoggerApplicationContext.xml"})
public class RequestSpecificCommunicationServiceImplTest {

    @Autowired
    private RequestSpecificCommunicationServiceImpl connectorRequestService;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private final String SERIALIZED_REQUEST_MESSAGE =  HelperUtil.readXmlTextFileAfterTag("src/test/resources/lightRequest.xml", "<lightRequest>");
    private static final AbstractCollection<AttributeDefinition<?>> REGISTRY = EidasSpec.REGISTRY.getAttributes();


    /**
     * Test method for
     * {@link RequestSpecificCommunicationServiceImpl#codecMarshall(ILightRequest)}
     * Returns a String
     * <p>
     * Must succeed.
     */
    @Test
    public void codecMarshall() throws SpecificCommunicationException {
        final ILightRequest originalLightRequest = LightRequestTestHelper.createDefaultLightRequest();

        final String xmlLightRequest = connectorRequestService.codecMarshall(originalLightRequest);

        MatcherAssert.assertThat(xmlLightRequest, Matchers.containsString(originalLightRequest.getId()));
        MatcherAssert.assertThat(xmlLightRequest, Matchers.containsString(originalLightRequest.getRelayState()));
        MatcherAssert.assertThat(xmlLightRequest, Matchers.containsString(originalLightRequest.getLevelsOfAssurance().get(0).getValue()));
        MatcherAssert.assertThat(xmlLightRequest, Matchers.containsString(originalLightRequest.getIssuer()));
        MatcherAssert.assertThat(xmlLightRequest, Matchers.containsString(originalLightRequest.getCitizenCountryCode()));
    }

    /**
     * Test method for
     * {@link RequestSpecificCommunicationServiceImpl#codecUnmarshall(String, Collection)}
     * Returns a {@link ILightRequest}
     * <p>
     * Must succeed.
     */
    @Test
    public void codecUnmarshall() throws SpecificCommunicationException {
        final ILightRequest iLightRequest = connectorRequestService.codecUnmarshall(SERIALIZED_REQUEST_MESSAGE, REGISTRY);

        Assert.assertNotNull(iLightRequest);
        Assert.assertNotNull(iLightRequest.getId());
    }

    /**
     * Test method for
     * {@link RequestSpecificCommunicationServiceImpl#codecUnmarshall(String, Collection)}
     * When the string xml is invalid, unmarshalling should be interrupted by an IllegalArgumentException
     * <p>
     * Must fail.
     */
    @Test
    public void codecUnmarshallInvalidXmlString() throws SpecificCommunicationException {
        expectedException.expect(IllegalArgumentException.class);

        connectorRequestService.codecUnmarshall("big mess" + SERIALIZED_REQUEST_MESSAGE, REGISTRY);
    }

    /**
     * Test method for
     * {@link RequestSpecificCommunicationServiceImpl#codecUnmarshall(String, Collection)}
     * When the string xml is null, unmarshalling should be interrupted by an SpecificCommunicationException
     * <p>
     * Must succeed.
     */
    @Test
    public void validateIncomingLightRequest() throws SpecificCommunicationException {
        expectedException.expect(SpecificCommunicationException.class);
        expectedException.expectMessage("Incoming light request is invalid.");

        connectorRequestService.codecUnmarshall(null, REGISTRY);
    }
}