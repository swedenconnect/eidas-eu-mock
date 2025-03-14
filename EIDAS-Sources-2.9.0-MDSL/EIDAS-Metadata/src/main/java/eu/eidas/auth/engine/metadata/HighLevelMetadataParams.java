/*
 * Copyright (c) 2024 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package eu.eidas.auth.engine.metadata;

import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataParameters;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class HighLevelMetadataParams extends EidasMetadataParameters implements HighLevelMetadataParamsI {

    public HighLevelMetadataParams(EidasMetadataParametersI metadataState) {
        super(metadataState);
    }

    @Override
    public List<EidasProtocolVersion> getMetadataProtocolVersions() {
        return EidasProtocolVersion.fromString(super.getEidasProtocolVersions());
    }

    @Override
    @Nullable
    public X509Certificate getEncryptionCertificate() {
        return Optional.ofNullable(getSPRoleDescriptor().getEncryptionCertificates()).stream()
                .flatMap(Collection::stream)
                .findFirst().orElse(null);
    }

    @Deprecated
    @Override
    @Nullable
    public X509Certificate getRequestSignatureCertificate() {
        return Optional.ofNullable(getSPRoleDescriptor().getSigningCertificates()).stream()
                .flatMap(Collection::stream)
                .findFirst().orElse(null);
    }

    @Nullable
    @Override
    public X509Certificate getRequestSignatureCertificate(AuthnRequest samlMessage) {
        return getMatchingSigningCertificate(samlMessage, getSPRoleDescriptor().getSigningCertificates());
    }

    @Deprecated
    @Override
    @Nullable
    public X509Certificate getResponseSignatureCertificate() {
        return Optional.ofNullable(getIDPRoleDescriptor().getSigningCertificates()).stream()
                .flatMap(Collection::stream)
                .findFirst().orElse(null);
    }

    @Override
    @Nullable
    public X509Certificate getResponseSignatureCertificate(Response samlMessage) {
        return getMatchingSigningCertificate(samlMessage, getIDPRoleDescriptor().getSigningCertificates());
    }

    @Override
    @Nonnull
    @SuppressWarnings("squid:S2583")
    public Set<String> getSupportedAttributes() {
        return getIDPRoleDescriptor().getSupportedAttributes();
    }

    @Override
    public boolean isSendRequesterId(String requesterId) {
        return super.isRequesterIdFlag() && StringUtils.isNotEmpty(requesterId);
    }

    @Override
    @Nonnull
    public EidasMetadataRoleParametersI getIDPRoleDescriptor() {
        return getFirstRoleParameter(MetadataRole.IDP);
    }

    @Override
    @Nonnull
    public EidasMetadataRoleParametersI getSPRoleDescriptor() {
        return getFirstRoleParameter(MetadataRole.SP);
    }

    @Nullable
    private X509Certificate getMatchingSigningCertificate(
            SignableSAMLObject samlMessage,
            @Nonnull List<X509Certificate> metadataSigningCertificates
    ) {
        final Optional<KeyInfo> keyInfo = Optional.ofNullable(samlMessage.getSignature()).map(Signature::getKeyInfo);
        if (keyInfo.isPresent()) try {
            return CertificateUtil.getMatchingCertificate(keyInfo.get(), metadataSigningCertificates);
        } catch (CertificateException e) {
            throw new IllegalStateException(e);
        }
        return metadataSigningCertificates.stream().findFirst().orElse(null);
    }


    @Nonnull
    private EidasMetadataRoleParametersI getFirstRoleParameter(@Nonnull MetadataRole metadataRole) {
        EidasMetadataRoleParametersI roleParamter = null;
        for (EidasMetadataRoleParametersI role : super.getRoleDescriptors()) {
            if (metadataRole.equals(role.getRole())) {
                roleParamter = role;
                break;
            }
        }
        if(roleParamter == null) {

            throw new RuntimeException(String.format("Requested metadata is not of %s type",
                    (metadataRole.equals(MetadataRole.SP) ? "Connector": "") +
                    (metadataRole.equals(MetadataRole.IDP) ? "ProxyService": "")));
        }
        return roleParamter;
    }
}
