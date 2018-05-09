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

package eu.eidas.specificcommunication.protocol.impl;

import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.SpecificCommunicationApplicationContextProvider;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import eu.eidas.specificcommunication.tx.CommunicationCache;

/**
 * Implements {@link SpecificCommunicationService} to be used for exchanging of
 * {@link ILightRequest} and {@link ILightRequest} between the specific
 * connector and node connector
 *
 * @since 2.0
 */
public class SpecificConnectorCommunicationServiceImpl implements SpecificCommunicationService {

	private static LightJAXBCodec codec;
	static {
		try {
			codec = new LightJAXBCodec(JAXBContext.newInstance(LightRequest.class, LightResponse.class,
					ImmutableAttributeMap.class, AttributeDefinition.class));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	private String lightTokenRequestIssuerName;

	private String lightTokenRequestSecret;

	private String lightTokenRequestAlgorithm;

	private String lightTokenResponseIssuerName;

	private String lightTokenResponseSecret;

	private String lightTokenResponseAlgorithm;

	SpecificConnectorCommunicationServiceImpl(final String lightTokenRequestIssuerName,
			final String lightTokenRequestSecret, final String lightTokenRequestAlgorithm,
			final String lightTokenResponseIssuerName, final String lightTokenResponseSecret,
			final String lightTokenResponseAlgorithm) {

		this.lightTokenRequestIssuerName = lightTokenRequestIssuerName;
		this.lightTokenRequestSecret = lightTokenRequestSecret;
		this.lightTokenRequestAlgorithm = lightTokenRequestAlgorithm;
		this.lightTokenResponseIssuerName = lightTokenResponseIssuerName;
		this.lightTokenResponseSecret = lightTokenResponseSecret;
		this.lightTokenResponseAlgorithm = lightTokenResponseAlgorithm;
	}

	@Override
	public BinaryLightToken putRequest(final ILightRequest iLightRequest) throws SpecificCommunicationException {
		final BinaryLightToken binaryLightToken = BinaryLightTokenHelper.createBinaryLightToken(
				getLightTokenRequestIssuerName(), getLightTokenRequestSecret(), getLightTokenRequestAlgorithm());
		final String tokenId = binaryLightToken.getToken().getId();
		final CommunicationCache specificNodeConnectorRequestProviderMap = getRequestCommunicationCache();
		specificNodeConnectorRequestProviderMap.put(tokenId, codec.marshall(iLightRequest));
		return binaryLightToken;
	}

	@Override
	public ILightRequest getAndRemoveRequest(final String tokenBase64,
			final Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
		final String binaryLightTokenId = BinaryLightTokenHelper.getBinaryLightTokenId(tokenBase64,
				getLightTokenRequestSecret(), getLightTokenRequestAlgorithm());
		final CommunicationCache specificNodeConnectorRequestProviderMap = getRequestCommunicationCache();
		ILightRequest req = codec.unmarshallRequest(specificNodeConnectorRequestProviderMap.remove(binaryLightTokenId),	registry);
		return req;
	}

	private CommunicationCache getRequestCommunicationCache() {
		return (CommunicationCache) SpecificCommunicationApplicationContextProvider
				.getApplicationContext()
				.getBean(SpecificCommunicationDefinitionBeanNames.SPECIFIC_NODE_CONNECTOR_MAP.toString());
	}

	@Override
	public BinaryLightToken putResponse(final ILightResponse iLightResponse) throws SpecificCommunicationException {
		final BinaryLightToken binaryLightToken = BinaryLightTokenHelper.createBinaryLightToken(
				getLightTokenResponseIssuerName(), getLightTokenResponseSecret(), getLightTokenResponseAlgorithm());
		final String tokenId = binaryLightToken.getToken().getId();
		final CommunicationCache nodeSpecificConnectorRequestProviderMap = getResponseCommunicationCache();
		nodeSpecificConnectorRequestProviderMap.put(tokenId, codec.marshall(iLightResponse));
		return binaryLightToken;
	}

	@Override
	public ILightResponse getAndRemoveResponse(final String tokenBase64,
			final Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
		final String binaryLightTokenId = BinaryLightTokenHelper.getBinaryLightTokenId(tokenBase64,
				getLightTokenResponseSecret(), getLightTokenResponseAlgorithm());
		final CommunicationCache nodeSpecificConnectorRequestProviderMap = getResponseCommunicationCache();
		ILightResponse resp = codec.unmarshallResponse(nodeSpecificConnectorRequestProviderMap.remove(binaryLightTokenId), registry);
		return resp;
	}

	private CommunicationCache getResponseCommunicationCache() {
		return (CommunicationCache) SpecificCommunicationApplicationContextProvider
				.getApplicationContext()
				.getBean(SpecificCommunicationDefinitionBeanNames.NODE_SPECIFIC_CONNECTOR_MAP.toString());
	}

	private String getLightTokenRequestIssuerName() {
		return lightTokenRequestIssuerName;
	}

	private String getLightTokenRequestSecret() {
		return lightTokenRequestSecret;
	}

	private String getLightTokenRequestAlgorithm() {
		return lightTokenRequestAlgorithm;
	}

	private String getLightTokenResponseIssuerName() {
		return lightTokenResponseIssuerName;
	}

	private String getLightTokenResponseSecret() {
		return lightTokenResponseSecret;
	}

	private String getLightTokenResponseAlgorithm() {
		return lightTokenResponseAlgorithm;
	}
}
