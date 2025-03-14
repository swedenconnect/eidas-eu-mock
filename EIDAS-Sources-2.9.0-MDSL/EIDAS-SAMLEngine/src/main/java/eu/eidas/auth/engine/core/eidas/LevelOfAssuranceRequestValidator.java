/*
 * Copyright (c) 2024 by European Commission
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

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.LevelOfAssuranceType;
import eu.eidas.auth.commons.light.impl.LevelOfAssuranceUtils;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validator class for levels of assurance in authentication requests.
 */
public class LevelOfAssuranceRequestValidator {

    /**
     * Checks whether the levels of assurance contained in the authentication request are valid
     *
     * @param iAuthenticationRequest the authentication request
     * @throws EIDASSAMLEngineException when request validation fails
     */
    public static void validate(IAuthenticationRequest iAuthenticationRequest) throws EIDASSAMLEngineException {
        validateLevelOfAssurancePrefix(iAuthenticationRequest);
        validateLevelOfAssurance(iAuthenticationRequest);
    }

    private static void validateLevelOfAssurancePrefix(IAuthenticationRequest iAuthenticationRequest) throws EIDASSAMLEngineException {
        for (ILevelOfAssurance iLevelOfAssurance : iAuthenticationRequest.getLevelsOfAssurance()) {
            if (isInvalidEidasLevelOfAssurancePrefix(iLevelOfAssurance.getValue())) {
                throwLoaValidationException(iAuthenticationRequest, iLevelOfAssurance);
            }
        }
    }

    private static void validateLevelOfAssurance(@Nonnull IAuthenticationRequest iAuthenticationRequest) throws EIDASSAMLEngineException {
        List<ILevelOfAssurance> loaList = iAuthenticationRequest.getLevelsOfAssurance();

        if (isOnlyNotified(loaList)) {
            validateOnlyNotified(iAuthenticationRequest);
        } else if (isOnlyNonNotified(loaList)) {
            validateOnlyNonNotified(iAuthenticationRequest);
        } else if (isNotifiedAndNonNotified(loaList)) {
            validateNotifiedAndNonNotified(iAuthenticationRequest);
        } else {
            validateOtherFailedCases(iAuthenticationRequest);
            throwLoaValidationException(iAuthenticationRequest);
        }
    }

    private static void validateOtherFailedCases(IAuthenticationRequest iAuthenticationRequest) throws EIDASSAMLEngineException {
        if (iAuthenticationRequest.getLevelsOfAssurance().isEmpty()) {
            throwLoaValidationException("Loa list can not be empty", iAuthenticationRequest);
        }
        if (getNotifiedLevelsOfAssurance(iAuthenticationRequest).size() > 1 && LevelOfAssuranceComparison.MINIMUM.equals(((IEidasAuthenticationRequest) iAuthenticationRequest).getLevelOfAssuranceComparison())) {
            throwLoaValidationException("Too many notified levels of assurance for comparison type", iAuthenticationRequest);
        }
        if (getNotifiedLevelsOfAssurance(iAuthenticationRequest).size() > 1 && LevelOfAssuranceComparison.EXACT.equals(((IEidasAuthenticationRequest) iAuthenticationRequest).getLevelOfAssuranceComparison())) {
            throwLoaValidationException("Comparison exact can only be used if non notified levels of assurance are present", iAuthenticationRequest);
        }
    }

    private static void validateOnlyNotified(@Nonnull IAuthenticationRequest iAuthenticationRequest) throws EIDASSAMLEngineException {
        validateLoaComparison(iAuthenticationRequest, LevelOfAssuranceComparison.MINIMUM);
    }

    private static void validateOnlyNonNotified(@Nonnull IAuthenticationRequest iAuthenticationRequest) throws EIDASSAMLEngineException {
        validateLoaComparison(iAuthenticationRequest, LevelOfAssuranceComparison.EXACT);
    }

    private static void validateNotifiedAndNonNotified(@Nonnull IAuthenticationRequest iAuthenticationRequest) throws EIDASSAMLEngineException {
        validateNotifiedAndNonNotifiedLoa(iAuthenticationRequest);
        validateLoaComparison(iAuthenticationRequest, LevelOfAssuranceComparison.EXACT);
    }

    private static void validateLoaComparison(@Nonnull IAuthenticationRequest iAuthenticationRequest, @Nonnull LevelOfAssuranceComparison loaComparison) throws EIDASSAMLEngineException {
        if (iAuthenticationRequest instanceof IEidasAuthenticationRequest) {
            IEidasAuthenticationRequest eidasAuthenticationRequest = (IEidasAuthenticationRequest) iAuthenticationRequest;
            if (!eidasAuthenticationRequest.getLevelOfAssuranceComparison().equals(loaComparison)) {
                throwLoaValidationException(iAuthenticationRequest, eidasAuthenticationRequest.getLevelOfAssuranceComparison());
            }
        } else {
            throwLoaValidationException(iAuthenticationRequest);
        }
    }

