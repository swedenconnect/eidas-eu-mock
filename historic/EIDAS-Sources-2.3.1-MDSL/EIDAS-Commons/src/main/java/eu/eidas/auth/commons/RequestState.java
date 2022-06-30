/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.commons;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    @Nullable
    String getLevelOfAssurance();

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

    void setLevelOfAssurance(@Nonnull String levelOfAssurance);

    void setProviderName(@Nonnull String providerName);

    void setQaa(@Nonnull String qaa);

    void setServiceUrl(@Nonnull String assertionConsumerServiceUrl);

    void setSpId(@Nonnull String spId);
}
