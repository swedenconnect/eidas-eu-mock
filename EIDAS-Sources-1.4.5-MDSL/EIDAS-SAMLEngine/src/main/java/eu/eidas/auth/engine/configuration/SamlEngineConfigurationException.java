package eu.eidas.auth.engine.configuration;

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * SamlEngineConfigurationException
 *
 * @since 1.1
 */
public class SamlEngineConfigurationException extends EIDASSAMLEngineException {

    public SamlEngineConfigurationException(Throwable cause) {
        super(cause);
    }

    public SamlEngineConfigurationException(String errorMessage) {
        super(errorMessage);
    }

    public SamlEngineConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SamlEngineConfigurationException(String newErrorCode, String errorMessage) {
        super(newErrorCode, errorMessage);
    }

    public SamlEngineConfigurationException(String newErrorCode, String errorMessage, Throwable cause) {
        super(newErrorCode, errorMessage, cause);
    }

    public SamlEngineConfigurationException(String newErrorCode, String errorMessage, String newErrorDetail) {
        super(newErrorCode, errorMessage, newErrorDetail);
    }
}
