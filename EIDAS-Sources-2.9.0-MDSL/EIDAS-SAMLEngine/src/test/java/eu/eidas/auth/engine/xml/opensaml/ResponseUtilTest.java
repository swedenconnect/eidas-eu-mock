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
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.core.eidas.DigestMethod;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.mockito.Mockito.when;

/**
 * Test Class for {@link ResponseUtil}
 */
public class ResponseUtilTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Need to load the OpenSaml context.
     */
    @BeforeClass
    public static void setUpClass() {
        OpenSamlHelper.initialize();
    }

    /**
     * Test method for
     * {@link ResponseUtil#extractVerifiedAssertion(Response, boolean, String, long, long, ZonedDateTime, String)}
     * when the response is a successful one with one assertion.
     * <p/>
     * Must succeed.
     *
     * @throws EIDASSAMLEngineException
     */
    @Test
    public void extractVerifiedAssertion() throws EIDASSAMLEngineException {
        Response samlResponse = ResponseTestHelper.generateTestResponse(1, 1, 1, StatusCode.SUCCESS);
        String userIpAddress = "demoIpAddress";
        boolean verifyBearerIpAddress = false;
        long beforeSkewTimeInMillis = 1000;
        long afterSkewTimeInMillis = 1000;
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        String audienceRestriction = null;

        Assertion assertion = ResponseUtil.extractVerifiedAssertion(
                samlResponse,
                verifyBearerIpAddress,
                userIpAddress,
                beforeSkewTimeInMillis,
                afterSkewTimeInMillis,
                now,
                audienceRestriction);

        Assert.assertNotNull(assertion);
    }


    /**
     * Test method for
     * {@link ResponseUtil#extractVerifiedAssertion(Response, boolean, String, long, long, ZonedDateTime, String)}
     * when the response is not a successful and with zero assertions.
     * <p/>
     * Must succeed.
     *
     * @throws EIDASSAMLEngineException
     */
    @Test
    public void extractVerifiedAssertionNoAssertionStatusCodeNotSuccess() throws EIDASSAMLEngineException {
        Response samlResponse = ResponseTestHelper.generateTestResponse(0, 1, 1, StatusCode.REQUESTER);
        String userIpAddress = "demoIpAddress";
        boolean verifyBearerIpAddress = false;
        long beforeSkewTimeInMillis = 1000;
        long afterSkewTimeInMillis = 1000;
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        String audienceRestriction = null;

        Assertion assertion = ResponseUtil.extractVerifiedAssertion(
                samlResponse,
                verifyBearerIpAddress,
                userIpAddress,
                beforeSkewTimeInMillis,
                afterSkewTimeInMillis,
                now,
                audienceRestriction);

        Assert.assertNull(assertion);
    }


    /**
     * Test method for
     * {@link ResponseUtil#extractVerifiedAssertion(Response, boolean, String, long, long, ZonedDateTime, String)}
     * when the response is not a successful and with zero assertions.
     * <p/>
     * Must fail.
     *
     * @throws EIDASSAMLEngineException
     */
    @Test
    public void extractVerifiedAssertion2AssertionsStatusCodeSucess() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        Response samlResponse = ResponseTestHelper.generateTestResponse(2, 1, 1, StatusCode.SUCCESS);
        String userIpAddress = "demoIpAddress";
        boolean verifyBearerIpAddress = false;
        long beforeSkewTimeInMillis = 1000;
        long afterSkewTimeInMillis = 1000;
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        String audienceRestriction = null;

        ResponseUtil.extractVerifiedAssertion(
                samlResponse,
                verifyBearerIpAddress,
                userIpAddress,
                beforeSkewTimeInMillis,
                afterSkewTimeInMillis,
                now,
                audienceRestriction);
    }

    /**
     * Test method for
     * {@link ResponseUtil#verifyTimeConditions(Conditions, ZonedDateTime)}
     * when notBefore from DateTime is null
     * <p>
     * Must fail.
     */
    @Test
    public void testVerifyTimeConditionsWithNotBeforeNull() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final Conditions mockConditions = Mockito.mock(Conditions.class);

        ResponseUtil.verifyTimeConditions(mockConditions, ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Test method for
     * {@link ResponseUtil#verifyTimeConditions(Conditions, ZonedDateTime)}
     * when notOnOrAfter from DateTime is null
     * <p>
     * Must fail.
     */
    @Test
    public void testVerifyTimeConditionsWithNotOnOrAfterNull() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final ZonedDateTime futureDateTime = ZonedDateTime.of(2100, 10, 10, 10, 10, 0, 0, ZoneOffset.UTC);
        final Instant pastDateTime = ZonedDateTime.of(2020, 10, 10, 10, 10, 0, 0, ZoneOffset.UTC).toInstant();

        final Conditions mockConditions = Mockito.mock(Conditions.class);
        Mockito.when(mockConditions.getNotBefore()).thenReturn(pastDateTime);

        ResponseUtil.verifyTimeConditions(mockConditions, futureDateTime);
    }

    /**
     * Test method for
     * {@link ResponseUtil#verifyTimeConditions(Conditions, ZonedDateTime)}
     * when notBefore is after (now plus 1 minute)
     * <p>
     * Must fail.
     */
    @Test
    public void testVerifyTimeConditionsWhenNotBeforeIsAfterNowPlus1Minute() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final Conditions mockConditions = Mockito.mock(Conditions.class);
        when(mockConditions.getNotBefore()).thenReturn(Instant.now().plus(Duration.ofDays(1)));

        ResponseUtil.verifyTimeConditions(mockConditions, ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Test method for
     * {@link ResponseUtil#verifyTimeConditions(Conditions, ZonedDateTime)}
     * when notOnOrAfter is before now
     * <p>
     * Must fail.
     */
    @Test
    public void testVerifyTimeConditionsWhenNotOnOrAfterIsBeforeNow() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final Conditions mockConditions = Mockito.mock(Conditions.class);
        when(mockConditions.getNotBefore()).thenReturn(Instant.now());
        when(mockConditions.getNotOnOrAfter()).thenReturn(Instant.now().minus(Duration.ofDays(1)));

        ResponseUtil.verifyTimeConditions(mockConditions, ZonedDateTime.now(ZoneOffset.UTC));
    }


    /**
     * Test method for
     * {@link ResponseUtil#findAttributeStatement(Assertion)}
     * when {@link AttributeStatement} is present in {@link Assertion}
     * <p>
     * Must succeed.
     */
    @Test
    public void testFindAttributeStatementWithAssertionWithAttributeStatement() throws EIDASSAMLEngineException {
        final Assertion mockAssertion = Mockito.mock(Assertion.class);
        final AttributeStatement mockAsAttributeStatement = Mockito.mock(AttributeStatement.class);
        ArrayList<XMLObject> attributeStatements = new ArrayList<>();
        attributeStatements.add(mockAsAttributeStatement);
        when(mockAssertion.getOrderedChildren()).thenReturn(attributeStatements);

        ResponseUtil.findAttributeStatement(mockAssertion);
    }

    /**
     * Test method for
     * {@link ResponseUtil#findAttributeStatement(Assertion)}
     * when another {@link XMLObject} is present {@link Assertion}
     * but not of type {@link AttributeStatement}
     * <p>
     * Must fail.
     */
    @Test
    public void testFindAttributeStatementWithAssertionWithXMLObjectButNotAttributeStatement() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final Assertion mockAssertion = Mockito.mock(Assertion.class);
        ArrayList<XMLObject> attributeStatements = new ArrayList<>();
        when(mockAssertion.getOrderedChildren()).thenReturn(attributeStatements);
        final DigestMethod mockADigestMethod = Mockito.mock(DigestMethod.class);
        attributeStatements.add(mockADigestMethod);

        ResponseUtil.findAttributeStatement(mockAssertion);
    }

}