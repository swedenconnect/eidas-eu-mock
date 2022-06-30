/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.commons.tx;

import java.io.ObjectStreamException;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.light.ILightRequest;

/**
 * An specific light request stored in a cache and to be remembered until the associated asynchronous response is sent
 * back.
 *
 * @since 1.1
 */
public final class StoredLightRequest extends AbstractStoredRequest<ILightRequest> {

    @SuppressWarnings("ParameterHidesMemberVariable")
    public static final class Builder extends AbstractBuilder<Builder, StoredLightRequest, ILightRequest> {

        public Builder() {
        }

        public Builder(@Nonnull StoredLightRequest copy) {
            super(copy);
        }

        public Builder(@Nonnull Builder copy) {
            super(copy);
        }

        @Override
        protected void validate() throws IllegalArgumentException {
            // ok with super
        }

        @Nonnull
        @Override
        protected StoredLightRequest newInstance() {
            return new StoredLightRequest(this);
        }
    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull StoredLightRequest copy) {
        return new Builder(copy);
    }

    private StoredLightRequest(@Nonnull Builder builder) {
        super(builder);
    }

    private Object readResolve() throws ObjectStreamException {
        return new Builder(this).build();
    }
}
