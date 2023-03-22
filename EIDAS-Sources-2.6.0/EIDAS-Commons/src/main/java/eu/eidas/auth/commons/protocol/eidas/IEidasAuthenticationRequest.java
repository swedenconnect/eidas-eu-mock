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

package eu.eidas.auth.commons.protocol.eidas;

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Extension of the {@link IAuthenticationRequest} interface which adds support for the eIDAS protocol.
 *
 * @since 1.1
 */
public interface IEidasAuthenticationRequest extends IAuthenticationRequest {

    /**
     * Get the minimum eidas level of Assurance of the EidasRequest.
     * @return the minimum eidas level of Assurance of the EidasRequest.
     */
    @Nullable
    NotifiedLevelOfAssurance getEidasLevelOfAssurance();

    /**
     * Get the list of non notified levels of assurance values associated with the EidasRequest.
     * @return the list of non notified levels of assurance values
     *         if there are no non notified levels of assurance, an empty list or null can be returned
     */
    @Nullable
    List<String> getNonNotifiedLevelsOfAssurance();

    @Nonnull
    LevelOfAssuranceComparison getLevelOfAssuranceComparison();

}
