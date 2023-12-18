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
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.LevelOfAssuranceType;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Abstract class for implementations of the {@link ILightRequest} interface.
 * <p>
 * This class uses the Builder Pattern.
 * <p>
 * Implementors of the {@link ILightRequest} should extend this class and its Builder.
 *
 * @since 1.1
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("ConstantConditions")
public abstract class AbstractLightRequest implements ILightRequest, Serializable {

    private AbstractLightRequest (){
        id = null;
        issuer = null;
        citizenCountryCode = null;
        levelsOfAssurance = null;
        nameIdFormat = null;
        providerName = null;
        requestedAttributes = null;
        spType = null;
        spCountryCode = null;
        requesterId = null;
        relayState = null;
    }

    /**
     * <p>
     * Abstract Builder pattern with self-bounding generics for {@link ILightRequest} subtypes.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * See Self-bounding generics: @see <a href="spec.html#section">http://www.artima.com/weblogs/viewpost.jsp?thread=136394</a>
     * @see <a href="spec.html#section">http://www.artima.com/forums/flat.jsp?forum=106&amp;thread=136394</a>
     * @see <a href="spec.html#section">http://en.wikipedia.org/wiki/Covariance_and_contravariance</a>
     *
     * @param <B> the type of the Builder itself
     * @param <T> the type being built by the {@link #build()} method of this builder.
     */
    @SuppressWarnings({"ParameterHidesMemberVariable", "unchecked"})
    @NotThreadSafe
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends ILightRequest> {

        private String id;

        private String issuer;

        private String citizenCountryCode;

        private List<LevelOfAssurance> levelsOfAssurance;

        private String nameIdFormat;

        private String providerName;

        private String spType;

        private String spCountryCode;

        private String requesterId;

        private String relayState;

        private ImmutableAttributeMap requestedAttributes;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractBuilder<?, ?> copy) {
            Preconditions.checkNotNull(copy, "copy");
            id = copy.id;
            issuer = copy.issuer;
            citizenCountryCode = copy.citizenCountryCode;
            levelsOfAssurance = copy.levelsOfAssurance;
            nameIdFormat = copy.nameIdFormat;
            providerName = copy.providerName;
            requestedAttributes = copy.requestedAttributes;
            spType = copy.spType;
            spCountryCode = copy.spCountryCode;
            requesterId = copy.requesterId;
            relayState = copy.relayState;
        }

        protected AbstractBuilder(@Nonnull ILightRequest copy) {
            Preconditions.checkNotNull(copy, "copy");
            id = copy.getId();
            issuer = copy.getIssuer();
            citizenCountryCode = copy.getCitizenCountryCode();
            levelsOfAssurance(copy.getLevelsOfAssurance());
            nameIdFormat = copy.getNameIdFormat();
            providerName = copy.getProviderName();
            requestedAttributes = copy.getRequestedAttributes();
            spType = copy.getSpType();
            spCountryCode = copy.getSpCountryCode();
            requesterId = copy.getRequesterId();
            relayState = copy.getRelayState();
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
            if (this.levelsOfAssurance == null) {
                this.levelsOfAssurance = new ArrayList<>();
            }
            LevelOfAssurance loa = LevelOfAssurance.build(levelOfAssurance);
            this.levelsOfAssurance.add(loa);
            this.levelsOfAssurance = trimNotifiedLoasToLowest(this.levelsOfAssurance);
            return (B) this;
        }

        @Nonnull
        public final B levelsOfAssurance(List<? extends ILevelOfAssurance> levelsOfAssurance) {
            List<String> levelsOfAssuranceValues = null;
            if (null != levelsOfAssurance) {
                levelsOfAssuranceValues = levelsOfAssurance.stream()
                        .map(ILevelOfAssurance::getValue)
                        .collect(Collectors.toList());
            }
            return levelsOfAssuranceValues(levelsOfAssuranceValues);
        }

        @Nonnull
        public final B levelsOfAssuranceValues(List<String> levelsOfAssurance) {
            if (levelsOfAssurance != null) {
                this.levelsOfAssurance = levelsOfAssurance.stream()
                        .map(LevelOfAssurance::build)
                        .collect(Collectors.toList());
                this.levelsOfAssurance = trimNotifiedLoasToLowest(this.levelsOfAssurance);
            } else {
                this.levelsOfAssurance = null;
            }
            return (B) this;
        }

