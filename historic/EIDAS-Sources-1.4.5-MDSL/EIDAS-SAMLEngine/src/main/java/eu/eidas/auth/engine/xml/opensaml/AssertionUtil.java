/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
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
package eu.eidas.auth.engine.xml.opensaml;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.attribute.*;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.auth.engine.core.SAMLExtensionFormat;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * AssertionUtil
 *
 * @since 1.1
 */
public final class AssertionUtil {

    private static final String FAILURE_SUBJECT_NAME_ID = "NotAvailable";
    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AssertionUtil.class);


    private AssertionUtil() {
        // empty constructor
    }


    /**
     * Generates the assertion for the response.
     *
     * @deprecated since 1.4.
     * Use {@link AssertionUtil#generateResponseAssertion(boolean, String, IAuthenticationRequest, Issuer, ImmutableAttributeMap, DateTime, String, String, SAMLExtensionFormat, boolean, DateTime)} instead.
     *
     * @param isFailure
     * @param ipAddress    the IP address.
     * @param request      the request for which the response is prepared
     * @param responseIssuer
     * @param attributeMap
     * @param notOnOrAfter the not on or after
     * @param formatEntity
     * @param responder
     * @param extensionFormat
     * @param isOneTimeUse
     * @return the assertion
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Deprecated
    public static final Assertion generateResponseAssertion(boolean isFailure,
                                                            String ipAddress,
                                                            IAuthenticationRequest request,
                                                            Issuer responseIssuer,
                                                            ImmutableAttributeMap attributeMap,
                                                            DateTime notOnOrAfter,
                                                            String formatEntity,
                                                            String responder,
                                                            SAMLExtensionFormat extensionFormat,
                                                            boolean isOneTimeUse) throws EIDASSAMLEngineException {


        //Solution to allow usage of this method by deprecated classes without changing their related code
        // and use the method {@link AssertionUtil#generateResponseAssertion(boolean, String, IAuthenticationRequest, Issuer, ImmutableAttributeMap, DateTime, String, String, SAMLExtensionFormat, boolean, SamlEngineClock)}
        // with the parameter for the rest of the code
        final DateTime currentTime = new DateTime();

        return generateResponseAssertion(isFailure, ipAddress, request, responseIssuer, attributeMap, notOnOrAfter, formatEntity, responder, extensionFormat, isOneTimeUse, currentTime);
    }


    /**
     * Generates the assertion for the response.
     *
     * @param isFailure
     * @param ipAddress    the IP address.
     * @param request      the request for which the response is prepared
     * @param responseIssuer
     * @param attributeMap
     * @param notOnOrAfter the not on or after
     * @param formatEntity
     * @param responder
     * @param extensionFormat
     * @param isOneTimeUse
     * @param currentTime the current time
     * @return the assertion
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    public static final Assertion generateResponseAssertion(boolean isFailure,
                                                            String ipAddress,
                                                            IAuthenticationRequest request,
                                                            Issuer responseIssuer,
                                                            ImmutableAttributeMap attributeMap,
                                                            DateTime notOnOrAfter,
                                                            String formatEntity,
                                                            String responder,
                                                            SAMLExtensionFormat extensionFormat,
                                                            boolean isOneTimeUse,
                                                            final DateTime currentTime) throws EIDASSAMLEngineException {
        LOG.trace("Generate Assertion.");

        // Mandatory
        LOG.trace("Generate Issuer to Assertion");
        Issuer issuerAssertion = BuilderFactoryUtil.generateIssuer();
        issuerAssertion.setValue(responseIssuer.getValue());

        // Format Entity Optional
        issuerAssertion.setFormat(formatEntity);

        Assertion assertion =
                BuilderFactoryUtil.generateAssertion(SAMLVersion.VERSION_20, SAMLEngineUtils.generateNCName(),
                        currentTime, issuerAssertion);

        // Subject is mandatory in non failure responses, in some cases it is available for failure also
        addSubjectToAssertion(isFailure, assertion, request, attributeMap, notOnOrAfter, ipAddress, responder, extensionFormat, currentTime);

        // Conditions that MUST be evaluated when assessing the validity of
        // and/or when using the assertion.
        Conditions conditions = generateConditions(currentTime, notOnOrAfter, request.getIssuer(), isOneTimeUse);

        assertion.setConditions(conditions);

        LOG.trace("Generate Authentication Statement.");
        /**TODO SubjectoLocality will be added by decision made on optional elements later,
        Address of entity is available in SubjectConfirmationData if provided */
        AuthnStatement eidasAuthnStat = generateAuthStatement(null, null, currentTime);
        assertion.getAuthnStatements().add(eidasAuthnStat);

        return assertion;
    }

    private static void addSubjectToAssertion(boolean isFailure,
                                              Assertion assertion,
                                              IAuthenticationRequest request,
                                              ImmutableAttributeMap attributeMap,
                                              DateTime notOnOrAfter,
                                              String ipAddress,
                                              String responder, SAMLExtensionFormat extensionFormat,
                                              final DateTime currentTime) throws EIDASSAMLEngineException {
        Subject subject = BuilderFactoryUtil.generateSubject();

        NameID nameId = getNameID(isFailure, request.getNameIdFormat(), attributeMap, responder, extensionFormat);
        subject.setNameID(nameId);

        // Mandatory if urn:oasis:names:tc:SAML:2.0:cm:bearer.
        // Optional in other case.
        LOG.trace("Generate SubjectConfirmationData.");
        SubjectConfirmationData dataBearer =
                BuilderFactoryUtil.generateSubjectConfirmationData(currentTime,
                                                                   request.getAssertionConsumerServiceURL(),
                                                                   request.getId());

        // Mandatory if urn:oasis:names:tc:SAML:2.0:cm:bearer.
        // Optional in other case.
        LOG.trace("Generate SubjectConfirmation");
        SubjectConfirmation subjectConf =
                BuilderFactoryUtil.generateSubjectConfirmation(SubjectConfirmation.METHOD_BEARER, dataBearer);

        SubjectConfirmationData subjectConfirmationData = subjectConf.getSubjectConfirmationData();
        /** STORK?
        if (SubjectConfirmation.METHOD_BEARER.equals(subjectConf.getMethod())) {
            // ipAddress Mandatory if method is Bearer.

            if (StringUtils.isBlank(ipAddress)) {
                LOG.info(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : ipAddress is null or empty");
                throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                        EidasErrorKey.INTERNAL_ERROR.errorCode(),
                        "ipAddress is null or empty");
            }

        } */
        if (StringUtils.isNotBlank(ipAddress) && SubjectConfirmation.METHOD_BEARER.equals(subjectConf.getMethod())) {
            subjectConfirmationData.setAddress(ipAddress.trim());
        }

        subjectConfirmationData.setRecipient(request.getAssertionConsumerServiceURL());
        subjectConfirmationData.setNotOnOrAfter(notOnOrAfter);

        // The SAML 2.0 specification allows multiple SubjectConfirmations
        subject.getSubjectConfirmations().addAll(Collections.singletonList(subjectConf));

        // Mandatory if not failure
        assertion.setSubject(subject);
    }

    private static NameID getNameID(boolean isFailure, String requestFormat, ImmutableAttributeMap attributeMap, String responder, SAMLExtensionFormat extensionFormat)
            throws EIDASSAMLEngineException {
        NameID nameId;
        String nameQualifier = responder;
        String format;
        String spNameQualifier = "";
        String nameIdValue;
        LOG.trace("Generate NameID");

        if (isFailure) {
            format = SamlNameIdFormat.UNSPECIFIED.getNameIdFormat();
            nameIdValue = FAILURE_SUBJECT_NAME_ID;
        } else {
            // Mandatory to be verified
            // String format = NameID.UNSPECIFIED
            // specification: 'SAML:2.0' exist
            // opensaml: "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified"
            // opensaml  "urn:oasis:names:tc:SAML:2.0:nameid-format:unspecified"
            format = requestFormat;
            if (null == format) {
                format =
                        SAMLExtensionFormat.EIDAS10 == extensionFormat ? SamlNameIdFormat.PERSISTENT
                                .getNameIdFormat() : SamlNameIdFormat.UNSPECIFIED.getNameIdFormat();
            }
            nameIdValue = getUniquenessIdentifier(attributeMap);
        }

        nameId = BuilderFactoryUtil.generateNameID(nameQualifier, format, spNameQualifier);
        nameId.setValue(nameIdValue);
        return nameId;
    }

    private static String getUniquenessIdentifier(@Nonnull ImmutableAttributeMap attributeMap)
            throws EIDASSAMLEngineException {
        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : attributeMap.getAttributeMap()
                .entrySet()) {
            AttributeDefinition<?> attributeDefinition = entry.getKey();
            ImmutableSet<? extends AttributeValue<?>> values = entry.getValue();
            if (attributeDefinition.isUniqueIdentifier() && !values.isEmpty()) {
                AttributeValueMarshaller<?> attributeValueMarshaller =
                        attributeDefinition.getAttributeValueMarshaller();
                try {
                    return attributeValueMarshaller.marshal((AttributeValue)values.iterator().next());
                } catch (AttributeValueMarshallingException e) {
                    LOG.error("BUSINESS EXCEPTION : Invalid Attribute Value " + e, e);
                    throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                            EidasErrorKey.INTERNAL_ERROR.errorCode(), e);
                }
            }
        }
        String message = "Unique Identifier not found: " + attributeMap;
        LOG.info("BUSINESS EXCEPTION : " + message);
        throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                EidasErrorKey.INTERNAL_ERROR.errorCode(), message);
    }

    /**
     * Generate conditions that MUST be evaluated when assessing the validity of and/or when using the assertion.
     *
     * @param notBefore    the not before
     * @param notOnOrAfter the not on or after
     * @param audienceURI  the audience URI.
     * @return the conditions
     */
    private static Conditions generateConditions(DateTime notBefore, DateTime notOnOrAfter, String audienceURI, boolean isOneTimeUse)
            throws EIDASSAMLEngineException {
        LOG.trace("Generate conditions.");
        Conditions conditions = (Conditions) BuilderFactoryUtil.buildXmlObject(Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore(notBefore);
        conditions.setNotOnOrAfter(notOnOrAfter);

        AudienceRestriction restrictions =
                (AudienceRestriction) BuilderFactoryUtil.buildXmlObject(AudienceRestriction.DEFAULT_ELEMENT_NAME);
        Audience audience = (Audience) BuilderFactoryUtil.buildXmlObject(Audience.DEFAULT_ELEMENT_NAME);
        audience.setAudienceURI(audienceURI);

        restrictions.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(restrictions);

        if (isOneTimeUse) {
            OneTimeUse oneTimeUse = (OneTimeUse) BuilderFactoryUtil.buildXmlObject(OneTimeUse.DEFAULT_ELEMENT_NAME);
            conditions.getConditions().add(oneTimeUse);
        }
        return conditions;
    }

    /**
     * Generate authentication statement.
     *
     * @param ipAddress the IP address
     * @param currentTime the saml engine clock
     * @return the authentication statement
     */
    public static AuthnStatement generateAuthStatement(String ipAddress, String dnsName, final DateTime currentTime) throws EIDASSAMLEngineException {
        LOG.trace("Generate authenticate statement.");

        SubjectLocality subjectLocality = null;
        if (StringUtils.isNotBlank(ipAddress) || StringUtils.isNotBlank(dnsName)) {
            subjectLocality = BuilderFactoryUtil.generateSubjectLocality(ipAddress, dnsName);
        }

        AuthnContext authnContext = (AuthnContext) BuilderFactoryUtil.buildXmlObject(AuthnContext.DEFAULT_ELEMENT_NAME);

        AuthnContextDecl authnContextDecl =
                (AuthnContextDecl) BuilderFactoryUtil.buildXmlObject(AuthnContextDecl.DEFAULT_ELEMENT_NAME);

        authnContext.setAuthnContextDecl(authnContextDecl);

        AuthnStatement authnStatement = BuilderFactoryUtil.generateAuthnStatement(currentTime, authnContext);

        // Optional
        authnStatement.setSessionIndex(null);
        if (subjectLocality != null) {
            authnStatement.setSubjectLocality(subjectLocality);
        }

        return authnStatement;
    }
}
