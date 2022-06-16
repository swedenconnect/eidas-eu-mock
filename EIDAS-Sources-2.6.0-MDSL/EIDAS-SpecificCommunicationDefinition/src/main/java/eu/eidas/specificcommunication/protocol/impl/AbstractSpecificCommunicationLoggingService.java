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

package eu.eidas.specificcommunication.protocol.impl;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.CommunicationCache;
import eu.eidas.specificcommunication.SpecificCommunicationApplicationContextProvider;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Implements {@link SpecificCommunicationLoggingService} to be used for logging
 * {@link ILightRequest} and {@link ILightResponse} between the specific
 * part and node
 *
 * @since 2.5
 */
public abstract class AbstractSpecificCommunicationLoggingService implements SpecificCommunicationLoggingService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSpecificCommunicationLoggingService.class);

    private LightJAXBCodec codec = LightJAXBCodec.buildDefault();

    private String lightTokenRequestSecret;

    private String lightTokenRequestAlgorithm;

    private String lightTokenResponseSecret;

    private String lightTokenResponseAlgorithm;

    AbstractSpecificCommunicationLoggingService(final String lightTokenRequestSecret, final String lightTokenRequestAlgorithm,
                                                    final String lightTokenResponseSecret, final String lightTokenResponseAlgorithm) {

        this.lightTokenRequestSecret = lightTokenRequestSecret;
        this.lightTokenRequestAlgorithm = lightTokenRequestAlgorithm;
        this.lightTokenResponseSecret = lightTokenResponseSecret;
        this.lightTokenResponseAlgorithm = lightTokenResponseAlgorithm;
    }

    /**
     * Gets the {@link ILightRequest} from the request communication cache
     * using as id/key the one obtained from the {@link BinaryLightToken}.
     *
     * @param tokenBase64 the {@link BinaryLightToken} in Base64 that holds the id to get the {@link ILightRequest}
     * @param registry    the collection of attributeDefinitions
     * @return the {@link ILightRequest} corresponding to the id
     * @throws SpecificCommunicationException if the id could not be obtained
     */
    @Override
    public ILightRequest getRequest(String tokenBase64, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
        final String lightRequest = getRequest(tokenBase64);
        return codec.unmarshallRequest(lightRequest, registry);
    }

    /**
     * Gets the {@link ILightRequest} as a {@link String}
     * using as id/key the one obtained from the {@link BinaryLightToken}.
     *
     * @param lightRequestToken the {@link BinaryLightToken} in Base64 that holds the id to get the {@link ILightRequest}
     * @return the LightRequest as a {@link String}, corresponding to the tokenBase64
     * @throws SpecificCommunicationException if the id could not be obtained
     */
    @Override
    public String getRequest(String lightRequestToken) throws SpecificCommunicationException {
        String lightRequest = getLightRequestFromTokenBase64(lightRequestToken);

        return lightRequest;
    }

    /**
     * Gets the {@link ILightResponse} from the response communication cache
     * using as id/key the one obtained from the {@link BinaryLightToken}.
     *
     * @param tokenBase64 the {@link BinaryLightToken} in Base64 that holds the id to get the {@link ILightResponse}
     * @param registry    the collection of attributeDefinitions
     * @return the {@link ILightResponse} corresponding to the id
     * @throws SpecificCommunicationException if the id could not be obtained
     */
    @Override
    public ILightResponse getResponse(String tokenBase64, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
        final String lightResponse = getResponse(tokenBase64);
        return codec.unmarshallResponse(lightResponse, registry);
    }

    /**
     * Gets the {@link ILightResponse} as a {@link String}
     * using as id/key the one obtained from the {@link BinaryLightToken}.
     *
     * @param tokenBase64 the {@link BinaryLightToken} in Base64 that holds the id to get the {@link ILightResponse}
     * @return the LightResponse as a {@link String}, corresponding to the tokenBase64
     * @throws SpecificCommunicationException if the id could not be obtained
     */
    @Override
    public String getResponse(String tokenBase64) throws SpecificCommunicationException {
        final String lightResponse = getLightResponseFromTokenBase64(tokenBase64);

        return lightResponse;
    }

    private String getLightRequestFromTokenBase64(String tokenBase64) throws SpecificCommunicationException {
        final String binaryLightTokenId = getBinaryLightTokenId(tokenBase64, getLightTokenRequestSecret(), getLightTokenRequestAlgorithm());
        final CommunicationCache requestCommunicationCache = getRequestCommunicationCache();

        return requestCommunicationCache.get(binaryLightTokenId);
    }

    /**
     * Method to extract {@link ILightResponse} as a {@link String} from the response communication cache, corresponding to the tokenBase64
     *
     * @param tokenBase64 the {@link BinaryLightToken} in Base64 that holds the id to get the {@link ILightResponse}
     * @return the LightResponse as a {@link String}, corresponding to the tokenBase64
     * @throws SpecificCommunicationException if the id could not be obtained
     */
    private String getLightResponseFromTokenBase64(String tokenBase64) throws SpecificCommunicationException {
        final String binaryLightTokenId = getBinaryLightTokenId(tokenBase64, getLightTokenResponseSecret(), getLightTokenResponseAlgorithm());
        final CommunicationCache specificNodeConnectorResponseCommunicationCache = getResponseCommunicationCache();

        return specificNodeConnectorResponseCommunicationCache.get(binaryLightTokenId);
    }

    private CommunicationCache getRequestCommunicationCache() {
        return (CommunicationCache) SpecificCommunicationApplicationContextProvider
                .getApplicationContext()
                .getBean(getRequestCacheName());
    }

    private CommunicationCache getResponseCommunicationCache() {
        return (CommunicationCache) SpecificCommunicationApplicationContextProvider
                .getApplicationContext()
                .getBean(getResponseCacheName());
    }

    private String getBinaryLightTokenId(String tokenBase64, String secret, String algorithm) throws SpecificCommunicationException {
        final String binaryLightTokenId = BinaryLightTokenHelper.getBinaryLightTokenId(tokenBase64,
                secret, algorithm);
        return binaryLightTokenId;
    }

    /**
     * Gets the request cache name specific to the implementation
     * @return the request cache name
     */
    protected abstract String getRequestCacheName();

    /**
     * Gets the response cache name specific to the implementation
     * @return the response cache name
     */
    protected abstract String getResponseCacheName();

    private String getLightTokenRequestSecret() {return lightTokenRequestSecret;}
    private String getLightTokenRequestAlgorithm() {return lightTokenRequestAlgorithm;}
    private String getLightTokenResponseSecret() {return lightTokenResponseSecret;}
    private String getLightTokenResponseAlgorithm() {return lightTokenResponseAlgorithm;}
}
