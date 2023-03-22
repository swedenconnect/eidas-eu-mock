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
import eu.eidas.auth.commons.light.LevelOfAssuranceType;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import org.junit.Assert;
import org.junit.Test;

import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_LOW;
import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL;
import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_HIGH;

/**
 * LightToEidasMinimumTest
 * Class to test {@link EidasAuthenticationRequest} as it decorates {@link LightRequest}
 * on the interface {@link ILightRequest} for comparison MINIMUM cases
 */
public class LightToEidasMinimumTest extends LightToEidasTest {

    /**
     * Test method for {@link EidasAuthenticationRequest#getLightRequest()}
     * when EidasAuthenticationRequest contains notified level of assurance 'low'
     * <p>
     * Must succeed
     */
    @Test
    public void eidasToLightMinimumNotifiedLow() {
        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(
                LevelOfAssuranceComparison.MINIMUM,
                LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_LOW)
        ).build();

        ILightRequest iLightRequest = eidasAuthenticationRequest.getLightRequest();

        Assert.assertEquals(LevelOfAssuranceComparison.MINIMUM, eidasAuthenticationRequest.getLevelOfAssuranceComparison());
        assertEqualsLoaList(eidasAuthenticationRequest, LEVEL_OF_ASSURANCE_NOTIFIED_LOW);

        Assert.assertEquals(1, iLightRequest.getLevelsOfAssurance().stream().filter(x -> LevelOfAssuranceType.NOTIFIED.stringValue().equals(x.getType())).count());
        assertEqualsLoaList(iLightRequest, LEVEL_OF_ASSURANCE_NOTIFIED_LOW);
    }

    /**
     * Test method for {@link EidasAuthenticationRequest#builder()}
     * when Light request contains notified level of assurance 'low'
     * <p>
     * Must succeed
     */
    @Test
    public void lightToEidasMinimumNotifiedLow() {
        LightRequest lightRequest = buildLightRequest(LEVEL_OF_ASSURANCE_NOTIFIED_LOW);
        assertNrNotifiedLoa(lightRequest, 1);
        assertEqualsLoaList(lightRequest, LEVEL_OF_ASSURANCE_NOTIFIED_LOW);

        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(lightRequest).build();

        Assert.assertEquals(LevelOfAssuranceComparison.MINIMUM, eidasAuthenticationRequest.getLevelOfAssuranceComparison());
        assertEqualsLoaList(eidasAuthenticationRequest, LEVEL_OF_ASSURANCE_NOTIFIED_LOW);
    }

    /**
     * Test method for {@link EidasAuthenticationRequest#getLightRequest()}
     * when EidasAuthenticationRequest contains notified level of assurance 'substantial'
     * <p>
     * Must succeed
     */
    @Test
    public void eidasToLightMinimumNotifiedSubstantial() {
        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(
                LevelOfAssuranceComparison.MINIMUM,
                LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL)
        ).build();
        assertEqualsLoaList(eidasAuthenticationRequest, LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL);

        ILightRequest iLightRequest = eidasAuthenticationRequest.getLightRequest();

        assertNrNotifiedLoa(iLightRequest, 1);
        assertEqualsLoaList(iLightRequest, LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL);
    }

    /**
     * Test method for {@link EidasAuthenticationRequest#builder()}
     * when Light request contains notified level of assurance 'substantial'
     * <p>
     * Must succeed
     */
    @Test
    public void lightToEidasMinimumNotifiedSubstantial() {
        LightRequest lightRequest = buildLightRequest(LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL);
        assertNrNotifiedLoa(lightRequest, 1);
        assertEqualsLoaList(lightRequest, LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL);

        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(lightRequest).build();

        Assert.assertEquals(LevelOfAssuranceComparison.MINIMUM, eidasAuthenticationRequest.getLevelOfAssuranceComparison());
        assertEqualsLoaList(eidasAuthenticationRequest, LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL);
    }

    /**
     * Test method for {@link EidasAuthenticationRequest#getLightRequest()}
     * when EidasAuthenticationRequest contains notified level of assurance 'high'
     * <p>
     * Must succeed
     */
    @Test
    public void eidasToLightMinimumNotifiedHigh() {
        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(
                LevelOfAssuranceComparison.MINIMUM,
                LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_HIGH)
        ).build();
        assertEqualsLoaList(eidasAuthenticationRequest, LEVEL_OF_ASSURANCE_NOTIFIED_HIGH);

        ILightRequest iLightRequest = eidasAuthenticationRequest.getLightRequest();

        assertNrNotifiedLoa(iLightRequest, 1);
        assertEqualsLoaList(iLightRequest, LEVEL_OF_ASSURANCE_NOTIFIED_HIGH);
    }

    /**
     * Test method for {@link EidasAuthenticationRequest#builder()}
     * when Light request contains notified level of assurance 'high'
     * <p>
     * Must succeed
     */
    @Test
    public void lightToEidasMinimumNotifiedHigh() {
        LightRequest lightRequest = buildLightRequest(LEVEL_OF_ASSURANCE_NOTIFIED_HIGH);
        assertNrNotifiedLoa(lightRequest, 1);
        assertEqualsLoaList(lightRequest, LEVEL_OF_ASSURANCE_NOTIFIED_HIGH);

        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder(lightRequest).build();

        Assert.assertEquals(LevelOfAssuranceComparison.MINIMUM, eidasAuthenticationRequest.getLevelOfAssuranceComparison());
        assertEqualsLoaList(eidasAuthenticationRequest, LEVEL_OF_ASSURANCE_NOTIFIED_HIGH);
    }
}
