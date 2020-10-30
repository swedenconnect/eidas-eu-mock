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

package eu.eidas.specificcommunication.protocol.util;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.specificcommunication.LightRequest;
import eu.eidas.specificcommunication.LightResponse;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

//import eu.eidas.specificcommunication.CountryCode;

/**
 * Class to convert LightRequest and LightResponse message to XML JavaObjects.
 */
public class LightMessagesConverter {

    private static final Logger LOG = LoggerFactory.getLogger(LightMessagesConverter.class);

    public LightRequest convert(ILightRequest iLightRequest) throws SpecificCommunicationException {
        if (iLightRequest == null) {
            return null;
        }
        LightRequest xmlLightRequest = new LightRequest();

        try {
            String citizenCountryCode = getCountryCode(iLightRequest.getCitizenCountryCode());
            xmlLightRequest.setCitizenCountryCode(citizenCountryCode);
            xmlLightRequest.setId(iLightRequest.getId());
            xmlLightRequest.setIssuer(iLightRequest.getIssuer());
            xmlLightRequest.getLevelOfAssurance().addAll(convertToRequestLoAs(iLightRequest.getLevelsOfAssurance()));
            xmlLightRequest.setNameIdFormat(iLightRequest.getNameIdFormat());
            xmlLightRequest.setProviderName(iLightRequest.getProviderName());
            xmlLightRequest.setSpType(iLightRequest.getSpType());
            final String spCountryCode = getCountryCode(iLightRequest.getSpCountryCode());
            xmlLightRequest.setSpCountryCode(spCountryCode);
            xmlLightRequest.setRequesterId(iLightRequest.getRequesterId());
            xmlLightRequest.setRelayState(iLightRequest.getRelayState());

            LightRequest.RequestedAttributes requestedAttributes =
                    convertRequestAttributes(iLightRequest.getRequestedAttributes());
            xmlLightRequest.setRequestedAttributes(requestedAttributes);

        } catch (AttributeValueMarshallingException e) {
            throw new SpecificCommunicationException("LightRequest conversion to xml failed", e);
        }
        return xmlLightRequest;

    }

    public ILightRequest convert(LightRequest xmlLightRequest, Collection<AttributeDefinition<?>> registry)
            throws SpecificCommunicationException {
        if (xmlLightRequest == null) {
            return null;
        }
        eu.eidas.auth.commons.light.impl.LightRequest.Builder lightRequestBuilder =
                eu.eidas.auth.commons.light.impl.LightRequest.builder();

        if (xmlLightRequest.getCitizenCountryCode() != null) {
            lightRequestBuilder.citizenCountryCode(xmlLightRequest.getCitizenCountryCode());
        }
        lightRequestBuilder.id(xmlLightRequest.getId());
        lightRequestBuilder.issuer(xmlLightRequest.getIssuer());
        lightRequestBuilder.levelsOfAssuranceValues(convertToStringListValues(xmlLightRequest.getLevelOfAssurance()));
        lightRequestBuilder.nameIdFormat(xmlLightRequest.getNameIdFormat());
        lightRequestBuilder.providerName(xmlLightRequest.getProviderName());
        lightRequestBuilder.spType(xmlLightRequest.getSpType());
        if (xmlLightRequest.getSpCountryCode() != null) {
            lightRequestBuilder.spCountryCode(xmlLightRequest.getSpCountryCode());
        }
        lightRequestBuilder.requesterId(xmlLightRequest.getRequesterId());
        lightRequestBuilder.relayState(xmlLightRequest.getRelayState());

        ImmutableAttributeMap attributeMap = convert(xmlLightRequest.getRequestedAttributes(), registry);
        lightRequestBuilder.requestedAttributes(attributeMap);

        return lightRequestBuilder.build();
    }

    public LightResponse convert(ILightResponse iLightResponse) throws SpecificCommunicationException {
        if (iLightResponse == null) {
            return null;
        }
        LightResponse xmlLightResponse = new LightResponse();

        try {
            xmlLightResponse.setId(iLightResponse.getId());
            xmlLightResponse.setInResponseToId(iLightResponse.getInResponseToId());
            xmlLightResponse.setConsent(iLightResponse.getConsent());
            xmlLightResponse.setIssuer(iLightResponse.getIssuer());
            xmlLightResponse.setIpAddress(iLightResponse.getIPAddress());
            xmlLightResponse.setRelayState(iLightResponse.getRelayState());
            xmlLightResponse.setSubject(iLightResponse.getSubject());
            xmlLightResponse.setSubjectNameIdFormat(iLightResponse.getSubjectNameIdFormat());
            xmlLightResponse.setLevelOfAssurance(iLightResponse.getLevelOfAssurance());
            xmlLightResponse.setStatus(convert(iLightResponse.getStatus()));

            LightResponse.Attributes responseAttributes = convertResponseAttributes(iLightResponse.getAttributes());
            xmlLightResponse.setAttributes(responseAttributes);
        } catch (AttributeValueMarshallingException e) {
            throw new SpecificCommunicationException("LightRequest conversion to xml failed", e);
        }
        return xmlLightResponse;

    }

