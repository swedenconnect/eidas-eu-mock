package eu.eidas.auth.engine;

import javax.annotation.Nonnull;

/**
 * Mixin interface for any kind of responses which can be correlated to a request.
 *
 * @since 1.1
 */
public interface Correlated {

    /**
     * Returns this response ID.
     * <p>
     * This is a unique ID which must be used to prevent replay attacks.
     *
     * @return this response ID.
     */
    @Nonnull
    String getId();

    /**
     * Returns the ID of the request corresponding to this response.
     * <p>
     * This is the unique ID of the request which permits to correlate this response to the originating request.
     *
     * @return the ID of the request corresponding to this response.
     */
    @Nonnull
    String getInResponseToId();
}