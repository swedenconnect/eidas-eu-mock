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
