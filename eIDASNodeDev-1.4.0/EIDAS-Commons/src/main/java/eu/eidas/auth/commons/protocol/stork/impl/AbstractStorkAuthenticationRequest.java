/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

/**
 * This work is Open Source and licensed by the European Commission under the conditions of the European Public License
 * v1.1
 * <p/>
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 * <p/>
 * any use of this file implies acceptance of the conditions of this license. Unless required by applicable law or
 * agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package eu.eidas.auth.commons.protocol.stork.impl;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AbstractAuthenticationRequest;
import eu.eidas.auth.commons.protocol.stork.IStorkAuthenticationRequest;
import eu.eidas.util.Preconditions;

/**
 * Abstract class for implementations of the {@link IAuthenticationRequest} interface which adds support for the STORK
 * protocol.
 * <p>
 * This class uses the Builder Pattern.
 * <p>
 * Implementors of the {@link IStorkAuthenticationRequest} should extend this class and its Builder.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
@Immutable
@ThreadSafe
public abstract class AbstractStorkAuthenticationRequest extends AbstractAuthenticationRequest
        implements IStorkAuthenticationRequest {

    /**
     * Abstract Builder pattern with self-bounding generics for {@link IStorkAuthenticationRequest} subtypes.
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
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends IStorkAuthenticationRequest>
            extends AbstractAuthenticationRequest.AbstractBuilder<B, T> {

        private String spId;

        private String spSector;

        private String spInstitution;

        private String spApplication;

        private int qaa;

        private boolean eidSectorShare;

        private boolean eidCrossSectorShare;

        private boolean eidCrossBorderShare;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractBuilder<?, ?> copy) {
            super(copy);
            spId = copy.spId;
            spSector = copy.spSector;
            spInstitution = copy.spInstitution;
            spApplication = copy.spApplication;
            qaa = copy.qaa;
            eidSectorShare = copy.eidSectorShare;
            eidCrossSectorShare = copy.eidCrossSectorShare;
            eidCrossBorderShare = copy.eidCrossBorderShare;
        }

        protected AbstractBuilder(@Nonnull IStorkAuthenticationRequest copy) {
            super(copy);
            spId = copy.getSpId();
            spSector = copy.getSpSector();
            spInstitution = copy.getSpInstitution();
            spApplication = copy.getSpApplication();
            qaa = copy.getQaa();
            eidSectorShare = copy.isEIDSectorShare();
            eidCrossSectorShare = copy.isEIDCrossSectorShare();
            eidCrossBorderShare = copy.isEIDCrossBorderShare();
        }

        @Nonnull
        public final B spId(String spId) {
            this.spId = spId;
            return (B) this;
        }

        @Nonnull
        public final B spSector(String spSector) {
            this.spSector = spSector;
            return (B) this;
        }

        @Nonnull
        public final B spInstitution(String spInstitution) {
            this.spInstitution = spInstitution;
            return (B) this;
        }

        @Nonnull
        public final B spApplication(String spApplication) {
            this.spApplication = spApplication;
            return (B) this;
        }

        @Nonnull
        public final B qaa(final int storkQaa) {
            this.qaa = storkQaa;
            return (B) this;
        }

        @Nonnull
        public final B eidCrossBorderShare(final boolean eidCrossBorderShare) {
            this.eidCrossBorderShare = eidCrossBorderShare;
            return (B) this;
        }

        @Nonnull
        public final B eidCrossBorderShare(final Boolean eidCrossBorderShare) {
            if (null != eidCrossBorderShare) {
                eidCrossBorderShare(eidCrossBorderShare.booleanValue());
            }
            return (B) this;
        }

        @Nonnull
        public final B eidCrossSectorShare(final boolean eidCrossSectorShare) {
            this.eidCrossSectorShare = eidCrossSectorShare;
            return (B) this;
        }

        @Nonnull
        public final B eidCrossSectorShare(final Boolean eidCrossSectorShare) {
            if (null != eidCrossSectorShare) {
                eidCrossSectorShare(eidCrossSectorShare.booleanValue());
            }
            return (B) this;
        }

        @Nonnull
        public final B eidSectorShare(final boolean eidSectorShare) {
            this.eidSectorShare = eidSectorShare;
            return (B) this;
        }

        @Nonnull
        public final B eidSectorShare(final Boolean eidSectorShare) {
            if (null != eidSectorShare) {
                eidSectorShare(eidSectorShare.booleanValue());
            }
            return (B) this;
        }

        /**
         * Validates the state of this Builder before allowing to create new instances of the built type {@code T}.
         *
         * @throws IllegalArgumentException if the builder is not in a legal state allowing to proceed with the creation
         * of a {@code T} instance.
         */
        @Override
        protected final void validate() throws IllegalArgumentException {
            Preconditions.checkWithinBounds(qaa, "qaa", 1, 4);
            validateOtherFields();
        }

        /**
         * Validates the state of this Builder before allowing to create new instances of the built type {@code T}.
         *
         * @throws IllegalArgumentException if the builder is not in a legal state allowing to proceed with the creation
         * of a {@code T} instance.
         */
        protected abstract void validateOtherFields() throws IllegalArgumentException;
    }

    private static final long serialVersionUID = -8736746751752334791L;

    /**
     * The Service Provider ID.
     * <p/>
     * TODO This is probably the "provider name". TODO rename
     * @serial
     */
    private final String spId;

    /**
     * The Service provider sector.
     * @serial
     */
    private final String spSector;

    /**
     * The Service provider institution.
     * @serial
     */
    private final String spInstitution;

    /**
     * The Service provider application name.
     * @serial
     */
    private final String spApplication;

    /**
     * The e id cross border share.
     * @serial
     */
    private final boolean eidCrossBorderShare;

    /**
     * The e id cross sector share.
     * @serial
     */
    private final boolean eidCrossSectorShare;

    /**
     * The e id sector share.
     * @serial
     */
    private final boolean eidSectorShare;

    /**
     * The quality of assurance.
     * @serial
     */
    private final int qaa;

    protected AbstractStorkAuthenticationRequest(@Nonnull AbstractBuilder<?, ?> builder) {
        super(builder);
        spId = builder.spId;
        spSector = builder.spSector;
        spInstitution = builder.spInstitution;
        spApplication = builder.spApplication;
        qaa = builder.qaa;
        eidCrossBorderShare = builder.eidCrossBorderShare;
        eidCrossSectorShare = builder.eidCrossSectorShare;
        eidSectorShare = builder.eidSectorShare;
    }

    @Override
    public final String getSpSector() {
        return spSector;
    }

    @Override
    public final String getSpInstitution() {
        return spInstitution;
    }

    @Override
    public final String getSpApplication() {
        return spApplication;
    }

    @Override
    public final boolean isEIDSectorShare() {
        return eidSectorShare;
    }

    @Override
    public final boolean isEIDCrossSectorShare() {
        return eidCrossSectorShare;
    }

    @Override
    public final boolean isEIDCrossBorderShare() {
        return eidCrossBorderShare;
    }

    @Override
    public final int getQaa() {
        return qaa;
    }

    @Override
    public final String getSpId() {
        return spId;
    }

    @Override
    protected StringBuilder toString(@Nonnull StringBuilder stringBuilder) {
        return toString(stringBuilder, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AbstractStorkAuthenticationRequest that = (AbstractStorkAuthenticationRequest) o;

        if (eidCrossBorderShare != that.eidCrossBorderShare) {
            return false;
        }
        if (eidCrossSectorShare != that.eidCrossSectorShare) {
            return false;
        }
        if (eidSectorShare != that.eidSectorShare) {
            return false;
        }
        if (qaa != that.qaa) {
            return false;
        }
        if (spId != null ? !spId.equals(that.spId) : that.spId != null) {
            return false;
        }
        if (spSector != null ? !spSector.equals(that.spSector) : that.spSector != null) {
            return false;
        }
        if (spInstitution != null ? !spInstitution.equals(that.spInstitution) : that.spInstitution != null) {
            return false;
        }
        return spApplication != null ? spApplication.equals(that.spApplication) : that.spApplication == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (spId != null ? spId.hashCode() : 0);
        result = 31 * result + (spSector != null ? spSector.hashCode() : 0);
        result = 31 * result + (spInstitution != null ? spInstitution.hashCode() : 0);
        result = 31 * result + (spApplication != null ? spApplication.hashCode() : 0);
        result = 31 * result + (eidCrossBorderShare ? 1 : 0);
        result = 31 * result + (eidCrossSectorShare ? 1 : 0);
        result = 31 * result + (eidSectorShare ? 1 : 0);
        result = 31 * result + qaa;
        return result;
    }

    public static StringBuilder toString(@Nonnull StringBuilder stringBuilder,
                                         @Nonnull IStorkAuthenticationRequest request) {
        return AbstractAuthenticationRequest.toString(stringBuilder, request)
                .append(", spId='")
                .append(request.getSpId())
                .append('\'')
                .append(", spSector='")
                .append(request.getSpSector())
                .append('\'')
                .append(", spInstitution='")
                .append(request.getSpInstitution())
                .append('\'')
                .append(", spApplication='")
                .append(request.getSpApplication())
                .append('\'')
                .append(", qaa='")
                .append(request.getQaa())
                .append('\'')
                .append(", eidCrossBorderShare='")
                .append(request.isEIDCrossBorderShare())
                .append('\'')
                .append(", eidCrossSectorShare='")
                .append(request.isEIDCrossSectorShare())
                .append('\'')
                .append(", eidSectorShare='")
                .append(request.isEIDSectorShare())
                .append('\'');
    }
}
