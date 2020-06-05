/*
 * Copyright (c) 2016 by European Commission
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
package eu.eidas.auth.engine.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import eu.eidas.util.Preconditions;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;

import eu.eidas.auth.engine.ProtocolEngineI;

public class MetadataConfigParams {
    public static final String SP_ID_PREFIX="SP";
    public static final String IDP_ID_PREFIX="IDP";
    public static final String DEFAULT_LANG="en";

    private final boolean wantAssertionsSigned;
    private final boolean authnRequestsSigned;
    private final String assertionConsumerUrl;
    private final String entityID;
    private final Signature spSignature;
    private final Signature idpSignature;
    private final Credential idpEncryptionCredential;
    private final Credential idpSigningCredential;
    private final Credential spEncryptionCredential;
    private final Credential spSigningCredential;
    private final Set<String> protocolBinding;
    private final HashMap<String,String> protocolBindingLocation;
    //supported protocol: SAML 2
    private final String spSamlProtocol;
    private final String idpSamlProtocol;
    private final String emailAddress;
    private final ProtocolEngineI idpEngine;
    private final ProtocolEngineI spEngine;
    private final String assuranceLevel;
    private final String spType;
    private final String digestMethods;
    private final String signingMethods;
    private final String encryptionAlgorithms;
    private final long validityDuration;
    private final OrganizationData organization;
    private final ContactData supportContact;
    private final ContactData technicalContact;

    private final String eidasProtocolVersion;
    private final String eidasApplicationIdentifier;

    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder {
        /**
         * 24 hours in seconds
         */
        public static final long ONE_DAY_DURATION=86400;

        private boolean wantAssertionsSigned = true;
        private boolean authnRequestsSigned = true;
        private  String assertionConsumerUrl="";
        private  String entityID;
        private  Signature spSignature;
        private  Signature idpSignature;
        private  Credential idpEncryptionCredential;
        private  Credential idpSigningCredential;
        private  Credential spEncryptionCredential;
        private  Credential spSigningCredential;
        private  Set<String> protocolBinding=new HashSet<String>();
        HashMap<String,String> protocolBindingLocation=new HashMap<String,String>();
        //supported protocol: SAML 2
        private String spSamlProtocol= SAMLConstants.SAML20P_NS;
        private String idpSamlProtocol=SAMLConstants.SAML20P_NS;
        private String emailAddress;
        private ProtocolEngineI idpEngine;
        private ProtocolEngineI spEngine;
        private String assuranceLevel;
        private String spType;
        private String digestMethods;
        private String signingMethods;
        private String encryptionAlgorithms;
        private long validityDuration = ONE_DAY_DURATION;
        private OrganizationData organization;
        private ContactData supportContact;
        private ContactData technicalContact;
        private String eidasProtocolVersion;
        private String eidasApplicationIdentifier;

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            this.wantAssertionsSigned = copy.wantAssertionsSigned;
            this.authnRequestsSigned = copy.authnRequestsSigned;
            this.assertionConsumerUrl = copy.assertionConsumerUrl;
            this.entityID = copy.entityID;
            this.spSignature = copy.spSignature;
            this.idpSignature = copy.idpSignature;
            this.idpEncryptionCredential = copy.idpEncryptionCredential;
            this.idpSigningCredential = copy.idpSigningCredential;
            this.spEncryptionCredential = copy.spEncryptionCredential;
            this.spSigningCredential = copy.spSigningCredential;
            this.protocolBinding = new HashSet<String>(copy.protocolBinding);
            this.protocolBindingLocation = new HashMap<String,String>(copy.protocolBindingLocation);
            //supported protocol: SAML 2
            this.spSamlProtocol = SAMLConstants.SAML20P_NS;
            this.idpSamlProtocol = SAMLConstants.SAML20P_NS;
            this.emailAddress = copy.emailAddress;
            this.idpEngine = copy.idpEngine;
            this.spEngine = copy.spEngine;
            this.assuranceLevel = copy.assuranceLevel;
            this.spType = copy.spType;
            this.digestMethods = copy.digestMethods;
            this.signingMethods = copy.signingMethods;
            this.encryptionAlgorithms = copy.encryptionAlgorithms;
            this.validityDuration = copy.validityDuration;
            this.organization = new OrganizationData(copy.organization);
            this.supportContact = new ContactData(copy.supportContact);
            this.technicalContact  = new ContactData(copy.technicalContact);
            this.eidasProtocolVersion = copy.eidasProtocolVersion;
            this.eidasApplicationIdentifier = copy.eidasApplicationIdentifier;
        }

        public Builder(@Nonnull MetadataConfigParams copy) {
            this.wantAssertionsSigned = copy.wantAssertionsSigned;
            this.authnRequestsSigned = copy.authnRequestsSigned;
            this.assertionConsumerUrl = copy.assertionConsumerUrl;
            this.entityID = copy.entityID;
            this.spSignature = copy.spSignature;
            this.idpSignature = copy.idpSignature;
            this.idpEncryptionCredential = copy.idpEncryptionCredential;
            this.idpSigningCredential = copy.idpSigningCredential;
            this.spEncryptionCredential = copy.spEncryptionCredential;
            this.spSigningCredential = copy.spSigningCredential;
            this.protocolBinding = new HashSet<String>(copy.protocolBinding);
            this.protocolBindingLocation = new HashMap<String,String>(copy.protocolBindingLocation);
            this.spSamlProtocol = copy.spSamlProtocol;
            this.idpSamlProtocol = copy.idpSamlProtocol;
            this.emailAddress = copy.emailAddress;
            this.idpEngine = copy.idpEngine;
            this.spEngine = copy.spEngine;
            this.assuranceLevel = copy.assuranceLevel;
            this.spType = copy.spType;
            this.digestMethods = copy.digestMethods;
            this.signingMethods = copy.signingMethods;
            this.encryptionAlgorithms = copy.encryptionAlgorithms;
            this.validityDuration = copy.validityDuration;
            this.organization = new OrganizationData(copy.organization);
            this.supportContact = new ContactData(copy.supportContact);
            this.technicalContact  = new ContactData(copy.technicalContact);
            this.eidasProtocolVersion = copy.eidasProtocolVersion;
            this.eidasApplicationIdentifier = copy.eidasApplicationIdentifier;
        }

        public Builder wantAssertionsSigned(final boolean wantAssertionsSigned) {
            this.wantAssertionsSigned = wantAssertionsSigned;
            return this;
        }

        public Builder authnRequestsSigned(final boolean authnRequestsSigned) {
            this.authnRequestsSigned = authnRequestsSigned;
            return this;
        }

        public Builder assertionConsumerUrl(final String assertionConsumerUrl) {
            this.assertionConsumerUrl = assertionConsumerUrl;
            return this;
        }

        public Builder entityID(final String entityID) {
            this.entityID = entityID;
            return this;
        }

        public Builder spSignature(final Signature spSignature) {
            this.spSignature = spSignature;
            return this;
        }

        public Builder idpSignature(final Signature idpSignature) {
            this.idpSignature = idpSignature;
            return this;
        }

        public Builder idpEncryptionCredential(final Credential idpEncryptionCredential) {
            this.idpEncryptionCredential = idpEncryptionCredential;
            return this;
        }

        public Builder idpSigningCredential(final Credential idpSigningCredential) {
            this.idpSigningCredential = idpSigningCredential;
            return this;
        }

        public Builder spEncryptionCredential(final Credential spEncryptionCredential) {
            this.spEncryptionCredential = spEncryptionCredential;
            return this;
        }

        public Builder spSigningCredential(final Credential spSigningCredential) {
            this.spSigningCredential = spSigningCredential;
            return this;
        }

        public Builder protocolBinding(final HashSet<String> protocolBinding) {
            this.protocolBinding = new HashSet<String>(protocolBinding);
            return this;
        }

        public Builder addProtocolBinding(final String protocolBinding) {
            this.protocolBinding.add(protocolBinding);
            return this;
        }

        public Builder protocolBindingLocation(final HashMap<String,String> protocolBindingLocation) {
            this.protocolBindingLocation = new HashMap<String,String>(protocolBindingLocation);
            return this;
        }

        public Builder addProtocolBindingLocation(final String binding, String protocolBindingLocation) {
            this.protocolBindingLocation.put(binding, protocolBindingLocation);
            return this;
        }

        public Builder spSamlProtocol(final String spSamlProtocol) {
            this.spSamlProtocol = spSamlProtocol;
            return this;
        }

        public Builder idpSamlProtocol(final String idpSamlProtocol) {
            this.idpSamlProtocol = idpSamlProtocol;
            return this;
        }

        public Builder emailAddress(final String emailAddress) {
            this.idpSamlProtocol = emailAddress;
            return this;
        }

        public Builder idpEngine(final ProtocolEngineI idpEngine) {
            this.idpEngine = idpEngine;
            return this;
        }

        public Builder spEngine(final ProtocolEngineI spEngine) {
            this.spEngine = spEngine;
            return this;
        }

        public Builder assuranceLevel(final String assuranceLevel) {
            this.assuranceLevel = assuranceLevel;
            return this;
        }

        public Builder spType(final String spType) {
            this.spType = spType;
            return this;
        }

        public Builder digestMethods(final String digestMethods) {
            this.digestMethods = digestMethods;
            return this;
        }

        public Builder signingMethods(final String signingMethods) {
            this.signingMethods = signingMethods;
            return this;
        }

        public Builder encryptionAlgorithms(final String encryptionAlgorithms) {
            this.encryptionAlgorithms = encryptionAlgorithms;
            return this;
        }

        public Builder validityDuration(final long validityDuration) {
            if(validityDuration > 0 && validityDuration < Integer.MAX_VALUE) {
                this.validityDuration = validityDuration;
            }else{
                this.validityDuration = Builder.ONE_DAY_DURATION;
            }
            return this;
        }

        public Builder organization(final OrganizationData organization) {
            this.organization = new OrganizationData(organization);
            return this;
        }

        public Builder supportContact(final ContactData supportContact) {
            this.supportContact = new ContactData(supportContact);
            return this;
        }

        public Builder technicalContact(final ContactData technicalContact) {
            this.technicalContact = new ContactData(technicalContact);
            return this;
        }

        public Builder eidasProtocolVersion(String eidasProtocolVersion) {
            this.eidasProtocolVersion = eidasProtocolVersion;
            return this;
        }

        public Builder eidasApplicationIdentifier(String eidasApplicationIdentifier) {
            this.eidasApplicationIdentifier = eidasApplicationIdentifier;
            return this;
        }

        @Nonnull
        public MetadataConfigParams build() {
            validate();
            return new MetadataConfigParams(this);
        }

        private void validate() {
            Preconditions.checkNotNull(technicalContact, "Technical contact");
            Preconditions.checkNotNull(supportContact, "Support contact");
            Preconditions.checkNotNull(organization, "Organization data");
        }
    }

    private MetadataConfigParams(@Nonnull Builder builder) {
        this.wantAssertionsSigned = builder.wantAssertionsSigned;
        this.authnRequestsSigned = builder.authnRequestsSigned;
        this.assertionConsumerUrl = builder.assertionConsumerUrl;
        this.entityID = builder.entityID;
        this.spSignature = builder.spSignature;
        this.idpSignature = builder.idpSignature;
        this.idpEncryptionCredential = builder.idpEncryptionCredential;
        this.idpSigningCredential = builder.idpSigningCredential;
        this.spEncryptionCredential = builder.spEncryptionCredential;
        this.spSigningCredential = builder.spSigningCredential;
        this.protocolBinding = new HashSet<String>(builder.protocolBinding);
        this.protocolBindingLocation = new HashMap<String,String>(builder.protocolBindingLocation);
        this.spSamlProtocol = builder.spSamlProtocol;
        this.idpSamlProtocol = builder.idpSamlProtocol;
        this.emailAddress = builder.emailAddress;
        this.idpEngine = builder.idpEngine;
        this.spEngine = builder.spEngine;
        this.assuranceLevel = builder.assuranceLevel;
        this.spType = builder.spType;
        this.digestMethods = builder.digestMethods;
        this.signingMethods = builder.signingMethods;
        this.encryptionAlgorithms = builder.encryptionAlgorithms;
        this.validityDuration = builder.validityDuration;
        this.organization = new OrganizationData(builder.organization);
        this.supportContact = new ContactData(builder.supportContact);
        this.technicalContact  = new ContactData(builder.technicalContact);
        this.eidasProtocolVersion = builder.eidasProtocolVersion;
        this.eidasApplicationIdentifier = builder.eidasApplicationIdentifier;
    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull MetadataConfigParams copy) {
        return new Builder(copy);
    }

    public boolean isWantAssertionsSigned() {
        return wantAssertionsSigned;
    }

    public boolean isAuthnRequestsSigned() {
        return authnRequestsSigned;
    }

    public String getAssertionConsumerUrl() {
        return assertionConsumerUrl;
    }

    public String getEntityID() {
        return entityID;
    }

    public Signature getSpSignature() {
        return spSignature;
    }

    public Signature getIdpSignature() {
        return idpSignature;
    }

    public Credential getIdpEncryptionCredential() {
        return idpEncryptionCredential;
    }

    public Credential getIdpSigningCredential() {
        return idpSigningCredential;
    }

    public Credential getSpEncryptionCredential() {
        return spEncryptionCredential;
    }

    public Credential getSpSigningCredential() {
        return spSigningCredential;
    }

    public Set<String> getProtocolBinding() {
        return protocolBinding;
    }

    public HashMap<String, String> getProtocolBindingLocation() {
        return protocolBindingLocation;
    }

    public String getSpSamlProtocol() {
        return spSamlProtocol;
    }

    public String getIdpSamlProtocol() {
        return idpSamlProtocol;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public ProtocolEngineI getIdpEngine() {
        return idpEngine;
    }

    public ProtocolEngineI getSpEngine() {
        return spEngine;
    }

    public String getAssuranceLevel() {
        return assuranceLevel;
    }

    public String getSpType() {
        return spType;
    }

    public String getDigestMethods() {
        return digestMethods;
    }

    public String getSigningMethods() {
        return signingMethods;
    }

    public String getEncryptionAlgorithms() {
        return encryptionAlgorithms;
    }

    public long getValidityDuration() {
        return validityDuration;
    }

    public OrganizationData getOrganization() {
        return organization;
    }

    public ContactData getSupportContact() {
        return supportContact;
    }

    public ContactData getTechnicalContact() {
        return technicalContact;
    }

    public java.lang.String getEidasProtocolVersion() {
        return eidasProtocolVersion;
    }

    public String getEidasApplicationIdentifier() {
        return eidasApplicationIdentifier;
    }
}
