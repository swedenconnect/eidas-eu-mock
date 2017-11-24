package eu.eidas.auth.commons.light.impl;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.util.Preconditions;

/**
 * Abstract class for implementations of the {@link IResponseStatus} interface.
 * <p>
 * This class uses the Builder Pattern.
 * <p>
 * Implementors of the {@link IResponseStatus} should extend this class and its Builder.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
public abstract class AbstractResponseStatus implements IResponseStatus, Serializable {

    /**
     * Abstract Builder pattern with self-bounding generics for {@link IResponseStatus} subtypes.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     * See Self-bounding generics:<p/> http://www.artima.com/weblogs/viewpost.jsp?thread=136394<p/>
     * http://www.artima.com/forums/flat.jsp?forum=106&thread=136394<p/> http://en.wikipedia.org/wiki/Covariance_and_contravariance<p/>
     *
     * @param B the type of the Builder itself
     * @param T the type being built by the {@link #build()} method of this builder.
     */
    @NotThreadSafe
    @SuppressWarnings({"ParameterHidesMemberVariable", "unchecked"})
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends IResponseStatus> {

        private boolean failure;

        private String statusCode;

        private String statusMessage;

        private String subStatusCode;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractBuilder<?, ?> copy) {
            Preconditions.checkNotNull(copy, "copy");
            failure = copy.failure;
            statusCode = copy.statusCode;
            statusMessage = copy.statusMessage;
            subStatusCode = copy.subStatusCode;
        }

        protected AbstractBuilder(@Nonnull IResponseStatus copy) {
            Preconditions.checkNotNull(copy, "copy");
            failure = copy.isFailure();
            statusCode = copy.getStatusCode();
            statusMessage = copy.getStatusMessage();
            subStatusCode = copy.getSubStatusCode();
        }

        @Nonnull
        public final B failure(boolean failure) {
            this.failure = failure;
            return (B) this;
        }

        @Nonnull
        public final B statusCode(String statusCode) {
            this.statusCode = statusCode;
            return (B) this;
        }

        @Nonnull
        public final B statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return (B) this;
        }

        @Nonnull
        public final B subStatusCode(String subStatusCode) {
            this.subStatusCode = subStatusCode;
            return (B) this;
        }

        /**
         * Validates the internal state of this Builder before allowing to create new instances of the built type {@code
         * T}.
         *
         * @throws IllegalArgumentException if the builder is not in a legal state allowing to proceed with the creation
         * of a {@code T} instance.
         */
        private void internalValidate() throws IllegalArgumentException {
            Preconditions.checkNotBlank(statusCode, "statusCode");
            validate();
        }

        /**
         * Validates the state of this Builder before allowing to create new instances of the built type {@code T}.
         *
         * @throws IllegalArgumentException if the builder is not in a legal state allowing to proceed with the creation
         * of a {@code T} instance.
         */
        protected abstract void validate() throws IllegalArgumentException;

        /**
         * Builds a new {@code T} instance based on this Builder instance (Builder pattern for {@link
         * IResponseStatus}).
         * <p/>
         * The {@link #validate()} is always called before creating a new instance through a call to the {@link
         * #newInstance()} method.
         *
         * @return a new {@code T} instance based on this Builder instance.
         */
        @Nonnull
        public final T build() {
            internalValidate();
            return newInstance();
        }

        /**
         * Method to be implemented by subtypes to create the right type {@code T} of instances.
         * <p/>
         * The typical implementation simply consists in writing <br/>{@code return new MySubType(this);}
         *
         * @return a new {@code T} instance based on this Builder instance.
         */
        @Nonnull
        protected abstract T newInstance();
    }

    private static final long serialVersionUID = -7069697598698947019L;

    /**
     * @serial
     */
    private final boolean failure;

    /**
     * @serial
     */
    @Nonnull
    private final String statusCode;

    /**
     * @serial
     */
    @Nullable
    private final String statusMessage;

    /**
     * @serial
     */
    @Nullable
    private final String subStatusCode;

    protected AbstractResponseStatus(@Nonnull AbstractBuilder<?, ?> builder) {
        failure = builder.failure;
        statusCode = builder.statusCode;
        statusMessage = builder.statusMessage;
        subStatusCode = builder.subStatusCode;
    }

    @Nonnull
    @Override
    public final String getStatusCode() {
        return statusCode;
    }

    @Nullable
    @Override
    public final String getStatusMessage() {
        return statusMessage;
    }

    @Nullable
    @Override
    public final String getSubStatusCode() {
        return subStatusCode;
    }

    @Override
    public final boolean isFailure() {
        return failure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractResponseStatus that = (AbstractResponseStatus) o;

        if (failure != that.failure) {
            return false;
        }
        if (!statusCode.equals(that.statusCode)) {
            return false;
        }
        if (statusMessage != null ? !statusMessage.equals(that.statusMessage) : that.statusMessage != null) {
            return false;
        }
        return subStatusCode != null ? subStatusCode.equals(that.subStatusCode) : that.subStatusCode == null;

    }

    @Override
    public int hashCode() {
        int result = (failure ? 1 : 0);
        result = 31 * result + (statusCode.hashCode());
        result = 31 * result + (statusMessage != null ? statusMessage.hashCode() : 0);
        result = 31 * result + (subStatusCode != null ? subStatusCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(getClass().getSimpleName()).append("{")).append('}').toString();
    }

    protected StringBuilder toString(@Nonnull StringBuilder stringBuilder) {
        return toString(stringBuilder, this);
    }

    public static StringBuilder toString(@Nonnull StringBuilder stringBuilder, @Nonnull IResponseStatus status) {
        Preconditions.checkNotNull(stringBuilder, "stringBuilder");
        Preconditions.checkNotNull(status, "status");
        return stringBuilder.append("failure='")
                .append(status.isFailure())
                .append('\'')
                .append(", ")
                .append("statusCode='")
                .append(status.getStatusCode())
                .append('\'')
                .append(", ")
                .append("statusMessage='")
                .append(status.getStatusMessage())
                .append('\'')
                .append(", ")
                .append("subStatusCode='")
                .append(status.getSubStatusCode())
                .append('\'');
    }
}
