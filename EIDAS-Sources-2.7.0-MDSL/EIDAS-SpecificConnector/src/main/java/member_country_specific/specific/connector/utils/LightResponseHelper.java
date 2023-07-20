/*
 * Copyright (c) 2022 by European Commission
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

package member_country_specific.specific.connector.utils;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.specificcommunication.SpecificCommunicationApplicationContextProvider;
import member_country_specific.specific.connector.SpecificConnectorBeanNames;
import member_country_specific.specific.connector.communication.SpecificConnector;

import java.util.UUID;

/**
 * Helper class for @{ILightResponse}.
 */
public class LightResponseHelper {

    private LightResponseHelper() {
    }

    private static SpecificConnector getSpecificProxyService() {
        return (SpecificConnector) SpecificCommunicationApplicationContextProvider.getApplicationContext()
                .getBean(SpecificConnectorBeanNames.SPECIFIC_CONNECTOR_SERVICE.toString());
    }

    public static ILightResponse createILightResponseFailure(String inResponseTo,
                                                             EIDASStatusCode eidasStatusCode,
                                                             EIDASSubStatusCode eidasSubStatusCode,
                                                             String statusMessage) {

        final ResponseStatus responseStatus = ResponseStatus.builder()
                .statusCode(eidasStatusCode.toString())
                .subStatusCode(eidasSubStatusCode.toString())
                .statusMessage(statusMessage)
                .failure(true)
                .build();

        return new LightResponse.Builder()
                .id(UUID.randomUUID().toString())
                .inResponseToId(inResponseTo)
                .issuer(getSpecificProxyService().getIssuerName())//TODO to be removed when the issuer if issuer is removed from light response
                .status(responseStatus).build();
    }

}
