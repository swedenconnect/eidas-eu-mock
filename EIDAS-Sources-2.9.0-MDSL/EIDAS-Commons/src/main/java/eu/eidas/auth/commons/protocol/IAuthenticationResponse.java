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
package eu.eidas.auth.commons.protocol;

import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.IResponseStatus;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;

/**
 * Interface for the full Response including every protocol detail.
 *
 * @since 1.1
 */
public interface IAuthenticationResponse extends ILightResponse, IResponseStatus {

    @Nonnull
    String getAudienceRestriction();

    @Nonnull
    String getCountry();

    @Nonnull
    ZonedDateTime getNotBefore();

    @Nonnull
    ZonedDateTime getNotOnOrAfter();

    boolean isEncrypted();
}
