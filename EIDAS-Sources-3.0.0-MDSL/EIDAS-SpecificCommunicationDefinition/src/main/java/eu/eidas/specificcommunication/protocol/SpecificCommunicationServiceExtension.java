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
package eu.eidas.specificcommunication.protocol;

import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;

/**
 * Interface with other methods not available in {@link SpecificCommunicationService}
 * for the specific communication service
 * between specific modules and node modules.
 *
 * @since 2.0
 */
public interface SpecificCommunicationServiceExtension{

    /**
     * Puts the value {@link ILightRequest} in the request communication cache
     * with the key given by an instance of {@link BinaryLightToken} as one of the parameters.
     *
     * @param tokenBase64 a {@link BinaryLightToken} that holds the id/key to the {@link ILightRequest} in the cache
     * @param iLightRequest the request to be put in the cache
     * @throws SpecificCommunicationException if the {@link ILightRequest} could not be put in the cache.
     */
    void putRequest(final String tokenBase64, final ILightRequest iLightRequest) throws SpecificCommunicationException;

    /**
     * Puts the value {@link ILightResponse} in the request communication cache
     * with the key given by an instance of {@link BinaryLightToken} as one of the parameters.
     *
     * @param tokenBase64 a {@link BinaryLightToken} that holds the id/key to the {@link ILightResponse} in the cache
     * @param iLightResponse the response to be put in the cache
     * @throws SpecificCommunicationException if the {@link ILightResponse} could not be put in the cache.
     */
    void putResponse(final String tokenBase64, final ILightResponse iLightResponse) throws SpecificCommunicationException;
}
