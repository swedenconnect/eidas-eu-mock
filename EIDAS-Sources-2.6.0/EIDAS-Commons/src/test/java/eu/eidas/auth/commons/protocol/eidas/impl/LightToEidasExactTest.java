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

package eu.eidas.auth.commons.protocol.eidas.impl;

import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import org.junit.Test;

import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.NON_NOTIFIED_LOA_ALPHA;
import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.NON_NOTIFIED_LOA_BETA;
import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_HIGH;
import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL;
import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_LOW;

/**
 * LightToEidasExactTest
 * Class to test {@link EidasAuthenticationRequest} as it decorates {@link LightRequest}
 * on the interface {@link ILightRequest} for comparison EXACT cases
 */
public class LightToEidasExactTest extends LightToEidasTest {

    /**
     * Test method for {@link EidasAuthenticationRequest#builder()}
     * when Light request contains non-notified levels of assurance
     * <p>
     * Must succeed
     */
    @Test
    public void lightToEidasExactNonNotified() {
        LightRequest lightRequest = buildLightRequest(NON_NOTIFIED_LOA_ALPHA, NON_NOTIFIED_LOA_BETA);

        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(lightRequest).build();

        assertNrNotifiedLoa(lightRequest, 0);
        assertEqualsLoaList(lightRequest, NON_NOTIFIED_LOA_ALPHA, NON_NOTIFIED_LOA_BETA);

        assertEqualsLoaList(eidasAuthenticationRequest, NON_NOTIFIED_LOA_ALPHA, NON_NOTIFIED_LOA_BETA);
    }

    /**
     * Test method for {@link EidasAuthenticationRequest#getLightRequest()}
     * when EidasAuthenticationRequest contains non-notified levels of assurance
     * <p>
     * Must succeed
     */
    @Test
    public void eidasToLightExactNonNotified() {
        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build(NON_NOTIFIED_LOA_ALPHA),
                LevelOfAssurance.build(NON_NOTIFIED_LOA_BETA)
        ).build();
        ILightRequest iLightRequest = eidasAuthenticationRequest.getLightRequest();

        assertEqualsLoaList(eidasAuthenticationRequest, NON_NOTIFIED_LOA_ALPHA, NON_NOTIFIED_LOA_BETA);

