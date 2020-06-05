package eu.eidas.auth.commons.light.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import eu.eidas.auth.commons.light.IResponseStatus;

/**
 * Concrete class implementing the {@link IResponseStatus} interface.
 * <p>
 * This class uses the Builder Pattern and is immutable thus thread-safe.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
@Immutable
@ThreadSafe
public final class ResponseStatus extends AbstractResponseStatus implements Serializable {

    /**
     * Builder pattern for the {@link ResponseStatus} class.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     */
    @NotThreadSafe
    @SuppressWarnings("ParameterHidesMemberVariable")
    public static final class Builder extends AbstractResponseStatus.AbstractBuilder<Builder, ResponseStatus> {

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            super(copy);
        }

        public Builder(@Nonnull IResponseStatus copy) {
            super(copy);
        }

        @Override
        protected void validate() throws IllegalArgumentException {
        }

        @Nonnull
        @Override
        protected ResponseStatus newInstance() {
            return new ResponseStatus(this);
        }
    }

    private static final long serialVersionUID = -3301069405898822193L;

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull IResponseStatus copy) {
        return new Builder(copy);
    }

    private ResponseStatus(@Nonnull Builder builder) {
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
