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
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.AbstractEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AbstractAuthenticationRequest;
import eu.eidas.auth.engine.util.tests.TestingConstants;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Arrays;

/**
 * Test Class for {@link LevelOfAssuranceRequestValidator}
 */
public class LevelOfAssuranceRequestValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains only one LevelOfAssuranceType of NonNotified
     * <p>
     * Must succeed.
     */
    @Test
    public void validateRequestOneNonNotifiedLoa() throws EIDASSAMLEngineException {
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = buildiEidasAuthenticationRequestWithNonNotifiedLoa();
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains LevelOfAssuranceType of both NonNotified and Notified
     * <p>
     * Must succeed.
     */
    @Test
    public void validateRequestNotifiedAndNonNotifiedLoa() throws EIDASSAMLEngineException {
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = buildiEidasAuthenticationRequestWithNotifiedAndNonNotifiedLoa();
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains only one Notified LevelOfAssurance
     * and when the request has the LevelOfAssuranceType of Minimum
     * <p>
     * Must succeed.
     */
    @Test
    public void passOnMinimumNotified() throws EIDASSAMLEngineException {
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IEidasAuthenticationRequest iEidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(
                LevelOfAssurance.build(NotifiedLevelOfAssurance.LOW.stringValue())
        ).build();
        Assert.assertEquals(LevelOfAssuranceComparison.MINIMUM, iEidasAuthenticationRequest.getLevelOfAssuranceComparison());

        levelOfAssuranceRequestValidator.validate(iEidasAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains two Non Notified LevelOfAssurance
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must succeed.
     */
    @Test
    public void passOnExactNonNotifiedLevelsOfAssurance() throws EIDASSAMLEngineException {
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IEidasAuthenticationRequest iEidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(
                LevelOfAssurance.build("http://service.memberstate.ms/NotNotified/LoA/low"),
                LevelOfAssurance.build("http://non.eidas.eu/NotNotified/LoA/1")
        ).build();
        Assert.assertEquals(LevelOfAssuranceComparison.EXACT, iEidasAuthenticationRequest.getLevelOfAssuranceComparison());

        levelOfAssuranceRequestValidator.validate(iEidasAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains one Notified LevelOfAssurance and two Non Notified LevelOfAssurance
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must succeed.
     */
    @Test
    public void passOnExactMixedHighLevelOfAssuranceNotified() throws EIDASSAMLEngineException {
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IEidasAuthenticationRequest iEidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build("http://service.memberstate.ms/NotNotified/LoA/low"),
                LevelOfAssurance.build("http://non.eidas.eu/NotNotified/LoA/1")
        ).build();
        Assert.assertEquals(LevelOfAssuranceComparison.EXACT, iEidasAuthenticationRequest.getLevelOfAssuranceComparison());

        levelOfAssuranceRequestValidator.validate(iEidasAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains three unique Notified LevelOfAssurance and two Non Notified LevelOfAssurance
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must succeed.
     */
    @Test
    public void passOnExactMixedLevelOfAssuranceNotifiedLow() throws EIDASSAMLEngineException {
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IEidasAuthenticationRequest iEidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(
                LevelOfAssurance.build(NotifiedLevelOfAssurance.LOW.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.SUBSTANTIAL.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build("http://service.memberstate.ms/NotNotified/LoA/low"),
                LevelOfAssurance.build("http://non.eidas.eu/NotNotified/LoA/1")
        ).build();
        Assert.assertEquals(LevelOfAssuranceComparison.EXACT, iEidasAuthenticationRequest.getLevelOfAssuranceComparison());

        levelOfAssuranceRequestValidator.validate(iEidasAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains two Non Notified LevelOfAssurance that are the same
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must succeed.
     */
    @Test()
    public void passOnExactDoubleNonNotified() throws EIDASSAMLEngineException {
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build("http://service.memberstate.ms/NotNotified/LoA/low"),
                LevelOfAssurance.build("http://service.memberstate.ms/NotNotified/LoA/low")
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains two Non Notified LevelOfAssurance  and Notified LevelOfAssurance High
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must fail.
     */
    @Test()
    public void passOnExactNotifiedHighReverseOrder() throws EIDASSAMLEngineException {
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build("http://service.memberstate.ms/NotNotified/LoA/low"),
                LevelOfAssurance.build("http://non.eidas.eu/NotNotified/LoA/1"),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue())
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains no LevelsOfAssurance
     * <p>
     * Must throw expected Exception.
     */
    @Test
    public void validateRequestEmptyLoa() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.toString());
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(LevelOfAssuranceComparison.EXACT);
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains two unique Notified LevelOfAssurance
     * and when the request has the LevelOfAssuranceType of Minimum
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnMinimumWithMultipleNotifiedLoa() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.MINIMUM,
                LevelOfAssurance.build(NotifiedLevelOfAssurance.LOW.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue())
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains one unique Non Notified LevelOfAssurance
     * and when the request has the LevelOfAssuranceType of Minimum
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnMinimumWithNonNotifiedLoa() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.MINIMUM,
                LevelOfAssurance.build("http://service.memberstate.ms/NotNotified/LoA/low")
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains two identical Notified LevelOfAssurance
     * and when the request has the LevelOfAssuranceType of Minimum
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnMinimumDoubleLowNotified() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.toString());
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.MINIMUM,
                LevelOfAssurance.build(NotifiedLevelOfAssurance.LOW.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.LOW.stringValue())
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains Notified LevelOfAssurance LOW and two Non Notified LevelOfAssurance
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnExactNotifiedMissingSubstantialAndHigh() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build(NotifiedLevelOfAssurance.LOW.stringValue()),
                LevelOfAssurance.build("http://service.memberstate.ms/NotNotified/LoA/low"),
                LevelOfAssurance.build("http://non.eidas.eu/NotNotified/LoA/1")
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains two Non Notified LevelOfAssurance and Notified LevelOfAssurance LOW
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnExactNotifiedLowMissingHigherLevelsReverseOrder() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build("http://service.memberstate.ms/NotNotified/LoA/low"),
                LevelOfAssurance.build("http://non.eidas.eu/NotNotified/LoA/1"),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.LOW.stringValue())
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains two Notified LevelOfAssurance HIGH and LOW but missing SUBSTANTIAL
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnExactNotifiedMissingHigherLevels() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.toString());
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build(NotifiedLevelOfAssurance.LOW.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue())
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains two identical Notified LevelOfAssurance HIGH
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnExactDoubleHighNotified() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue())
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains two identical Notified LevelOfAssurance HIGH and one Non Notified LevelOfAssurance
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnExactMixedDoubleHighNotified() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build("http://non.eidas.eu/NotNotified/LoA/1")
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains three identical Notified LevelOfAssurance HIGH and one Non Notified LevelOfAssurance
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnExactMixedTripleHighNotified() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build("http://non.eidas.eu/NotNotified/LoA/1")
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains four identical Notified LevelOfAssurance HIGH and one Non Notified LevelOfAssurance
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnExactMixedQuadrupleHighNotified() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build("http://non.eidas.eu/NotNotified/LoA/1")
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains two Non Notified LevelOfAssurance that use the reserved eidas prefix
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnNonNotifiedLoaWithEidasPrefix() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();

        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.EXACT,
                mockILevelOfAssurance("http://eidas.europa.eu/LoA/NotNotified/A"),
                mockILevelOfAssurance("http://eidas.europa.eu/LoA/NotNotified/B")
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request contains two Non Notified LevelOfAssurance that use the reserved eidas prefix
     * and when the request has the LevelOfAssuranceType of Exact
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnNonNotifiedLoaWithEidasPrefixLowercase() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();

        final IAuthenticationRequest iAuthenticationRequest = mockIEidasAuthenticationRequest(
                LevelOfAssuranceComparison.EXACT,
                mockILevelOfAssurance("http://eidas.europa.eu/loa/NotNotified/A")
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }


    /**
     * Test method for
     * {@link LevelOfAssuranceRequestValidator#validate(IAuthenticationRequest)}
     * when the request is not an {@link IEidasAuthenticationRequest}
     * <p>
     * Must fail.
     */
    @Test()
    public void failOnIAuthenticationNotIEidasAuthentication() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        final LevelOfAssuranceRequestValidator levelOfAssuranceRequestValidator = new LevelOfAssuranceRequestValidator();
        final IAuthenticationRequest iAuthenticationRequest = mockIAuthenticationRequest(
                LevelOfAssurance.build(NotifiedLevelOfAssurance.HIGH.stringValue()),
                LevelOfAssurance.build("http://non.eidas.eu/NotNotified/LoA/1")
        );
        levelOfAssuranceRequestValidator.validate(iAuthenticationRequest);
    }

    private IEidasAuthenticationRequest buildiEidasAuthenticationRequestWithNonNotifiedLoa() {
        return getEidasAuthenticationRequestBuilder()
                .levelOfAssurance("http://non.eidas.eu/NotNotified/LoA/4")
                .levelOfAssuranceComparison("exact")
                .providerName("DEMO_SP")
                .build();
    }

    private IEidasAuthenticationRequest buildiEidasAuthenticationRequestWithNotifiedAndNonNotifiedLoa() {
        return getEidasAuthenticationRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .levelOfAssurance("http://non.eidas.eu/NotNotified/LoA/4")
                .levelOfAssuranceComparison("exact")
                .providerName("DEMO_SP")
                .build();
    }

    private EidasAuthenticationRequest.Builder getEidasAuthenticationRequestBuilder() {
        final EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder = EidasAuthenticationRequest.builder();

        eidasAuthenticationRequestBuilder.id(TestingConstants.SAML_ID_CONS.toString())
                .destination(TestingConstants.REQUEST_DESTINATION_CONS.toString())
                .issuer(TestingConstants.REQUEST_ISSUER_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .assertionConsumerServiceURL(TestingConstants.ASSERTION_URL_CONS.toString());

        return eidasAuthenticationRequestBuilder;
    }

    private EidasAuthenticationRequest.Builder getEidasAuthenticationRequestBuilder(ILevelOfAssurance... loas) {
        return EidasAuthenticationRequest.builder()
                .id(TestingConstants.SAML_ID_CONS.toString())
                .destination(TestingConstants.REQUEST_DESTINATION_CONS.toString())
                .issuer(TestingConstants.REQUEST_ISSUER_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .assertionConsumerServiceURL(TestingConstants.ASSERTION_URL_CONS.toString())
                .levelsOfAssurance(Arrays.asList(loas));
    }

    private ILevelOfAssurance mockILevelOfAssurance(final String uri) {
        final ILevelOfAssurance levelOfAssurance = Mockito.mock(ILevelOfAssurance.class);
        Mockito.when(levelOfAssurance.getValue()).thenReturn(uri);
        return levelOfAssurance;
    }

    private IAuthenticationRequest mockIAuthenticationRequest(ILevelOfAssurance... loas) {
        final IAuthenticationRequest authenticationRequest = Mockito.mock(AbstractAuthenticationRequest.class);
        Mockito.when(authenticationRequest.getLevelsOfAssurance()).thenReturn(Arrays.asList(loas));
        return authenticationRequest;
    }

    private IEidasAuthenticationRequest mockIEidasAuthenticationRequest(LevelOfAssuranceComparison comparison, ILevelOfAssurance... loas) {
        final IEidasAuthenticationRequest eidasAuthenticationRequest = Mockito.mock(AbstractEidasAuthenticationRequest.class);
        Mockito.when(eidasAuthenticationRequest.getLevelOfAssuranceComparison()).thenReturn(comparison);
        Mockito.when(eidasAuthenticationRequest.getLevelsOfAssurance()).thenReturn(Arrays.asList(loas));
        return eidasAuthenticationRequest;
    }
}