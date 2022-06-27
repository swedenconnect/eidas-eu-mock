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
package eu.eidas.auth.commons;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * RequestState
 *
 * @since 1.1
 */
public interface RequestState {

    @Nullable
    String getErrorCode();

    @Nullable
    String getErrorMessage();

    @Nullable
    String getErrorSubcode();

    @Nullable
    String getInResponseTo();

    @Nullable
    String getIssuer();

    /**
     * @return the first level of assurance or null.
     * @deprecated since 2.5 should use {@link #getLevelsOfAssurance()}
     */
    @Nullable
    @Deprecated
    String getLevelOfAssurance();

    List<String> getLevelsOfAssurance();

    @Nullable
    String getProviderName();

    @Nullable
    String getQaa();

    @Nullable
    String getServiceUrl();

    @Nullable
    String getSpId();

    void setErrorCode(@Nonnull String errorCode);

    void setErrorMessage(@Nonnull String errorMessage);

    void setErrorSubcode(@Nonnull String errorSubcode);

    void setInResponseTo(@Nonnull String inResponseTo);

    void setIssuer(@Nonnull String issuer);

    /**
     * Set the levels of assurance to be a list of one element containing the given level of assurance.
     * @param levelOfAssurance the level of assurance
     * @deprecated since 2.5 should use {@link #setLevelsOfAssurance(List)}
     */
    @Deprecated
    void setLevelOfAssurance(@Nonnull String levelOfAssurance);

    void setLevelsOfAssurance(@Nonnull List<String> levelsOfAssurance);

    void setProviderName(@Nonnull String providerName);

    void setQaa(@Nonnull String qaa);

    void setServiceUrl(@Nonnull String assertionConsumerServiceUrl);

    void setSpId(@Nonnull String spId);
}
