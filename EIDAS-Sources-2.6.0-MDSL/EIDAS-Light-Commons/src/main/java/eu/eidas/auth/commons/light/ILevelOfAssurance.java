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

package eu.eidas.auth.commons.light;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Interface representing the Level of Assurance.
 * <p>
 * The Level of Assurance must contain an URI value and may have type.
 * Type values are either "notified" or "nonNotified".
 * The type default value is "notified"
 * <p>
 * When the type is "notified", the value should start with the prefix "http://eidas.europa.eu/LoA/"
 * If the type is "nonNotified", the value must start with a different prefix.
 *
 * @since 2.5
 */
public interface ILevelOfAssurance extends Serializable {

    String EIDAS_LOA_PREFIX = "http://eidas.europa.eu/LoA/";
    String EIDAS_LOA_LOW = EIDAS_LOA_PREFIX + "low";
    String EIDAS_LOA_SUBSTANTIAL = EIDAS_LOA_PREFIX + "substantial";
    String EIDAS_LOA_HIGH = EIDAS_LOA_PREFIX + "high";

    /**
     * Returns the Level of Assurance's value.
     *
     * @return the Level of Assurance's value.
     */
    @Nonnull
    String getValue();

    /**
     * Returns the Level of Assurance's type
     * Only valid types are "notified" or "nonNotified"
     *
     * @return the Level of Assurance's type.
     */
    @Nullable
    String getType();
}
