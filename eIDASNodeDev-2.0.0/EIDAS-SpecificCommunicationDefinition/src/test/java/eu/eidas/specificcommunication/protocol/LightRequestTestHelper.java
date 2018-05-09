/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.specificcommunication.protocol;

import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.impl.LightRequest;

import java.util.UUID;

public class LightRequestTestHelper {

    private LightRequestTestHelper() {}

    public static ILightRequest createLightRequest(String citizenCountry, String issuerName, String relayState, String loa) {

        final LightRequest.Builder builder = LightRequest.builder()
                .id(UUID.randomUUID().toString())
                .citizenCountryCode(citizenCountry)
                .issuer(issuerName)
                .relayState(relayState)
                .levelOfAssurance(loa);

        return builder.build();

    }

    public static ILightRequest createDefaultLightRequest() {
        return createLightRequest("citizenCountry", "issuerName", "relayState", "loa");
    }
}
