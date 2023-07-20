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

package eu.eidas.auth.engine.core.eidas;

import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.commons.lang.StringUtils;

/**
 * @since 2.5
 * Validator class for levels of assurance in authentication responses.
 */
public class LevelOfAssuranceResponseValidator {

    /**
     * Checks whether the level of assurance contained in the authentication response is valid
     *
     * @param authenticationResponse the authentication response
     * @throws EIDASSAMLEngineException when response validation fails
     */
    public static void validate(AuthenticationResponse authenticationResponse) throws EIDASSAMLEngineException {
        final String levelOfAssurance = authenticationResponse.getLevelOfAssurance();
        if (!authenticationResponse.isFailure()) {
            if (isEmptyLevelOfAssurance(levelOfAssurance)) {
                throw new EIDASSAMLEngineException("Level of assurance is empty");
            }
            if (isInvalidEidasLevelOfAssurancePrefix(levelOfAssurance)) {
                throw new EIDASSAMLEngineException("Non notified level of assurance uses invalid eidas prefix");
            }
        }
    }

    private static boolean isEmptyLevelOfAssurance(String levelOfAssurance) {
        return StringUtils.isEmpty(levelOfAssurance);
    }

    private static boolean isInvalidEidasLevelOfAssurancePrefix(String levelOfAssurance) {
        levelOfAssurance = levelOfAssurance.trim();
        return levelOfAssurance.startsWith(ILevelOfAssurance.EIDAS_LOA_PREFIX) &&
                NotifiedLevelOfAssurance.fromString(levelOfAssurance) == null;
    }
}
