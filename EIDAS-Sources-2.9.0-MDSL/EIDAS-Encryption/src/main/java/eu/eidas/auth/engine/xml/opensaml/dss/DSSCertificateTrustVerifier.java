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
package eu.eidas.auth.engine.xml.opensaml.dss;

import eu.eidas.auth.engine.xml.opensaml.CertificateTrustVerifier;
import eu.eidas.auth.engine.xml.opensaml.CertificateVerifierParams;
import eu.eidas.auth.engine.xml.opensaml.exception.CertificateRevokedException;
import eu.eidas.auth.engine.xml.opensaml.exception.MissingCertificateRevocationDataException;
import eu.eidas.auth.engine.xml.opensaml.exception.UntrustedCertificateException;
import eu.europa.esig.dss.alert.LogOnStatusAlert;
import eu.europa.esig.dss.alert.SilentOnStatusAlert;
import eu.europa.esig.dss.enumerations.CertificateSourceType;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.x509.CertificateReorderer;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLSource;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPSource;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignatureValidationContext;
import eu.europa.esig.dss.xml.common.SchemaFactoryBuilder;
import eu.europa.esig.dss.xml.common.XmlDefinerUtils;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.opensaml.security.x509.X509Credential;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DSSCertificateTrustVerifier implements CertificateTrustVerifier {
    private final static String EX_UNTRUSTED_CERT = "untrusted certificate";

    private final OCSPSource ocspSource;
    private final CRLSource crlSource;

    public DSSCertificateTrustVerifier() {
        final CommonsDataLoader dataLoader = new CommonsDataLoader();
        dataLoader.setTrustStrategy(TrustAllStrategy.INSTANCE);

        this.ocspSource = new OnlineOCSPSource(new OCSPDataLoader());
        this.crlSource = new OnlineCRLSource(dataLoader);
    }

    public DSSCertificateTrustVerifier(final OCSPSource ocspSource, final CRLSource crlSource) {
        this.ocspSource = ocspSource;
        this.crlSource = crlSource;
    }

    @Override
    public void verify(@Nonnull final X509Credential target,
                       @Nonnull final Collection<X509Certificate> trustAnchors,
                       @Nonnull final CertificateVerifierParams verifierParams) throws CertificateRevokedException, MissingCertificateRevocationDataException, UntrustedCertificateException {

        final CertificateToken certificateToValidate = new CertificateToken(target.getEntityCertificate());

        // Prepare the adjunct certificates to build the chain
        final CommonCertificateSource adjunctCertificates = this.createAdjunctCertificateSource(target.getEntityCertificateChain());

        // Add all trust anchors to trusted certificate store
        final CommonTrustedCertificateSource trustedCertificateSource = this.createTrustedCertificateSource(trustAnchors);

        final CertificateVerifier certificateVerifier = this.createDefaultVerifier(trustedCertificateSource, adjunctCertificates);

        this.configureVerifier(certificateVerifier, verifierParams);

        final SignatureValidationContext signatureValidationContext = new SignatureValidationContext();
        signatureValidationContext.initialize(certificateVerifier);

        signatureValidationContext.addCertificateTokenForVerification(certificateToValidate);

        signatureValidationContext.validate();

        signatureValidationContext.checkAllRequiredRevocationDataPresent();
        signatureValidationContext.checkCertificateNotRevoked(certificateToValidate);

        // Verify certificate chain
        final Collection<CertificateToken> processedCertificateSources = signatureValidationContext.getProcessedCertificates();
        final CertificateReorderer cr = new CertificateReorderer(processedCertificateSources);

        final Map<CertificateToken, List<CertificateToken>> orderedCertificateChains = cr.getOrderedCertificateChains();

        for (final CertificateToken certificateToken : orderedCertificateChains.get(certificateToValidate)) {
            final boolean isTrusted = signatureValidationContext.getAllCertificateSources()
                    .getCertificateSourceType(certificateToken)
                    .stream()
                    .anyMatch(CertificateSourceType::isTrusted);

            if (isTrusted) {
                return; // One valid trust anchor found, search can be stopped
            }
        }

        throw new UntrustedCertificateException(EX_UNTRUSTED_CERT);
    }

    private CommonTrustedCertificateSource createTrustedCertificateSource(final Collection<X509Certificate> trustAnchors) {
        final CommonTrustedCertificateSource trustedCertificateSource = new CommonTrustedCertificateSource();
        trustAnchors.stream()
                .map(CertificateToken::new)
                .forEach(trustedCertificateSource::addCertificate);

        return trustedCertificateSource;
    }

    private CommonCertificateSource createAdjunctCertificateSource(final Collection<X509Certificate> certificateChain) {
        final CommonCertificateSource adjunctCertificates = new CommonCertificateSource();
        certificateChain.stream()
                .map(CertificateToken::new)
                .forEach(adjunctCertificates::addCertificate);

        return adjunctCertificates;
    }

    private void configureVerifier(final CertificateVerifier certificateVerifier,
                                   final CertificateVerifierParams verifierParams) {
        if (!verifierParams.isCheckRevocation()) { // Disable revocation data loading
            certificateVerifier.setRevocationDataLoadingStrategyFactory(NoOpRevocationDataLoadingStrategy::new); // Do not download revocation data if it is disabled
            certificateVerifier.setAlertOnRevokedCertificate(new SilentOnStatusAlert());
            certificateVerifier.setAlertOnMissingRevocationData(new SilentOnStatusAlert());
        } else {
            certificateVerifier.setAlertOnRevokedCertificate(status -> {
                throw new CertificateRevokedException("The certificate is revoked: " + status.getErrorString());
            });
            certificateVerifier.setAlertOnMissingRevocationData(status -> {
                throw new MissingCertificateRevocationDataException("Revocation data are missing for one or more certificate: " + status.getErrorString());
            });
        }

        if (verifierParams.isSoftFailRevocation() && verifierParams.isCheckRevocation()) {
            certificateVerifier.setAlertOnMissingRevocationData(new LogOnStatusAlert());
        }
    }

    private CertificateVerifier createDefaultVerifier(final CommonTrustedCertificateSource trustedCertificateSource,
                                                      final CommonCertificateSource adjunctCertificates) {

        configureSecureSchemaFactoryBuilder();
        final CertificateVerifier certificateVerifier = new CommonCertificateVerifier();
        certificateVerifier.setAIASource(null);
        certificateVerifier.setOcspSource(this.ocspSource);
        certificateVerifier.setCrlSource(this.crlSource);
        certificateVerifier.addAdjunctCertSources(adjunctCertificates);
        certificateVerifier.addTrustedCertSources(trustedCertificateSource);

        return certificateVerifier;
    }


    /**
     * Configures the SchemaFactoryBuilder by removing attributes related to external entity resolution,
     * such as XMLConstants.ACCESS_EXTERNAL_DTD and XMLConstants.ACCESS_EXTERNAL_SCHEMA.
     */
    private void configureSecureSchemaFactoryBuilder() {
        XmlDefinerUtils.getInstance().setSchemaFactoryBuilder(
                SchemaFactoryBuilder.getSecureSchemaBuilder()
                        .removeAttribute(XMLConstants.ACCESS_EXTERNAL_DTD)
                        .removeAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA));
    }

}
