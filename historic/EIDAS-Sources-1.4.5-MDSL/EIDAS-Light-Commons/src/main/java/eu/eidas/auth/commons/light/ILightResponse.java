/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.auth.commons.light;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;

import java.io.Serializable;

/**
 * Interface for the Lightweight Response processed in the specific module.
 * <p>
 * This interface is a Data Transfer Object (DTO) exchanged between the eIDAS protocol and the specific protocol.
 *
 * @since 1.1
 */
public interface ILightResponse extends Serializable{

    /**
     * Returns the response attributes if the response is a success response , or an empty map if the response is a
     * failure response, never {@code null}.
     *
     * @return the response attributes if the response is a success response , or an empty map if the response is a
     * failure response, never {@code null}.
     */
    @Nonnull
    ImmutableAttributeMap getAttributes();

    /**
     * Returns the IP address of the connected end-user.
     *
     * @return the IP address of the connected end-user.
     */
    @Nullable
    String getIPAddress();

    /**
     * Returns the response ID.
     * <p>
     * This is a unique ID which must be used to prevent replay attacks.
     *
     * @return the response ID.
     */
    @Nonnull
    String getId();

    /**
     * Returns the ID of the request corresponding to this response.
     * <p>
     * This is the unique ID of the request which permits to correlate this response to the originating request.
     *
     * @return the ID of the request corresponding to this response.
     */
    @Nonnull
    String getInResponseToId();

    /**
     * Returns the issuer URI of this response.
     *
     * @return the issuer URI of this response.
     */
    @Nonnull
    String getIssuer();

    /**
     * Returns the level of assurance (LoA) in the authentication performed for this response, or {@code null} if the
     * response is a failure response.
     *
     * @return the level of assurance (LoA) in the authentication performed for this response, or {@code null} if the
     * response is a failure response.
     */
    @Nullable
    String getLevelOfAssurance();

    /**
     * The status of the response.
     * <p>
     * This allows to determine whether this response is a success or a failure response and optionally to have more
     * detailed status messages.
     *
     * @return the response status.
     */
    @Nonnull
    IResponseStatus getStatus();
}
