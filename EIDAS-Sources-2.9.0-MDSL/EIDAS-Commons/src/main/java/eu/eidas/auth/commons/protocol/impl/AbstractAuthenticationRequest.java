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
package eu.eidas.auth.commons.protocol.impl;

import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.impl.AbstractLightRequest;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract class for implementations of the {@link IAuthenticationRequest} interface.
 * <p>
 * This class uses the Builder Pattern.
 * <p>
 * Implementors of the {@link IAuthenticationRequest} should extend this class and its Builder.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
public abstract class AbstractAuthenticationRequest implements IAuthenticationRequest, Serializable {

    /**
     * Abstract Builder pattern with self-bounding generics for {@link IAuthenticationRequest} subtypes.
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
     */
    @SuppressWarnings({"ParameterHidesMemberVariable", "unchecked"})
    @NotThreadSafe
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends IAuthenticationRequest> {

        private LightRequest.Builder lightRequestBuilder = LightRequest.builder();

        private String assertionConsumerServiceURL;

        private String binding;

        private String destination;

        private String originalIssuer;

        private String serviceProviderCountryCode;

        private String originCountryCode;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractBuilder<?, ?> copy) {
            Preconditions.checkNotNull(copy, "copy");
            lightRequestBuilder = LightRequest.builder(copy.lightRequestBuilder);
            assertionConsumerServiceURL = copy.assertionConsumerServiceURL;
            binding = copy.binding;
            destination = copy.destination;
            originalIssuer = copy.originalIssuer;
            serviceProviderCountryCode = copy.serviceProviderCountryCode;
            originCountryCode = copy.originCountryCode;
        }

        protected AbstractBuilder(@Nonnull IAuthenticationRequest copy) {
            Preconditions.checkNotNull(copy, "copy");
            id(copy.getId());
            issuer(copy.getIssuer());
            providerName(copy.getProviderName());
            citizenCountryCode(copy.getCitizenCountryCode());
            requestedAttributes(copy.getRequestedAttributes());
            nameIdFormat(copy.getNameIdFormat());
            levelsOfAssurance(copy.getLevelsOfAssurance());
            spType(copy.getSpType());
            spCountryCode(copy.getSpCountryCode());
            requesterId(copy.getRequesterId());
            relayState(copy.getRelayState());
            assertionConsumerServiceURL = copy.getAssertionConsumerServiceURL();
            binding = copy.getBinding();
            destination = copy.getDestination();
            serviceProviderCountryCode = copy.getServiceProviderCountryCode();
        }

        @Nonnull
        public final B lightRequest(ILightRequest lightRequest) {
            Preconditions.checkNotNull(lightRequest, "lightRequest");
            id(lightRequest.getId());
            issuer(lightRequest.getIssuer());
            providerName(lightRequest.getProviderName());
            citizenCountryCode(lightRequest.getCitizenCountryCode());
            relayState(lightRequest.getRelayState());
            requestedAttributes(lightRequest.getRequestedAttributes());
            nameIdFormat(lightRequest.getNameIdFormat());
            levelsOfAssurance(lightRequest.getLevelsOfAssurance());
            spType(lightRequest.getSpType());
            spCountryCode(lightRequest.getSpCountryCode());
            requesterId(lightRequest.getRequesterId());
            return (B) this;
        }

        @Nonnull
        public final B id(String id) {
            lightRequestBuilder.id(id);
            return (B) this;
        }

        @Nonnull
        public final B assertionConsumerServiceURL(String assertionConsumerServiceURL) {
            this.assertionConsumerServiceURL = assertionConsumerServiceURL;
            return (B) this;
        }

        @Nonnull
        public final B binding(String binding) {
            this.binding = binding;
            return (B) this;
        }

        @Nonnull
        public final B destination(String destination) {
            this.destination = destination;
            return (B) this;
        }

        @Nonnull
        public final B issuer(String issuer) {
            lightRequestBuilder.issuer(issuer);
            return (B) this;
        }

        @Nonnull
        public final B originalIssuer(String originalIssuer) {
            this.originalIssuer = originalIssuer;
            return (B) this;
        }

        @Nonnull
        public final B providerName(String providerName) {
            lightRequestBuilder.providerName(providerName);
            return (B) this;
        }

        @Nonnull
        public final B serviceProviderCountryCode(String spCountry) {
            this.serviceProviderCountryCode = spCountry;
            return (B) this;
        }

        @Nonnull
        public final B citizenCountryCode(String citizenCountry) {
            lightRequestBuilder.citizenCountryCode(citizenCountry);
            return (B) this;
        }

        @Nonnull
        public final B requestedAttributes(ImmutableAttributeMap requestedAttributes) {
            lightRequestBuilder.requestedAttributes(requestedAttributes);
            return (B) this;
        }

        @Nonnull
        public final B nameIdFormat(String nameIdFormat) {
            lightRequestBuilder.nameIdFormat(nameIdFormat);
            return (B) this;
        }

        @Nonnull
        public final B levelOfAssurance(String levelOfAssurance) {
            lightRequestBuilder.levelOfAssurance(levelOfAssurance);
            return (B) this;
        }

        @Nonnull
        public final B levelsOfAssurance(List<? extends ILevelOfAssurance> levelsOfAssurance) {
            lightRequestBuilder.levelsOfAssurance(levelsOfAssurance);
            return (B) this;
        }

        @Nonnull
        public final B spType(String spType) {
            lightRequestBuilder.spType(spType);
            return (B) this;
        }

        @Nonnull
        public final B spCountryCode(String spCountryCode) {
            lightRequestBuilder.spCountryCode(spCountryCode);
            return (B) this;
        }

