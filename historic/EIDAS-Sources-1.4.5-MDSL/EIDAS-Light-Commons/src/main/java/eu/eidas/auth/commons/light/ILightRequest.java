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

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;

/**
 * Interface for the Lightweight Request processed in the specific module.
 * <p>
 * This interface is a Data Transfer Object (DTO) exchanged between the eIDAS protocol and the specific protocol.
 *
 * @since 1.1
 */
public interface ILightRequest extends Serializable {

    /**
     * Returns the 2-letter country code as defined in ISO 3166 of the country where the citizen must authenticate.
     *
     * @return returns the 2-letter country code as defined in ISO 3166 of the country where the citizen must
     * authenticate.
     */
    String getCitizenCountryCode();

    /**
     * Returns the Request ID.
     * <p>
     * This is the unique ID of the request which permits to correlate this request to its corresponding response.
     *
     * @return the Request ID.
     */
    @Nonnull
    String getId();

    /**
     * Returns the issuer of this request.
     *
     * @return the issuer of this request.
     */
    String getIssuer();

    /**
     * Returns the requested Level of Assurance (LoA) for the authentication to be performed.
     *
     * @return the requested Level of Assurance (LoA) for the authentication to be performed.
     */
    @Nullable
    String getLevelOfAssurance();

    /**
     * Returns the NameIdFormat (optional).
     * <p>
     * This attribute must be transported from the Connector to the IdP if it has an impact on how the attribute values
     * are returned.
     *
     * @return the name id Format, can be {@code null}.
     */
    @Nullable
    String getNameIdFormat();

    /**
     * Returns the provider name (optional).
     *
     * @return the provider name, can be {@code null}.
     */
    @Nullable
    String getProviderName();

    /**
     * Returns the type of SP (optional).
     *
     * @return type of SP, can be {@code null}.
     */
    @Nullable
    String getSpType();

    /**
     * Returns the requested attributes.
     *
     * @return the requested attributes.
     */
    @Nonnull
    ImmutableAttributeMap getRequestedAttributes();
}
