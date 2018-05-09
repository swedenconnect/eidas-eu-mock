/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */
package eu.eidas.auth.engine.metadata.impl;

import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;

import javax.annotation.Nonnull;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EidasMetadataRoleParameters implements EidasMetadataRoleParametersI {

    protected MetadataRole role;
    protected boolean wantAssertionsSigned;
    protected boolean authnRequestsSigned;
    protected X509Certificate encryptionCertificate;
    protected X509Certificate signingCertificate;
    protected String samlProtocol;
    protected String encryptionAlgorithms;
    protected ImmutableSortedSet<String> supportedAttributes;
    protected String defaultBinding;
    protected Map<String, String> protocolBindingLocations = new HashMap<>();

    public EidasMetadataRoleParameters() {
    }

    public EidasMetadataRoleParameters(@Nonnull EidasMetadataRoleParameters emrp) {
        this.role = emrp.role;
        this.wantAssertionsSigned = emrp.wantAssertionsSigned;
        this.authnRequestsSigned = emrp.authnRequestsSigned;
        this.encryptionCertificate = emrp.encryptionCertificate;
        this.signingCertificate = emrp.signingCertificate;
        this.defaultBinding = emrp.defaultBinding;
        this.protocolBindingLocations = new HashMap<>(emrp.protocolBindingLocations);
        this.encryptionAlgorithms = emrp.encryptionAlgorithms;
        this.supportedAttributes = ImmutableSortedSet.copyOf(emrp.supportedAttributes);
    }

    public EidasMetadataRoleParameters(MetadataRole role, boolean wantAssertionsSigned,
                                       boolean authnRequestsSigned, X509Certificate encryptionCertificate, X509Certificate signingCertificate, String samlProtocol,
                                       String encryptionAlgorithms, ImmutableSortedSet<String> supportedAttributes,
                                       String defaultBinding, Map<String, String> protocolBindingLocations) {
        this.role = role;
        this.wantAssertionsSigned = wantAssertionsSigned;
        this.authnRequestsSigned = authnRequestsSigned;
        this.encryptionCertificate = encryptionCertificate;
        this.signingCertificate = signingCertificate;
        this.samlProtocol = samlProtocol;
        this.encryptionAlgorithms = encryptionAlgorithms;
        this.supportedAttributes = supportedAttributes;
        this.defaultBinding = defaultBinding;
        this.protocolBindingLocations = protocolBindingLocations;
    }

    public MetadataRole getRole() {
        return role;
    }

    public void setRole(MetadataRole role) {
        this.role = role;
    }

    @Override
    public boolean isWantAssertionsSigned() {
        return wantAssertionsSigned;
    }

    @Override
    public void setWantAssertionsSigned(boolean wantAssertionsSigned) {
        this.wantAssertionsSigned = wantAssertionsSigned;
    }

    @Override
    public boolean isAuthnRequestsSigned() {
        return authnRequestsSigned;
    }

    @Override
    public void setAuthnRequestsSigned(boolean authnRequestsSigned) {
        this.authnRequestsSigned = authnRequestsSigned;
    }

    @Override
    public String getDefaultAssertionConsumerUrl() {
        return getProtocolBindingLocations().get(getDefaultBinding());
    }

    @Override
    public X509Certificate getEncryptionCertificate() {
        return encryptionCertificate;
    }

    @Override
    public void setEncryptionCertificate(X509Certificate encryptionCertificate) {
        this.encryptionCertificate = encryptionCertificate;
    }

    @Override
    public X509Certificate getSigningCertificate() {
        return signingCertificate;
    }

    @Override
    public void setSigningCertificate(X509Certificate signingCertificate) {
        this.signingCertificate = signingCertificate;
    }

    @Override
    public String getDefaultBinding() {
        return defaultBinding;
    }

    @Override
    public void setDefaultBinding(String defaultBinding) {
        this.defaultBinding = defaultBinding;
    }
    @Override
    public Set<String> getProtocolBindings() {
        return getProtocolBindingLocations().keySet();
    }


    @Override
    public Map<String, String> getProtocolBindingLocations() {
        return protocolBindingLocations;
    }

    @Override
    public void setProtocolBindingLocations(Map<String, String> protocolBindingLocation) {
        this.protocolBindingLocations = protocolBindingLocation;
    }

    @Override
    public void addProtocolBindingLocation(String protocolBinding, String protocolBindingLocation) {
        if (this.protocolBindingLocations == null) {
            this.protocolBindingLocations = new HashMap<>();
        }
        this.protocolBindingLocations.put(protocolBinding, protocolBindingLocation);
    }

    @Override
    public String getSamlProtocol() {
        return samlProtocol;
    }

    @Override
    public void setSamlProtocol(String samlProtocol) {
        this.samlProtocol = samlProtocol;
    }

    @Override
    public String getEncryptionAlgorithms() {
        return encryptionAlgorithms;
    }

    @Override
    public void setEncryptionAlgorithms(String encryptionAlgorithms) {
        this.encryptionAlgorithms = encryptionAlgorithms;
    }

    @Override
    public ImmutableSortedSet<String> getSupportedAttributes() {
        return supportedAttributes;
    }

    @Override
    public void setSupportedAttributes(ImmutableSortedSet<String> supportedAttributes) {
        this.supportedAttributes = supportedAttributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        EidasMetadataRoleParameters that = (EidasMetadataRoleParameters) o;

        if (wantAssertionsSigned != that.wantAssertionsSigned)
            return false;
        if (authnRequestsSigned != that.authnRequestsSigned)
            return false;
        if (role != that.role)
            return false;
        if (defaultBinding != null ? !defaultBinding.equals(that.defaultBinding) : that.defaultBinding != null)
            return false;
        if (encryptionCertificate != null ? !encryptionCertificate.equals(that.encryptionCertificate) : that.encryptionCertificate != null)
            return false;
        if (signingCertificate != null ? !signingCertificate.equals(that.signingCertificate) : that.signingCertificate != null)
            return false;
        if (protocolBindingLocations != null ? !protocolBindingLocations.equals(that.protocolBindingLocations) : that.protocolBindingLocations != null)
            return false;
        if (samlProtocol != null ? !samlProtocol.equals(that.samlProtocol) : that.samlProtocol != null)
            return false;
        if (supportedAttributes != null ? !supportedAttributes.equals(that.supportedAttributes) : that.supportedAttributes != null)
            return false;
        return !(encryptionAlgorithms != null ? !encryptionAlgorithms.equals(that.encryptionAlgorithms) : that.encryptionAlgorithms != null);

    }

    @Override
    public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        result = 31 * result + (wantAssertionsSigned ? 1 : 0);
        result = 31 * result + (authnRequestsSigned ? 1 : 0);
        result = 31 * result + (defaultBinding != null ? defaultBinding.hashCode() : 0);
        result = 31 * result + (encryptionCertificate != null ? encryptionCertificate.hashCode() : 0);
        result = 31 * result + (signingCertificate != null ? signingCertificate.hashCode() : 0);
        result = 31 * result + (protocolBindingLocations != null ? protocolBindingLocations.hashCode() : 0);
        result = 31 * result + (samlProtocol != null ? samlProtocol.hashCode() : 0);
        result = 31 * result + (encryptionAlgorithms != null ? encryptionAlgorithms.hashCode() : 0);
        result = 31 * result + (supportedAttributes != null ? supportedAttributes.hashCode() : 0);
        return result;
    }
}
