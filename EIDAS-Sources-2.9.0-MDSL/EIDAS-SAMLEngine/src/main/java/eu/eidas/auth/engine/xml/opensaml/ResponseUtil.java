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
package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.engine.AbstractProtocolEngine;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class pertaining to the Response.
 */
public final class ResponseUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseUtil.class);

    public static String extractSubject(@Nonnull Assertion assertion) {
        String subject = null;
        if (assertion.getSubject() != null && assertion.getSubject().getNameID() != null) {
            NameID nameId = assertion.getSubject().getNameID();
            subject = nameId.getValue();
        }
        return subject;
    }

    public static String extractSubjectNameIdFormat(@Nonnull Assertion assertion) {
        String format = null;
        if (assertion.getSubject() != null && assertion.getSubject().getNameID() != null) {
            NameID nameId = assertion.getSubject().getNameID();
            format = nameId.getFormat();
        }
        return format;
    }

    public static String extractSubjectConfirmationIPAddress(@Nonnull Assertion assertion) {
        String ipAddress = null;
        if (assertion.getSubject() != null && assertion.getSubject().getSubjectConfirmations() != null) {
            List<SubjectConfirmation> confirmations = assertion.getSubject().getSubjectConfirmations();
            if (!confirmations.isEmpty()) {
                SubjectConfirmation confirmation = confirmations.get(0);
                ipAddress = confirmation.getSubjectConfirmationData().getAddress();
            }
        }
        return ipAddress;
    }

    @Nonnull
    public static IResponseStatus extractResponseStatus(@Nonnull Response samlResponse) {
        ResponseStatus.Builder builder = ResponseStatus.builder();

        Status status = samlResponse.getStatus();
        StatusCode statusCode = status.getStatusCode();
        String statusCodeValue = statusCode.getValue();
        builder.statusCode(statusCodeValue);
        builder.failure(isFailureStatusCode(statusCodeValue));

        // Subordinate code.
        StatusCode subStatusCode = statusCode.getStatusCode();
        if (subStatusCode != null) {
            builder.subStatusCode(subStatusCode.getValue());
        }

        if (status.getStatusMessage() != null) {
            builder.statusMessage(status.getStatusMessage().getValue());
        }

        return builder.build();
    }

    /**
     * Extracts the verified assertion from a given successful response.
     * <p>
     * Except for failure responses the assertion is not extracted.
     *
     * @param samlResponse           the SAML response
     * @param verifyBearerIpAddress  the flag to verifiy or not Bearer Ip Address
     * @param userIpAddress          the user IP address
     * @param beforeSkewTimeInMillis the before Skew Time in Millis
     * @param afterSkewTimeInMillis  the after Skew Time in Millis
     * @param now                    the current DateTime
     * @param audienceRestriction    the
     * @return the assertion for the successful SAML Response or null if SAML Response is a failure one
     * @throws EIDASSAMLEngineException if assertion cannot be verified or for other cases than successful SAML Response or failure SAML Response
     */
    @Nullable
    public static Assertion extractVerifiedAssertion(@Nonnull Response samlResponse,
                                                     boolean verifyBearerIpAddress,
                                                     @Nullable String userIpAddress,
                                                     long beforeSkewTimeInMillis,
                                                     long afterSkewTimeInMillis,
                                                     @Nonnull ZonedDateTime now,
                                                     @Nullable String audienceRestriction)
            throws EIDASSAMLEngineException {

        IResponseStatus responseStatus = ResponseUtil.extractResponseStatus(samlResponse);
        final int numberAssertionsInResponse = samlResponse.getAssertions().size();
        if (isFailure(responseStatus)) {

            return null;
        } else if (isNotFailureResponseAndCorrectNumberAssertions(responseStatus, numberAssertionsInResponse)) {
            Assertion assertion = samlResponse.getAssertions().get(0);
            verifyAssertion(assertion, verifyBearerIpAddress, userIpAddress, beforeSkewTimeInMillis, afterSkewTimeInMillis, now, audienceRestriction);

            return assertion;
        } else {
            //in replace of throwing  EIDASSAMLEngineException("Assertion is null or empty.")
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE,
                    "BUSINESS EXCEPTION : Assertion is other that null for failure SAML Responses or other that 1 for sucessful SAML Responses.");
            throw new EIDASSAMLEngineException(
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "Assertion is other that null for failure SAML Responses or other that 1 for sucessful SAML Responses.");
        }

    }

    private static boolean isNotFailureResponseAndCorrectNumberAssertions(@Nonnull IResponseStatus responseStatus, int numberAssertionsInResponse) {
        return !isFailure(responseStatus) && numberAssertionsInResponse == 1;
    }

    public static boolean isFailure(@Nonnull IResponseStatus responseStatus) {
        return responseStatus.isFailure() || isFailureStatusCode(responseStatus.getStatusCode());
    }

    public static boolean isFailureStatusCode(@Nonnull String statusCodeValue) {
        return !StatusCode.SUCCESS.equals(statusCodeValue);
    }

    public static void verifyAssertion(@Nonnull Assertion assertion,
                                       boolean verifyBearerIpAddress,
                                       @Nonnull String userIpAddress,
                                       long beforeSkewTimeInMillis,
                                       long afterSkewTimeInMillis,
                                       @Nonnull ZonedDateTime now,
                                       @Nullable String audienceRestriction) throws EIDASSAMLEngineException {
        if (verifyBearerIpAddress) {
            Subject subject = assertion.getSubject();
            verifyBearerIpAddress(subject, userIpAddress);
        }

        // Applying skew time conditions before testing it
        Instant skewedNotBefore = assertion.getConditions().getNotBefore().plusMillis(beforeSkewTimeInMillis);
        Instant skewedNotOnOrAfter = assertion.getConditions().getNotOnOrAfter().plusMillis(afterSkewTimeInMillis);
        LOG.debug(AbstractProtocolEngine.SAML_EXCHANGE, "skewTimeInMillis notBefore : {}", beforeSkewTimeInMillis);
        LOG.debug(AbstractProtocolEngine.SAML_EXCHANGE, "skewTimeInMillis notOnOrAfter: {}", afterSkewTimeInMillis);
        LOG.debug(AbstractProtocolEngine.SAML_EXCHANGE, "skewedNotBefore       : {}", skewedNotBefore);
        LOG.debug(AbstractProtocolEngine.SAML_EXCHANGE, "skewedNotOnOrAfter    : {}", skewedNotOnOrAfter);
        assertion.getConditions().setNotBefore(skewedNotBefore);
        assertion.getConditions().setNotOnOrAfter(skewedNotOnOrAfter);

        Conditions conditions = assertion.getConditions();
        verifyConditions(conditions, now, audienceRestriction);
    }

    public static void verifyAudienceRestriction(@Nonnull Conditions conditions, @Nonnull final String audienceRestriction)
            throws EIDASSAMLEngineException {
        if (conditions.getAudienceRestrictions() == null || conditions.getAudienceRestrictions().isEmpty()) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : AudienceRestriction must be present");
            throw new EIDASSAMLEngineException(
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "AudienceRestriction must be present");
        }
        AudienceRestriction firstAudienceRestriction = conditions.getAudienceRestrictions().get(0);
        List<Audience> audiences = firstAudienceRestriction.getAudiences();
        if (CollectionUtils.isEmpty(audiences)) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Audiences must not be empty");
            throw new EIDASSAMLEngineException(
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "Audiences must not be empty");
        }
        boolean audienceAllowed = audiences.stream()
                .anyMatch(audience -> audience.getURI().equals(audienceRestriction));
        if (!audienceAllowed) {
            List<String> audienceUris = audiences.stream()
                    .map(Audience::getURI)
                    .collect(Collectors.toList());
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE,
                    "BUSINESS EXCEPTION : audiences " + audienceUris + " are not allowed");
            throw new EIDASSAMLEngineException(
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "Audiences " + audienceUris + " are not allowed");
        }
    }

    public static void verifyBearerIpAddress(@Nonnull Subject subject, @Nonnull String userIpAddress)
            throws EIDASSAMLEngineException {
        LOG.trace("Verified method Bearer");

        if (null == subject) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : subject is null.");
            throw new EIDASSAMLEngineException(
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "subject is null.");
        }
        List<SubjectConfirmation> subjectConfirmations = subject.getSubjectConfirmations();
        if (null == subjectConfirmations || subjectConfirmations.isEmpty()) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE,
                    "BUSINESS EXCEPTION : SubjectConfirmations are null or empty.");
            throw new EIDASSAMLEngineException(
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "SubjectConfirmations are null or empty.");
        }
        for (final SubjectConfirmation element : subjectConfirmations) {
            boolean isBearer = SubjectConfirmation.METHOD_BEARER.equals(element.getMethod());
            SubjectConfirmationData subjectConfirmationData = element.getSubjectConfirmationData();
            if (null == subjectConfirmationData) {
                LOG.error(AbstractProtocolEngine.SAML_EXCHANGE,
                        "BUSINESS EXCEPTION : subjectConfirmationData is null.");
                throw new EIDASSAMLEngineException(
                        EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                        "subjectConfirmationData is null.");
            }
            String address = subjectConfirmationData.getAddress();
            if (isBearer) {
                if (StringUtils.isBlank(userIpAddress)) {
                    LOG.error(AbstractProtocolEngine.SAML_EXCHANGE,
                            "BUSINESS EXCEPTION : browser_ip is null or empty.");
                    throw new EIDASSAMLEngineException(
                            EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                            "browser_ip is null or empty.");
                } else if (StringUtils.isBlank(address)) {
                    LOG.error(AbstractProtocolEngine.SAML_EXCHANGE,
                            "BUSINESS EXCEPTION : token_ip attribute is null or empty.");
                    throw new EIDASSAMLEngineException(
                            EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                            "token_ip attribute is null or empty.");
                }
            }
            boolean ipEqual = address.equals(userIpAddress);
            // Validation ipUser
            if (!ipEqual) {
                LOG.error(AbstractProtocolEngine.SAML_EXCHANGE,
                        "BUSINESS EXCEPTION : SubjectConfirmation BEARER: IPs doesn't match : token_ip [{}] browser_ip [{}]",
                        address, userIpAddress);
                throw new EIDASSAMLEngineException(
                        EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                        "IPs doesn't match : token_ip (" + address + ") browser_ip (" + userIpAddress + ")");
            }
        }
    }

    public static void verifyConditions(@Nonnull Conditions conditions,
                                        @Nonnull ZonedDateTime now,
                                        @Nullable String audienceRestriction) throws EIDASSAMLEngineException {
        if (null != audienceRestriction) {
            verifyAudienceRestriction(conditions, audienceRestriction);
        }

        verifyTimeConditions(conditions, now);
    }

    public static void verifyTimeConditions(@Nonnull Conditions conditions, @Nonnull ZonedDateTime now)
            throws EIDASSAMLEngineException {
        LOG.debug("serverDate            : " + now);
        ZonedDateTime notBefore = conditions.getNotBefore() != null ? ZonedDateTime.ofInstant(conditions.getNotBefore(), ZoneId.systemDefault()) : null;
        if (notBefore == null) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : NotBefore must be present");
            throw new EIDASSAMLEngineException(
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "NotBefore must be present");
        }
        if (notBefore.isAfter(now.plusMinutes(1))) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE,
                    "BUSINESS EXCEPTION : Current time is before NotBefore condition");
            throw new EIDASSAMLEngineException(
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "Current time is before NotBefore condition");
        }
        ZonedDateTime notOnOrAfter = conditions.getNotOnOrAfter() != null ? ZonedDateTime.ofInstant(conditions.getNotOnOrAfter(), ZoneId.systemDefault()) : null;
        if (notOnOrAfter == null) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : NotOnOrAfter must be present");
            throw new EIDASSAMLEngineException(
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "NotOnOrAfter must be present");
        }
        if (notOnOrAfter.isBefore(now)) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE,
                    "BUSINESS EXCEPTION : Token date expired (getNotOnOrAfter =  " + notOnOrAfter + ", server_date: "
                            + now + ")");
            throw new EIDASSAMLEngineException(
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "Token date expired (getNotOnOrAfter =  " + notOnOrAfter + " ), server_date: " + now);
        }
    }

    @Nonnull
    public static AttributeStatement findAttributeStatement(@Nonnull Assertion assertion) throws EIDASSAMLEngineException {
        AttributeStatement attributeStatement = findAttributeStatementNullable(assertion);
        if (null != attributeStatement) {
            return attributeStatement;
        }

        LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : AttributeStatement not present.");
        throw new EIDASSAMLEngineException(
                EidasErrorKey.INTERNAL_ERROR,
                "AttributeStatement not present.");
    }

    @Nullable
    public static AttributeStatement findAttributeStatementNullable(@Nonnull Assertion assertion) {
        List<XMLObject> orderedChildren = assertion.getOrderedChildren();
        // Search the attribute statement.
        for (XMLObject child : orderedChildren) {
            if (child instanceof AttributeStatement) {
                return (AttributeStatement) child;
            }
        }
        return null;
    }

    private ResponseUtil() {
    }
}
