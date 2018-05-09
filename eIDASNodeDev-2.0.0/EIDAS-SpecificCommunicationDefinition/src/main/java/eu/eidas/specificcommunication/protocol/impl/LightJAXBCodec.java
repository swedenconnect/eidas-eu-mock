package eu.eidas.specificcommunication.protocol.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap.ImmutableAttributeEntry;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;

class LightJAXBCodec {
	private static final Logger LOG = LoggerFactory.getLogger(AttributeRegistry.class);

	JAXBContext jaxbCtx;

	LightJAXBCodec(JAXBContext jaxbCtx) throws JAXBException {
		this.jaxbCtx = jaxbCtx;
	}

	public <T> String marshall(T input) throws SpecificCommunicationException {
		if (input == null) {
			return null;
		}
		StringWriter writer = new StringWriter();
		try {
			createMarshaller().marshal(input, writer);
		} catch (JAXBException e) {
			throw new SpecificCommunicationException(e);
		}
		return writer.toString();
	}

	public <T extends ILightRequest> T unmarshallRequest(String input,
			Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
		if (input == null) {
			return null;
		}
		if (registry == null) {
			throw new SpecificCommunicationException("missing registry");
		}
		try {
			T unmarshalled = (T) createUnmarshaller().unmarshal(new StringReader(input));
			LightRequest.Builder resultBuilder = LightRequest.builder(unmarshalled);
			ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();

			for (ImmutableAttributeEntry<?> entry : unmarshalled.getRequestedAttributes().entrySet()) {
				URI nameUri = entry.getKey().getNameUri();
				AttributeDefinition<?> definition = getByName(nameUri, registry);

				Iterable values = unmarshalValues(entry, definition);
				mapBuilder.put(definition, values);
			}
			T result = (T) resultBuilder.requestedAttributes(mapBuilder.build()).build();
			return result;
		} catch (JAXBException | AttributeValueMarshallingException e) {
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
			T unmarshalled = (T) createUnmarshaller().unmarshal(new StringReader(input));
			LightResponse.Builder resultBuilder = LightResponse.builder(unmarshalled);

			ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();

			if (unmarshalled.getAttributes() == null) {
				LOG.error("\n>>>>>>>>>> UNMARSHALLED null attributes set >>>>>>>>>> \n" + unmarshalled);
				LOG.error("\n>>>>>>>>>> INPUT was >>>>>>>>>> \n" + input);
				throw new SpecificCommunicationException("missing registry");
			}

			for (ImmutableAttributeEntry<?> entry : unmarshalled.getAttributes().entrySet()) {
				AttributeDefinition<?> definition = entry.getKey();
				if (registry != null) {
					URI nameUri = entry.getKey().getNameUri();
					definition = getByName(nameUri, registry);
				}
				Iterable values = unmarshalValues(entry, definition);
				mapBuilder.put(definition, values);
			}
			T result = (T) resultBuilder.attributes(mapBuilder.build()).build();
			return result;
		} catch (JAXBException | AttributeValueMarshallingException e) {
			throw new SpecificCommunicationException(e);
		}
	}

	private Iterable unmarshalValues(ImmutableAttributeEntry<?> entry, AttributeDefinition<?> definition)
			throws AttributeValueMarshallingException {
		ImmutableSet.Builder<AttributeValue<?>> valuesBuilder = ImmutableSet.builder();

		for (Object value : entry.getValues()) {
			AttributeValueMarshaller<?> valueMarshaller = definition.getAttributeValueMarshaller();
			boolean nonLatin = definition.isTransliterationMandatory();
			valuesBuilder.add(valueMarshaller.unmarshal(value.toString(), nonLatin));
		}
		return (Iterable) valuesBuilder.build();
	}

	private AttributeDefinition<? extends Object> getByName(URI nameUri,
			Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
		if (nameUri == null) {
			throw new SpecificCommunicationException("Invalid lookup nameUri");
		}
		for (Iterator<AttributeDefinition<?>> iterator = registry.iterator(); iterator.hasNext();) {
			AttributeDefinition<?> next = iterator.next();
			if (next.getNameUri() == null)
				throw new SpecificCommunicationException(
						String.format("Attribute with null nameUri: %s , present in the registry", next));
			if (next.getNameUri().equals(nameUri)) {
				return next;
			}
		}
		throw new SpecificCommunicationException(String.format("Attribute %s not present in the registry", nameUri));
	}

	private Marshaller createMarshaller() throws JAXBException {
		Marshaller marshaller = jaxbCtx.createMarshaller();
		marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); // NOI18N
		marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		return marshaller;
	}

	private Unmarshaller createUnmarshaller() throws JAXBException {
		Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
		return unmarshaller;
	}

}
