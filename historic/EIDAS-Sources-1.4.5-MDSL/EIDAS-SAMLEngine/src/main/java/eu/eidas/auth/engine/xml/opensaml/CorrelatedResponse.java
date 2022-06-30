package eu.eidas.auth.engine.xml.opensaml;

import javax.annotation.Nonnull;

import org.opensaml.saml2.core.Response;

import eu.eidas.auth.engine.Correlated;
import eu.eidas.util.Preconditions;

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
