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
package eu.eidas.auth.engine.metadata.impl;

import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import org.opensaml.saml.saml2.core.NameIDType;

import javax.annotation.Nonnull;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class EidasMetadataRoleParameters implements EidasMetadataRoleParametersI {

    protected MetadataRole role;
    protected boolean wantAssertionsSigned;
    protected boolean authnRequestsSigned;
    protected List<X509Certificate> encryptionCertificates = new ArrayList<>();
    protected List<X509Certificate> signingCertificates = new ArrayList<>();
    protected String samlProtocol;
    protected String encryptionAlgorithms;
    protected SortedSet<String> supportedAttributes;
    protected String defaultBinding;
    protected SortedSet<String> nameIDFormats;
    protected Map<String, String> protocolBindingLocations = new HashMap<>();

    public EidasMetadataRoleParameters() {
    }

    public EidasMetadataRoleParameters(@Nonnull EidasMetadataRoleParameters emrp) {
        this.role = emrp.role;
        this.wantAssertionsSigned = emrp.wantAssertionsSigned;
        this.authnRequestsSigned = emrp.authnRequestsSigned;
        this.encryptionCertificates = emrp.encryptionCertificates;
        this.signingCertificates = emrp.signingCertificates;
        this.defaultBinding = emrp.defaultBinding;
        this.protocolBindingLocations = new HashMap<>(emrp.protocolBindingLocations);
        this.encryptionAlgorithms = emrp.encryptionAlgorithms;
        this.supportedAttributes = new TreeSet<>(emrp.supportedAttributes);
    }

    public EidasMetadataRoleParameters(MetadataRole role, boolean wantAssertionsSigned,
                                       boolean authnRequestsSigned, List<X509Certificate> encryptionCertificates, List<X509Certificate> signingCertificates, String samlProtocol,
                                       String encryptionAlgorithms, SortedSet<String> supportedAttributes,
                                       String defaultBinding, Map<String, String> protocolBindingLocations) {
        this.role = role;
        this.wantAssertionsSigned = wantAssertionsSigned;
        this.authnRequestsSigned = authnRequestsSigned;
        this.encryptionCertificates = encryptionCertificates;
        this.signingCertificates = signingCertificates;
        this.samlProtocol = samlProtocol;
        this.encryptionAlgorithms = encryptionAlgorithms;
        this.supportedAttributes = supportedAttributes;
        this.defaultBinding = defaultBinding;
        this.protocolBindingLocations = protocolBindingLocations;
        this.nameIDFormats = new TreeSet<>(Arrays.asList(NameIDType.PERSISTENT, NameIDType.TRANSIENT, NameIDType.UNSPECIFIED));
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
    public List<X509Certificate> getEncryptionCertificates() {
        return encryptionCertificates;
    }

    @Deprecated
    @Override
    public void setEncryptionCertificate(X509Certificate encryptionCertificates) {
        this.encryptionCertificates = List.of(encryptionCertificates);
    }

    @Override
    public void setEncryptionCertificates(List<X509Certificate> encryptionCertificate) {
        this.encryptionCertificates = encryptionCertificate;
    }

    @Override
    public List<X509Certificate> getSigningCertificates() {
        return signingCertificates;
    }

    @Deprecated
    @Override
    public void setSigningCertificate(X509Certificate signingCertificates) {
        this.setSigningCertificates(List.of(signingCertificates));
    }

    @Override
    public void setSigningCertificates(Collection<X509Certificate> signingCertificate) {
        this.signingCertificates = new ArrayList<>(signingCertificate);
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
    public SortedSet<String> getSupportedAttributes() {
        return supportedAttributes;
    }

    @Override
    public void setSupportedAttributes(SortedSet<String> supportedAttributes) {
        this.supportedAttributes = supportedAttributes;
    }

    @Override
    public Set<String> getNameIDFormats() {
        return nameIDFormats;
    }

    @Override
    public void setNameIDFormats(Set<String> nameIDFormats) {
        if (nameIDFormats == null) {
            this.nameIDFormats = null;
        } else {
            this.nameIDFormats = new TreeSet<>(nameIDFormats);
        }
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
        if (encryptionCertificates != null ? !encryptionCertificates.equals(that.encryptionCertificates) : that.encryptionCertificates != null)
            return false;
        if (signingCertificates != null ? !signingCertificates.equals(that.signingCertificates) : that.signingCertificates != null)
            return false;
        if (protocolBindingLocations != null ? !protocolBindingLocations.equals(that.protocolBindingLocations) : that.protocolBindingLocations != null)
            return false;
        if (samlProtocol != null ? !samlProtocol.equals(that.samlProtocol) : that.samlProtocol != null)
            return false;
        if (supportedAttributes != null ? !supportedAttributes.equals(that.supportedAttributes) : that.supportedAttributes != null)
            return false;
        if (nameIDFormats != null ? !nameIDFormats.equals(that.nameIDFormats) : that.nameIDFormats != null)
            return false;
        return !(encryptionAlgorithms != null ? !encryptionAlgorithms.equals(that.encryptionAlgorithms) : that.encryptionAlgorithms != null);

    }

    @Override
    public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        result = 31 * result + (wantAssertionsSigned ? 1 : 0);
        result = 31 * result + (authnRequestsSigned ? 1 : 0);
        result = 31 * result + (defaultBinding != null ? defaultBinding.hashCode() : 0);
        result = 31 * result + (encryptionCertificates != null ? encryptionCertificates.hashCode() : 0);
        result = 31 * result + (signingCertificates != null ? signingCertificates.hashCode() : 0);
        result = 31 * result + (protocolBindingLocations != null ? protocolBindingLocations.hashCode() : 0);
        result = 31 * result + (samlProtocol != null ? samlProtocol.hashCode() : 0);
        result = 31 * result + (encryptionAlgorithms != null ? encryptionAlgorithms.hashCode() : 0);
        result = 31 * result + (supportedAttributes != null ? supportedAttributes.hashCode() : 0);
        result = 31 * result + (nameIDFormats != null ? nameIDFormats.hashCode() : 0);
        return result;
    }
}
