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

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;

import java.util.UUID;

public class LightResponseTestHelper {

    private LightResponseTestHelper() {}

    public static ILightResponse createLightResponse(String inResponseTo, String statusCode, String subject, String subjectNameIdFormat) {

        final ResponseStatus responseStatus = ResponseStatus.builder()
                .statusCode(statusCode)
                .build();

        return new LightResponse.Builder()
                .id(UUID.randomUUID().toString())
                .inResponseToId(inResponseTo)
                .subject(subject)
                .subjectNameIdFormat(subjectNameIdFormat)
                .issuer("issuerName")//TODO to be removed when the issuer if issuer is removed from light response
                .status(responseStatus).build();
    }

    public static ILightResponse createDefaultLightResponse() {
        return createLightResponse("inResponseTo", EIDASStatusCode.SUCCESS_URI.toString(), "subject", "subjectNameIdFormat");
    }
}
