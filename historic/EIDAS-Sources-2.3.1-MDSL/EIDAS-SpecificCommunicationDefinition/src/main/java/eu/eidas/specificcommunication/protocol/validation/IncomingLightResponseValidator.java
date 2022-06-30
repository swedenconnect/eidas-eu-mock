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
 * Validates the Light Response incoming from Specific modules.
 *
 * @since 2.3
 */
public class IncomingLightResponseValidator {

    /**
     * Maximum number of characters of a Light Response
     */
    private final int lightResponseMaxCharNumber;

    IncomingLightResponseValidator(final String lightResponseMaxNumberChar) {
        this.lightResponseMaxCharNumber = Integer.parseInt(lightResponseMaxNumberChar);
    }

    /**
     * Checks if the token received as parameter is valid.
     *
     * @param lightResponse the light response string
     * @return true if is an invalid light response.
     */
    public boolean isInvalid(String lightResponse) {
        return isNullLightResponse(lightResponse) || isLightResponseMaxSizeInvalid(lightResponse);
    }

    /**
     * Checks if the maximum number of character for light response
     *
     * @param lightResponse the light response
     * @return true if the number of character is
     * equal or less than {@link IncomingLightResponseValidator#lightResponseMaxCharNumber}
     */
    private boolean isLightResponseMaxSizeInvalid(@Nonnull final String lightResponse) {
        return lightResponse.length() > lightResponseMaxCharNumber;
    }

    /**
     * Checks if light response is null
     *
     * @param lightResponse the light response
     * @return true lightResponse param is null
     *
     */
    private boolean isNullLightResponse(final String lightResponse) {
        return null == lightResponse;
    }
}
