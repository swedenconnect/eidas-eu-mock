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

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.CommunicationCache;
import eu.eidas.specificcommunication.SpecificCommunicationApplicationContextProvider;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationServiceExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Implements {@link SpecificCommunicationServiceExtension} to be used for exchanging of
 * {@link ILightRequest} and {@link ILightResponse} between the specific
 * connector and node connector
 *
 * @since 2.3
 */
public class SpecificConnectorCommunicationServiceExtensionImpl implements SpecificCommunicationServiceExtension {
    private static final Logger LOG = LoggerFactory.getLogger(SpecificCommunicationServiceExtension.class);
    private static LightJAXBCodec codec;
    static {
        try {
            codec = new LightJAXBCodec(JAXBContext.newInstance(LightRequest.class, LightResponse.class,
                    ImmutableAttributeMap.class, AttributeDefinition.class));
        } catch (JAXBException e) {
            LOG.error("Unable to instantiate in static initializer ",e);
        }
    }
    private String lightTokenRequestNodeId;

    private String lightTokenRequestSecret;

    private String lightTokenRequestAlgorithm;

    private String lightTokenResponseNodeId;

    private String lightTokenResponseSecret;

    private String lightTokenResponseAlgorithm;

    SpecificConnectorCommunicationServiceExtensionImpl(final String lightTokenRequestNodeId, final String lightTokenRequestSecret, final String lightTokenRequestAlgorithm,
                                                       final String lightTokenResponseNodeId, final String lightTokenResponseSecret, final String lightTokenResponseAlgorithm) {

        this.lightTokenRequestNodeId = lightTokenRequestNodeId;
        this.lightTokenRequestSecret = lightTokenRequestSecret;
        this.lightTokenRequestAlgorithm = lightTokenRequestAlgorithm;
        this.lightTokenResponseNodeId = lightTokenResponseNodeId;
        this.lightTokenResponseSecret = lightTokenResponseSecret;
        this.lightTokenResponseAlgorithm = lightTokenResponseAlgorithm;
    }

    @Override
    public void putRequest(final String tokenBase64, final ILightRequest iLightRequest) throws SpecificCommunicationException {
        final String binaryLightTokenId = BinaryLightTokenHelper.getBinaryLightTokenId(tokenBase64,
                getLightTokenRequestSecret(), getLightTokenRequestAlgorithm());
        final CommunicationCache specificNodeConnectorRequestCommunicationCache = getRequestCommunicationCache();
        specificNodeConnectorRequestCommunicationCache.put(binaryLightTokenId, codec.marshall(iLightRequest));
    }

    @Override
    public void putResponse(final String tokenBase64, final ILightResponse iLightResponse) throws SpecificCommunicationException {
        final String binaryLightTokenId = BinaryLightTokenHelper.getBinaryLightTokenId(tokenBase64,
                getLightTokenResponseSecret(), getLightTokenResponseAlgorithm());
        final CommunicationCache specificNodeConnectorResponseCommunicationCache = getResponseCommunicationCache();
        specificNodeConnectorResponseCommunicationCache.put(binaryLightTokenId, codec.marshall(iLightResponse));
    }

    private CommunicationCache getRequestCommunicationCache() {
        return (CommunicationCache) SpecificCommunicationApplicationContextProvider
                .getApplicationContext()
                .getBean(SpecificCommunicationDefinitionBeanNames.SPECIFIC_NODE_CONNECTOR_CACHE.toString());
    }

    private CommunicationCache getResponseCommunicationCache() {
        return (CommunicationCache) SpecificCommunicationApplicationContextProvider
                .getApplicationContext()
                .getBean(SpecificCommunicationDefinitionBeanNames.NODE_SPECIFIC_CONNECTOR_CACHE.toString());
    }

    public String getLightTokenRequestNodeId() { return lightTokenRequestNodeId; }

    private String getLightTokenRequestSecret() {
        return lightTokenRequestSecret;
    }

    private String getLightTokenRequestAlgorithm() {
        return lightTokenRequestAlgorithm;
    }

    public String getLightTokenResponseNodeId() { return lightTokenResponseNodeId; }

    private String getLightTokenResponseSecret() {
        return lightTokenResponseSecret;
    }

    private String getLightTokenResponseAlgorithm() {
        return lightTokenResponseAlgorithm;
    }
}