        @Nonnull
        public final B requesterId(String requesterId) {
            lightRequestBuilder.requesterId(requesterId);
            return (B) this;
        }

        @Nonnull
        public final B relayState(String relayState) {
            lightRequestBuilder.relayState(relayState);
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
            Preconditions.checkNotBlank(destination, "destination");
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
         * IAuthenticationRequest}).
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

    private static final long serialVersionUID = -8135480618479254281L;

    // Defined in the base interface

    /**
     * The samlId.
     *
     * @serial
     */
    @Nonnull
    private final ILightRequest lightRequest;

    /**
     * The assertion consumer service url.
     *
     * @serial
     */
    @Nullable
    private final String assertionConsumerServiceURL;

    /**
     * @serial
     */
    @Nullable
    private final String binding;

    /**
     * The destination.
     *
     * @serial
     */
    @Nonnull
    private final String destination;
    /**
     * The country of the originator of the request.
     *
     * @serial
     */
    private final String serviceProviderCountryCode;

    protected AbstractAuthenticationRequest(@Nonnull AbstractBuilder<?, ?> builder) {
        lightRequest = builder.lightRequestBuilder.build();
        assertionConsumerServiceURL = builder.assertionConsumerServiceURL;
        binding = builder.binding;
        destination = builder.destination;
        serviceProviderCountryCode = builder.serviceProviderCountryCode;
    }

    @Override
    @Nonnull
    public final String getId() {
        return lightRequest.getId();
    }

    @Override
    @Nullable
    public final String getAssertionConsumerServiceURL() {
        return assertionConsumerServiceURL;
    }

    @Nullable
    @Override
    public String getBinding() {
        return binding;
    }

    @Override
    @Nonnull
    public final String getDestination() {
        return destination;
    }

    @Override
    @Nonnull
    public final String getIssuer() {
        return lightRequest.getIssuer();
    }

    @Override
    @Nonnull
    public final String getSpType() {
        return lightRequest.getSpType();
    }

    @Override
    @Nonnull
    public final String getSpCountryCode() {
        return lightRequest.getSpCountryCode();
    }

    @Override
    public final String getRequesterId() {
        return lightRequest.getRequesterId();
    }

    @Override
    @Nonnull
    public final String getRelayState() {
        return lightRequest.getRelayState();
    }

    @Override
    @Nullable
    public final String getProviderName() {
        return lightRequest.getProviderName();
    }

    @Override
    public final String getServiceProviderCountryCode() {
        return serviceProviderCountryCode;
    }

    @Override
    public final String getCitizenCountryCode() {
        return lightRequest.getCitizenCountryCode();
    }

    @Nonnull
    public final ILightRequest getLightRequest() {
        return lightRequest;
    }

    @Override
    @Nullable
    public final String getNameIdFormat() {
        return lightRequest.getNameIdFormat();
    }

    @Override
    @Nullable
    public final String getLevelOfAssurance() {
        return lightRequest.getLevelOfAssurance();
    }

    @Override
    @Nullable
    public List<ILevelOfAssurance> getLevelsOfAssurance() {
        List<ILevelOfAssurance> loaList = lightRequest.getLevelsOfAssurance();
        NotifiedLevelOfAssurance lowestNotifiedLevelOfAssurance = NotifiedLevelOfAssurance.fromString(lightRequest.getLevelOfAssurance());
        if(lowestNotifiedLevelOfAssurance != null && loaList.size() > 1) {
            loaList.addAll(lowestNotifiedLevelOfAssurance.getHigherLevelsOfAssurance().stream()
                    .map(NotifiedLevelOfAssurance::stringValue)
                    .map(LevelOfAssurance::build).collect(Collectors.toList()));
        }
        return loaList;
    }

    @Override
    @Nonnull
    public final ImmutableAttributeMap getRequestedAttributes() {
        return lightRequest.getRequestedAttributes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractAuthenticationRequest that = (AbstractAuthenticationRequest) o;

        if (!lightRequest.equals(that.lightRequest)) { // LightRequest cannot be null
            return false;
        }
        if (assertionConsumerServiceURL != null ? !assertionConsumerServiceURL.equals(that.assertionConsumerServiceURL)
                                                : that.assertionConsumerServiceURL != null) {
            return false;
        }
        if (binding != null ? !binding.equals(that.binding) : that.binding != null) {
            return false;
        }
        if (!destination.equals(that.destination)) { // destination cannot be null
            return false;
        }
        return serviceProviderCountryCode != null ? serviceProviderCountryCode.equals(that.serviceProviderCountryCode)
                                         : that.serviceProviderCountryCode == null;

    }

    @Override
    public int hashCode() {
        int result = lightRequest.hashCode(); // lightRequest cannot be null
        result = 31 * result + (null != assertionConsumerServiceURL ? assertionConsumerServiceURL.hashCode() : 0);
        result = 31 * result + (null != binding ? binding.hashCode() : 0);
        result = 31 * result + (destination.hashCode()); // destination cannot be null
        result = 31 * result + (null != serviceProviderCountryCode ? serviceProviderCountryCode.hashCode() : 0);
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
                                         @Nonnull IAuthenticationRequest request) {
        Preconditions.checkNotNull(stringBuilder, "stringBuilder");
        Preconditions.checkNotNull(request, "request");
        return AbstractLightRequest.toString(stringBuilder, request)
                .append(", assertionConsumerServiceURL='")
                .append(request.getAssertionConsumerServiceURL())
                .append('\'')
                .append(", binding='")
                .append(request.getBinding())
                .append('\'')
                .append(", destination='")
                .append(request.getDestination())
                .append('\'')
                .append(", serviceProviderCountryCode='")
                .append(request.getServiceProviderCountryCode())
                .append('\'');
    }

}
