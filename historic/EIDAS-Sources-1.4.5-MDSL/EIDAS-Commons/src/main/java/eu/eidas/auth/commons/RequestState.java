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
