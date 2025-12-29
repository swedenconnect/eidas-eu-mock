/*
 * Copyright (c) 2021 by European Commission
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

package eu.eidas.auth.engine.core;

import org.opensaml.security.x509.X509Credential;

/**
 * Class that represents the needed context information for a signature.
 */
public class SigningContext {

    private X509Credential signingCredential;

    private String signingAlgorithm;

    private boolean isSignWithKeyValue;

    private SigningContext() {
    }

    public X509Credential getSigningCredential() {
        return signingCredential;
    }

    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public boolean isSignWithKeyValue() {
        return isSignWithKeyValue;
    }

    public static class Builder {

        private SigningContext signingContext;

        public Builder() {
            signingContext = new SigningContext();
        }

        public Builder setSigningCredential(X509Credential signingCredential) {
            signingContext.signingCredential = signingCredential;
            return this;
        }

        public Builder setSigningAlgorithm(String signingAlgorithm) {
            signingContext.signingAlgorithm = signingAlgorithm;
            return this;
        }

        public Builder setSignWithKeyValue(boolean isSignWithKeyValue) {
            signingContext.isSignWithKeyValue = isSignWithKeyValue;
            return this;
        }

        public SigningContext build() {
            return buildInternal();
        }

        /**
         * Private build method used to not return the same instance when builder is used multiple times.
         * @return a soft copy of the SigningContext instance variable.
         */
        private SigningContext buildInternal() {
            SigningContext copy = new SigningContext();
            copy.signingCredential = this.signingContext.signingCredential;
            copy.signingAlgorithm = this.signingContext.signingAlgorithm;
            copy.isSignWithKeyValue = this.signingContext.isSignWithKeyValue;
            return copy;
        }

    }
}