    public ILightResponse convert(LightResponse xmlLightResponse, Collection<AttributeDefinition<?>> registry)
            throws SpecificCommunicationException {
        if (xmlLightResponse == null) {
            return null;
        }
        eu.eidas.auth.commons.light.impl.LightResponse.Builder lightResponseBuilder =
                eu.eidas.auth.commons.light.impl.LightResponse.builder();

        lightResponseBuilder.id(xmlLightResponse.getId());
        lightResponseBuilder.inResponseToId(xmlLightResponse.getInResponseToId());
        lightResponseBuilder.consent(xmlLightResponse.getConsent());
        lightResponseBuilder.issuer(xmlLightResponse.getIssuer());
        lightResponseBuilder.ipAddress(xmlLightResponse.getIpAddress());
        lightResponseBuilder.relayState(xmlLightResponse.getRelayState());
        lightResponseBuilder.subject(xmlLightResponse.getSubject());
        lightResponseBuilder.subjectNameIdFormat(xmlLightResponse.getSubjectNameIdFormat());
        lightResponseBuilder.levelOfAssurance(xmlLightResponse.getLevelOfAssurance());
        lightResponseBuilder.status(convert(xmlLightResponse.getStatus()));

        ImmutableAttributeMap attributeMap = convert(xmlLightResponse.getAttributes(), registry);
        lightResponseBuilder.attributes(attributeMap);

        return lightResponseBuilder.build();
    }

    private List<String> convertToStringListValues(List<LightRequest.LevelOfAssurance> levelsOfAssurance) {
        List<String> levelsOfAssuranceAsString = null;
        if (levelsOfAssurance != null) {
            levelsOfAssuranceAsString = levelsOfAssurance.stream()
                    .map(LightRequest.LevelOfAssurance::getValue)
                    .collect(Collectors.toList());
        }
        return levelsOfAssuranceAsString;
    }

    private List<LightRequest.LevelOfAssurance> convertToRequestLoAs(List<ILevelOfAssurance> levelsOfAssurance) {
        List<LightRequest.LevelOfAssurance> levelsOfAssuranceAsString = new ArrayList<>();
        if (levelsOfAssurance != null) {
            levelsOfAssuranceAsString = levelsOfAssurance.stream()
                    .map(this::convert)
                    .collect(Collectors.toList());
        }
        return levelsOfAssuranceAsString;
    }

    private LightRequest.LevelOfAssurance convert(ILevelOfAssurance levelOfAssurance) {
        LightRequest.LevelOfAssurance requestLevelOfAssurance = new LightRequest.LevelOfAssurance();
        requestLevelOfAssurance.setType(levelOfAssurance.getType());
        requestLevelOfAssurance.setValue(levelOfAssurance.getValue());
        return requestLevelOfAssurance;
    }

    private LightRequest.RequestedAttributes convertRequestAttributes(ImmutableAttributeMap attributes)
            throws AttributeValueMarshallingException {
        LightRequest.RequestedAttributes requestedAttributes = new LightRequest.RequestedAttributes();

        for (ImmutableAttributeMap.ImmutableAttributeEntry attributeEntry : attributes.entrySet()) {
            LightRequest.RequestedAttributes.Attribute requestedAttribute =
                    createRequestAttribute(attributeEntry.getKey(), attributeEntry.getValues());
            requestedAttributes.getAttribute().add(requestedAttribute);
        }

        return requestedAttributes;
    }

    private LightResponse.Attributes convertResponseAttributes(ImmutableAttributeMap attributes)
            throws AttributeValueMarshallingException {
        LightResponse.Attributes responseAttributes = new LightResponse.Attributes();

        for (ImmutableAttributeMap.ImmutableAttributeEntry attributeEntry : attributes.entrySet()) {
            LightResponse.Attributes.Attribute responseAttribute =
                    createResponseAttribute(attributeEntry.getKey(), attributeEntry.getValues());
            responseAttributes.getAttribute().add(responseAttribute);
        }

        return responseAttributes;
    }

