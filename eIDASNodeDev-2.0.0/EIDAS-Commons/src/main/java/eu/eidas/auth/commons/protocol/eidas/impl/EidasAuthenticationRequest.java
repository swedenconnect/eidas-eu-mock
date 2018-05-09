package eu.eidas.auth.commons.protocol.eidas.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;

/**
 * Concrete implementation of the {@link IAuthenticationRequest} interface which adds support for the eIDAS protocol.
 *
 * @since 1.1
 */
@Immutable
@ThreadSafe
public final class EidasAuthenticationRequest extends AbstractEidasAuthenticationRequest implements Serializable {

    /**
     * Builder pattern for the {@link EidasAuthenticationRequest} class.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder
            extends AbstractEidasAuthenticationRequest.AbstractBuilder<Builder, EidasAuthenticationRequest> {

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            super(copy);
        }

        public Builder(@Nonnull IEidasAuthenticationRequest copy) {
            super(copy);
        }

        @Override
        protected void validateOtherFields() throws IllegalArgumentException {
        }

        @Nonnull
        @Override
        protected EidasAuthenticationRequest newInstance() {
            return new EidasAuthenticationRequest(this);
        }
    }

    private static final long serialVersionUID = -7245088584610722179L;

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull IEidasAuthenticationRequest copy) {
        return new Builder(copy);
    }

    private EidasAuthenticationRequest(@Nonnull Builder builder) {
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
