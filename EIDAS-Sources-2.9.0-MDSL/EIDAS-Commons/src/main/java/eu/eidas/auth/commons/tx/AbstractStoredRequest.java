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
package eu.eidas.auth.commons.tx;

import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;

/**
 * An object stored in a cache and to be remembered until the associated asynchronous response is sent back.
 *
 * @param <R> the type stored by this implementation, must be {@link Serializable}.
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
public abstract class AbstractStoredRequest<R extends Serializable> implements Serializable {

    /**
     * Abstract Builder pattern with self-bounding generics for {@link AbstractStoredRequest} subtypes.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p>
     * See Self-bounding generics:</p>
     * <p>http://www.artima.com/weblogs/viewpost.jsp?thread=136394</p>
     * <p>http://www.artima.com/forums/flat.jsp?forum=106&amp;thread=136394</p>
     * <p>http://en.wikipedia.org/wiki/Covariance_and_contravariance</p>
     *
     * @param <B> the type of the Builder itself
     * @param <T> the type being built by the {@link #build()} method of this builder.
     * @param <R> the actual subtype of {@link ILightRequest} in use by this implementation.
     */
    @SuppressWarnings({"ParameterHidesMemberVariable", "unchecked"})
    @NotThreadSafe
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T, R>, T extends AbstractStoredRequest<R>, R extends Serializable> {

        private String remoteIpAddress;

        private R request;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractStoredRequest<R> copy) {
            Preconditions.checkNotNull(copy, "copy");
            remoteIpAddress = copy.remoteIpAddress;
            request = copy.request;
        }

        protected AbstractBuilder(@Nonnull AbstractBuilder<?, ?, R> copy) {
            Preconditions.checkNotNull(copy, "copy");
            remoteIpAddress = copy.remoteIpAddress;
            request = copy.request;
        }

        @Nonnull
        public final B remoteIpAddress(final String remoteIpAddress) {
            this.remoteIpAddress = remoteIpAddress;
            return (B) this;
        }

        @Nonnull
        public final B request(final R request) {
            this.request = request;
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
            Preconditions.checkNotBlank(remoteIpAddress, "remoteIpAddress");
            Preconditions.checkNotNull(request, "request");
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
         * AbstractStoredRequest}).
         * <p>
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
         * <p>
         * The typical implementation simply consists in writing <br>{@code return new MySubType(this);}
         *
         * @return a new {@code T} instance based on this Builder instance.
         */
        @Nonnull
        protected abstract T newInstance();
    }

    @Nonnull
    private final String remoteIpAddress;

    @Nonnull
    private final R request;

    protected AbstractStoredRequest(@Nonnull AbstractBuilder<?, ?, R> builder) {
        remoteIpAddress = builder.remoteIpAddress;
        request = builder.request;
    }

    @Nonnull
    public final String getRemoteIpAddress() {
        return remoteIpAddress;
    }

    @Nonnull
    public final R getRequest() {
        return request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractStoredRequest<R> that = (AbstractStoredRequest<R>) o;

        if (!remoteIpAddress.equals(that.remoteIpAddress)) { // remoteIpAddress cannot be null
            return false;
        }
        return request.equals(that.request);                // request cannot be null
    }

    @Override
    public int hashCode() {
        int result = remoteIpAddress.hashCode();    // remoteIpAddress cannot be null
        result = 31 * result + (request.hashCode());            // request cannot be null
        return result;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(getClass().getSimpleName()).append("{")).append('}').toString();
    }

    protected StringBuilder toString(@Nonnull StringBuilder stringBuilder) {
        return toString(stringBuilder, this);
    }

    public static StringBuilder toString(@Nonnull StringBuilder stringBuilder,
                                         @Nonnull AbstractStoredRequest<?> request) {
        Preconditions.checkNotNull(stringBuilder, "stringBuilder");
        Preconditions.checkNotNull(request, "request");
        return stringBuilder.append("remoteIpAddress='")
                .append(request.getRemoteIpAddress())
                .append('\'')
                .append(", request='")
                .append(request.getRequest())
                .append('\'');
    }

}
