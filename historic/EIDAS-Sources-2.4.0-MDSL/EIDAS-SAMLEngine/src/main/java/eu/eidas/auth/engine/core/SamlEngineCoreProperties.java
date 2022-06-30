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
package eu.eidas.auth.engine.core;

import java.util.Set;

/**
 * SamlEngineCoreProperties
 *
 * @since 1.1
 */
public interface SamlEngineCoreProperties {

    String getConsentAuthnRequest();

    String getConsentAuthnResp();

    String getConsentAuthnResponse();

    String getFormatEntity();

    String getProperty(String key);

    String getProtocolBinding();

    String getRequester();

    String getResponder();

    Set<String> getSupportedMessageFormatNames();

    Integer getTimeNotOnOrAfter();

    String isEidCrossBordShare();

    String isEidCrossBorderShare();

    String isEidCrossSectShare();

    String isEidCrossSectorShare();

    String isEidSectorShare();

    boolean isIpValidation();

    boolean isOneTimeUse();

    boolean isValidateSignature();
}
