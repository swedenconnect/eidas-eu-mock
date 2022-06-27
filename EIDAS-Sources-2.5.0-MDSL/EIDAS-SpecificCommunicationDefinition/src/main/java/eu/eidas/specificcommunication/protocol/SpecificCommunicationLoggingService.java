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
package eu.eidas.specificcommunication.protocol;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;

import java.util.Collection;

/**
 * Interface for logging the specific communication service
 * between specific modules and node modules.
 *
 * @since 2.5
 */
public interface SpecificCommunicationLoggingService {

    /**
     * Gets the {@link ILightRequest} from the request communication cache
     * using as id/key the one obtained from the {@link BinaryLightToken}.
     *
     * @param tokenBase64 the Base64 {@link BinaryLightToken} that holds the id to get {@link ILightRequest}
     * @param registry the collection of attributeDefinitions
     * @return the {@link ILightRequest} corresponding to the id
     * @throws SpecificCommunicationException if the id could not be obtained
     */
    ILightRequest getRequest(String tokenBase64, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException;

    /**
     * returns the marshalled {@link ILightRequest} from the request communication cache
     *
     * @param lightRequestToken in Base64 that converts to the {@link BinaryLightToken} which holds the id to get the {@link ILightRequest}
     * @return marshalled lightRequest as a {@link String} corresponding to the tokenBase64
     * @throws SpecificCommunicationException if the id could not be obtained
     */
    String getRequest(String lightRequestToken) throws SpecificCommunicationException;

    /**
     * Gets the {@link ILightResponse} from the response communication cache
     * using as id/key the one obtained from the {@link BinaryLightToken}.
     *
     * @param tokenBase64 the {@link BinaryLightToken} in Base64 that holds the id to get the {@link ILightResponse}
     * @param registry the collection of attributeDefinitions
     * @return the {@link ILightResponse} corresponding to the id
     * @throws SpecificCommunicationException if the id could not be obtained
     */
    ILightResponse getResponse(String tokenBase64, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException;

    /**
     * Gets the {@link ILightResponse} from the response communication cache
     * using as id/key the one obtained from the {@link BinaryLightToken}.
     *
     * @param tokenBase64 the {@link BinaryLightToken} in Base64 that holds the id to get the {@link ILightResponse}
     * @return the LightResponse as a {@link String}, corresponding to the tokenBase64
     * @throws SpecificCommunicationException if the id could not be obtained
     */
    String getResponse(String tokenBase64) throws SpecificCommunicationException;
}
