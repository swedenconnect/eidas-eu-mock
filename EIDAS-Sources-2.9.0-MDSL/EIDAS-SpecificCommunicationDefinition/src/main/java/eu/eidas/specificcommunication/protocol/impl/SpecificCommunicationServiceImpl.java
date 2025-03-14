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
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.CommunicationCache;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;

import java.util.Collection;

abstract public class SpecificCommunicationServiceImpl <T> {

    final protected static LightJAXBCodec codec = LightJAXBCodec.buildDefault();
    final private String lightTokenIssuerName;
    final private String lightTokenSecret;
    final private String lightTokenAlgorithm;
    final private CommunicationCache communicationCache;

    public SpecificCommunicationServiceImpl(String lightTokenIssuerName, String lightTokenSecret, String lightTokenAlgorithm, CommunicationCache communicationCache) {
        this.lightTokenIssuerName = lightTokenIssuerName;
        this.lightTokenSecret = lightTokenSecret;
        this.lightTokenAlgorithm = lightTokenAlgorithm;
        this.communicationCache = communicationCache;
    }

    /**
     * Extra "Extension" method for inserting a Specific Communication Objects with a custom token.
     * @param tokenBase64 token from the browser
     * @param iLightMessage
     * @throws SpecificCommunicationException
     */
    public void put(final String tokenBase64, final T iLightMessage) throws SpecificCommunicationException {
        final String binaryLightTokenId = BinaryLightTokenHelper.getBinaryLightTokenId(
                tokenBase64,
                lightTokenSecret,
                lightTokenAlgorithm
        );
        final CommunicationCache communicationCache = getCommunicationCache();
        communicationCache.put(binaryLightTokenId, codecMarshall(iLightMessage));
    }

    /**
     * Core method for a Specific Communication Object to enter the cache: Generates a new token to store the object.
     * @param iLightMessage
     * @return BinaryLightToken
     * @throws SpecificCommunicationException
     */
    public BinaryLightToken put(final T iLightMessage) throws SpecificCommunicationException {
        final BinaryLightToken binaryLightToken = BinaryLightTokenHelper.createBinaryLightToken(
                lightTokenIssuerName,
                lightTokenSecret,
                lightTokenAlgorithm
        );
        final String tokenId = binaryLightToken.getToken().getId();
        final CommunicationCache communicationCache = getCommunicationCache();
        communicationCache.put(tokenId, codecMarshall(iLightMessage));
        return binaryLightToken;
    }

    /**
     * Core method for Specific Communication Objects to leave the cache: Retrieves and removes Objects.
     * @param tokenBase64 token from the browser
     * @param registry Collection of attribute definitions. eg: EidasSpec.REGISTRY.getAttributes()
     * @return T Specific Communication Object
     * @throws SpecificCommunicationException
     */
    public T getAndRemove(final String tokenBase64, final Collection<AttributeDefinition<?>> registry)
            throws SpecificCommunicationException {
        final String binaryLightTokenId = BinaryLightTokenHelper.getBinaryLightTokenId(
                tokenBase64,
                lightTokenSecret,
                lightTokenAlgorithm
        );
        final CommunicationCache communicationCache = getCommunicationCache();
        final String lightMessage = communicationCache.getAndRemove(binaryLightTokenId);
        validateIncomingString(lightMessage);
        return codecUnmarshall(lightMessage, registry);
    }

    /**
     * Logging method for retrieving a Specific Communication Object to be logged.
     * @param tokenBase64 from the browser
     * @param registry Collection of attribute definitions. eg: EidasSpec.REGISTRY.getAttributes()
     * @return T Specific Communication Object
     * @throws SpecificCommunicationException
     */
    public T get(final String tokenBase64, final Collection<AttributeDefinition<?>> registry)
            throws SpecificCommunicationException {
        return codecUnmarshall(getString(tokenBase64), registry);
    }

    /**
     * Logging method for retrieving a Specific Communication Object as a String to be logged.
     * @param tokenBase64 token from the browser
     * @return String of the Specific Communication Object
     * @throws SpecificCommunicationException
     */
    public String getString(final String tokenBase64)
            throws SpecificCommunicationException {
        final String binaryLightTokenId = BinaryLightTokenHelper.getBinaryLightTokenId(
                tokenBase64,
                lightTokenSecret,
                lightTokenAlgorithm
        );
        final String lightMessage = getCommunicationCache().get(binaryLightTokenId);
        validateIncomingString(lightMessage);
        return lightMessage;
    }

    private CommunicationCache getCommunicationCache() {
        return communicationCache;
    }

    abstract String codecMarshall(T iLightMessage) throws SpecificCommunicationException;

    abstract T codecUnmarshall(String lightMessage, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException;

    abstract void validateIncomingString(String lightMessage) throws SpecificCommunicationException;
}
