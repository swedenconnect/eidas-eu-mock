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

package member_country_specific.specific.connector;

import javax.annotation.Nonnull;


/**
 * This enum class contains view names.
 *
 * @since 2.0
 */
public enum SpecificConnectorViewNames {

    COLLEAGUE_RESPONSE_REDIRECT("/internal/colleagueResponseRedirect.jsp"),

    TOKEN_REDIRECT("/internal/tokenRedirectToConnector.jsp"),

    ;

    /**
     * constant name.
     */
    @Nonnull
    private final transient String name;

    /**
     * Constructor
     *
     * @param nameValue name of the bean
     */
    SpecificConnectorViewNames(@Nonnull String nameValue) {
        name = nameValue;
    }

    @Nonnull
    @Override
    public String toString() {
        return name;
    }

}
