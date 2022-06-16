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

package eu.eidas.auth.commons.light.impl;

import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.LevelOfAssuranceType;

/**
 * @since 2.5
 * Util class for levels of assurance.
 */
public class LevelOfAssuranceUtils {

    /**
     * Checks whether the levels of assurance is of type notified
     *
     * @param loa the Level Of Assurance
     * @return {@code true} if loa type equals notified loa type
     */
    public static boolean isNotified (LevelOfAssurance loa){
        return LevelOfAssuranceType.NOTIFIED.stringValue().equals(loa.getType());
    }

    /**
     * Checks whether the levels of assurance is of type notified
     *
     * @param loa the Level Of Assurance
     * @return {@code true} if loa type equals notified loa type
     */
    public static boolean isNotified (ILevelOfAssurance loa){
        return LevelOfAssuranceType.NOTIFIED.stringValue().equals(loa.getType());
    }

    /**
     * Checks whether the levels of assurance is of type non-notified
     *
     * @param loa the Level Of Assurance
     * @return {@code true} if loa type equals non-notified loa type
     */
    public static boolean isNonNotified (LevelOfAssurance loa){
        return LevelOfAssuranceType.NON_NOTIFIED.stringValue().equals(loa.getType());
    }

    /**
     * Checks whether the levels of assurance is of type non-notified
     *
     * @param loa the Level Of Assurance
     * @return {@code true} if loa type equals non-notified loa type
     */
    public static boolean isNonNotified (ILevelOfAssurance loa){
        return LevelOfAssuranceType.NON_NOTIFIED.stringValue().equals(loa.getType());
    }
}
