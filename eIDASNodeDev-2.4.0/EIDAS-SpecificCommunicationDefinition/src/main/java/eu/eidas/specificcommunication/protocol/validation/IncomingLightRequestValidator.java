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

package eu.eidas.specificcommunication.protocol.validation;

import javax.annotation.Nonnull;

/**
 * Validates the Light Request incoming from Specific modules.
 *
 * @since 2.3
 */
public class IncomingLightRequestValidator {

    /**
     * Maximum number of characters of a light request
     */
    private final int lightRequestMaxCharNumber;

    IncomingLightRequestValidator(final String lightRequestMaxNumberChar) {
        this.lightRequestMaxCharNumber = Integer.parseInt(lightRequestMaxNumberChar);
    }

    /**
     * Checks if the token received as parameter is valid.
     *
     * @param lightRequest the light request string
     * @return true if is an invalid light request.
     */
    public boolean isInvalid(String lightRequest) {
        return isNullLightRequest(lightRequest) || isLightRequestMaxSizeInvalid(lightRequest);
    }

    /**
     * Checks if the maximum number of character for light request
     *
     * @param ilightRequest the light request
     * @return true if the number of character is
     * equal or less than {@link IncomingLightRequestValidator#lightRequestMaxCharNumber}
     */
    private boolean isLightRequestMaxSizeInvalid(@Nonnull final String ilightRequest) {
        return ilightRequest.length() > lightRequestMaxCharNumber;
    }

    /**
     * Checks if light request is null
     *
     * @param ilightRequest the light request
     * @return true ilightRequest param is null
     *
     */
    private boolean isNullLightRequest(final String ilightRequest) {
        return null == ilightRequest;
    }
}
