/*
 * Copyright (c) 2021 by European Commission
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

package eu.eidas.encryption.support;

import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.criteria.impl.EvaluableX509DigestCredentialCriterion;
import org.opensaml.security.credential.criteria.impl.EvaluableX509SubjectKeyIdentifierCredentialCriterion;
import org.opensaml.security.credential.criteria.impl.EvaluableX509SubjectNameCredentialCriterion;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.security.x509.X509DigestCriterion;
import org.opensaml.security.x509.X509IssuerSerialCriterion;
import org.opensaml.security.x509.X509SubjectKeyIdentifierCriterion;
import org.opensaml.security.x509.X509SubjectNameCriterion;
import org.opensaml.xmlsec.encryption.RecipientKeyInfo;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoResolutionContext;
import org.opensaml.xmlsec.keyinfo.impl.LocalKeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.X509Digest;
import org.opensaml.xmlsec.signature.X509IssuerSerial;
import org.opensaml.xmlsec.signature.X509SKI;
import org.opensaml.xmlsec.signature.X509SubjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * A simple specialization of {@link LocalKeyInfoCredentialResolver}
 * which is capable of using information from a {@link org.opensaml.xmlsec.signature.KeyInfo} to resolve
 * local credentials from a supplied {@link CredentialResolver} which manages local credentials.
 *
 * With a specific handling of RecipientKeyInfo elements to help the local credential resolver to associate
 * correctly the local credential with the one from the RecipientKeyInfo.
 * Implementation was based on se.swedenconnect.opensaml:opensaml-security-ext library.
 */
public class EidasKeyInfoCredentialResolver extends LocalKeyInfoCredentialResolver {

    /** Logger */
    private final Logger log = LoggerFactory.getLogger(EidasKeyInfoCredentialResolver.class);

    /**
     * Constructor.
     *
     * @param keyInfoProviders        the list of {@link KeyInfoProvider}s to use in this resolver
     * @param localCredentialResolver resolver of local credentials
     */
    public EidasKeyInfoCredentialResolver(@Nonnull List<KeyInfoProvider> keyInfoProviders,
            @Nonnull CredentialResolver localCredentialResolver) {
        super(keyInfoProviders, localCredentialResolver);
    }

    protected void postProcess(@Nonnull final KeyInfoResolutionContext kiContext,
            @Nullable final CriteriaSet criteriaSet, @Nonnull final List<Credential> credentials)
            throws ResolverException {
        if (kiContext.getKeyInfo() instanceof RecipientKeyInfo) {
            RecipientKeyInfo recipientKeyInfo = (RecipientKeyInfo) kiContext.getKeyInfo();
            processRecipientKeyInfo(recipientKeyInfo, credentials);
            return;
        } else {
            super.postProcess(kiContext, criteriaSet, credentials);
        }
    }

    private void processRecipientKeyInfo(@Nonnull final RecipientKeyInfo recipientKeyInfo,
            @Nonnull final List<Credential> credentials) throws ResolverException {
        final ArrayList<Credential> localCreds = new ArrayList<>();

        CriteriaSet recipientKeyCriterias = null;
        if (!credentials.isEmpty()) {
            if (credentials.get(0) instanceof X509Credential) {
                X509Credential recipientKeyInfoCredential = (X509Credential) credentials.get(0);
                recipientKeyCriterias = this.buildCriteriaSet(recipientKeyInfoCredential);
            }
        }
        if (recipientKeyCriterias == null || recipientKeyCriterias.isEmpty()) {
            recipientKeyCriterias = this.buildCriteriaSet(recipientKeyInfo);
        }
        for (final Credential cred : getLocalCredentialResolver().resolve(recipientKeyCriterias)) {
            if (isLocalCredential(cred)) {
                localCreds.add(cred);
            }
        }

        credentials.clear();
        credentials.addAll(localCreds);
    }

    /**
     * Based on the X509Credential, that should have been resolved from the RecipientKeyInfo, we create the
     * {@link CriteriaSet} to get the matching local credentials.
     *
     * @param credential
     *          the X509Credential that has been resolved from the RecipientKeyInfo
     * @return a criteria set
     */
    private CriteriaSet buildCriteriaSet(X509Credential credential) {
        CriteriaSet criterias = new CriteriaSet();

        // Certificates
        if (!credential.getEntityCertificateChain().isEmpty()) {
            List<X509Certificate> x509CertificateList = new ArrayList<>();
            x509CertificateList.addAll(credential.getEntityCertificateChain());
            criterias.add(new EvaluableX509CertificatesCredentialCriterion(x509CertificateList));
        }

        return criterias;
    }

    /**
     * Based on the {@code xenc:RecipientKeyInfo} (if available) we build a criteria set that helps us find the EC private
     * key to use. We make some simplifications. For example, we only handle one X509Data element.
     * </p>
     *
     * @param recipientKeyInfo
     *          the Recipient keyInfo element from the agreement method element
     * @return a criteria set
     */
    private CriteriaSet buildCriteriaSet(RecipientKeyInfo recipientKeyInfo) {
        CriteriaSet criterias = new CriteriaSet();

        final X509Data x509data = recipientKeyInfo.getX509Datas().get(0);
        if (x509data == null) {
            return criterias;
        }

        try {
            // Certificates
            if (!x509data.getX509Certificates().isEmpty()) {
                List<X509Certificate> certificatesList = CertificateUtil.getCertificates(recipientKeyInfo);
                criterias.add(new EvaluableX509CertificatesCredentialCriterion(certificatesList));
            }

            // Issuer and serial number
            if (!x509data.getX509IssuerSerials().isEmpty()) {
                final X509IssuerSerial is = x509data.getX509IssuerSerials().get(0);
                if (is.getX509IssuerName() != null && is.getX509SerialNumber() != null) {
                    criterias.add(new X509IssuerSerialCriterion(new X500Principal(is.getX509IssuerName().getValue()), is.getX509SerialNumber()
                            .getValue()));
                }
            }

            // Subject key info
            if (!x509data.getX509SKIs().isEmpty()) {
                final X509SKI ski = x509data.getX509SKIs().get(0);
                criterias.add(new EvaluableX509SubjectKeyIdentifierCredentialCriterion(new X509SubjectKeyIdentifierCriterion(Base64.getDecoder()
                        .decode(ski.getValue()))));
            }

            // Subject name
            if (!x509data.getX509SubjectNames().isEmpty()) {
                final X509SubjectName sn = x509data.getX509SubjectNames().get(0);
                criterias.add(new EvaluableX509SubjectNameCredentialCriterion(new X509SubjectNameCriterion(new X500Principal(sn.getValue()))));
            }

            // Digest
            if (!x509data.getX509Digests().isEmpty()) {
                final X509Digest digest = x509data.getX509Digests().get(0);
                criterias.add(new EvaluableX509DigestCredentialCriterion(new X509DigestCriterion(digest.getAlgorithm(), Base64.getDecoder()
                        .decode(
                                digest.getValue()))));
            }
        }
        catch (Exception e) {
            log.error("Error during building of criteria set during RecipientKeyInfo resolving - {}", e.getMessage(), e);
        }

        return criterias;
    }

}
