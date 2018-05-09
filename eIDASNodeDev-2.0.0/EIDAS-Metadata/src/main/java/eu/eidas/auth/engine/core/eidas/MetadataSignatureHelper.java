package eu.eidas.auth.engine.core.eidas;

import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.security.credential.UsageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * MetadataSignatureHelper
 *
 * @since 1.1
 */
public final class MetadataSignatureHelper extends AbstractMetadataHelper {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MetadataSignatureHelper.class);

    public MetadataSignatureHelper(@Nonnull MetadataFetcherI metadataFetcher, @Nonnull MetadataSignerI metadataSigner, @Nonnull MetadataClockI metadataClock) {
        super(metadataFetcher, metadataSigner, metadataClock);
    }

    private X509Certificate getMetadataSignatureCertificate(RoleDescriptor rd) throws EIDASMetadataException {
        for (KeyDescriptor kd : rd.getKeyDescriptors()) {
            if (kd.getUse() == UsageType.SIGNING) {
                try {
                    return CertificateUtil.toCertificate(kd.getKeyInfo());
                } catch (CertificateException e) {
                    throw new EIDASMetadataException(e);
                }
            }
        }
        return null;
    }

    @Nullable
    public X509Certificate getMetadataSignatureCertificate(SignableSAMLObject signableObject)
            throws EIDASMetadataException {
        Issuer issuer = null;
        if (signableObject instanceof AuthnRequest) {
            issuer = ((AuthnRequest) signableObject).getIssuer();
            if (null != issuer) {
                return getRequestSignatureCertificate(issuer.getValue());
            }
        } else if (signableObject instanceof Response) {
            issuer = ((Response) signableObject).getIssuer();
            if (null != issuer) {
                return getResponseSignatureCertificate(issuer.getValue());
            }
        }
        return null;
    }

    @Nullable
    public X509Certificate getRequestSignatureCertificate(@Nullable String metadataUrl)
            throws EIDASMetadataException {
        if (StringUtils.isNotBlank(metadataUrl)) {
            EidasMetadataRoleParametersI spssoDescriptor = getSPSSODescriptor(metadataUrl);
            if (null == spssoDescriptor) {
                LOG.info("METADATA EXCEPTION : cannot retrieve entity descriptor from url " + metadataUrl);
            } else {
                X509Certificate certificate = spssoDescriptor.getSigningCertificate();
                if (null != certificate) {
                    return certificate;
                }
            }
        }
        return null;
    }

    @Nullable
    public X509Certificate getResponseSignatureCertificate(@Nullable String metadataUrl)
            throws EIDASMetadataException {
        if (StringUtils.isNotBlank(metadataUrl)) {
            EidasMetadataRoleParametersI idpssoDescriptor = getIDPSSODescriptor(metadataUrl);
            if (null == idpssoDescriptor) {
                LOG.info("METADATA EXCEPTION : cannot retrieve entity descriptor from url " + metadataUrl);
            } else {
                X509Certificate certificate = idpssoDescriptor.getSigningCertificate();
                if (null != certificate) {
                    return certificate;
                }
            }
        }
        return null;
    }
}
