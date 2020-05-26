/*
 * Copyright (c) 2019 by European Commission
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
 *
 */
package eu.eidas.auth.commons.protocol.impl;

import eu.eidas.auth.commons.protocol.IAuthenticationResponse;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Concrete implementation of the {@link IAuthenticationResponse} interface.
 */
@Immutable
@ThreadSafe
public final class AuthenticationResponse extends AbstractAuthenticationResponse implements Serializable {

    /**
     * Builder pattern for the {@link AuthenticationResponse} class.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <br>
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder
            extends AbstractAuthenticationResponse.AbstractBuilder<Builder, AuthenticationResponse> {

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            super(copy);
        }

        public Builder(@Nonnull IAuthenticationResponse copy) {
            super(copy);
        }

        @Override
        protected void validate() throws IllegalArgumentException {
        }

        @Nonnull
        @Override
        protected AuthenticationResponse newInstance() {
            return new AuthenticationResponse(this);
        }
    }

    private static final long serialVersionUID = 2095012420974837449L;

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull IAuthenticationResponse copy) {
        return new Builder(copy);
    }

    private AuthenticationResponse(@Nonnull Builder builder) {
        super(builder);
    }

    /**
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     * <p>
     * Used upon de-serialization, not serialization.
     * <p>
     * The state of this class is transformed back into the class it represents.
     * @return any {@link AuthenticationResponse} object
     * @throws ObjectStreamException exception bulding the {@link AuthenticationResponse}
     */
    private Object readResolve() throws ObjectStreamException {
        return new Builder(this).build();
    }
}
