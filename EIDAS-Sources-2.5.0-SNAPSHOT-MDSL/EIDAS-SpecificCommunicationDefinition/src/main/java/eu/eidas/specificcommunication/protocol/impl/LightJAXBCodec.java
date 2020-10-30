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
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.specificcommunication.LightRequest;
import eu.eidas.specificcommunication.LightResponse;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.util.LightMessagesConverter;
import eu.eidas.specificcommunication.protocol.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import java.io.StringWriter;
import java.util.Collection;

class LightJAXBCodec {
	private static final Logger LOG = LoggerFactory.getLogger(AttributeRegistry.class);
	protected static final Class[] LIGHT_REQUEST_CODEC = {
			LightRequest.class
	};
	protected static final Class[] LIGHT_RESPONSE_CODEC = {
			LightResponse.class
	};

	LightMessagesConverter messagesConverter = new LightMessagesConverter();
	JAXBContext lightRequestJAXBCtx;
	JAXBContext lightResponseJAXBCtx;

	LightJAXBCodec(JAXBContext lightRequestJAXBCtx, JAXBContext lightResponseJAXBCtx) {
		this.lightRequestJAXBCtx = lightRequestJAXBCtx;
		this.lightResponseJAXBCtx = lightResponseJAXBCtx;
	}

	public static LightJAXBCodec buildDefault() {
		JAXBContext lightRequestJAXBContext = getJAXBContext(LIGHT_REQUEST_CODEC);
		JAXBContext lightResponseJAXBContext = getJAXBContext(LIGHT_RESPONSE_CODEC);
		return new LightJAXBCodec(lightRequestJAXBContext, lightResponseJAXBContext);
	}

	private static JAXBContext getJAXBContext(Class[] contextClasses) {
		try {
			return JAXBContext.newInstance(contextClasses);
		} catch (JAXBException e) {
			LOG.error("Unable to instantiate the JAXBContext", e);
		}
		return null;
	}

	public <T> String marshall(T input) throws SpecificCommunicationException {
		if (input == null) {
			return null;
		}
		StringWriter writer = new StringWriter();
		try {
			createMarshaller(input.getClass()).marshal(input, writer);
		} catch (JAXBException e) {
			throw new SpecificCommunicationException(e);
		}
		return writer.toString();
	}

	public String marshall(ILightRequest lightRequest) throws SpecificCommunicationException {
		eu.eidas.specificcommunication.LightRequest xmlLightRequest = messagesConverter.convert(lightRequest);
		return marshall(xmlLightRequest);
	}

	public String marshall(ILightResponse lightResponse) throws SpecificCommunicationException {
		eu.eidas.specificcommunication.LightResponse xmlLightResponse = messagesConverter.convert(lightResponse);
		return marshall(xmlLightResponse);
	}

	public <T extends ILightRequest> T unmarshallRequest(String input, Collection<AttributeDefinition<?>> registry)
			throws SpecificCommunicationException {
		if (input == null) {
			return null;
		}
		if (registry == null) {
			throw new SpecificCommunicationException("missing registry");
		}
		try {
			SAXSource secureSaxSource = SecurityUtils.createSecureSaxSource(input);

			LightRequest rawRequest = (LightRequest) createUnmarshaller(LightRequest.class).unmarshal(secureSaxSource);
			T lightRequest = (T) messagesConverter.convert(rawRequest, registry);

			return lightRequest;
		} catch (JAXBException | SAXNotSupportedException | SAXNotRecognizedException | ParserConfigurationException e) {
			throw new SpecificCommunicationException(e);
		} catch (SAXException e) {
			throw new SpecificCommunicationException(e);
		}
	}

	public <T extends ILightResponse> T unmarshallResponse(String input,
			Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
		if (input == null) {
			return null;
		}
		if (registry == null) {
			throw new SpecificCommunicationException("missing registry");
		}
		try {
			SAXSource secureSaxSource = SecurityUtils.createSecureSaxSource(input);

			LightResponse rawResponse = (LightResponse) createUnmarshaller(LightResponse.class).unmarshal(secureSaxSource);
			T lightResponse = (T) messagesConverter.convert(rawResponse, registry);

			return lightResponse;
		} catch (JAXBException | SAXException |ParserConfigurationException e) {
			throw new SpecificCommunicationException(e);
		}
	}

	private Marshaller createMarshaller(Class srcType) throws JAXBException {
		Marshaller marshaller;
		if (LightRequest.class.isAssignableFrom(srcType)) {
			marshaller = lightRequestJAXBCtx.createMarshaller();
		} else {
			marshaller = lightResponseJAXBCtx.createMarshaller();
		}
		marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); // NOI18N
		marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		return marshaller;
	}

	private Unmarshaller createUnmarshaller(Class targetType) throws JAXBException {
		Unmarshaller unmarshaller;
		if (LightRequest.class.equals(targetType)) {
			unmarshaller = lightRequestJAXBCtx.createUnmarshaller();
		} else {
			unmarshaller = lightResponseJAXBCtx.createUnmarshaller();
		}
		return unmarshaller;
	}

}