        assertNrNotifiedLoa(iLightRequest, 0);
        assertEqualsLoaList(iLightRequest, NON_NOTIFIED_LOA_ALPHA, NON_NOTIFIED_LOA_BETA);
    }


    /**
     * Test method for {@link EidasAuthenticationRequest#getLightRequest()}
     * when EidasAuthenticationRequest contains non-notified levels of assurance
     * and one notified level of assurance of 'high'
     * <p>
     * Must succeed
     */
    @Test
    public void eidasToLightExactMixedHigh() {
        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_HIGH),
                LevelOfAssurance.build(NON_NOTIFIED_LOA_ALPHA),
                LevelOfAssurance.build(NON_NOTIFIED_LOA_BETA)
        ).build();
        assertEqualsLoaList(eidasAuthenticationRequest, LEVEL_OF_ASSURANCE_NOTIFIED_HIGH, NON_NOTIFIED_LOA_ALPHA, NON_NOTIFIED_LOA_BETA);

        ILightRequest iLightRequest = eidasAuthenticationRequest.getLightRequest();

        assertNrNotifiedLoa(iLightRequest, 1);
        assertEqualsLoaList(iLightRequest, LEVEL_OF_ASSURANCE_NOTIFIED_HIGH, NON_NOTIFIED_LOA_ALPHA, NON_NOTIFIED_LOA_BETA);

        EidasAuthenticationRequest eidasAuthenticationRequestMirror = getEidasAuthenticationRequestBuilder(iLightRequest).build();

        assertEqualsLoaList(eidasAuthenticationRequestMirror, LEVEL_OF_ASSURANCE_NOTIFIED_HIGH, NON_NOTIFIED_LOA_ALPHA, NON_NOTIFIED_LOA_BETA);
    }

    /**
     * Test method for {@link EidasAuthenticationRequest#getLightRequest()}
     * when EidasAuthenticationRequest contains non-notified levels of assurance,
     * one notified level of assurance of 'substantial'
     * and one notified level of assurance of 'high'
     * <p>
     * Must succeed
     */
    @Test
    public void eidasToLightExactMixedSubstantialHigh() {
        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build(NON_NOTIFIED_LOA_ALPHA),
                LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL),
                LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_HIGH),
                LevelOfAssurance.build(NON_NOTIFIED_LOA_BETA)
        ).build();

        ILightRequest iLightRequest = eidasAuthenticationRequest.getLightRequest();

        assertEqualsLoaList(eidasAuthenticationRequest,
                LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL,
                NON_NOTIFIED_LOA_ALPHA,
                NON_NOTIFIED_LOA_BETA,
                LEVEL_OF_ASSURANCE_NOTIFIED_HIGH);

        assertNrNotifiedLoa(iLightRequest, 1);
        assertEqualsLoaList(iLightRequest, LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL, NON_NOTIFIED_LOA_ALPHA, NON_NOTIFIED_LOA_BETA);

    }

    /**
     * Test method for {@link EidasAuthenticationRequest#builder()}
     * when Light request contains non-notified levels of assurance,
     * one notified level of assurance of 'substantial'
     * and one notified level of assurance of 'high'
     * <p>
     * Must succeed
     */
    @Test
    public void lightToEidasExactMixedSubstantialHigh() {
        LightRequest lightRequest = buildLightRequest(
                NON_NOTIFIED_LOA_ALPHA,
                LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL,
                LEVEL_OF_ASSURANCE_NOTIFIED_HIGH,
                NON_NOTIFIED_LOA_BETA
        );
        assertNrNotifiedLoa(lightRequest, 1);
        assertEqualsLoaList(lightRequest, LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL, NON_NOTIFIED_LOA_ALPHA, NON_NOTIFIED_LOA_BETA);

        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(lightRequest).build();

        assertEqualsLoaList(eidasAuthenticationRequest,
                LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL,
                NON_NOTIFIED_LOA_ALPHA,
                NON_NOTIFIED_LOA_BETA,
                LEVEL_OF_ASSURANCE_NOTIFIED_HIGH);
    }

    /**
     * Test method for {@link EidasAuthenticationRequest#getLightRequest()}
     * when EidasAuthenticationRequest contains non-notified levels of assurance,
     * one notified level of assurance of 'low',
     * one notified level of assurance of 'substantial'
     * and one notified level of assurance of 'high'
     * <p>
     * Must succeed
     */
    @Test
    public void eidasToLightExactMixedLowSubstantialHigh() {
        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssurance.build(NON_NOTIFIED_LOA_ALPHA),
                LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_LOW),
                LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_HIGH),
                LevelOfAssurance.build(NON_NOTIFIED_LOA_BETA)
        ).build();
        ILightRequest iLightRequest = eidasAuthenticationRequest.getLightRequest();

        assertEqualsLoaList(eidasAuthenticationRequest,
                LEVEL_OF_ASSURANCE_NOTIFIED_LOW,
                NON_NOTIFIED_LOA_ALPHA,
                NON_NOTIFIED_LOA_BETA,
                LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL,
                LEVEL_OF_ASSURANCE_NOTIFIED_HIGH);

        assertNrNotifiedLoa(iLightRequest, 1);
        assertEqualsLoaList(iLightRequest,
                LEVEL_OF_ASSURANCE_NOTIFIED_LOW,
                NON_NOTIFIED_LOA_ALPHA,
                NON_NOTIFIED_LOA_BETA);
    }

    /**
     * Test method for {@link EidasAuthenticationRequest#builder()}
     * when Light request contains non-notified levels of assurance,
     * one notified level of assurance of 'low'
     * one notified level of assurance of 'substantial'
     * and one notified level of assurance of 'high'
     * <p>
     * Must succeed
     */
    @Test
    public void lightToEidasExactMixedLowSubstantialHigh() {
        LightRequest lightRequest = buildLightRequest(
                        NON_NOTIFIED_LOA_ALPHA,
                        LEVEL_OF_ASSURANCE_NOTIFIED_LOW,
                        LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL,
                        LEVEL_OF_ASSURANCE_NOTIFIED_HIGH,
                        NON_NOTIFIED_LOA_BETA
                );
        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(lightRequest).build();

        assertNrNotifiedLoa(lightRequest, 1);
        assertEqualsLoaList(lightRequest,
                LEVEL_OF_ASSURANCE_NOTIFIED_LOW,
                NON_NOTIFIED_LOA_ALPHA,
                NON_NOTIFIED_LOA_BETA);

        assertEqualsLoaList(eidasAuthenticationRequest,
                LEVEL_OF_ASSURANCE_NOTIFIED_LOW,
                NON_NOTIFIED_LOA_ALPHA,
                NON_NOTIFIED_LOA_BETA,
                LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL,
                LEVEL_OF_ASSURANCE_NOTIFIED_HIGH);
    }

}
