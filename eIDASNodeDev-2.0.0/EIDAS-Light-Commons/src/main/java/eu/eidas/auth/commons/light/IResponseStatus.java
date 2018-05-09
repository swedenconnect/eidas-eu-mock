package eu.eidas.auth.commons.light;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Interface representing the status of a response.
 * <p>
 * The status must contain a status code and may contain a sub-status code and a status message.
 * <p>
 * A response is either a success response or a failure response hence the corresponding flag {@link #isFailure()}.
 *
 * @since 1.1
 */
public interface IResponseStatus extends Serializable {

    /**
     * Returns the status code of the response.
     *
     * @return the status code of the response.
     */
    @Nonnull
    String getStatusCode();

    /**
     * Returns the detailed status message of the response.
     *
     * @return the detailed status message of the response.
     */
    @Nullable
    String getStatusMessage();

    /**
     * Returns the secondary status code of the response.
     *
     * @return the secondary status code of the response.
     */
    @Nullable
    String getSubStatusCode();

    /**
     * Returns {@code true} if the authentication failed, returns {@code false} otherwise.
     *
     * @return {@code true} if the authentication failed, returns {@code false} otherwise.
     */
    boolean isFailure();
}
