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
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.light.impl.AbstractLightResponse;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Abstract class for implementations of the {@link IAuthenticationResponse} interface.
 * <p>
 * This class uses the Builder Pattern.
 * <p>
 * Implementors of the {@link IAuthenticationResponse} should extend this class and its Builder.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
public abstract class AbstractAuthenticationResponse implements IAuthenticationResponse, Serializable {

    /**
     * Abstract Builder pattern with self-bounding generics for {@link IAuthenticationResponse} subtypes.
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
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends IAuthenticationResponse> {

        private LightResponse.Builder lightResponseBuilder = LightResponse.builder();

        private ResponseStatus.Builder responseStatusBuilder = ResponseStatus.builder();

        /**
         * Audience restriction.
         */
        private String audienceRestriction;

        /**
         * Expiration date.
         */
        private ZonedDateTime notOnOrAfter;

        /**
         * Creation date.
         */
        private ZonedDateTime notBefore;

        /**
         * Country.
         */
        private String country;

        private boolean encrypted;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractBuilder<?, ?> copy) {
            Preconditions.checkNotNull(copy, "copy");
            lightResponseBuilder = LightResponse.builder(copy.lightResponseBuilder);
            responseStatusBuilder = ResponseStatus.builder(copy.responseStatusBuilder);
            audienceRestriction = copy.audienceRestriction;
            notOnOrAfter = copy.notOnOrAfter;
            notBefore = copy.notBefore;
            country = copy.country;
            encrypted = copy.encrypted;
        }

        protected AbstractBuilder(@Nonnull IAuthenticationResponse copy) {
            Preconditions.checkNotNull(copy, "copy");
            responseStatus(copy.getStatus());
            id(copy.getId());
            relayState(copy.getRelayState());
            inResponseTo(copy.getInResponseToId());
            attributes(copy.getAttributes());
            issuer(copy.getIssuer());
            consent(copy.getConsent());
            subject(copy.getSubject());
            subjectNameIdFormat(copy.getSubjectNameIdFormat());
            levelOfAssurance(copy.getLevelOfAssurance());
            ipAddress(copy.getIPAddress());
            audienceRestriction = copy.getAudienceRestriction();
            notOnOrAfter = copy.getNotOnOrAfter();
            notBefore = copy.getNotBefore();
            country = copy.getCountry();
            encrypted = copy.isEncrypted();
        }

        @Nonnull
        public final B lightResponse(@Nonnull ILightResponse lightResponse) {
            Preconditions.checkNotNull(lightResponse, "lightResponse");
            responseStatus(lightResponse.getStatus());
            id(lightResponse.getId());
            relayState(lightResponse.getRelayState());
            inResponseTo(lightResponse.getInResponseToId());
            attributes(lightResponse.getAttributes());
            issuer(lightResponse.getIssuer());
            consent(lightResponse.getConsent());
            subject(lightResponse.getSubject());
            subjectNameIdFormat(lightResponse.getSubjectNameIdFormat());
            levelOfAssurance(lightResponse.getLevelOfAssurance());
            ipAddress(lightResponse.getIPAddress());
            return (B) this;
        }

        @Nonnull
        public final B responseStatus(@Nonnull IResponseStatus responseStatus) {
            Preconditions.checkNotNull(responseStatus, "responseStatus");
            failure(responseStatus.isFailure());
            statusCode(responseStatus.getStatusCode());
            subStatusCode(responseStatus.getSubStatusCode());
            statusMessage(responseStatus.getStatusMessage());
            return (B) this;
        }

        @Nonnull
        public final B id(final String id) {
            lightResponseBuilder.id(id);
            return (B) this;
        }

        @Nonnull
        public final B relayState(final String relayState) {
            lightResponseBuilder.relayState(relayState);
            return (B) this;
        }

        @Nonnull
        public final B failure(final boolean failure) {
            responseStatusBuilder.failure(failure);
            return (B) this;
        }

        @Nonnull
        public final B failure(final Boolean failure) {
            if (null != failure) {
                failure(failure.booleanValue());
            }
            return (B) this;
        }

        @Nonnull
        public final B statusCode(final String statusCode) {
            responseStatusBuilder.statusCode(statusCode);
            return (B) this;
        }

        @Nonnull
        public final B subStatusCode(final String subStatusCode) {
            responseStatusBuilder.subStatusCode(subStatusCode);
            return (B) this;
        }

        @Nonnull
        public final B audienceRestriction(final String audienceRest) {
            this.audienceRestriction = audienceRest;
            return (B) this;
        }

        @Nonnull
        public final B statusMessage(final String statusMessage) {
            responseStatusBuilder.statusMessage(statusMessage);
            return (B) this;
        }

        @Nonnull
        public final B inResponseTo(final String inResponseTo) {
            lightResponseBuilder.inResponseToId(inResponseTo);
            return (B) this;
        }

        @Nonnull
        public final B notOnOrAfter(final ZonedDateTime notOnOrAfter) {
            this.notOnOrAfter = notOnOrAfter;
            return (B) this;
        }

        @Nonnull
        public final B notBefore(final ZonedDateTime notBefore) {
            this.notBefore = notBefore;
            return (B) this;
        }

        @Nonnull
        public final B country(final String country) {
            this.country = country;
            return (B) this;
        }

        @Nonnull
        public final B encrypted(final boolean encrypted) {
            this.encrypted = encrypted;
            return (B) this;
        }

        @Nonnull
        public final B encrypted(final Boolean encrypted) {
            if (null != encrypted) {
                failure(encrypted.booleanValue());
            }
            return (B) this;
        }

        @Nonnull
        public final B attributes(final ImmutableAttributeMap attributes) {
            lightResponseBuilder.attributes(attributes);
            return (B) this;
        }

        @Nonnull
        public final B issuer(final String issuer) {
            lightResponseBuilder.issuer(issuer);
            return (B) this;
        }

        @Nonnull
        public final B consent(final String consent) {
            lightResponseBuilder.consent(consent);
            return (B) this;
        }

        @Nonnull
        public final B subject(final String subject) {
            lightResponseBuilder.subject(subject);
            return (B) this;
        }

        @Nonnull
        public final B subjectNameIdFormat(final String subjectNameIdFormat) {
            lightResponseBuilder.subjectNameIdFormat(subjectNameIdFormat);
            return (B) this;
        }

        @Nonnull
        public final B levelOfAssurance(final String levelOfAssurance) {
            lightResponseBuilder.levelOfAssurance(levelOfAssurance);
            return (B) this;
        }

        @Nonnull
        public final B ipAddress(final String ipAddress) {
            lightResponseBuilder.ipAddress(ipAddress);
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
            ResponseStatus responseStatus = responseStatusBuilder.build();
            lightResponseBuilder.status(responseStatus);
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
         * IAuthenticationResponse}).
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

    private static final long serialVersionUID = 7115608793863572955L;

    /**
     * @serial
     */
    private final ILightResponse lightResponse;

    /**
     * Audience restriction.
     *
     * @serial
     */
    private final String audienceRestriction;

    /**
     * Expiration date.
     *
     * @serial
     */
    private final ZonedDateTime notOnOrAfter;

    /**
     * Creation date.
     *
     * @serial
     */
    private final ZonedDateTime notBefore;

    /**
     * Country.
     *
     * @serial
     */
    private final String country;

    /**
     * @serial
     */
    private final boolean encrypted;

    protected AbstractAuthenticationResponse(@Nonnull AbstractBuilder<?, ?> builder) {
        lightResponse = builder.lightResponseBuilder.build();
        audienceRestriction = builder.audienceRestriction;
        notOnOrAfter = builder.notOnOrAfter;
        notBefore = builder.notBefore;
        country = builder.country;
        encrypted = builder.encrypted;
    }

    /**
     * Getter for the subStatusCode.
     *
     * @return The subStatusCode value.
     */
    @Override
    public final String getSubStatusCode() {
        return lightResponse.getStatus().getSubStatusCode();
    }

    /**
     * Getter for audienceRestriction.
     *
     * @return The audienceRestriction value.
     */
    @Override
    @Nonnull
    public final String getAudienceRestriction() {
        return audienceRestriction;
    }

    /**
     * Getter for the country name.
     *
     * @return The country name value.
     */
    @Override
    @Nonnull
    public final String getCountry() {
        return country;
    }

    /**
     * Getter for the attribute map.
     *
     * @return the attribute map.
     * @see ImmutableAttributeMap
     */
    @Override
    public final ImmutableAttributeMap getAttributes() {
        return lightResponse.getAttributes();
    }

    /**
     * Getter for the inResponseTo value.
     *
     * @return The inResponseTo value.
     */
    @Override
    public final String getInResponseToId() {
        return lightResponse.getInResponseToId();
    }

    /**
     * Getter for the fail value.
     *
     * @return The fail value.
     */
    @Override
    public final boolean isFailure() {
        return lightResponse.getStatus().isFailure();
    }

    /**
     * Getter for the message value.
     *
     * @return The message value.
     */
    @Override
    public final String getStatusMessage() {
        return lightResponse.getStatus().getStatusMessage();
    }

    /**
     * Getter for the statusCode value.
     *
     * @return The statusCode value.
     */
    @Override
    public final String getStatusCode() {
        return lightResponse.getStatus().getStatusCode();
    }

    /**
     * Getter for the samlId value.
     *
     * @return The samlId value.
     */
    @Nonnull
    @Override
    public final String getId() {
        return lightResponse.getId();
    }

    /**
     * Getter for the relayState value.
     *
     * @return The relayState value.
     */
    @Override
    @Nullable
    public String getRelayState() {
        return lightResponse.getRelayState();
    }

    /**
     * Getter for the notOnOrAfter value.
     *
     * @return The notOnOrAfter value.
     * @see ZonedDateTime
     */
    @Override
    @Nonnull
    public final ZonedDateTime getNotOnOrAfter() {
        return this.notOnOrAfter;
    }

    /**
     * Getter for the notBefore value.
     *
     * @return The notBefore value.
     * @see ZonedDateTime
     */
    @Override
    @Nonnull
    public final ZonedDateTime getNotBefore() {
        return notBefore;
    }

    @Override
    @Nullable
    public final String getLevelOfAssurance() {
        return lightResponse.getLevelOfAssurance();
    }

    @Nonnull
    public final ILightResponse getLightResponse() {
        return lightResponse;
    }

    @Override
    @Nonnull
    public final IResponseStatus getStatus() {
        return lightResponse.getStatus();
    }

    @Override
    @Nonnull
    public String getIssuer() {
        return lightResponse.getIssuer();
    }

    @Override
    @Nonnull
    public String getConsent() {
        return lightResponse.getConsent();
    }

    @Override
    @Nullable
    public String getSubject() {
        return lightResponse.getSubject();
    }

    @Override
    @Nullable
    public String getSubjectNameIdFormat() {
        return lightResponse.getSubjectNameIdFormat();
    }

    @Override
    public boolean isEncrypted() {
        return encrypted;
    }

    @Override
    @Nullable
    public String getIPAddress() {
        return lightResponse.getIPAddress();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractAuthenticationResponse that = (AbstractAuthenticationResponse) o;

        if (encrypted != that.encrypted) {
            return false;
        }
        if (lightResponse != null ? !lightResponse.equals(that.lightResponse) : that.lightResponse != null) {
            return false;
        }
        if (audienceRestriction != null ? !audienceRestriction.equals(that.audienceRestriction)
                                        : that.audienceRestriction != null) {
            return false;
        }
        if (notOnOrAfter != null ? !notOnOrAfter.equals(that.notOnOrAfter) : that.notOnOrAfter != null) {
            return false;
        }
        if (notBefore != null ? !notBefore.equals(that.notBefore) : that.notBefore != null) {
            return false;
        }
        return country != null ? country.equals(that.country) : that.country == null;

    }

    @Override
    public int hashCode() {
        int result = lightResponse != null ? lightResponse.hashCode() : 0;
        result = 31 * result + (audienceRestriction != null ? audienceRestriction.hashCode() : 0);
        result = 31 * result + (notOnOrAfter != null ? notOnOrAfter.hashCode() : 0);
        result = 31 * result + (notBefore != null ? notBefore.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (encrypted ? 1 : 0);
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
                                         @Nonnull IAuthenticationResponse response) {
        Preconditions.checkNotNull(stringBuilder, "stringBuilder");
        Preconditions.checkNotNull(response, "response");
        return AbstractLightResponse.toString(stringBuilder, response)
                .append(", audienceRestriction='")
                .append(response.getAudienceRestriction())
                .append('\'')
                .append(", notOnOrAfter='")
                .append(response.getNotOnOrAfter())
                .append('\'')
                .append(", notBefore='")
                .append(response.getNotBefore())
                .append('\'')
                .append(", country='")
                .append(response.getCountry())
                .append('\'')
                .append(", encrypted='")
                .append(response.isEncrypted())
                .append('\'');
    }
}
