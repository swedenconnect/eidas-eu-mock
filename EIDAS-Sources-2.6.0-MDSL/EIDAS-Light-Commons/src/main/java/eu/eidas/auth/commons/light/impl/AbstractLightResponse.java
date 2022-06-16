/*
 * Copyright (c) 2020 by European Commission
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
package eu.eidas.auth.commons.light.impl;

import eu.eidas.auth.commons.attribute.AttributeMapType;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;

/**
 * Abstract class for implementations of the {@link ILightResponse} interface.
 * <p>
 * This class uses the Builder Pattern.
 * <p>
 * Implementors of the {@link ILightResponse} should extend this class and its Builder.
 *
 * @since 1.1
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("ConstantConditions")
@XmlSeeAlso(ResponseStatus.class)
public abstract class AbstractLightResponse implements ILightResponse, Serializable {

    private AbstractLightResponse() {
        id = null;
        relayState = null;
        issuer = null;
        consent = null;
        ipAddress = null;
        subject = null;
        subjectNameIdFormat = null;
        status = null;
        inResponseToId = null;
        levelOfAssurance = null;
        attributes = null;
    }

    /**
     * <p>
     * Abstract Builder pattern with self-bounding generics for {@link ILightResponse} subtypes.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     *
     * <p>See Self-bounding generics: <p>http://www.artima.com/weblogs/viewpost.jsp?thread=136394
     * <p>http://www.artima.com/forums/flat.jsp?forum=106&amp;thread=136394 <p>http://en.wikipedia.org/wiki/Covariance_and_contravariance
     *
     * @param <B> the type of the Builder itself
     * @param <T> the type being built by the {@link #build()} method of this builder.
     */
    @NotThreadSafe
    @SuppressWarnings({"ParameterHidesMemberVariable", "unchecked"})
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends LightResponse> {

        private String id;

        private String relayState;

        private String issuer;

        private String consent;

        private String ipAddress;

        private String subject;

        private String subjectNameIdFormat;

        private ResponseStatus status;

        private String inResponseToId;

        private String levelOfAssurance;

        private ImmutableAttributeMap attributes;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractBuilder<?, ?> copy) {
            Preconditions.checkNotNull(copy, "copy");
            id = copy.id;
            relayState = copy.relayState;
            issuer = copy.issuer;
            consent = copy.consent;
            ipAddress = copy.ipAddress;
            subject = copy.subject;
            subjectNameIdFormat = copy.subjectNameIdFormat;
            status = copy.status;
            inResponseToId = copy.inResponseToId;
            levelOfAssurance = copy.levelOfAssurance;
            attributes = copy.attributes;
        }

        protected AbstractBuilder(@Nonnull ILightResponse copy) {
            Preconditions.checkNotNull(copy, "copy");
            id = copy.getId();
            relayState = copy.getRelayState();
            issuer = copy.getIssuer();
            consent = copy.getConsent();
            ipAddress = copy.getIPAddress();
            subject = copy.getSubject();
            subjectNameIdFormat = copy.getSubjectNameIdFormat();
            status = (ResponseStatus) copy.getStatus();
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
        public final B status(ResponseStatus status) {
            this.status = status;
            return (B) this;
        }

        @Nonnull
        public final B id(String id) {
            this.id = id;
            return (B) this;
        }

        @Nonnull
        public final B relayState(String relayState) {
            this.relayState = relayState;
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
        public final B consent(String consent) {
            this.consent = consent;
            return (B) this;
        }

        @Nonnull
        public final B subject(String subject) {
            this.subject = subject;
            return (B) this;
        }

        @Nonnull
        public final B subjectNameIdFormat(String subjectNameIdFormat) {
            this.subjectNameIdFormat = subjectNameIdFormat;
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
            if (!status.isFailure()) {
                Preconditions.checkNotBlank(subject, "subject");
                Preconditions.checkNotBlank(subjectNameIdFormat, "subjectNameIdFormat");
            }
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
         * <p>
         * Builds a new {@code T} instance based on this Builder instance (Builder pattern for {@link ILightResponse}).
         *
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
         * <p>
         * Method to be implemented by subtypes to create the right type {@code T} of instances.
         *
         * <br>The typical implementation simply consists in writing {@code return new MySubType(this);}
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
     * relayState
     */
    @Nullable
    private final String relayState;

    /**
     * @serial
     */
    @Nonnull
    private final String issuer;

    /**
     * @serial
     */
    @Nonnull
    private final String consent;

    /**
     * @serial
     */
    @Nullable
    private final String ipAddress;

    /**
     * @serial
     */
    @Nullable
    private final String subject;

    /**
     * @serial
     */
    @Nullable
    private final String subjectNameIdFormat;

    /**
     * @serial
     */
    @Nonnull @XmlElement
    private final ResponseStatus status;

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
    @XmlJavaTypeAdapter(AttributeMapType.ImmutableAttributeMapAdapter.class)
    private final ImmutableAttributeMap attributes;

    protected AbstractLightResponse(@Nonnull AbstractBuilder<?, ?> builder) {
        id = builder.id;
        relayState = builder.relayState;
        issuer = builder.issuer;
        consent = builder.consent;
        ipAddress = builder.ipAddress;
        subject = builder.subject;
        subjectNameIdFormat = builder.subjectNameIdFormat;
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

    @Nullable
    @Override
    public final String getRelayState() {
        return relayState;
    }

    @Nonnull
    @Override
    public final String getInResponseToId() {
        return inResponseToId;
    }

    @Nullable
    @Override
    public final String getSubject() {
        return subject;
    }

    @Nullable
    @Override
    public final String getSubjectNameIdFormat() {
        return subjectNameIdFormat;
    }

    @Nonnull
    @Override
    public final String getIssuer() {
        return issuer;
    }

    @Nonnull
    @Override
    public final String getConsent() {
        return consent;
    }

    @Nullable
    @Override
    public final String getLevelOfAssurance() {
        return levelOfAssurance;
    }

    @Nonnull
    @Override
    public final ResponseStatus getStatus() {
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
        if (relayState != null ? !relayState.equals(that.relayState) : that.relayState != null) {
            return false;
        }
        if (!issuer.equals(that.issuer)) {
            return false;
        }
        if (subject != null ? !subject.equals(that.subject) : that.subject != null) {
            return false;
        }
        if (subjectNameIdFormat != null ? !subjectNameIdFormat.equals(that.subjectNameIdFormat) : that.subjectNameIdFormat != null) {
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
        result = 31 * result + (null != relayState ? relayState.hashCode() : 0);
        result = 31 * result + (issuer.hashCode());
        result = 31 * result + (null != subject ? subject.hashCode() : 0);
        result = 31 * result + (null != subjectNameIdFormat ? subjectNameIdFormat.hashCode() : 0);
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
                .append(", relayState='")
                .append(response.getRelayState())
                .append('\'')
                .append(", issuer='")
                .append(response.getIssuer())
                .append('\'')
                .append(", consent='")
                .append(response.getConsent())
                .append('\'')
                .append(", subject='")
                .append(response.getSubject())
                .append('\'')
                .append(", subjectNameIdFormat='")
                .append(response.getSubjectNameIdFormat())
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
