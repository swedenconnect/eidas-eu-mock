package eu.eidas.auth.commons.light.impl;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.util.Preconditions;

/**
 * Abstract class for implementations of the {@link ILightResponse} interface.
 * <p>
 * This class uses the Builder Pattern.
 * <p>
 * Implementors of the {@link ILightResponse} should extend this class and its Builder.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
public abstract class AbstractLightResponse implements ILightResponse, Serializable {

    /**
     * Abstract Builder pattern with self-bounding generics for {@link ILightResponse} subtypes.
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
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends ILightResponse> {

        private String id;

        private String issuer;

        private String ipAddress;

        private IResponseStatus status;

        private String inResponseToId;

        private String levelOfAssurance;

        private ImmutableAttributeMap attributes;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractBuilder<?, ?> copy) {
            Preconditions.checkNotNull(copy, "copy");
            id = copy.id;
            issuer = copy.issuer;
            ipAddress = copy.ipAddress;
            status = copy.status;
            inResponseToId = copy.inResponseToId;
            levelOfAssurance = copy.levelOfAssurance;
            attributes = copy.attributes;
        }

        protected AbstractBuilder(@Nonnull ILightResponse copy) {
            Preconditions.checkNotNull(copy, "copy");
            id = copy.getId();
            issuer = copy.getIssuer();
            ipAddress = copy.getIPAddress();
            status = copy.getStatus();
            inResponseToId = copy.getInResponseToId();
            levelOfAssurance = copy.getLevelOfAssurance();
            attributes = copy.getAttributes();
        }

        @Nonnull
        public final B attributes(ImmutableAttributeMap attributes) {
            this.attributes = attributes;
            return (B) this;
        }

        @Nonnull
        public final B status(IResponseStatus status) {
            this.status = status;
            return (B) this;
        }

        @Nonnull
        public final B id(String id) {
            this.id = id;
            return (B) this;
        }

        @Nonnull
        public final B inResponseToId(String inResponseToId) {
            this.inResponseToId = inResponseToId;
            return (B) this;
        }

        @Nonnull
        public final B ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return (B) this;
        }

        @Nonnull
        public final B issuer(String issuer) {
            this.issuer = issuer;
            return (B) this;
        }

        @Nonnull
        public final B levelOfAssurance(String levelOfAssurance) {
            this.levelOfAssurance = levelOfAssurance;
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
            Preconditions.checkNotBlank(id, "id");
            Preconditions.checkNotBlank(issuer, "issuer");
            Preconditions.checkNotNull(status, "status");
            Preconditions.checkNotBlank(inResponseToId, "inResponseToId");
            if (null == attributes) {
                attributes = ImmutableAttributeMap.of();
            }
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
         * Builds a new {@code T} instance based on this Builder instance (Builder pattern for {@link ILightResponse}).
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

    private static final long serialVersionUID = 2377174927794266976L;

    /**
     * @serial
     */
    @Nonnull
    private final String id;

    /**
     * @serial
     */
    @Nonnull
    private final String issuer;

    /**
     * @serial
     */
    @Nullable
    private final String ipAddress;

    /**
     * @serial
     */
    @Nonnull
    private final IResponseStatus status;

    /**
     * @serial
     */
    @Nonnull
    private final String inResponseToId;

    /**
     * @serial
     */
    @Nullable
    private final String levelOfAssurance;

    /**
     * @serial
     */
    @Nonnull
    private final ImmutableAttributeMap attributes;

    protected AbstractLightResponse(@Nonnull AbstractBuilder<?, ?> builder) {
        id = builder.id;
        issuer = builder.issuer;
        ipAddress = builder.ipAddress;
        status = builder.status;
        inResponseToId = builder.inResponseToId;
        levelOfAssurance = builder.levelOfAssurance;
        attributes = builder.attributes;
    }

    @Nonnull
    @Override
    public final ImmutableAttributeMap getAttributes() {
        return attributes;
    }

    @Nullable
    @Override
    public final String getIPAddress() {
        return ipAddress;
    }

    @Nonnull
    @Override
    public final String getId() {
        return id;
    }

    @Nonnull
    @Override
    public final String getInResponseToId() {
        return inResponseToId;
    }

    @Nonnull
    @Override
    public final String getIssuer() {
        return issuer;
    }

    @Nullable
    @Override
    public final String getLevelOfAssurance() {
        return levelOfAssurance;
    }

    @Nonnull
    @Override
    public final IResponseStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractLightResponse that = (AbstractLightResponse) o;

        if (!id.equals(that.id)) {
            return false;
        }
        if (!issuer.equals(that.issuer)) {
            return false;
        }
        if (ipAddress != null ? !ipAddress.equals(that.ipAddress) : that.ipAddress != null) {
            return false;
        }
        if (!status.equals(that.status)) {
            return false;
        }
        if (!inResponseToId.equals(that.inResponseToId)) {
            return false;
        }
        if (levelOfAssurance != null ? !levelOfAssurance.equals(that.levelOfAssurance)
                                     : that.levelOfAssurance != null) {
            return false;
        }
        return attributes.equals(that.attributes);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (issuer.hashCode());
        result = 31 * result + (null != ipAddress ? ipAddress.hashCode() : 0);
        result = 31 * result + (status.hashCode());
        result = 31 * result + (inResponseToId.hashCode());
        result = 31 * result + (null != levelOfAssurance ? levelOfAssurance.hashCode() : 0);
        result = 31 * result + (attributes.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(getClass().getSimpleName()).append("{")).append('}').toString();
    }

    protected StringBuilder toString(@Nonnull StringBuilder stringBuilder) {
        return toString(stringBuilder, this);
    }

    public static StringBuilder toString(@Nonnull StringBuilder stringBuilder, @Nonnull ILightResponse response) {
        Preconditions.checkNotNull(stringBuilder, "stringBuilder");
        Preconditions.checkNotNull(response, "response");
        return stringBuilder.append("id=\'")
                .append(response.getId())
                .append('\'')
                .append(", issuer='")
                .append(response.getIssuer())
                .append('\'')
                .append(", status='")
                .append(response.getStatus())
                .append('\'')
                .append(", ipAddress='")
                .append(response.getIPAddress())
                .append('\'')
                .append(", inResponseToId='")
                .append(response.getInResponseToId())
                .append('\'')
                .append(", levelOfAssurance='")
                .append(response.getLevelOfAssurance())
                .append('\'')
                .append(", attributes='")
                .append(response.getAttributes())
                .append('\'');
    }
}
