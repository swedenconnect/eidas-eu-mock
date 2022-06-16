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

import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.criteria.impl.EvaluableCredentialCriterion;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class EvaluableX509CertificatesCredentialCriterion implements EvaluableCredentialCriterion {

    /** Class logger. */
    private static final Logger log = LoggerFactory.getLogger(EvaluableX509CertificatesCredentialCriterion.class);

    /** The selectors we use to match certificates. */
    private List<X509CertSelector> selectors;

    /**
     * Constructor.
     *
     * @param certificates
     *          a list of certificate encodings (from a KeyInfo)
     */
    public EvaluableX509CertificatesCredentialCriterion(@Nonnull final List<X509Certificate> certificates) {
        assert certificates != null : "certificates must not be null";

        this.selectors = new ArrayList<>();
        for (X509Certificate certificate : certificates) {
            try {
                X509CertSelector selector = new X509CertSelector();
                selector.setCertificate(certificate);
                this.selectors.add(selector);
            } catch (Exception e) {
                log.error("Failed to decode certificate", e);
                continue;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean test(final Credential input) {
        if (input == null) {
            log.error("Credential input was null");
            return false;
        }
        else if (!X509Credential.class.isInstance(input)) {
            log.info("Credential is not an X509Credential, cannot evaluate certificate criteria");
            return false;
        }
        X509Certificate entityCertificate = ((X509Credential) input).getEntityCertificate();
        if (entityCertificate == null) {
            log.info("X509Credential did not contain an entity certificate, cannot evaluate certificate criteria");
            return false;
        }
        for (X509CertSelector selector : this.selectors) {
            if (selector.match(entityCertificate)) {
                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "EvaluableX509CertificatesCredentialCriterion [selectors=<contents not displayable>]";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.selectors == null ? 0 : this.selectors.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        EvaluableX509CertificatesCredentialCriterion other = (EvaluableX509CertificatesCredentialCriterion) obj;
        if (this.selectors == null) {
            if (other.selectors != null) {
                return false;
            }
        }
        else if (!this.selectors.equals(other.selectors)) {
            return false;
        }
        return true;
    }
}
