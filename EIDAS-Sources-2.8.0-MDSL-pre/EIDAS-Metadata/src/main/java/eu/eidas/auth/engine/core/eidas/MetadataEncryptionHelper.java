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

package eu.eidas.auth.engine.core.eidas;

import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.HighLevelMetadataParamsI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.engine.exceptions.EIDASMetadataException;
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
 * MetadataEncryptionHelper
 *
 * @since 1.1
 * @deprecated use {@link eu.eidas.auth.engine.metadata.HighLevelMetadataParamsI} instead
 */
@Deprecated
public final class MetadataEncryptionHelper extends AbstractMetadataHelper {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MetadataEncryptionHelper.class);

    public MetadataEncryptionHelper(@Nonnull MetadataFetcherI metadataFetcher,
                                    @Nonnull MetadataSignerI metadataSigner,
                                    @Nonnull MetadataClockI metadataClock) {
        super(metadataFetcher, metadataSigner, metadataClock);
    }

    /**
     * @deprecated use {@link HighLevelMetadataParamsI#getEncryptionCertificate()}}
     */
    @Deprecated
    @Nullable
    public X509Certificate getEncryptionCertificate(@Nullable String requestIssuer) throws EIDASMetadataException {
        return getMetadataEncryptionCertificate(requestIssuer);
    }

    /**
     * @deprecated use {@link HighLevelMetadataParamsI#getEncryptionCertificate()}}
     */
    @Deprecated
    private X509Certificate getMetadataEncryptionCertificate(RoleDescriptor rd) throws EIDASMetadataException {
        for (KeyDescriptor kd : rd.getKeyDescriptors()) {
            if (kd.getUse() == UsageType.ENCRYPTION) {
                try {
                    return CertificateUtil.toCertificate(kd.getKeyInfo());
                } catch (CertificateException e) {
                    throw new EIDASMetadataException(e);
                }
            }
        }
        return null;
    }

    /**
     * @deprecated use {@link HighLevelMetadataParamsI#getEncryptionCertificate()}}
     */
    @Deprecated
    private X509Certificate getMetadataEncryptionCertificate(String metadataUrl) throws EIDASMetadataException {
        EidasMetadataRoleParametersI spssoDescriptor = getSPSSODescriptor(metadataUrl);
        if (spssoDescriptor == null) {
            LOG.info("METADATA EXCEPTION : cannot retrieve entity descriptor from url " + metadataUrl);
        } else {
            X509Certificate certificate = spssoDescriptor.getEncryptionCertificate();
            if (null != certificate) {
                return certificate;
            }
        }
        return null;
    }
}
