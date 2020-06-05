package eu.eidas.auth.engine.core.eidas;

import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
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
 * MetadataSignatureHelper
 *
 * @since 1.1
 */
public final class MetadataSignatureHelper extends AbstractMetadataHelper {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MetadataSignatureHelper.class);

    public MetadataSignatureHelper(@Nonnull MetadataFetcherI metadataFetcher, @Nonnull MetadataSignerI metadataSigner) {
        super(metadataFetcher, metadataSigner);
    }

    private X509Certificate getMetadataSignatureCertificate(RoleDescriptor rd) throws EIDASSAMLEngineException {
        for (KeyDescriptor kd : rd.getKeyDescriptors()) {
            if (kd.getUse() == UsageType.SIGNING) {
                return CertificateUtil.toCertificate(kd.getKeyInfo());
            }
        }
        return null;
    }

    @Nullable
    public X509Certificate getMetadataSignatureCertificate(SignableSAMLObject signableObject)
            throws EIDASSAMLEngineException {
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
            throws EIDASSAMLEngineException {
        if (StringUtils.isNotBlank(metadataUrl)) {
            SPSSODescriptor spssoDescriptor = getSPSSODescriptor(metadataUrl);
            if (null == spssoDescriptor) {
                LOG.info("METADATA EXCEPTION : cannot retrieve entity descriptor from url " + metadataUrl);
            } else {
                X509Certificate certificate = getMetadataSignatureCertificate(spssoDescriptor);
                if (null != certificate) {
                    return certificate;
                }
            }
        }
        return null;
    }

    @Nullable
    public X509Certificate getResponseSignatureCertificate(@Nullable String metadataUrl)
            throws EIDASSAMLEngineException {
        if (StringUtils.isNotBlank(metadataUrl)) {
            IDPSSODescriptor idpssoDescriptor = getIDPSSODescriptor(metadataUrl);
            if (null == idpssoDescriptor) {
                LOG.info("METADATA EXCEPTION : cannot retrieve entity descriptor from url " + metadataUrl);
            } else {
                X509Certificate certificate = getMetadataSignatureCertificate(idpssoDescriptor);
                if (null != certificate) {
                    return certificate;
                }
            }
        }
        return null;
    }
}
