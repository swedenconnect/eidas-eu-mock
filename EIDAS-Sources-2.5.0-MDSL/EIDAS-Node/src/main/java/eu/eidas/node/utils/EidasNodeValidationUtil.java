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

package eu.eidas.node.utils;

import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author vanegdi on 14/08/2015. Remark : moved from eidasUtil
 */
public class EidasNodeValidationUtil {

    private EidasNodeValidationUtil() {
        // Private default constructor for utility class.
    }

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasNodeValidationUtil.class.getName());

    /**
     *  Validates message destination property against URI accessible from configuration. Now it is using parameters form eidas.xml
     *  same, as for metadata, but later this function to be refactored into samlengine, because destination check is a saml2
     *  standard requirement. Also, TODO do not check web binding, check message binding instead.
     *
     * @param authnRequest  the instance of {@link IAuthenticationRequest}
     * @param serviceUtil   the instance of {@link AUSERVICEUtil}
     * @param httpMethod    the HTTP method
     * @param reportedErr   the instance of {@link EidasErrorKey}
     */
    public static void validateServiceDestination(IAuthenticationRequest authnRequest,
                                                  AUSERVICEUtil serviceUtil,
                                                  String httpMethod,
                                                  EidasErrorKey reportedErr) {
        if ("POST".equals(httpMethod) &&
                !serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_POST_URIDEST.toString()).equalsIgnoreCase(authnRequest.getDestination())) {
            LOG.info("Expected auth request destination {} but got {}", serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_POST_URIDEST.toString()), authnRequest.getDestination());
            throw new EidasNodeException(
                    EidasErrors.get(reportedErr.errorCode()),
                    EidasErrors.get(reportedErr.errorMessage()),
                    new InternalErrorEIDASException(
                            EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL.errorCode()),
                            EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL.errorMessage())
                    )
            );
        } else {
            if (!serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_REDIRECT_URIDEST.toString()).equalsIgnoreCase(authnRequest.getDestination())) {
                LOG.info("Expected auth request destination {} but got {}", serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_REDIRECT_URIDEST.toString()), authnRequest.getDestination());
                throw new InternalErrorEIDASException(
                        EidasErrors.get(reportedErr.errorCode()),
                        EidasErrors.get(reportedErr.errorMessage()),
                        new InternalErrorEIDASException(
                                EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL.errorCode()),
                                EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL.errorMessage())
                        )
                );
            }
        }
    }

    /**
     *  Validates assertion consumer URL against Connector metadata.
     *
     * @param authnRequest  the instance of {@link IAuthenticationRequest}
     * @param metadataAssertationConsumerURL    the metadata Assertation Consumer URL
     * @param reportedErr   the instance of {@link EidasErrorKey}
     */
    public static void validateAssertionConsumerURL(IAuthenticationRequest authnRequest,
                                                    String metadataAssertationConsumerURL,
                                                    EidasErrorKey reportedErr) {
        if (!authnRequest.getAssertionConsumerServiceURL().equals(metadataAssertationConsumerURL)) {
            LOG.info("Expected auth request assertion consumer url {} but got {}", metadataAssertationConsumerURL, authnRequest.getAssertionConsumerServiceURL());
            throw new EidasNodeException(
                    EidasErrors.get(reportedErr.errorCode()),
                    EidasErrors.get(reportedErr.errorMessage()),
                    new InternalErrorEIDASException(
                            EidasErrors.get(reportedErr.errorCode()),
                            EidasErrors.get(reportedErr.errorMessage())
                    )
            );
        }
    }

    /**
     * validates the current binding with that configured in the SAMLRequest
     *
     * @param authRequest   the instance of {@link IAuthenticationRequest}
     * @param method        the HTTP method
     * @param reportedErr   the instance of {@link EidasErrorKey}
     */
    public static void validateBinding(IAuthenticationRequest authRequest,
                                       BindingMethod method,
                                       EidasErrorKey reportedErr) {

        if (authRequest.getBinding() != null && !authRequest.getBinding().equalsIgnoreCase(method.getValue())
                || authRequest.getBinding() == null && !(BindingMethod.POST.equals(method) || BindingMethod.GET.equals(method))) {
            LOG.info("Expected auth request protocol binding {} but got {}", method, authRequest.getBinding());
            throw new EidasNodeException(EidasErrors.get(reportedErr.errorCode()),
                                                  EidasErrors.get(reportedErr.errorMessage()),
                                                  new InternalErrorEIDASException(EidasErrors.get(
                                                          EidasErrorKey.INVALID_PROTOCOL_BINDING.errorCode()),
                                                                                  EidasErrors.get(
                                                                                          EidasErrorKey.INVALID_PROTOCOL_BINDING
                                                                                                  .errorMessage())));
        }
    }

    /**
     * Check if the Level of assurance is valid
     *
     * @param authnRequest  the instance of {@link IAuthenticationRequest}
     * @param stringMaxLoA - max LoA value of the responder
     * @return true when the LoA value in the request exists and is inferior (or equal) to that of the responder
     * @deprecated since 2.5 {@link #isRequestLoAValid(IEidasAuthenticationRequest, List)} be used instead
     */
    @Deprecated
    public static boolean isRequestLoAValid(IAuthenticationRequest authnRequest, String stringMaxLoA) {
        if (authnRequest instanceof IEidasAuthenticationRequest) {
            IEidasAuthenticationRequest request = (IEidasAuthenticationRequest) authnRequest;
            boolean invalidLoa = StringUtils.isEmpty(stringMaxLoA) || NotifiedLevelOfAssurance.getLevel(stringMaxLoA) == null ||
                    request == null || request.getLevelOfAssurance() == null
                    || NotifiedLevelOfAssurance.getLevel(request.getLevelOfAssurance()) == null;
            if (!invalidLoa) {
                return isLoAValid(request.getLevelOfAssuranceComparison(), request.getLevelOfAssurance(), stringMaxLoA);
            }

            return !invalidLoa;
        }
        return true;
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

    /**
     * Checks if the first element of the list of LoAs is also the highest notified level of the list
     *
     * @param allowedLoAs the list of allowed LoAs
     * @return {@code true} if highest notified LoA in allowed LoAs is not null and is the first LoA in allowedLoAs
     */
    public static boolean isFirstLoaIsHighestNotifiedLoa(final List<String> allowedLoAs) {
        final NotifiedLevelOfAssurance highestNotified = getHighestNotifiedLevelOfAssuranceAllowed(allowedLoAs);
        return (highestNotified != null) && highestNotified.stringValue().equals(allowedLoAs.get(0));
    }

    /**
     * Get the highest notified Level of Assurance from a list of allowed Levels of Assurance.
     * @param allowedLoAs the list of allowed Levels of Assurance.
     * @return the highest notified Level of Assurance
     */
    private static NotifiedLevelOfAssurance getHighestNotifiedLevelOfAssuranceAllowed(List<String> allowedLoAs) {
        NotifiedLevelOfAssurance maxEidasLoA = null;
        for (String loa: allowedLoAs) {
            NotifiedLevelOfAssurance eidasLoA = NotifiedLevelOfAssurance.getLevel(loa);
            if (eidasLoA != null) {
                if (maxEidasLoA == null || maxEidasLoA.numericValue() < eidasLoA.numericValue()) {
                    maxEidasLoA = eidasLoA;
                }
            }
        }
        return maxEidasLoA;
    }

    /**
     * Check if the Level of assurance is valid compared to a given max value
     *
     * @param comparisonType   the instance of {@link LevelOfAssuranceComparison}
     * @param requestLoA    the request's Loa
     * @param stringMaxLoA - max LoA value of the responder
     * @return true when the LoA compare type and value exist and the value is inferior (or equal) to that of the
     * responder
     */
    public static boolean isLoAValid(final LevelOfAssuranceComparison comparisonType, final String requestLoA, String stringMaxLoA) {
        boolean invalidLoa = StringUtils.isEmpty(stringMaxLoA) || NotifiedLevelOfAssurance.getLevel(stringMaxLoA) == null ||
                requestLoA == null || NotifiedLevelOfAssurance.getLevel(requestLoA) == null;
        if (!invalidLoa) {
            if (null != comparisonType) {
                invalidLoa = NotifiedLevelOfAssurance.getLevel(requestLoA).numericValue() > NotifiedLevelOfAssurance.getLevel(stringMaxLoA)
                        .numericValue();
            } else {
                invalidLoa = true;
            }
        }

        return !invalidLoa;
    }

    /**
     * Check if the Level of assurance is valid compared to a given list of values
     *
     * @param comparisonType   the instance of {@link LevelOfAssuranceComparison}
     * @param requestLoAs    the request's Loas
     * @param stringResponseLoA - LoA value of the responder
     * @return true when the LoA compare type and list of values exist and the list of values contains that of the
     * responder
     */
    public static boolean isNonNotifiedLoAValid(final LevelOfAssuranceComparison comparisonType, @Nonnull final List<ILevelOfAssurance> requestLoAs, String stringResponseLoA) {
        boolean invalidLoa = StringUtils.isEmpty(stringResponseLoA) || requestLoAs.isEmpty();
        if (!invalidLoa) {
            if (null != comparisonType) {
                invalidLoa = !requestLoAs.contains(LevelOfAssurance.build(stringResponseLoA));
            } else {
                invalidLoa = true;
            }
        }

        return !invalidLoa;
    }
}
