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
