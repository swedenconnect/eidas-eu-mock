/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */

package eu.eidas.specificcommunication.protocol.impl;

import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link SpecificConnectorCommunicationServiceExtensionImpl}
 */
public class SpecificConnectorCommunicationServiceExtensionImplTest {

    private final String LIGHT_TOKEN_SECRET = "LIGHT_TOKEN_SECRET";

    private final String LIGHT_TOKEN_RESPONSE_ALGORITHM = "SHA-256";

    private final String LIGHT_TOKEN_REQUEST_NODE_ID = "lightTokenRequestNodeId";

    private final String LIGHT_TOKEN_RESPONSE_NODE_ID = "lightTokenResponseNodeId";

    /**
     * Test method for
     * {@link SpecificConnectorCommunicationServiceExtensionImpl#putRequest(String, ILightRequest)}
     * when a valid {@link BinaryLightToken} is set as key and a string is set as value (the light request)
     *
     * @throws SpecificCommunicationException if a {@link BinaryLightToken}
     * or the {@link SpecificConnectorCommunicationServiceExtensionImpl#putRequest(String, ILightRequest)}
     * could not be done.
     *
     * Must succeed.
     */
    @Test
    public void putRequest() throws SpecificCommunicationException {

        SpecificConnectorCommunicationServiceExtensionImpl specificConnectorCommunicationServiceExtension
                = createSpecificConnectorCommunicationServiceExtension();
        LightRequest lightRequest = createLightRequest();
        BinaryLightToken binaryLightToken = createBinaryLightToken();
        String binaryLightTokenBase64 = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);

        specificConnectorCommunicationServiceExtension.putRequest(binaryLightTokenBase64, lightRequest);
    }

    /**
     * Test method for
     * {@link SpecificConnectorCommunicationServiceExtensionImpl#putResponse(String, ILightResponse)}
     * when a valid {@link BinaryLightToken} is set as key and a string is set as value (the light request)
     *
     * @throws SpecificCommunicationException if a {@link BinaryLightToken}
     * or the {@link SpecificConnectorCommunicationServiceExtensionImpl#putResponse(String, ILightResponse)}
     * could not be done.
     *
     * Must succeed.
     */
    @Test
    public void putResponse() throws SpecificCommunicationException {
        SpecificConnectorCommunicationServiceExtensionImpl specificConnectorCommunicationServiceExtension
                = createSpecificConnectorCommunicationServiceExtension();
        LightResponse lightResponse = createLightResponse();
        BinaryLightToken binaryLightToken = createBinaryLightToken();
        String binaryLightTokenBase64 = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);

        specificConnectorCommunicationServiceExtension.putResponse(binaryLightTokenBase64, lightResponse);
    }

    /**
     * Test method for
     * {@link SpecificConnectorCommunicationServiceExtensionImpl#getLightTokenRequestNodeId()}
     * when the instance of {@link SpecificConnectorCommunicationServiceExtensionImpl} is valid.
     *
     * Must succeed.
     */
    @Test
    public void getLightTokenRequestNodeId(){
        SpecificConnectorCommunicationServiceExtensionImpl specificConnectorCommunicationServiceExtension
                = createSpecificConnectorCommunicationServiceExtension();

        String lightTokenRequestNodeId = specificConnectorCommunicationServiceExtension.getLightTokenRequestNodeId();

        Assert.assertEquals(LIGHT_TOKEN_REQUEST_NODE_ID, lightTokenRequestNodeId);
    }

    /**
     * Test method for
     * {@link SpecificConnectorCommunicationServiceExtensionImpl#getLightTokenResponseNodeId()}
     * when the instance of {@link SpecificConnectorCommunicationServiceExtensionImpl} is valid.
     *
     * Must succeed.
     */
    @Test
    public void getLightTokenResponseNodeId() {
        SpecificConnectorCommunicationServiceExtensionImpl specificConnectorCommunicationServiceExtension
                = createSpecificConnectorCommunicationServiceExtension();

        String lightTokenResponseNodeId = specificConnectorCommunicationServiceExtension.getLightTokenResponseNodeId();

        Assert.assertEquals(LIGHT_TOKEN_RESPONSE_NODE_ID, lightTokenResponseNodeId);
    }

    /**
     * Auxiliary method to create a {@link SpecificConnectorCommunicationServiceExtensionImpl}.
     *
     * @return the instance of {@link SpecificConnectorCommunicationServiceExtensionImpl}
     */
    private SpecificConnectorCommunicationServiceExtensionImpl createSpecificConnectorCommunicationServiceExtension() {
        return new SpecificConnectorCommunicationServiceExtensionImpl(LIGHT_TOKEN_REQUEST_NODE_ID,
                LIGHT_TOKEN_SECRET, LIGHT_TOKEN_RESPONSE_ALGORITHM,
                LIGHT_TOKEN_RESPONSE_NODE_ID, LIGHT_TOKEN_SECRET,
                LIGHT_TOKEN_RESPONSE_ALGORITHM);
    }

    /**
     * Auxiliary method to create a {@link SpecificConnectorCommunicationServiceExtensionImpl}.
     *
     * @throws SpecificCommunicationException if the {@link BinaryLightToken} could not be created
     * @return the instance of {@link BinaryLightToken}
     */
    private BinaryLightToken createBinaryLightToken() throws SpecificCommunicationException {
        BinaryLightToken binaryLightToken = BinaryLightTokenHelper.createBinaryLightToken(
                "specificCommunicationDefinition",
                LIGHT_TOKEN_SECRET,
                LIGHT_TOKEN_RESPONSE_ALGORITHM);
        return binaryLightToken;
    }

    /**
     * Auxiliary method to create a {@link LightRequest}.
     *
     * @return the instance of {@link LightRequest}
     */
    private LightRequest createLightRequest() {
        LightRequest.Builder lightRequesBuilder = new LightRequest.Builder();

        lightRequesBuilder
                .id("id")
                .issuer("issuer")
                .citizenCountryCode("CA")
        ;

        return lightRequesBuilder.build();
    }

    /**
     * Auxiliary method to create a {@link LightResponse}.
     *
     * @return the instance of {@link LightResponse}
     */
    private LightResponse createLightResponse() {
        LightResponse.Builder lightResponseBuilder = new LightResponse.Builder();

        ResponseStatus.Builder responseStatusBuilder = ResponseStatus.builder();
        responseStatusBuilder.statusCode("statusCode");
        ResponseStatus responseStatus = responseStatusBuilder.build();

        lightResponseBuilder
                .id("id")
                .issuer("issuer")
                .status(responseStatus)
                .subject("subject")
                .subjectNameIdFormat("subjectNameFormat")
                .inResponseToId("inResponseToId")
        ;

        return lightResponseBuilder.build();
    }
}