    private static void validateNotifiedAndNonNotifiedLoa(@Nonnull IAuthenticationRequest iAuthenticationRequest) throws EIDASSAMLEngineException {
        List<ILevelOfAssurance> loaList = iAuthenticationRequest.getLevelsOfAssurance();
        final List<NotifiedLevelOfAssurance> levelsAsNotified = getNotifiedLevelsOfAssurance(iAuthenticationRequest);

        if (loaList.stream().filter(LevelOfAssuranceUtils::isNotified).count() > 3) {
            throwLoaValidationException(iAuthenticationRequest);
        }

        List<NotifiedLevelOfAssurance> requiredNotifiedLoas = getRequiredNotifiedLevelsOfAssurance(levelsAsNotified);

        if (!levelsAsNotified.containsAll(requiredNotifiedLoas)) {
            throwLoaValidationException(iAuthenticationRequest);
        }
    }

    private static List<NotifiedLevelOfAssurance> getNotifiedLevelsOfAssurance(IAuthenticationRequest iAuthenticationRequest) {
        return iAuthenticationRequest.getLevelsOfAssurance().stream()
                .map(ILevelOfAssurance::getValue)
                .map(str -> NotifiedLevelOfAssurance.fromString(str))
                .filter(optional -> optional != null).collect(Collectors.toList());
    }

    private static List<NotifiedLevelOfAssurance> getRequiredNotifiedLevelsOfAssurance(List<NotifiedLevelOfAssurance> levelsAsNotified) {
        List<NotifiedLevelOfAssurance> requiredNotifiedLoas = new ArrayList<>();
        requiredNotifiedLoas.add(NotifiedLevelOfAssurance.HIGH);
        if (levelsAsNotified.size() >= 2) {
            requiredNotifiedLoas.add(NotifiedLevelOfAssurance.SUBSTANTIAL);
        }
        if (levelsAsNotified.size() >= 3) {
            requiredNotifiedLoas.add(NotifiedLevelOfAssurance.LOW);
        }

        return requiredNotifiedLoas;
    }

    private static void throwLoaValidationException(String message, @Nonnull IAuthenticationRequest iAuthenticationRequest) throws EIDASSAMLEngineException {
        String errorDetail =
                message + ": \"" + iAuthenticationRequest.getLevelsOfAssurance() + "\" in request: " + iAuthenticationRequest;
        throw new EIDASSAMLEngineException(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA, errorDetail);
    }

    private static void throwLoaValidationException(@Nonnull IAuthenticationRequest iAuthenticationRequest) throws EIDASSAMLEngineException {
        String errorDetail =
                "Invalid level of assurance: \"" + iAuthenticationRequest.getLevelsOfAssurance() + "\" in request: " + iAuthenticationRequest;
        throw new EIDASSAMLEngineException(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA, errorDetail);
    }

    private static void throwLoaValidationException(@Nonnull IAuthenticationRequest iAuthenticationRequest, ILevelOfAssurance iLevelOfAssurance) throws EIDASSAMLEngineException {
        String errorDetail =
                "Invalid level of assurance: \"" + iLevelOfAssurance + "\" in request: " + iAuthenticationRequest;
        throw new EIDASSAMLEngineException(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA, errorDetail);
    }

    private static void throwLoaValidationException(@Nonnull IAuthenticationRequest iAuthenticationRequest, LevelOfAssuranceComparison levelOfAssuranceComparison) throws EIDASSAMLEngineException {
        String errorDetail =
                "Invalid level of assurance comparison: \"" + levelOfAssuranceComparison.toString() +
                        "\" in request: " + iAuthenticationRequest;
        throw new EIDASSAMLEngineException(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA, errorDetail);
    }

    private static boolean isOnlyNotified(final List<ILevelOfAssurance> loaList) {
        return (loaList != null && loaList.size() == 1 && LevelOfAssuranceType.fromLoAValue(loaList.get(0).getValue()).equals(LevelOfAssuranceType.NOTIFIED));
    }

    private static boolean isOnlyNonNotified(final List<ILevelOfAssurance> loaList) {
        return (loaList != null && loaList.size() >= 1 &&
                loaList.stream().noneMatch(LevelOfAssuranceUtils::isNotified) &&
                loaList.stream().anyMatch(LevelOfAssuranceUtils::isNonNotified));
    }

    private static boolean isNotifiedAndNonNotified(final List<ILevelOfAssurance> loaList) {
        return (loaList != null && loaList.size() > 1 &&
                loaList.stream().anyMatch(LevelOfAssuranceUtils::isNotified) &&
                loaList.stream().anyMatch(LevelOfAssuranceUtils::isNonNotified));
    }

    private static boolean isInvalidEidasLevelOfAssurancePrefix(@Nonnull String levelOfAssurance) {
        return levelOfAssurance.trim().toUpperCase().startsWith(ILevelOfAssurance.EIDAS_LOA_PREFIX.toUpperCase()) &&
                NotifiedLevelOfAssurance.fromString(levelOfAssurance) == null;
    }
}
