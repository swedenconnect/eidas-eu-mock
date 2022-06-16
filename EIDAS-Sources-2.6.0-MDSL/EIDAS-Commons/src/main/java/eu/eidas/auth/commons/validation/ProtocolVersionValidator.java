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

package eu.eidas.auth.commons.validation;

import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A base implementation for Protocol version validators
 *
 * @param <T> the type of value to validate
 */
public abstract class ProtocolVersionValidator<T> extends BaseValueValidator<T> {

    private List<EidasProtocolVersion> protocolVersions;

    protected List<EidasProtocolVersion> getProtocolVersions() {
        return protocolVersions;
    }

    /**
     * Builder class for a validator based on the protocol version.
     *
     * @param <V> the type of ProtocolVersionValidator to build.
     */
    public static abstract class Builder<B extends Builder, V extends ProtocolVersionValidator>
            extends BaseValueValidator.Builder<V> {

        private List<EidasProtocolVersion> protocolVersions = new ArrayList<>();

        public Builder(Supplier<V> supplier) {
            super(supplier);
        }

        /**
         * Set the protocol version to validate with
         * @param protocolVersion the eIDAS protocol version value.
         * @return this builder
         */
        public B protocolVersion(String protocolVersion) {
            EidasProtocolVersion eidasProtocolVersion = EidasProtocolVersion.fromString(protocolVersion);
            return protocolVersion(eidasProtocolVersion);
        }

        /**
         * Set the protocol version to validate with
         * @param protocolVersion the eIDAS protocol version
         * @return this builder
         */
        public B protocolVersion(EidasProtocolVersion protocolVersion) {
            this.protocolVersions.add(protocolVersion);
            return (B) this;
        }

        /**
         * Set the protocol version to validate with
         * @param protocolVersions the eIDAS protocol version
         * @return this builder
         */
        public B protocolVersions(List<EidasProtocolVersion> protocolVersions) {
            this.protocolVersions = protocolVersions;
            return (B) this;
        }

        /**
         * Constructs a ProtocolVersionValidator.
         * @return the build ProtocolVersionValidator
         */
        public V build() {
            V validator = super.build();
            ((ProtocolVersionValidator) validator).protocolVersions = this.protocolVersions;
            return validator;
        }
    }

}