    private ImmutableAttributeMap convert(LightRequest.RequestedAttributes attributes,
                                          Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();

        if (attributes != null && !attributes.getAttribute().isEmpty()) {
            for (LightRequest.RequestedAttributes.Attribute attribute : attributes.getAttribute()) {
                try {
                    URI nameUri = new URI(attribute.getDefinition());
                    AttributeDefinition<?> definition = getByName(nameUri, registry);

                    Iterable values = unmarshalValues(attribute.getValue(), definition);
                    mapBuilder.put(definition, values);
                } catch (Exception e) {
                    LOG.error("\n>>>>>>>>>> UNMARSHALLED null attributes set >>>>>>>>>> \n" + attribute);
                    throw new SpecificCommunicationException("Missing registry", e);
                }
            }
        }

        return mapBuilder.build();
    }

    private ImmutableAttributeMap convert(LightResponse.Attributes attributes,
                                          Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();

        if (attributes != null && !attributes.getAttribute().isEmpty()) {
            for (LightResponse.Attributes.Attribute attribute : attributes.getAttribute()) {
                try {
                    URI nameUri = new URI(attribute.getDefinition());
                    AttributeDefinition<?> definition = getByName(nameUri, registry);

                    Iterable values = unmarshalValues(attribute.getValue(), definition);
                    mapBuilder.put(definition, values);
                } catch (Exception e) {
                    LOG.error("\n>>>>>>>>>> UNMARSHALLED null attributes set >>>>>>>>>> \n" + attribute);
                    throw new SpecificCommunicationException("Missing registry");
                }
            }
        }

        return mapBuilder.build();
    }

    private ResponseStatus convert(LightResponse.Status responseStatus) {
        if (responseStatus == null) {
            return null;
        }
        ResponseStatus.Builder responseStatusBuilder = ResponseStatus.builder();

        responseStatusBuilder.failure(responseStatus.isFailure());
        responseStatusBuilder.statusCode(responseStatus.getStatusCode());
        responseStatusBuilder.subStatusCode(responseStatus.getSubStatusCode());
        responseStatusBuilder.statusMessage(responseStatus.getStatusMessage());

        return responseStatusBuilder.build();
    }

    private LightResponse.Status convert(IResponseStatus responseStatus) {
        if (responseStatus == null) {
            return null;
        }
        LightResponse.Status lightResponseStatus = new LightResponse.Status();

        lightResponseStatus.setFailure(responseStatus.isFailure());
        lightResponseStatus.setStatusCode(responseStatus.getStatusCode());
        lightResponseStatus.setSubStatusCode(responseStatus.getSubStatusCode());
        lightResponseStatus.setStatusMessage(responseStatus.getStatusMessage());

        return lightResponseStatus;
    }

    private String getCountryCode(String countryCodeValue) {
        String countryCode = null;
        try {
            if (countryCodeValue != null) {
                countryCode = countryCodeValue;
            }
        } catch (Exception e) {
            LOG.error("Country code " + countryCodeValue + " doesn't exist");
        }
        return countryCode;
    }

    private AttributeDefinition<? extends Object> getByName(URI nameUri, Collection<AttributeDefinition<?>> registry)
            throws SpecificCommunicationException {
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

    private Iterable unmarshalValues(List<String> values, AttributeDefinition<?> definition)
            throws AttributeValueMarshallingException {
        if (values == null) {
            return null;
        }
        ImmutableSet.Builder<AttributeValue<?>> valuesBuilder = ImmutableSet.builder();

        for (Object value : values) {
            AttributeValueMarshaller<?> valueMarshaller = definition.getAttributeValueMarshaller();
            boolean nonLatin = definition.isTransliterationMandatory();
            valuesBuilder.add(valueMarshaller.unmarshal(value.toString(), nonLatin));
        }
        return valuesBuilder.build();
    }

    private LightRequest.RequestedAttributes.Attribute createRequestAttribute(AttributeDefinition attributeDefinition,
                                                                              ImmutableSet<AttributeValue> attributeValues) throws AttributeValueMarshallingException {
        LightRequest.RequestedAttributes.Attribute attribute = new LightRequest.RequestedAttributes.Attribute();
        attribute.setDefinition(attributeDefinition.getNameUri().toASCIIString());
        if (attributeValues != null) {
            for (AttributeValue attributeValue: attributeValues) {
                String value = attributeDefinition.getAttributeValueMarshaller().marshal(attributeValue);
                attribute.getValue().add(value);
            }
        }
        return attribute;
    }

    private LightResponse.Attributes.Attribute createResponseAttribute(AttributeDefinition attributeDefinition,
                                                                       ImmutableSet<AttributeValue> attributeValues) throws AttributeValueMarshallingException {
        LightResponse.Attributes.Attribute attribute = new LightResponse.Attributes.Attribute();
        attribute.setDefinition(attributeDefinition.getNameUri().toASCIIString());
        if (attributeValues != null) {
            for (AttributeValue attributeValue: attributeValues) {
                String value = attributeDefinition.getAttributeValueMarshaller().marshal(attributeValue);
                attribute.getValue().add(value);
            }
        }
        return attribute;
    }
}
