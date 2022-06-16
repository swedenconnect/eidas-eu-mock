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

import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.light.impl.LevelOfAssuranceUtils;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import org.junit.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LightToEidasTest
 * Class to test {@link EidasAuthenticationRequest} as it decorates {@link LightRequest}
 * on the interface {@link ILightRequest}
 */
public abstract class LightToEidasTest {

    LightRequest buildLightRequest(String... loa) {
        List<LevelOfAssurance> levelOfAssuranceList = Arrays.stream(loa).map(LevelOfAssurance::build).collect(Collectors.toList());
        return new LightRequest.Builder()
                .id("ID")
                .issuer("ISSUER")
                .citizenCountryCode("CITIZEN_COUNTRY_CODE")
                .levelsOfAssurance(levelOfAssuranceList)
                .build();
    }

    EidasAuthenticationRequest.Builder getEidasAuthenticationRequestBuilder(LevelOfAssuranceComparison comparison, ILevelOfAssurance... loas) {
        return EidasAuthenticationRequest.builder()
                .id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                .destination("REQUEST_DESTINATION")
                .issuer("REQUEST_ISSUER")
                .citizenCountryCode("PT")
                .assertionConsumerServiceURL("ASSERTION_URL")
                .levelsOfAssurance(Arrays.asList(loas))
                .levelOfAssuranceComparison(comparison);
    }

    EidasAuthenticationRequest.Builder getEidasAuthenticationRequestBuilder(ILightRequest iLightRequest) {
        return EidasAuthenticationRequest.builder()
                .id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                .destination("REQUEST_DESTINATION")
                .issuer("REQUEST_ISSUER")
                .citizenCountryCode("PT")
                .assertionConsumerServiceURL("ASSERTION_URL")
                .lightRequest(iLightRequest);
    }

    void assertEqualsLoaList(ILightRequest anyRequest, String... expectedLoas) {
        Assert.assertEquals(expectedLoas.length, anyRequest.getLevelsOfAssurance().size());
        assertListEquals(
                anyRequest.getLevelsOfAssurance(),
                Arrays.stream(expectedLoas).map(LevelOfAssurance::build).collect(Collectors.toList()));
    }

    void assertNrNotifiedLoa(ILightRequest anyRequest, int notifiedLevelsOfAssurance) {
        Assert.assertEquals(notifiedLevelsOfAssurance, filterOnlyNotified(anyRequest.getLevelsOfAssurance()).size());
    }

    private void assertListEquals(List expected, List actual) {
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    List<ILevelOfAssurance> filterOnlyNotified(List<ILevelOfAssurance> loa) {
        return loa.stream().filter(LevelOfAssuranceUtils::isNotified).collect(Collectors.toList());
    }
}