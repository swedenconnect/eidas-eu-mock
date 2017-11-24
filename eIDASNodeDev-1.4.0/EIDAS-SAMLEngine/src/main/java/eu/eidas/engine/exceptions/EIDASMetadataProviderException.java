package eu.eidas.engine.exceptions;

/**
 * EIDASMetadataProviderException
 *
 * @since 1.1
 */
public class EIDASMetadataProviderException extends EIDASSAMLEngineException {

    public EIDASMetadataProviderException(String errorMessage) {
        super(errorMessage);
    }

    public EIDASMetadataProviderException(String message, Exception wrappedException) {
        super(message, wrappedException);
    }

    public EIDASMetadataProviderException(String newErrorCode, String errorMessage, String newErrorDetail) {
        super(newErrorCode, errorMessage, newErrorDetail);
    }

    public EIDASMetadataProviderException(String newErrorCode, String errorMessage, Exception wrappedException) {
        super(newErrorCode, errorMessage, wrappedException);
    }
}
