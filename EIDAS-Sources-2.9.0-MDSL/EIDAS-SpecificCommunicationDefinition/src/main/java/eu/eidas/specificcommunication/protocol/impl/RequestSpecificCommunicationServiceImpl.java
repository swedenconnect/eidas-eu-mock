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
import eu.eidas.specificcommunication.CommunicationCache;
import eu.eidas.specificcommunication.SpecificCommunicationApplicationContextProvider;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.validation.IncomingLightRequestValidator;
import eu.eidas.specificcommunication.protocol.validation.IncomingLightRequestValidatorLoAComponent;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Collection;

public class RequestSpecificCommunicationServiceImpl extends SpecificCommunicationServiceImpl<ILightRequest> {
    public RequestSpecificCommunicationServiceImpl(String lightTokenIssuerName, String lightTokenSecret, String lightTokenAlgorithm, CommunicationCache communicationCache) {
        super(lightTokenIssuerName, lightTokenSecret, lightTokenAlgorithm, communicationCache);
    }

    @Override
    protected String codecMarshall(ILightRequest iLightMessage) throws SpecificCommunicationException {
        return codec.marshall(iLightMessage);
    }

    @Override
    protected ILightRequest codecUnmarshall(String lightMessage, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
        validateIncomingString(lightMessage);
        return codec.unmarshallRequest(lightMessage, registry);
    }

    protected void validateIncomingString(String lightRequest) throws SpecificCommunicationException {
        validateIncomingLightRequest(lightRequest);
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    private void validateIncomingLightRequestId(String lightRequest) {
        try {
            incomingLightRequestValidator.validateLightRequestIdElement(lightRequest);
        } catch (ParserConfigurationException | SAXException | SpecificCommunicationException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void validateIncomingLightRequest(String lightRequest) throws SpecificCommunicationException {
        if (incomingLightRequestValidator.isInvalid(lightRequest)) {
            throw new SpecificCommunicationException("Incoming light request is invalid.");
        }
        validateIncomingLightRequestId(lightRequest);
    }

    private IncomingLightRequestValidator incomingLightRequestValidator =
            (IncomingLightRequestValidator) SpecificCommunicationApplicationContextProvider
                    .getApplicationContext()
                    .getBean(SpecificCommunicationDefinitionBeanNames.INCOMING_LIGHT_REQUEST_VALIDATOR.toString());
}
