package eu.eidas.auth.engine.core.eidas;

import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xml.security.credential.UsageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * MetadataEncryptionHelper
 *
 * @since 1.1
 */
public final class MetadataEncryptionHelper extends AbstractMetadataHelper {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MetadataEncryptionHelper.class);

    public MetadataEncryptionHelper(@Nonnull MetadataFetcherI metadataFetcher,
                                    @Nonnull MetadataSignerI metadataSigner) {
        super(metadataFetcher, metadataSigner);
    }

    @Nullable
    public X509Certificate getEncryptionCertificate(@Nullable String requestIssuer) throws EIDASSAMLEngineException {
        return getMetadataEncryptionCertificate(requestIssuer);
    }

    private X509Certificate getMetadataEncryptionCertificate(RoleDescriptor rd) throws EIDASSAMLEngineException {
        for (KeyDescriptor kd : rd.getKeyDescriptors()) {
            if (kd.getUse() == UsageType.ENCRYPTION) {
                return CertificateUtil.toCertificate(kd.getKeyInfo());
            }
        }
        return null;
    }

    private X509Certificate getMetadataEncryptionCertificate(String metadataUrl) throws EIDASSAMLEngineException {
        SPSSODescriptor spssoDescriptor = getSPSSODescriptor(metadataUrl);
        if (spssoDescriptor == null) {
            LOG.info("METADATA EXCEPTION : cannot retrieve entity descriptor from url " + metadataUrl);
        } else {
            X509Certificate certificate = getMetadataEncryptionCertificate(spssoDescriptor);
            if (null != certificate) {
                return certificate;
            }
        }
        return null;
    }
}
