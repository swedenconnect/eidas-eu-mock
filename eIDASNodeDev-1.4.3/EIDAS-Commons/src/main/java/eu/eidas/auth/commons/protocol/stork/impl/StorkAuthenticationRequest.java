package eu.eidas.auth.commons.protocol.stork.impl;

import java.io.ObjectStreamException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.stork.IStorkAuthenticationRequest;

/**
 * Concrete implementation of the {@link IAuthenticationRequest} interface which adds support for the STORK protocol.
 *
 * @since 1.1
 */
@Immutable
@ThreadSafe
public final class StorkAuthenticationRequest extends AbstractStorkAuthenticationRequest {

    /**
     * Builder pattern for the {@link StorkAuthenticationRequest} class.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder extends AbstractBuilder<Builder, StorkAuthenticationRequest> {

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            super(copy);
        }

        public Builder(@Nonnull IStorkAuthenticationRequest copy) {
            super(copy);
        }

        @Override
        protected void validateOtherFields() throws IllegalArgumentException {
        }

        @Nonnull
        @Override
        protected StorkAuthenticationRequest newInstance() {
            return new StorkAuthenticationRequest(this);
        }
    }

    private static final long serialVersionUID = -9010159843217022156L;

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull IStorkAuthenticationRequest copy) {
        return new Builder(copy);
    }

    private StorkAuthenticationRequest(@Nonnull Builder builder) {
        super(builder);
    }

    /**
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     * <p/>
     * Used upon de-serialization, not serialization.
     * <p/>
     * The state of this class is transformed back into the class it represents.
     */
    private Object readResolve() throws ObjectStreamException {
        return new Builder(this).build();
    }
}
