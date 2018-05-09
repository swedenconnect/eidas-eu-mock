/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.auth.engine.Correlated;
import eu.eidas.util.Preconditions;
import org.opensaml.saml.saml2.core.Response;

import javax.annotation.Nonnull;

/**
 * CorrelatedResponse
 *
 * @since 1.1
 */
public final class CorrelatedResponse implements Correlated {

    @Nonnull
    private final Response response;

    public CorrelatedResponse(@Nonnull Response response) {
        Preconditions.checkNotNull(response, "response");
        this.response = response;
    }

    @Nonnull
    @Override
    public String getId() {
        return response.getID();
    }

    @Nonnull
    @Override
    public String getInResponseToId() {
        return response.getInResponseTo();
    }

    @Nonnull
    public Response getResponse() {
        return response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CorrelatedResponse that = (CorrelatedResponse) o;

        return response.equals(that.response);
    }

    @Override
    public int hashCode() {
        return response.hashCode();
    }

    @Override
    public String toString() {
        return "CorrelatedResponse{" +
                "response=" + response +
                '}';
    }
}
