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

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextDecl;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.OneTimeUse;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for test classes, generates Responses instances and other elements that can be found in Response.
 */
public class ResponseTestHelper {

    /**
     * Generates a response .
     *
     * @param numberAssertions          The number of Assertions to be generated.
     * @param numberAuthnStatements     The number of AuthnStatements to be generated.
     * @param numberAttributeStatements The number of AttributeStatements to be generated.
     * @param statusCodeUri
     * @return The response.
     */
    public static Response generateTestResponse(int numberAssertions, int numberAuthnStatements, int numberAttributeStatements, String statusCodeUri) throws EIDASSAMLEngineException {
        final StatusCode statusCode = BuilderFactoryUtil.generateStatusCode(statusCodeUri);
        final Status status = BuilderFactoryUtil.generateStatus(statusCode);

        ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC);
        List<Assertion> assertions = generateAssertion(numberAssertions, currentTime);
        for (Assertion assertion : assertions) {
            assertion.getAuthnStatements().addAll(generateAuthnStatements(numberAuthnStatements));
            assertion.getAttributeStatements().addAll(generateAttributeStatements(numberAttributeStatements));
        }

        final Response response = BuilderFactoryUtil.generateResponse(
                SAMLEngineUtils.generateNCName(),
                ZonedDateTime.now(ZoneOffset.UTC), status);
        response.getAssertions().addAll(assertions);
        return response;
    }


    /**
     * Generates a list of assertions must be generated to be included in a response.
     *
     * @return The list of assertions.
     */
    public static  List<Assertion> generateAssertion(int numberAssertions, ZonedDateTime currentTime) throws EIDASSAMLEngineException {
        List<Assertion> output = new ArrayList<>();
        for (int i = 0; i < numberAssertions; i++) {
            final Issuer issuerAssertion = BuilderFactoryUtil.generateIssuer();
            final Assertion assertion = BuilderFactoryUtil.generateAssertion(
                    SAMLVersion.VERSION_20, SAMLEngineUtils.generateNCName(),
                    ZonedDateTime.now(ZoneOffset.UTC), issuerAssertion);


            Instant notBefore = currentTime.toInstant();
            notBefore = notBefore.minus(1L, ChronoUnit.DAYS);

            Instant notOnOrAfter = currentTime.toInstant();
            notOnOrAfter = notOnOrAfter.plus(1L, ChronoUnit.DAYS);

            Conditions conditions = generateConditions(notBefore, notOnOrAfter, "requestIssuer", true);
            assertion.setConditions(conditions);
            output.add(assertion);
        }

        return output;
    }

    /**
     * Generates a list of authn statements must be generated to be included in an assertion.
     *
     * @param numberStatements The number Statements to be generated.
     * @return The list of authn statements.
     */
    public static  List<AuthnStatement> generateAuthnStatements(int numberStatements) throws EIDASSAMLEngineException {
        List<AuthnStatement> output = new ArrayList<>();
        for (int i = 0; i < numberStatements; i++) {
            final AuthnContext authnContext = (AuthnContext) BuilderFactoryUtil.buildXmlObject(AuthnContext.DEFAULT_ELEMENT_NAME);
            final AuthnContextDecl authnContextDecl = (AuthnContextDecl) BuilderFactoryUtil.buildXmlObject(AuthnContextDecl.DEFAULT_ELEMENT_NAME);
            authnContext.setAuthnContextDecl(authnContextDecl);
            final AuthnStatement authnStatement = BuilderFactoryUtil.generateAuthnStatement(ZonedDateTime.now(ZoneOffset.UTC), authnContext);
            output.add(authnStatement);
        }

        return output;
    }

    /**
     * Generates a list of attribute statements must be generated to be included in an assertion.
     *
     * @param numberStatements The number Statements to be generated.
     * @return The list of attribute statements.
     */
    public static  List<AttributeStatement> generateAttributeStatements(int numberStatements) throws EIDASSAMLEngineException {
        List<AttributeStatement> output = new ArrayList<>();
        for (int i = 0; i < numberStatements; i++) {
            final AttributeStatement attrStatement = (AttributeStatement) BuilderFactoryUtil.buildXmlObject(AttributeStatement.DEFAULT_ELEMENT_NAME);
            output.add(attrStatement);
        }

        return output;
    }

    /**
     * Generate conditions that MUST be evaluated when assessing the validity of and/or when using the assertion.
     *
     * @param notBefore    the not before
     * @param notOnOrAfter the not on or after
     * @param audienceURI  the audience URI.
     * @return the conditions
     */
    public static Conditions generateConditions(Instant notBefore, Instant notOnOrAfter, String audienceURI, boolean isOneTimeUse)
            throws EIDASSAMLEngineException {
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
}
