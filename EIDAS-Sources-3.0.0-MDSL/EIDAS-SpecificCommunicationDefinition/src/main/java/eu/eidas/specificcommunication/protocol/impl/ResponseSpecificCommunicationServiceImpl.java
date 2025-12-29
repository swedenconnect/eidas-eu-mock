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
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.specificcommunication.CommunicationCache;
import eu.eidas.specificcommunication.SpecificCommunicationApplicationContextProvider;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.validation.IncomingLightResponseValidator;

import java.util.Collection;

public class ResponseSpecificCommunicationServiceImpl extends SpecificCommunicationServiceImpl<ILightResponse> {
    public ResponseSpecificCommunicationServiceImpl(String lightTokenIssuerName, String lightTokenSecret, String lightTokenAlgorithm, CommunicationCache communicationCache) {
        super(lightTokenIssuerName, lightTokenSecret, lightTokenAlgorithm, communicationCache);
    }

    @Override
    protected String codecMarshall(ILightResponse iLightMessage) throws SpecificCommunicationException {
        return codec.marshall(iLightMessage);
    }

    @Override
    protected ILightResponse codecUnmarshall(String lightMessage, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
        validateIncomingString(lightMessage);
        final ILightResponse response = codec.unmarshallResponse(lightMessage, registry);
        validateLightResponse(response);
        return response;
    }

    @Override
    void validateIncomingString(String lightResponse) throws SpecificCommunicationException {
        validateIncomingLightResponse(lightResponse);
    }

    private void validateIncomingLightResponse(final String lightResponse) throws SpecificCommunicationException {
        if (incomingLightResponseValidator.isInvalid(lightResponse)) {
            throw new SpecificCommunicationException("Incoming light response is invalid.");
        }
    }

    private void validateLightResponse(ILightResponse lightResponse) throws SpecificCommunicationException {
        incomingLightResponseValidator.validate(lightResponse);
    }

    private IncomingLightResponseValidator incomingLightResponseValidator =
            (IncomingLightResponseValidator) SpecificCommunicationApplicationContextProvider
                    .getApplicationContext()
                    .getBean(SpecificCommunicationDefinitionBeanNames.INCOMING_LIGHT_RESPONSE_VALIDATOR.toString());
}
