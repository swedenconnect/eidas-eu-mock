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

package eu.eidas.specificcommunication.protocol;

import java.util.Collection;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;

/**
 * Interface for the specific communication service
 * between specific modules and node modules.
 *
 * @since 2.0
 */
public interface SpecificCommunicationService {

    /**
     * Puts the {@link ILightRequest} in the request communication cache.
     * <p/>
     * Creates a {@link BinaryLightToken} which id will be used as key for the {@link ILightRequest}.
     *
     * @param iLightRequest the request to be put in the cache
     * @return the {@link BinaryLightToken} that holds the id/key to the {@link ILightRequest} in the cache
     * @throws SpecificCommunicationException if the {@link BinaryLightToken} could not be created.
     */
    BinaryLightToken putRequest(final ILightRequest iLightRequest) throws SpecificCommunicationException;

    /**
     * Removes the {@link ILightRequest} from the request communication cache
     * using as id/key the one obtained from the {@link BinaryLightToken}.
     *
     * @param tokenBase64 the {@link BinaryLightToken} in Base64 that holds the id to remove the {@link ILightRequest}
     * @return the {@link ILightRequest} corresponding to the id
     * @throws SpecificCommunicationException if the id could not be obtained
     */
    ILightRequest getAndRemoveRequest(String tokenBase64, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException;

    /**
     * Puts {@link ILightResponse} in the response communication cache.
     * <p/>
     * Creates a {@link BinaryLightToken} which id will be used as key for the {@link ILightResponse}.
     *
     * @param iLightResponse the {@link ILightResponse} to put in the cache
     * @return the {@link BinaryLightToken} that holds the id/key to the {@link ILightResponse} in the cache
     * @throws SpecificCommunicationException
     */
    BinaryLightToken putResponse(ILightResponse iLightResponse) throws SpecificCommunicationException;

    /**
     * Removes the {@link ILightResponse} from the response communication cache
     * using as id/key the one obtained from the {@link BinaryLightToken}.
     *
     * @param tokenBase64 the {@link BinaryLightToken} in Base64 that holds the id to remove the {@link ILightResponse}
     * @return the {@link ILightResponse} corresponding to the id
     * @throws SpecificCommunicationException if the id could not be obtained
     */
    ILightResponse getAndRemoveResponse(String tokenBase64, Collection<AttributeDefinition<?>> registry) throws SpecificCommunicationException;
}
