package eu.eidas.engine.exceptions;

/**
 * EIDASMetadataProviderException
 *
 * @since 2.0.0
 */
public class EIDASMetadataProviderException extends EIDASMetadataException {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -3170936826098244230L;


    /**
     * Instantiates a new EIDASMetadataProviderException engine exception.
     *
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
     *              value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EIDASMetadataProviderException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new EIDASMetadataProviderException engine exception.
     *
     * @param errorMessage the error message
     */
    public EIDASMetadataProviderException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Instantiates a new EIDASMetadataProviderException engine exception.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
     */
    public EIDASMetadataProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new EIDASMetadataProviderException engine exception.
     *
     * @param newErrorCode the error code
     * @param errorMessage the error message
     */
    public EIDASMetadataProviderException(String newErrorCode, String errorMessage) {
        super(newErrorCode, errorMessage);
    }

    /**
     * Instantiates a new EIDASMetadataProviderException engine exception.
     *
     * @param newErrorCode the error code
     * @param errorMessage the error message
     * @param cause        the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
     */
    public EIDASMetadataProviderException(String newErrorCode, String errorMessage, Throwable cause) {
        super(newErrorCode, errorMessage, cause);
    }

    /**
     * Instantiates a new EIDASMetadataProviderException engine exception.
     *
     * @param newErrorCode   the error code
     * @param errorMessage   the error message
     * @param newErrorDetail the error detail
     */
    public EIDASMetadataProviderException(String newErrorCode, String errorMessage, String newErrorDetail) {
        super(newErrorCode, errorMessage, newErrorDetail);
    }
}
