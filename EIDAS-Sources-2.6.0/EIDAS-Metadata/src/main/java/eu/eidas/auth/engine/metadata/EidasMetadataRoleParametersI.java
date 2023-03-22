/*
 * Copyright (c) 2020 by European Commission
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

import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

/**
 * This interface is representing either SP or IDP role descriptors accessible from {@link EidasMetadataParametersI}
 * Rhe role is determined by the {@link #getRole()} field.
 *
 * To create an instance invoke @see #eu.eidas.auth.engine.metadata.MetadataConfiguration.newRoleParametersInstance()
 *
 * Foreseen to be implemented as either proxy (embedded metadata service) or as DTO (external metadata service).
 *
 */
public interface EidasMetadataRoleParametersI extends Serializable {

    /**
     * Gets the roledescriptor's role
     * @return role
     */
    MetadataRole getRole();

    /**
     * Sets the Role to IDP or SP
     * @param role the roledescriptor's role
     */
    void setRole(MetadataRole role);

    /**
     * SP role: notify the IDP if the Assertion should be signed or not (eIDAS: signed)
     * @return true or false if the Assertion should be signed or not
     */
    boolean isWantAssertionsSigned();

    /**
     * IDP role: the SP asks for signed Assertion
     * @param wantAssertionsSigned boolean parameter
     */
    void setWantAssertionsSigned(boolean wantAssertionsSigned);

    /**
     * IDP role: notify the SP if the Request should be signed or not (eIDAS: signed)
     * @return true or false if the Request should be signed or not
     */
    boolean isAuthnRequestsSigned();

    /**
     * SP role: the IDP asks for signed requests
     * @param authnRequestsSigned boolean parameter
     */
    void setAuthnRequestsSigned(boolean authnRequestsSigned);

    /**
     * Returns with the certificate what is used for message (Assertion) encryption .
     * On generator side: own certificate containing the public key to be published
     * On consumer side (default usage): remote party's public key what this node must use to encrypt
     * @return X509 type certificate
     */
    X509Certificate getEncryptionCertificate();

    /**
     * Sets the certificate what is used for encryption.
     * On generator side (default usage): own certificate containing the public key to be published
     * On consumer side: remote party's public key what this node must use to encrypt
     *
     * @param encryptionCredential X509 type certificate
     */
    void setEncryptionCertificate(X509Certificate encryptionCredential);

    /**
     * Returns with the certificate what is used for message signing.
     * On generator side: own certificate containing the public key to be published
     * On consumer side (default usage): remote party's public key what this node must use to validate the signature of a message
     * @return X509 type certificate
     */
    X509Certificate getSigningCertificate();

    /**
     * Sets the certificate what is used for message signing.
     * On generator side (default usage): own certificate containing the public key to be published
     * On consumer side: remote party's public key what this node must use to validate the signature of a message
     * @param signingCredential X509 type certificate
     */
    void setSigningCertificate(X509Certificate signingCredential);

    /**
     * Returns with the list of possible SAML bindings offered. It is already translated to
     * {@link eu.eidas.auth.commons.BindingMethod} strings, and does not contain the SAML URI.
     * This is aligned with the map keys provided by {@link #getProtocolBindingLocations()}, so the
     * according setter can be used.
     * For Connector as consumer this holds binding methods for SingleSignOnService.
     * For Proxy Service as a consumer this holds binding methods for AssertionConsumerService.
     * @return set of strings from {@link eu.eidas.auth.commons.BindingMethod}
     */
    Set<String> getProtocolBindings();

    /**
     * This is a derived function, it returns with the location of AssertionConsumerService what
     * is marked as default in the Metadata.
     * @return default AssertionConsumerService url
     */
    String getDefaultAssertionConsumerUrl();

    /**
     * This is a derived function, it returns with the default binding method for an AssertionConsumerService.
     * @return one of {@link eu.eidas.auth.commons.BindingMethod} strings
     */
    String getDefaultBinding();

    /**
     * Sets the default binding for an AssertionConsumerService. The paramteres is an internal representation
     * and not the SAML URL ({@link eu.eidas.auth.commons.BindingMethod} strings)
     * @param defaultBinding one of {@link eu.eidas.auth.commons.BindingMethod} strings
     */
    void setDefaultBinding(String defaultBinding);

    /**
     * Returns with the list of possible SAML bindings and endpoint locations offered. The methods are
     * {@link eu.eidas.auth.commons.BindingMethod} strings, and does not contain the SAML URI.
     * This is aligned with the map keys provided by {@link #getProtocolBindings()}.
     * For Connector as consumer this holds binding methods / locations for SingleSignOnService.
     * For Proxy Service as a consumer this holds binding methods / locations for AssertionConsumerService.
     * @return the list of possible SAML bindings and endpoint locations offered.
     */
    Map<String, String> getProtocolBindingLocations();

    /**
     * Sets the protocol binding methods and locations. The methods are {@link eu.eidas.auth.commons.BindingMethod} strings,
     * and does not contain the SAML URI.
     * For Connector as producer this sets binding methods / locations for AssertionConsumerService.
     * For Proxy Service as a producer this sets binding methods / locations for SingleSignOnService.
     * @param protocolBindingLocation the list of possible SAML bindings and endpoint locations offered.
     */
    void setProtocolBindingLocations(Map<String, String> protocolBindingLocation);

    /**
     * Adds a new protocol binding method / location pair. The methods are {@link eu.eidas.auth.commons.BindingMethod} strings,
     * and does not contain the SAML URI.
     * For Connector as producer this adds binding method / location for AssertionConsumerService.
     * For Proxy Service as a producer this adds binding method / location for SingleSignOnService.
     * @param protocolBinding the protocol binding method
     * @param protocolBindingLocation the protocol binding location pair
     */
    void addProtocolBindingLocation(String protocolBinding, String protocolBindingLocation);

    /**
     * Returns with protocol(s) implemented by the other party. SAML url.
     * @return the protocol(s) implemented by the other party
     */
    String getSamlProtocol();

    /**
     * Sets the protocol(s) implemented by this Node. Saml url.
     * @param samlProtocol the protocol(s) implemented by this Node
     */
    void setSamlProtocol(String samlProtocol);

    /**
     * Returns with the encryption algorithms supported by the other party.
     * The result is a list of ISO defined URLs separated with a ";".
     * @return the encryption algorithms supported by the other party.
     */
    //TODO improve this method to handle list of strings
    String getEncryptionAlgorithms();

    /**
     * Sets the list of encryption algorithms supported by this node.
     * The string should contain ISO defined URLs separated with a ";".
     * @param encryptionAlgorithms the list of encryption algorithms supported by this node.
     */
    //TODO improve this method to handle list of strings
    void setEncryptionAlgorithms(String encryptionAlgorithms);

    /**
     * Returns with the list of Service supported attributes, list of full nameURIs as
     * defined in EIDAS profile.
     * @return the list of Service supported attributes
     */
    ImmutableSortedSet<String> getSupportedAttributes();

    /**
     * Sets the list of Service supported attributes, list of full nameURIs as
     * defined in EIDAS profile.
     * @param supportedAttributes the list of Service supported attributes
     */
    void setSupportedAttributes(ImmutableSortedSet<String> supportedAttributes);

    /**
     * Returns with the list of Service supported nameID formats, list of full URIs
     * @return the list of Service supported nameID Formats
     */
    Set<String> getNameIDFormats();

    /**
     * Sets the list of Service supported nameID formats, list of full URIs
     * @param nameIDFormats the list of Service supported nameID formats
     */
    void setNameIDFormats(Set<String> nameIDFormats);

}
