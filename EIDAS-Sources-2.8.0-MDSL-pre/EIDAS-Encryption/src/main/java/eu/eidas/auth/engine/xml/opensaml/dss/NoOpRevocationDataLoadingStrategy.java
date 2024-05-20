package eu.eidas.auth.engine.xml.opensaml.dss;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.x509.revocation.RevocationToken;
import eu.europa.esig.dss.validation.RevocationDataLoadingStrategy;

/**
 * Disable validation data loading (if we want to ignore revocation data check, there is no point to load it)
 * */
public class NoOpRevocationDataLoadingStrategy extends RevocationDataLoadingStrategy {
    @Override
    public RevocationToken getRevocationToken(final CertificateToken certificateToken, final CertificateToken issuerCertificateToken) {
        return null;
    }
}
