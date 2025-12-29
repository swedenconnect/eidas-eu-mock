/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.auth.engine.core.validator.eidas;

import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.engine.exceptions.ValidationException;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator class for RequestedAuthContext in SAML authentication requests.
 */
public class EidasRequestedAuthContextValidator implements EidasValidator {
    final String NO_LOA_FOUND = "There are no levels of assurance";
    final String DUPLICATES = "Levels of Assurance Contain Duplicates";
    final String MINIMUM_MORE_THEN_ONE = "Minimum Comparison can only have 1 level of assurance";
    final String MINIMUM_CONTAINS_NON_NOTIFIED = "Minimum Comparison does not support non notified level of assurance";
    final String EXACT_NOTIFIED_MISSING_HIGHER_LEVELS =
            "When a notified level of assurance is used exact matching, all higher notified levels must be present";
    final String EXACT_CONTAINS_ONLY_NOTIFIED = "When only requesting Notified, Comparison has to be MINIMUM";
    final String COMPARISON_NOT_SUPPORTED = "the level of assurance comparison is not supported";

    public void validate(RequestedAuthnContext authnContext) throws ValidationException {
        validateLevelsOfAssurance(authnContext);
    }

    /**
     * Validates Levels of Assurance and LevelOfAssuranceComparison business logic in EidasRequest
     *
     * @param authnContext Context containing levels of assurance
     * @throws ValidationException when levels of assurance are not valid
     */
    public void validateLevelsOfAssurance(RequestedAuthnContext authnContext) throws ValidationException {
        if (null == authnContext) {
            throw new ValidationException("RequestedAuthnContext is null");
        }
        final AuthnContextComparisonTypeEnumeration authnContextComparison = authnContext.getComparison();
        final List<AuthnContextClassRef> authnContextClassRefs = authnContext.getAuthnContextClassRefs();

        final List<String> levelsAsString = getLevelsOfAssurance(authnContextClassRefs);
        final List<NotifiedLevelOfAssurance> levelsAsNotified = getNotifiedLevelsOfAssurance(levelsAsString);

        LevelOfAssuranceComparison comparison = (authnContextComparison != null) ?
                LevelOfAssuranceComparison.fromString(authnContextComparison.toString()) : LevelOfAssuranceComparison.EXACT;

        if (levelsAsString.isEmpty()) {
            throw new ValidationException(NO_LOA_FOUND);
        }
        if (new HashSet<>(levelsAsString).size() != levelsAsString.size()) {
            throw new ValidationException(DUPLICATES);
        }
        if (comparison == null) {
            throw new ValidationException(COMPARISON_NOT_SUPPORTED);
        }

        if (LevelOfAssuranceComparison.MINIMUM.equals(comparison)) {
            if (levelsAsString.size() > 1) {
                throw new ValidationException(MINIMUM_MORE_THEN_ONE);
            }
            if (levelsAsString.size() > levelsAsNotified.size()) {
                throw new ValidationException(MINIMUM_CONTAINS_NON_NOTIFIED);
            }
        }

        if (LevelOfAssuranceComparison.EXACT.equals(comparison)) {
            if (!levelsAsNotified.containsAll(higherNotifiedLevels(levelsAsNotified))) {
                throw new ValidationException(EXACT_NOTIFIED_MISSING_HIGHER_LEVELS);
            }
            if (levelsAsNotified.size() == levelsAsString.size()) {
                throw new ValidationException(EXACT_CONTAINS_ONLY_NOTIFIED);
            }
        }
    }


    private List<String> getLevelsOfAssurance(List<AuthnContextClassRef> authnContextClassRefs) {
        return authnContextClassRefs.stream().map(AuthnContextClassRef::getURI).collect(Collectors.toList());
    }

    private List<NotifiedLevelOfAssurance> getNotifiedLevelsOfAssurance(List<String> levelsAsString) {
        return levelsAsString.stream()
                .map(str -> NotifiedLevelOfAssurance.fromString(str))
                .filter(optional -> optional != null).collect(Collectors.toList());
    }

    private Set<NotifiedLevelOfAssurance> higherNotifiedLevels(Collection<NotifiedLevelOfAssurance> levelsAsNotified) {
        return levelsAsNotified.stream()
                .map(NotifiedLevelOfAssurance::getHigherLevelsOfAssurance)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
