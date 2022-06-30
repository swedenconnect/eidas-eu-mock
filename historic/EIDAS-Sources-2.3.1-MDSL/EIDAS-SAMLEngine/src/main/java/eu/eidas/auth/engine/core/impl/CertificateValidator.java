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

package eu.eidas.auth.engine.core.impl;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * CertificateValidator
 *
 * @since 1.1
 */
public final class CertificateValidator {

    public static final String CHECK_VALIDITY_PERIOD_PROPERTY = "check_certificate_validity_period";

    public static final String DISALLOW_SELF_SIGNED_CERTIFICATE_PROPERTY = "disallow_self_signed_certificate";

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CertificateValidator.class);

    public static void checkCertificateIssuer(X509Certificate certificate) throws EIDASSAMLEngineException {
        if (CertificateUtil.isCertificateSelfSigned(certificate)) {
            LOG.error("ERROR : The certificate with reference '{}' failed check (selfsigned)",
                      certificate.getIssuerDN());
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_CERTIFICATE_SIGN.errorCode(),
                                               EidasErrorKey.INVALID_CERTIFICATE_SIGN.errorMessage());
        }
    }

    public static void checkCertificateIssuer(Map<?, ?> properties, X509Certificate certificate)
            throws EIDASSAMLEngineException {
        checkCertificateIssuer(isDisallowedSelfSignedCertificate(properties), certificate);
    }

    public static void checkCertificateIssuer(boolean isDisallowedSelfSignedCertificate, X509Certificate certificate)
            throws EIDASSAMLEngineException {
        if (isDisallowedSelfSignedCertificate) {
            checkCertificateIssuer(certificate);
        }
    }

    public static void checkCertificateValidityPeriod(X509Certificate certificate) throws EIDASSAMLEngineException {
        Date notBefore = certificate.getNotBefore();
        Date notAfter = certificate.getNotAfter();
        Date currentDate = Calendar.getInstance().getTime();
        if (currentDate.before(notBefore) || currentDate.after(notAfter)) {
            LOG.error(
                    "ERROR : The certificate with reference '{}' failed check (out of date) [notBefore={}, notAfter={}]",
                    certificate.getIssuerDN(), notBefore, notAfter);
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_CERTIFICATE_SIGN.errorCode(),
                                               EidasErrorKey.INVALID_CERTIFICATE_SIGN.errorMessage());
        }
    }

    public static void checkCertificateValidityPeriod(Map<?, ?> properties, X509Certificate certificate)
            throws EIDASSAMLEngineException {
        checkCertificateValidityPeriod(isCheckedValidityPeriod(properties), certificate);
    }

    public static void checkCertificateValidityPeriod(boolean isCheckedValidityPeriod, X509Certificate certificate)
            throws EIDASSAMLEngineException {
        if (isCheckedValidityPeriod) {
            checkCertificateValidityPeriod(certificate);
        }
    }

    public static boolean isCheckedValidityPeriod(Map<?, ?> properties) {
        return isEnabled(properties, CHECK_VALIDITY_PERIOD_PROPERTY, true);
    }

    public static boolean isDisallowedSelfSignedCertificate(Map<?, ?> properties) {
        return isEnabled(properties, DISALLOW_SELF_SIGNED_CERTIFICATE_PROPERTY, true);
    }

    public static boolean isEnabled(Map<?, ?> properties, String key, boolean defaultValue) {
        String property = (String) properties.get(key);
        if (StringUtils.isNotBlank(property)) {
            return Boolean.parseBoolean(property);
        } else {
            return defaultValue;
        }
    }

    private CertificateValidator() {
    }
}