        private List<LevelOfAssurance> trimNotifiedLoasToLowest(List<LevelOfAssurance> sourceLoas) {
            List<LevelOfAssurance> filteredList = sourceLoas.stream()
                    .filter(LevelOfAssuranceUtils::isNonNotified)
                    .collect(Collectors.toList());
            String lowestNotifiedLevelOfAssurance = getLowestNotifiedLevelOfAssuranceFrom(sourceLoas);
            if(lowestNotifiedLevelOfAssurance != null) {
                filteredList.add(0, LevelOfAssurance.build(lowestNotifiedLevelOfAssurance));
            }
            return filteredList;
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
        public final B spCountryCode(String spCountryCode) {
            this.spCountryCode = spCountryCode;
            return (B) this;
        }

        @Nonnull
        public final B requesterId(String requesterId) {
            this.requesterId = requesterId;
            return (B) this;
        }

        @Nonnull
        public final B relayState(String relayState) {
            this.relayState = relayState;
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
            validateLevelsOfAssurance();
        }

        private void validateLevelsOfAssurance() {
            if (null == levelsOfAssurance || levelsOfAssurance.isEmpty()) {
                 String message = "LoA is not set, LightRequest requires at least one levelOfAssurance";
                 throw new IllegalArgumentException("Invalid LoA: " + message);
            } else {
                boolean hasNotifiedLoA = false;
                for (ILevelOfAssurance loa: levelsOfAssurance) {
                    LevelOfAssurance.builder(loa).validate();
                    if (LevelOfAssuranceType.NOTIFIED.stringValue().equalsIgnoreCase(loa.getType())) {
                        if (hasNotifiedLoA) {
                            String message = "LightRequest has more than one notified LoA";
                            throw new IllegalArgumentException("Invalid LoA: " + message);
                        }
                        hasNotifiedLoA = true;
                    }
                }
            }
        }

        /**
         * Validates the state of this Builder before allowing to create new instances of the built type {@code T}.
         *
         * @throws IllegalArgumentException if the builder is not in a legal state allowing to proceed with the creation
         * of a {@code T} instance.
         */
        protected void validate() throws IllegalArgumentException {
            internalValidate();
        }

        /**
         * <p>
         * Builds a new {@code T} instance based on this Builder instance (Builder pattern for {@link ILightRequest}).
         *
         * The {@link #validate()} is always called before creating a new instance through a call to the {@link
         * #newInstance()} method.
         *
         * @return a new {@code T} instance based on this Builder instance.
         */
        @Nonnull
        public final T build() {
            validate();
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
    @XmlElement(name="levelOfAssurance")
    private final List<LevelOfAssurance> levelsOfAssurance;

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
    @Nullable
    private final String spCountryCode;

    /**
     * @serial
     */
    @Nullable
    private final String requesterId;

    /**
     * relayState
     */
    @Nullable
    private String relayState;

    /**
     * @serial
     */
    @Nonnull
    @XmlJavaTypeAdapter(AttributeMapType.ImmutableAttributeMapAdapter.class)
    private final ImmutableAttributeMap requestedAttributes;

    protected AbstractLightRequest(@Nonnull AbstractBuilder<?, ?> builder) {
        id = builder.id;
        issuer = builder.issuer;
        citizenCountryCode = builder.citizenCountryCode;
        levelsOfAssurance = builder.levelsOfAssurance;
        nameIdFormat = builder.nameIdFormat;
        providerName = builder.providerName;
        requestedAttributes = builder.requestedAttributes;
        spType = builder.spType;
        spCountryCode = builder.spCountryCode;
        requesterId = builder.requesterId;
        relayState = builder.relayState;
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
        return getLowestNotifiedLevelOfAssuranceFrom(levelsOfAssurance);
    }

    @Nullable
    private static String getLowestNotifiedLevelOfAssuranceFrom(List<LevelOfAssurance> levelsOfAssurance) {
        List<String> sorting = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_LOW, ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL, ILevelOfAssurance.EIDAS_LOA_HIGH);
        return (levelsOfAssurance == null || levelsOfAssurance.isEmpty()) ? null :
                levelsOfAssurance.stream()
                        .filter(LevelOfAssuranceUtils::isNotified)
                        .min(Comparator.comparingInt(loa -> sorting.indexOf(loa.getValue())))
                        .map(loa-> loa.getValue())
                        .orElse(null);
    }

    @Override
    public final List<ILevelOfAssurance> getLevelsOfAssurance() {
        if (levelsOfAssurance == null) return null;
        return new ArrayList<>(levelsOfAssurance);
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

    @Nullable
    @Override
    public final String getSpCountryCode() {
        return spCountryCode;
    }

    @Nullable
    @Override
    public final String getRequesterId() {
        return requesterId;
    }

    @Nullable
    @Override
    public final String getRelayState() {
        return relayState;
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
        if (!Objects.equals(levelsOfAssurance, lightRequestObj.levelsOfAssurance)) {
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

        if (spCountryCode != null ? !spCountryCode.equals(lightRequestObj.spCountryCode)
                : lightRequestObj.spCountryCode != null) {
            return false;
        }

        if (!Objects.equals(requesterId, lightRequestObj.requesterId)) {
            return false;
        }

        if (relayState != null ? !relayState.equals(lightRequestObj.relayState)
                : lightRequestObj.relayState != null) {
            return false;
        }
        return requestedAttributes.equals(lightRequestObj.requestedAttributes);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (issuer.hashCode());
        result = 31 * result + (citizenCountryCode.hashCode());
        result = 31 * result + (null != levelsOfAssurance ? levelsOfAssurance.hashCode() : 0);
        result = 31 * result + (null != providerName ? providerName.hashCode() : 0);
        result = 31 * result + (null != nameIdFormat ? nameIdFormat.hashCode() : 0);
        result = 31 * result + (null != spType ? spType.hashCode() : 0);
        result = 31 * result + (null != spCountryCode ? spCountryCode.hashCode() : 0);
        result = 31 * result + (null != requesterId ? requesterId.hashCode() : 0);
        result = 31 * result + (null != relayState ? relayState.hashCode() : 0);
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
                .append(", levelsOfAssurance='")
                .append(request.getLevelsOfAssurance())
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
                .append(", spCountryCode='")
                .append(request.getSpCountryCode())
                .append('\'')
                .append(", requesterId='")
                .append(request.getRequesterId())
                .append('\'')
                .append(", relayState='")
                .append(request.getRelayState())
                .append('\'')
                .append(", requestedAttributes='")
                .append(request.getRequestedAttributes())
                .append('\'');
    }
}
