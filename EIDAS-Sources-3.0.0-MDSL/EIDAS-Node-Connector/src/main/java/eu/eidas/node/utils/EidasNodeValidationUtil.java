/*
 * Copyright (c) 2025 by European Commission
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

package eu.eidas.node.utils;

import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EidasNodeValidationUtil {

    private EidasNodeValidationUtil() {
        // Private default constructor for utility class.
    }

    /**
     * Check if at least one of the requested levels of assurance is valid.
     * Request loAs less than or equal to Response loAs less than or equal to Metadata loAs
     *
     * @param authnRequest the request
     * @param allowedLoAs  the allowed Levels of assurance
     * @return true if at least one of the requested Level of Assurance is valid, false otherwise
     */
    public static boolean isRequestLoAValid(IEidasAuthenticationRequest authnRequest, List<String> allowedLoAs) {
        if (authnRequest == null || authnRequest.getLevelsOfAssurance() == null || allowedLoAs == null) {
            return false;
        }
        final List<String> levelsOfAssurance = authnRequest.getLevelsOfAssurance().stream().map(ILevelOfAssurance::getValue).collect(Collectors.toList());
        return isEqualOrBetterLoAs(levelsOfAssurance, allowedLoAs);
    }

    /**
     * Check if there is at least one equals or better loa in the second parameter compared to the first
     * Reference LoAs greater than or equal to equalsOrBetterLoAs
     *
     * @param loAs               the reference loas
     * @param equalsOrBetterLoAs the collection that contains at least 1 equal or better LoA
     * @return true if at least one of the requested Level of Assurance is valid, false otherwise
     */
    public static boolean isEqualOrBetterLoAs(final List<String> loAs, final List<String> equalsOrBetterLoAs) {
        final Set<String> loAsWithHigherLevels = loAs.stream().map(NotifiedLevelOfAssurance::fromString).filter(Objects::nonNull)
                .flatMap(notified -> notified.getHigherLevelsOfAssurance().stream())
                .map(NotifiedLevelOfAssurance::stringValue).collect(Collectors.toSet());
        loAsWithHigherLevels.addAll(loAs);

        final Set<String> intersection = new HashSet<>(loAsWithHigherLevels);
        intersection.retainAll(equalsOrBetterLoAs);

        return (intersection.size() > 0);
    }

    /**
     * Check if there is at least one common loa between the two lists
     * firstListOfLoAs has overlap with secondListOfLoAs
     *
     * @param firstListOfLoAs  the reference loas
     * @param secondListOfLoAs the collection that contains at least 1 equal or better LoA
     * @return true if they have at least one of the Level of Assurances in common
     */
    public static boolean hasCommonLoa(final List<String> firstListOfLoAs, final List<String> secondListOfLoAs) {
        final Set<String> intersection = new HashSet<>(firstListOfLoAs);
        intersection.retainAll(secondListOfLoAs);
        return (intersection.size() > 0);
    }

}
