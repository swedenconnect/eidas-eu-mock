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

package eu.eidas.specificcommunication.protocol.validation;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Predicate;

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

    /**
     * Validate the lightResponse object
     *
     * @param lightResponse the lightResponse
     * @throws SpecificCommunicationException if the lightResponse is not valid
     */
    public void validate(ILightResponse lightResponse) throws SpecificCommunicationException {
        if (null == lightResponse) {
            throw new SpecificCommunicationException("LightResponse is null");
        }
        validateResponseStatus(lightResponse.getStatus());
    }

    /**
     * Validate the response status.
     *
     * @param responseStatus the response status
     * @throws SpecificCommunicationException if the response status is not valid.
     */
    private void validateResponseStatus(IResponseStatus responseStatus) throws SpecificCommunicationException {
        if (null == responseStatus) {
            throw new SpecificCommunicationException("ResponseStatus cannot be null");
        }
        validateStatusCodeValue(responseStatus.getStatusCode());
        validateSubStatusCodeValue(responseStatus.getSubStatusCode());
    }

    private void validateStatusCodeValue(String statusCode) throws SpecificCommunicationException {
        if (null != statusCode) {
            Arrays.stream(EIDASStatusCode.values())
                    .map(EIDASStatusCode::getValue)
                    .filter(Predicate.isEqual(statusCode))
                    .findAny()
                    .orElseThrow(() -> new SpecificCommunicationException("StatusCode : " + statusCode + " is invalid"));
        }
    }

    private void validateSubStatusCodeValue(String subStatusCode) throws SpecificCommunicationException {
        if (null != subStatusCode) {
            Arrays.stream(EIDASSubStatusCode.values())
                    .map(EIDASSubStatusCode::getValue)
                    .filter(Predicate.isEqual(subStatusCode))
                    .findAny()
                    .orElseThrow(() -> new SpecificCommunicationException("SubStatusCode : " + subStatusCode + " is invalid"));
        }
    }

    private void validateNameIDFormat(String nameIDFormat) throws SpecificCommunicationException {
        if (null != nameIDFormat) {
            Arrays.stream(SamlNameIdFormat.values())
                    .map(SamlNameIdFormat::getNameIdFormat)
                    .filter(Predicate.isEqual(nameIDFormat))
                    .findAny()
                    .orElseThrow(() -> new SpecificCommunicationException("NameID Format : " + nameIDFormat + " is invalid"));
        }
    }

}
