package eu.eidas.auth.engine.configuration;

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * SamlEngineConfigurationException
 *
 * @since 1.1
 */
public class ProtocolEngineConfigurationException extends EIDASSAMLEngineException {

    public ProtocolEngineConfigurationException(Throwable cause) {
        super(cause);
    }

    public ProtocolEngineConfigurationException(String errorMessage) {
        super(errorMessage);
    }

    public ProtocolEngineConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolEngineConfigurationException(String newErrorCode, String errorMessage) {
        super(newErrorCode, errorMessage);
    }

    public ProtocolEngineConfigurationException(String newErrorCode, String errorMessage, Throwable cause) {
        super(newErrorCode, errorMessage, cause);
    }

    public ProtocolEngineConfigurationException(String newErrorCode, String errorMessage, String newErrorDetail) {
        super(newErrorCode, errorMessage, newErrorDetail);
    }
}
