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

package eu.eidas.auth.engine.core.eidas;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * LevelOfAssuranceResponseValidatorTest
 *
 * Class to test {@link LevelOfAssuranceResponseValidator}
 */
public class LevelOfAssuranceResponseValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link LevelOfAssuranceResponseValidator#validate(AuthenticationResponse)}
     * when the response contains one Notified LevelOfAssurance
     * <p>
     * Must succeed.
     */
    @Test
    public void passOnNotifiedLevelOfAssurance() throws EIDASSAMLEngineException {
        LevelOfAssuranceResponseValidator levelOfAssuranceResponseValidator = new LevelOfAssuranceResponseValidator();
        AuthenticationResponse authenticationResponse = createAuthenticationResponseBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .build();
        levelOfAssuranceResponseValidator.validate(authenticationResponse);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceResponseValidator#validate(AuthenticationResponse)}
     * when the response contains one Non Notified LevelOfAssurance
     * <p>
     * Must succeed.
     */
    @Test
    public void passOnNonNotifiedLevelOfAssurance() throws EIDASSAMLEngineException {
        LevelOfAssuranceResponseValidator levelOfAssuranceResponseValidator = new LevelOfAssuranceResponseValidator();
        AuthenticationResponse authenticationResponse = createAuthenticationResponseBuilder()
                .levelOfAssurance("http://eidas.memberstate.ms/LoA/NotAnOfficialLoa")
                .build();
        levelOfAssuranceResponseValidator.validate(authenticationResponse);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceResponseValidator#validate(AuthenticationResponse)}
     * when the response is a failure
     * when the response contains no LevelsOfAssurace
     * <p>
     * Must succeed.
     */
    @Test
    public void PassOnRejected() throws EIDASSAMLEngineException {
        LevelOfAssuranceResponseValidator levelOfAssuranceResponseValidator = new LevelOfAssuranceResponseValidator();
        AuthenticationResponse authenticationResponse = createAuthenticationResponseBuilder()
                .failure(true)
                .build();
        levelOfAssuranceResponseValidator.validate(authenticationResponse);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceResponseValidator#validate(AuthenticationResponse)}
     * when the response contains no LevelOfAssurance
     * <p>
     * Must fail.
     */
    @Test
    public void failOnLevelsOfAssuranceIsEmpty() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        LevelOfAssuranceResponseValidator levelOfAssuranceResponseValidator = new LevelOfAssuranceResponseValidator();
        AuthenticationResponse authenticationResponse = createAuthenticationResponseBuilder()
                .build();
        levelOfAssuranceResponseValidator.validate(authenticationResponse);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceResponseValidator#validate(AuthenticationResponse)}
     * when the response contains one NonNotified LevelOfAssurance using the Notified EIDAS prefix
     * <p>
     * Must fail.
     */
    @Test
    public void failOnLevelsOfAssuranceIsInvalidNotified() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        LevelOfAssuranceResponseValidator levelOfAssuranceResponseValidator = new LevelOfAssuranceResponseValidator();
        AuthenticationResponse authenticationResponse = createAuthenticationResponseBuilder()
                .levelOfAssurance("http://eidas.europa.eu/LoA/NotAnOfficialLoa")
                .build();
        levelOfAssuranceResponseValidator.validate(authenticationResponse);
    }

    private AuthenticationResponse.Builder createAuthenticationResponseBuilder() {
        AuthenticationResponse.Builder eidasAuthnResponse = new AuthenticationResponse.Builder();
        eidasAuthnResponse.country("UK");
        eidasAuthnResponse.id("QDS2QFD");
        eidasAuthnResponse.audienceRestriction("PUBLIC");
        eidasAuthnResponse.inResponseTo("6E97069A1754ED");
        eidasAuthnResponse.failure(false);
        eidasAuthnResponse.statusCode(EIDASStatusCode.REQUESTER_URI.toString());
        eidasAuthnResponse.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        eidasAuthnResponse.statusMessage("TEST");
        eidasAuthnResponse.notBefore(new DateTime());
        eidasAuthnResponse.notOnOrAfter(new DateTime());
        eidasAuthnResponse.ipAddress("123.123.123.123");

        eidasAuthnResponse.issuer("issuer");
        eidasAuthnResponse.subject("subject");
        eidasAuthnResponse.subjectNameIdFormat("subjectNameIdFormat");

        return eidasAuthnResponse;
    }
}