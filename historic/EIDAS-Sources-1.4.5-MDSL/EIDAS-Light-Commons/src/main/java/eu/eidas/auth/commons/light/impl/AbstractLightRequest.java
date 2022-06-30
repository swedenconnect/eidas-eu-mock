package eu.eidas.auth.commons.light.impl;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.util.Preconditions;

/**
 * Abstract class for implementations of the {@link ILightRequest} interface.
 * <p>
 * This class uses the Builder Pattern.
 * <p>
 * Implementors of the {@link ILightRequest} should extend this class and its Builder.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
public abstract class AbstractLightRequest implements ILightRequest, Serializable {

    /**
     * Abstract Builder pattern with self-bounding generics for {@link ILightRequest} subtypes.
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
    @SuppressWarnings({"ParameterHidesMemberVariable", "unchecked"})
    @NotThreadSafe
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends ILightRequest> {

        private String id;

        private String issuer;

        private String citizenCountryCode;

        private String levelOfAssurance;

        private String nameIdFormat;

        private String providerName;

        private String spType;

        private ImmutableAttributeMap requestedAttributes;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractBuilder<?, ?> copy) {
            Preconditions.checkNotNull(copy, "copy");
            id = copy.id;
            issuer = copy.issuer;
            citizenCountryCode = copy.citizenCountryCode;
            levelOfAssurance = copy.levelOfAssurance;
            nameIdFormat = copy.nameIdFormat;
            providerName = copy.providerName;
            requestedAttributes = copy.requestedAttributes;
            spType = copy.spType;
        }

        protected AbstractBuilder(@Nonnull ILightRequest copy) {
            Preconditions.checkNotNull(copy, "copy");
            id = copy.getId();
            issuer = copy.getIssuer();
            citizenCountryCode = copy.getCitizenCountryCode();
            levelOfAssurance = copy.getLevelOfAssurance();
            nameIdFormat = copy.getNameIdFormat();
            providerName = copy.getProviderName();
            requestedAttributes = copy.getRequestedAttributes();
            spType = copy.getSpType();
        }

        @Nonnull
        public final B id(String id) {
            this.id = id;
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

        @Nonnull
        public final B nameIdFormat(String nameIdFormat) {
            this.nameIdFormat = nameIdFormat;
            return (B) this;
        }

        @Nonnull
        public final B providerName(String providerName) {
            this.providerName = providerName;
            return (B) this;
        }

        @Nonnull
        public final B citizenCountryCode(String citizenCountryCode) {
            this.citizenCountryCode = citizenCountryCode;
            return (B) this;
        }

        @Nonnull
        public final B spType(String spType) {
            this.spType = spType;
            return (B) this;
        }

        @Nonnull
        public final B requestedAttributes(ImmutableAttributeMap requestedAttributes) {
            this.requestedAttributes = requestedAttributes;
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
            Preconditions.checkNotBlank(citizenCountryCode, "citizenCountryCode");
            if (null == requestedAttributes) {
                requestedAttributes = ImmutableAttributeMap.of();
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
         * Builds a new {@code T} instance based on this Builder instance (Builder pattern for {@link ILightRequest}).
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

    private static final long serialVersionUID = 901735201136963817L;

    /**
     * @serial
     */
    private final String citizenCountryCode;

    /**
     * @serial
     */
    @Nonnull
    private final String id;

    /**
     * @serial
     */
    private final String issuer;

    /**
     * @serial
     */
    @Nullable
    private final String levelOfAssurance;

    /**
     * @serial
     */
    @Nullable
    private final String nameIdFormat;

    /**
     * @serial
     */
    @Nullable
    private final String providerName;

    /**
     * @serial
     */
    @Nullable
    private final String spType;

    /**
     * @serial
     */
    @Nonnull
    private final ImmutableAttributeMap requestedAttributes;

    protected AbstractLightRequest(@Nonnull AbstractBuilder<?, ?> builder) {
        id = builder.id;
        issuer = builder.issuer;
        citizenCountryCode = builder.citizenCountryCode;
        levelOfAssurance = builder.levelOfAssurance;
        nameIdFormat = builder.nameIdFormat;
        providerName = builder.providerName;
        requestedAttributes = builder.requestedAttributes;
        spType = builder.spType;
    }

    @Override
    public final String getCitizenCountryCode() {
        return citizenCountryCode;
    }

    @Nonnull
    @Override
    public final String getId() {
        return id;
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

    @Nullable
    @Override
    public final String getNameIdFormat() {
        return nameIdFormat;
    }

    @Nullable
    @Override
    public final String getProviderName() {
        return providerName;
    }

    @Nullable
    @Override
    public final String getSpType() {
        return spType;
    }

    @Nonnull
    @Override
    public final ImmutableAttributeMap getRequestedAttributes() {
        return requestedAttributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        AbstractLightRequest lightRequestObj = (AbstractLightRequest) obj;

        if (!id.equals(lightRequestObj.id)) {
            return false;
        }
        if (citizenCountryCode != null ? !citizenCountryCode.equals(lightRequestObj.citizenCountryCode)
                                       : lightRequestObj.citizenCountryCode != null) {
            return false;
        }
        if (!issuer.equals(lightRequestObj.issuer)) {
            return false;
        }
        if (levelOfAssurance != null ? !levelOfAssurance.equals(lightRequestObj.levelOfAssurance)
                                     : lightRequestObj.levelOfAssurance != null) {
            return false;
        }
        if (providerName != null ? !providerName.equals(lightRequestObj.providerName)
                                 : lightRequestObj.providerName != null) {
            return false;
        }
        if (nameIdFormat != null ? !nameIdFormat.equals(lightRequestObj.nameIdFormat)
                                 : lightRequestObj.nameIdFormat != null) {
            return false;
        }
        if (spType != null ? !spType.equals(lightRequestObj.spType)
                : lightRequestObj.spType != null) {
            return false;
        }
        return !(!requestedAttributes.equals(lightRequestObj.requestedAttributes));
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (issuer.hashCode());
        result = 31 * result + (citizenCountryCode.hashCode());
        result = 31 * result + (null != levelOfAssurance ? levelOfAssurance.hashCode() : 0);
        result = 31 * result + (null != providerName ? providerName.hashCode() : 0);
        result = 31 * result + (null != nameIdFormat ? nameIdFormat.hashCode() : 0);
        result = 31 * result + (null != spType ? spType.hashCode() : 0);
        result = 31 * result + (requestedAttributes.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(getClass().getSimpleName()).append("{")).append('}').toString();
    }

    protected StringBuilder toString(@Nonnull StringBuilder stringBuilder) {
        return toString(stringBuilder, this);
    }

    public static StringBuilder toString(@Nonnull StringBuilder stringBuilder, @Nonnull ILightRequest request) {
        Preconditions.checkNotNull(stringBuilder, "stringBuilder");
        Preconditions.checkNotNull(request, "request");
        return stringBuilder.append("id='")
                .append(request.getId())
                .append('\'')
                .append(", citizenCountryCode='")
                .append(request.getCitizenCountryCode())
                .append('\'')
                .append(", issuer='")
                .append(request.getIssuer())
                .append('\'')
                .append(", levelOfAssurance='")
                .append(request.getLevelOfAssurance())
                .append('\'')
                .append(", providerName='")
                .append(request.getProviderName())
                .append('\'')
                .append(", nameIdFormat='")
                .append(request.getNameIdFormat())
                .append('\'')
                .append(", spType='")
                .append(request.getSpType())
                .append('\'')
                .append(", requestedAttributes='")
                .append(request.getRequestedAttributes())
                .append('\'');
    }
}
