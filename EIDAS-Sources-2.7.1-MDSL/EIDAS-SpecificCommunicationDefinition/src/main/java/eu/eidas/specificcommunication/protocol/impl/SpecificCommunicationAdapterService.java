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
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationServiceExtension;

import java.util.Collection;

public class SpecificCommunicationAdapterService implements SpecificCommunicationServiceExtension, SpecificCommunicationService, SpecificCommunicationLoggingService {

    final private SpecificCommunicationServiceImpl <ILightRequest> requestSpecificCommunicationService;

    final private SpecificCommunicationServiceImpl <ILightResponse> responseSpecificCommunicationService;

    public SpecificCommunicationAdapterService(SpecificCommunicationServiceImpl <ILightRequest> requestSpecificCommunicationService, SpecificCommunicationServiceImpl <ILightResponse> responseSpecificCommunicationService) {
        this.requestSpecificCommunicationService = requestSpecificCommunicationService;
        this.responseSpecificCommunicationService = responseSpecificCommunicationService;
    }

    @Override
    public ILightRequest getRequest(String tokenBase64, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
        return requestSpecificCommunicationService.get(tokenBase64, registry);
    }

    @Override
    public String getRequest(String lightRequestToken) throws SpecificCommunicationException {
        return requestSpecificCommunicationService.getString(lightRequestToken);
    }

    @Override
    public ILightResponse getResponse(String tokenBase64, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
        return responseSpecificCommunicationService.get(tokenBase64, registry);
    }

    @Override
    public String getResponse(String tokenBase64) throws SpecificCommunicationException {
        return responseSpecificCommunicationService.getString(tokenBase64);
    }

    @Override
    public BinaryLightToken putRequest(ILightRequest iLightRequest) throws SpecificCommunicationException {
        return requestSpecificCommunicationService.put(iLightRequest);
    }

    @Override
    public ILightRequest getAndRemoveRequest(String tokenBase64, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
        return requestSpecificCommunicationService.getAndRemove(tokenBase64, registry);
    }

    @Override
    public BinaryLightToken putResponse(ILightResponse iLightResponse) throws SpecificCommunicationException {
        return responseSpecificCommunicationService.put(iLightResponse);
    }

    @Override
    public ILightResponse getAndRemoveResponse(String tokenBase64, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException {
        return responseSpecificCommunicationService.getAndRemove(tokenBase64, registry);
    }

    @Override
    public void putRequest(String tokenBase64, ILightRequest iLightRequest) throws SpecificCommunicationException {
        requestSpecificCommunicationService.put(tokenBase64, iLightRequest);
    }

    @Override
    public void putResponse(String tokenBase64, ILightResponse iLightResponse) throws SpecificCommunicationException {
        responseSpecificCommunicationService.put(tokenBase64, iLightResponse);
    }
}
