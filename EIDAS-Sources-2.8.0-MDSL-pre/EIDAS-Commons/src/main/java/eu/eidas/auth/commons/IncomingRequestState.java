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
package eu.eidas.auth.commons;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * IncomingRequestState
 *
 * @since 1.1
 */
final class IncomingRequestState implements RequestState {

    private static final class AtomicState {

        private final String errorCode;

        private final String errorMessage;

        private final String errorSubcode;

        private final String inResponseTo;

        private final String issuer;

        private final List<String> levelsOfAssurance;

        private final String providerName;

        private final String serviceUrl;

        private final String spId;

        private AtomicState(@Nonnull Builder builder) {
            errorCode = builder.errorCode;
            errorMessage = builder.errorMessage;
            errorSubcode = builder.errorSubcode;
            inResponseTo = builder.inResponseTo;
            issuer = builder.issuer;
            levelsOfAssurance = builder.levelsOfAssurance;
            providerName = builder.providerName;
            serviceUrl = builder.serviceUrl;
            spId = builder.spId;
        }
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    private static final class Builder {

        private String errorCode;

        private String errorMessage;

        private String errorSubcode;

        private String inResponseTo;

        private String issuer;

        private List<String> levelsOfAssurance;

        private String providerName;

        private String serviceUrl;

        private String spId;

        Builder() {
        }

        Builder(@Nonnull AtomicState atomicState) {
            errorCode = atomicState.errorCode;
            errorMessage = atomicState.errorMessage;
            errorSubcode = atomicState.errorSubcode;
            inResponseTo = atomicState.inResponseTo;
            issuer = atomicState.issuer;
            levelsOfAssurance = atomicState.levelsOfAssurance;
            providerName = atomicState.providerName;
            serviceUrl = atomicState.serviceUrl;
            spId = atomicState.spId;
        }

        AtomicState build() {
            return new AtomicState(this);
        }

        Builder errorCode(final String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        Builder errorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        Builder errorSubcode(final String errorSubcode) {
            this.errorSubcode = errorSubcode;
            return this;
        }

        Builder inResponseTo(final String inResponseTo) {
            this.inResponseTo = inResponseTo;
            return this;
        }

        Builder issuer(final String issuer) {
            this.issuer = issuer;
            return this;
        }

        Builder levelOfAssurance(final String levelOfAssurance) {
            this.levelsOfAssurance = Arrays.asList(levelOfAssurance);
            return this;
        }

        Builder levelsOfAssurance(final List<String> levelsOfAssurance) {
            this.levelsOfAssurance = levelsOfAssurance;
            return this;
        }

        Builder providerName(final String providerName) {
            this.providerName = providerName;
            return this;
        }

        Builder serviceUrl(final String serviceUrl) {
            this.serviceUrl = serviceUrl;
            return this;
        }

        Builder spId(final String spId) {
            this.spId = spId;
            return this;
        }

        private void validate() {
            // validation logic
        }
    }

    /**
     * A functional interface to update the state.
     */
    private interface UpdateFunction {

        /**
         * Updates the existing state to a new state.
         *
         * @param builder the current state
         * @return the new state
         */
        @Nonnull
        Builder updateBuilder(@Nonnull Builder builder);
    }

    private static final AtomicState EMPTY = new Builder().build();

    @Nonnull
    private final AtomicReference<AtomicState> stateRef = new AtomicReference<>(EMPTY);

    @Override
    @Nullable
    public String getErrorCode() {
        return getState().errorCode;
    }

    @Override
    @Nullable
    public String getErrorMessage() {
        return getState().errorMessage;
    }

    @Override
    @Nullable
    public String getErrorSubcode() {
        return getState().errorSubcode;
    }

    @Override
    @Nullable
    public String getInResponseTo() {
        return getState().inResponseTo;
    }

    @Override
    @Nullable
    public String getIssuer() {
        return getState().issuer;
    }

    @Override
    @Nullable
    public List<String> getLevelsOfAssurance() {
        return getState().levelsOfAssurance;
    }

    @Override
    @Nullable
    public String getProviderName() {
        return getState().providerName;
    }

    @Override
    @Nullable
    public String getServiceUrl() {
        return getState().serviceUrl;
    }

    @Override
    @Nullable
    public String getSpId() {
        return getState().spId;
    }

    private AtomicState getState() {
        return stateRef.get();
    }

    @Override
    public void setErrorCode(@Nonnull final String errorCode) {
        updateState(new UpdateFunction() {

            @Nonnull
            @Override
            public Builder updateBuilder(@Nonnull Builder builder) {
                return builder.errorCode(errorCode);
            }
        });
    }

    @Override
    public void setErrorMessage(@Nonnull final String errorMessage) {
        updateState(new UpdateFunction() {

            @Nonnull
            @Override
            public Builder updateBuilder(@Nonnull Builder builder) {
                return builder.errorMessage(errorMessage);
            }
        });
    }

    @Override
    public void setErrorSubcode(@Nonnull final String errorSubcode) {
        updateState(new UpdateFunction() {

            @Nonnull
            @Override
            public Builder updateBuilder(@Nonnull Builder builder) {
                return builder.errorSubcode(errorSubcode);
            }
        });
    }

    @Override
    public void setInResponseTo(@Nonnull final String inResponseTo) {
        updateState(new UpdateFunction() {

            @Nonnull
            @Override
            public Builder updateBuilder(@Nonnull Builder builder) {
                return builder.inResponseTo(inResponseTo);
            }
        });
    }

    @Override
    public void setIssuer(@Nonnull final String issuer) {
        updateState(new UpdateFunction() {

            @Nonnull
            @Override
            public Builder updateBuilder(@Nonnull Builder builder) {
                return builder.issuer(issuer);
            }
        });
    }

    @Override
    public void setLevelsOfAssurance(@Nonnull final List<String> levelsOfAssurance) {
        updateState(new UpdateFunction() {

            @Nonnull
            @Override
            public Builder updateBuilder(@Nonnull Builder builder) {
                return builder.levelsOfAssurance(levelsOfAssurance);
            }
        });
    }

    @Override
    public void setProviderName(@Nonnull final String providerName) {
        updateState(new UpdateFunction() {

            @Nonnull
            @Override
            public Builder updateBuilder(@Nonnull Builder builder) {
                return builder.providerName(providerName);
            }
        });
    }

    @Override
    public void setServiceUrl(@Nonnull final String assertionConsumerServiceUrl) {
        updateState(new UpdateFunction() {

            @Nonnull
            @Override
            public Builder updateBuilder(@Nonnull Builder builder) {
                return builder.serviceUrl(assertionConsumerServiceUrl);
            }
        });
    }

    @Override
    public void setSpId(@Nonnull final String spId) {
        updateState(new UpdateFunction() {

            @Nonnull
            @Override
            public Builder updateBuilder(@Nonnull Builder builder) {
                return builder.spId(spId);
            }
        });
    }

    @Nonnull
    private AtomicState updateState(@Nonnull UpdateFunction updateFunction) {
        while (true) {
            AtomicState previousState = getState();
            AtomicState newState = updateFunction.updateBuilder(new Builder(previousState)).build();
            // identity function?
            if (newState == previousState) {
                return previousState;
            }
            if (stateRef.compareAndSet(previousState, newState)) {
                return previousState;
            }
        }
    }
}
