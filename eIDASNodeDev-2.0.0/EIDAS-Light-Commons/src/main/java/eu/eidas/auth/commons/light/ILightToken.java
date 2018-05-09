/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 *  EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the
 * "NOTICE" text file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution.
 * Any derivative works that you distribute must include a readable
 * copy of the "NOTICE" text file.
 */
package eu.eidas.auth.commons.light;

import org.joda.time.DateTime;

import javax.annotation.Nonnull;

/**
 * This ILightToken is the interface representing the key of data exchange between EidasNode Core and MS-Specific parts.
 * It is encapsulating a collection of information provided by the originator party.
 *
 * This is not the class for the HTTP session, check {@link eu.eidas.auth.commons.tx.BinaryLightToken}.
 *
 * @since 2.0.0
 */
public interface ILightToken {

    /**
     * Returns with the LightToken ID.
     * @return LightTokenID
     */
    @Nonnull
    String getId();

    /**
     * Returns with the Issuer (originator).
     * @return LightTokenIssuer
     */
    @Nonnull
    String getIssuer();

    /**
     * Returns with the timestamp when the Token was created.
     * @return LightTokenCreatedOn
     */
    @Nonnull
    DateTime getCreatedOn();

    /**
     * Returns with the formatted, string representation of timestamp when the Token was created.
     * The format follows what {@link eu.eidas.auth.commons.light.impl.AbstractLightToken.LIGHTTOKEN_DATE_FORMAT} dictates.
     * @return LightTokenFormattedCreatedOn
     */
    @Nonnull
    String getFormattedCreatedOn();

    /**
     * Returns with simple string token representation to be used as internal reference, as key of correlation.
     * Format is ISSUER/ID/CREATEDON.
     * @return key format for internal corrlation
     */
    @Nonnull
    String getKey();

}
