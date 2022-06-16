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

package eu.eidas.auth.engine.metadata;

import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.core.eidas.DigestMethod;
import eu.eidas.auth.engine.core.eidas.SigningMethod;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.auth.engine.metadata.samlobjects.NodeCountryType;
import eu.eidas.auth.engine.metadata.samlobjects.SPType;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.auth.engine.xml.opensaml.MetadataBuilderFactoryUtil;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.opensaml.core.xml.Namespace;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Company;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.ContactPersonTypeEnumeration;
import org.opensaml.saml.saml2.metadata.EmailAddress;
import org.opensaml.saml.saml2.metadata.EncryptionMethod;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.GivenName;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml.saml2.metadata.OrganizationName;
import org.opensaml.saml.saml2.metadata.OrganizationURL;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.SurName;
import org.opensaml.saml.saml2.metadata.TelephoneNumber;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Representing a full, final Eidas Metadata object built in XML, represented in String to be served over HTTP.
 * The internal Generator class is responsible to build this object from the provided information.
 */
public class EidasMetadata {

    public static final String ERROR_ERROR_GENERATING_THE_ORGANIZATION_DATA = "ERROR : error generating the OrganizationData: {}";
    private static final Logger LOGGER = LoggerFactory.getLogger(EidasMetadata.class.getName());
    private static final Set<String> DEFAULT_BINDING = new HashSet<>(Arrays.asList(SAMLConstants.SAML2_PAOS_BINDING_URI));

    public static final String PROTOCOL_VERSION_URI = "http://eidas.europa.eu/entity-attributes/protocol-version";
    public static final String APPLICATION_IDENTIFIER = "http://eidas.europa.eu/entity-attributes/application-identifier";

    static final String ENTITY_CATEGORY_ATTRIBUTE_NAME = "http://macedir.org/entity-category";
    static final String REQUESTER_ID_FLAG_VALUE = "http://eidas.europa.eu/entity-attributes/termsofaccess/requesterid";

    private final String metadata;
    private final boolean idpRole;
    private final boolean spRole;
    private final boolean wantAssertionsSigned;
    private final boolean authnRequestsSigned;
    private final String assertionConsumerUrl;
    private final String entityId;
    private final Signature spSignature;
    private final Signature idpSignature;
    private final Credential idpEncryptionCredential;
    private final Credential idpSigningCredential;
    private final Credential spEncryptionCredential;
    private final Credential spSigningCredential;
    private final Set<String> protocolBinding;
    private final HashMap<String, String> protocolBindingLocation;
    //supported protocol: SAML 2
    private final String spSamlProtocol;
    private final String idpSamlProtocol;
    private final String emailAddress;
    private final List<String> assuranceLevels;
    private final String spType;
    private final String nodeCountry;
    private final String digestMethods;
    private final String signingMethods;
    private final String encryptionAlgorithms;
    private final DateTime validityDuration;
    private final OrganizationData organization;
    private final ContactData supportContact;
    private final ContactData technicalContact;
    private final ImmutableSortedSet<String> supportedAttributes;
    private final boolean hideLoaType;

    private List<String> eidasApplicationIdentifiers;
    private List<String> eidasProtocolVersions;
    private String eidasReleaseVersion;

    private EidasMetadata(@Nonnull Generator generator) throws EIDASMetadataException {
        this.metadata = generator.metadata;
        this.wantAssertionsSigned = generator.wantAssertionsSigned;
        this.idpRole = generator.idpRole;
        this.spRole = generator.spRole;
        this.authnRequestsSigned = generator.authnRequestsSigned;
        this.assertionConsumerUrl = generator.assertionConsumerUrl;
        this.entityId = generator.entityId;
        this.spSignature = generator.spSignature;
        this.idpSignature = generator.idpSignature;
        this.idpEncryptionCredential = generator.idpEncryptionCredential;
        this.idpSigningCredential = generator.idpSigningCredential;
        this.spEncryptionCredential = generator.spEncryptionCredential;
        this.spSigningCredential = generator.spSigningCredential;
        this.protocolBinding = new HashSet<String>(generator.protocolBinding);
        this.protocolBindingLocation = new HashMap<String, String>(generator.protocolBindingLocation);
        this.spSamlProtocol = generator.spSamlProtocol;
        this.idpSamlProtocol = generator.idpSamlProtocol;
        this.emailAddress = generator.emailAddress;
        this.assuranceLevels = generator.assuranceLevels;
        this.spType = generator.spType;
        this.nodeCountry = generator.nodeCountry;
        this.digestMethods = generator.digestMethods;
        this.signingMethods = generator.signingMethods;
        this.encryptionAlgorithms = generator.encryptionAlgorithms;
        this.validityDuration = generator.validityDuration;
        this.organization = new OrganizationData(generator.organization);
        this.supportContact = new ContactData(generator.supportContact);
        this.technicalContact = new ContactData(generator.technicalContact);
        this.supportedAttributes = generator.supportedAttributes;
        this.hideLoaType = generator.hideLoaType;

        this.eidasProtocolVersions = generator.eidasProtocolVersions;
        this.eidasApplicationIdentifiers = generator.eidasApplicationIdentifiers;
    }

    @Nonnull
    public static Generator generator() {
        return new Generator();
    }

    @Nonnull
    public static Generator generator(@Nonnull Generator copy) {
        return new Generator(copy);
    }

    @Nonnull
    public static Generator generator(@Nonnull EidasMetadataParametersI emp) {
        return new Generator(emp);
    }

    public String getMetadata() {
        return metadata;
    }

    @NotThreadSafe
    @SuppressWarnings("ParameterHidesMemberVariable")
    public static final class Generator {

        /**
         * 24 hours in seconds
         */
        public static transient final int ONE_DAY_DURATION = 86400;
        public static transient final String DEFAULT_LANG = "en";
        private transient XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        private transient SPSSODescriptor spSSODescriptor = null;
        private transient IDPSSODescriptor idpSSODescriptor = null;
        private transient String metadata;
        private transient boolean idpRole = false;
        private transient boolean spRole = false;
        private transient boolean wantAssertionsSigned = true;
        private transient boolean authnRequestsSigned = true;
        private transient String assertionConsumerUrl = "";
        private transient String entityId;
        private transient Signature spSignature;
        private transient Signature idpSignature;
        private transient Credential idpEncryptionCredential;
        private transient Credential idpSigningCredential;
        private transient Credential spEncryptionCredential;
        private transient Credential spSigningCredential;
        private transient Set<String> protocolBinding = new HashSet<String>();
        private transient HashMap<String, String> protocolBindingLocation = new HashMap<String, String>();
        //supported protocol: SAML 2
        private transient String spSamlProtocol = SAMLConstants.SAML20P_NS;
        private transient String idpSamlProtocol = SAMLConstants.SAML20P_NS;
        private transient String emailAddress;
        private transient List<String> assuranceLevels;
        private transient String spType;
        private transient String nodeCountry;
        private transient String digestMethods;
        private transient String signingMethods;
        private transient String encryptionAlgorithms;
        private transient DateTime validityDuration;
        private transient OrganizationData organization;
        private transient ContactData supportContact;
        private transient ContactData technicalContact;
        private transient ImmutableSortedSet<String> supportedAttributes;
        private transient Set<String> additionalNameIDFormats;
        /*TODO: remove this attribute after trasition period*/
        private transient boolean hideLoaType;

        private transient List<String> eidasProtocolVersions;
        private transient List<String> eidasApplicationIdentifiers;

        private transient boolean requesterIdFlag;

        public Generator() {
        }

        public Generator(@Nonnull Generator copy) {
            Preconditions.checkNotNull(copy, "copy");
            this.spSSODescriptor = copy.spSSODescriptor;
            this.idpSSODescriptor = copy.idpSSODescriptor;
            this.idpRole = copy.idpRole;
            this.spRole = copy.spRole;
            this.wantAssertionsSigned = copy.wantAssertionsSigned;
            this.authnRequestsSigned = copy.authnRequestsSigned;
            this.assertionConsumerUrl = copy.assertionConsumerUrl;
            this.entityId = copy.entityId;
            this.spSignature = copy.spSignature;
            this.idpSignature = copy.idpSignature;
            this.idpEncryptionCredential = copy.idpEncryptionCredential;
            this.idpSigningCredential = copy.idpSigningCredential;
            this.spEncryptionCredential = copy.spEncryptionCredential;
            this.spSigningCredential = copy.spSigningCredential;
            this.protocolBinding = new HashSet<>(copy.protocolBinding);
            this.protocolBindingLocation = new HashMap<>(copy.protocolBindingLocation);
            //supported protocol: SAML 2
            this.spSamlProtocol = SAMLConstants.SAML20P_NS;
            this.idpSamlProtocol = SAMLConstants.SAML20P_NS;
            this.emailAddress = copy.emailAddress;
            this.assuranceLevels = copy.assuranceLevels;
            this.digestMethods = copy.digestMethods;
            this.signingMethods = copy.signingMethods;
            this.spType = copy.spType;
            this.nodeCountry = copy.nodeCountry;
            this.encryptionAlgorithms = copy.encryptionAlgorithms;
            this.validityDuration = copy.validityDuration;
            this.organization = new OrganizationData(copy.organization);
            this.supportContact = new ContactData(copy.supportContact);
            this.technicalContact = new ContactData(copy.technicalContact);
            supportedAttributes = copy.supportedAttributes;
            this.additionalNameIDFormats = copy.additionalNameIDFormats;
            this.hideLoaType = copy.hideLoaType;

            this.eidasProtocolVersions = copy.eidasProtocolVersions;
            this.eidasApplicationIdentifiers = copy.eidasApplicationIdentifiers;

            this.requesterIdFlag = copy.requesterIdFlag;
        }

        public Generator(@Nonnull EidasMetadataParametersI emp) {
            Preconditions.checkNotNull(emp, "EidasMetadataParameters");
            Preconditions.checkNotNull(emp.getRoleDescriptors(), "EidasMetadataRoleParameters");
            EidasMetadataRoleParametersI emrp = emp.getRoleDescriptors().iterator().next();
            this.idpRole = emrp.getRole().equals(MetadataRole.IDP);
            this.spRole = emrp.getRole().equals(MetadataRole.SP);
            this.wantAssertionsSigned = emrp.isWantAssertionsSigned();
            this.authnRequestsSigned = emrp.isAuthnRequestsSigned();
            this.assertionConsumerUrl = emrp.getDefaultAssertionConsumerUrl();
            this.entityId = emp.getEntityID();
            if (this.idpRole) {
                this.idpEncryptionCredential = CertificateUtil.toCredential(emrp.getEncryptionCertificate());
                this.idpSigningCredential = CertificateUtil.toCredential(emrp.getSigningCertificate());
            } else {
                this.spEncryptionCredential = CertificateUtil.toCredential(emrp.getEncryptionCertificate());
                this.spSigningCredential = CertificateUtil.toCredential(emrp.getSigningCertificate());
            }
            this.protocolBinding = new HashSet<>(emrp.getProtocolBindings());
            this.protocolBindingLocation = new HashMap<>(emrp.getProtocolBindingLocations());
            this.spSamlProtocol = SAMLConstants.SAML20P_NS;
            this.idpSamlProtocol = SAMLConstants.SAML20P_NS;
            this.assuranceLevels = emp.getAssuranceLevels();
            this.digestMethods = emp.getDigestMethods();
            this.signingMethods = emp.getSigningMethods();
            this.spType = emp.getSpType();
            this.nodeCountry = emp.getNodeCountry();
            this.encryptionAlgorithms = emrp.getEncryptionAlgorithms();
            this.validityDuration = emp.getValidUntil();
            this.organization = new OrganizationData(emp.getOrganization());
            this.supportContact = new ContactData(emp.getSupportContact());
            this.technicalContact = new ContactData(emp.getTechnicalContact());
            supportedAttributes = emrp.getSupportedAttributes();
            this.additionalNameIDFormats = getAdditionalNameIDFormats(emrp.getNameIDFormats());
            this.hideLoaType = emp.isHideLoaType();

            this.eidasProtocolVersions = emp.getEidasProtocolVersions();
            this.eidasApplicationIdentifiers = emp.getEidasApplicationIdentifiers();

            this.requesterIdFlag = emp.isRequesterIdFlag();
        }


        @Nonnull
        public EidasMetadata generate(MetadataSignerI signer) throws EIDASMetadataException {
            metadata = generateMetadata(signer);
            return new EidasMetadata(this);
        }

        private void generateDigest(final Extensions eidasExtensions) throws EIDASMetadataException {
            if (!StringUtils.isEmpty(digestMethods)) {
                List<String> digestMethodsList = EidasStringUtil.getDistinctValues(digestMethods);
                for (String digestMethod : digestMethodsList) {
                    final DigestMethod dm = (DigestMethod) MetadataBuilderFactoryUtil.buildXmlObject(DigestMethod.DEF_ELEMENT_NAME);
                    if (dm != null) {
                        dm.setAlgorithm(digestMethod);
                        eidasExtensions.getUnknownXMLObjects().add(dm);
                    } else {
                        LOGGER.info("BUSINESS EXCEPTION error adding DigestMethod extension");
                    }
                }
            }
        }

        private Extensions generateExtensions() throws EIDASMetadataException {
            final Extensions eidasExtensions = MetadataBuilderFactoryUtil.generateMetadataExtension();

            generateEntityAttributes(eidasExtensions);
            generateSpType(eidasExtensions);
            generateDigest(eidasExtensions);
            generateSigningMethods(eidasExtensions);

            return eidasExtensions;
        }

        private Extensions generateIDPSSODescriptorExtensions() throws EIDASMetadataException {
            final Extensions idpSSODescriptorExtensions = MetadataBuilderFactoryUtil.generateMetadataExtension();

            NodeCountryType nodeCountryType = generateNodeCountry();
            if (nodeCountryType != null) {
                idpSSODescriptorExtensions.getUnknownXMLObjects().add(nodeCountryType);
            }

            return idpSSODescriptorExtensions;
        }

        private Extensions generateSPSSODescriptorExtensions() throws EIDASMetadataException {
            final Extensions spSSODescriptorExtensions = MetadataBuilderFactoryUtil.generateMetadataExtension();

            NodeCountryType nodeCountryType = generateNodeCountry();
            if (nodeCountryType != null) {
                spSSODescriptorExtensions.getUnknownXMLObjects().add(nodeCountryType);
            }

            return spSSODescriptorExtensions;
        }

        private void generateSigningMethods(Extensions eidasExtensions) throws EIDASMetadataException {
            if (!StringUtils.isEmpty(signingMethods)) {
                List<String> signMethods = EidasStringUtil.getDistinctValues(signingMethods);
                for (String signMethod : signMethods) {
                    final SigningMethod sm =
                            (SigningMethod) MetadataBuilderFactoryUtil.buildXmlObject(SigningMethod.DEF_ELEMENT_NAME);
                    if (sm != null) {
                        sm.setAlgorithm(signMethod);
                        eidasExtensions.getUnknownXMLObjects().add(sm);
                    } else {
                        LOGGER.info("BUSINESS EXCEPTION error adding SigningMethod extension");
                    }
                }
            }
        }

        private void generateSpType(Extensions eidasExtensions) throws EIDASMetadataException {
            if (!StringUtils.isEmpty(spType)) {
                final SPType spTypeObj = (SPType) MetadataBuilderFactoryUtil.buildXmlObject(SPType.DEF_ELEMENT_NAME);
                if (spTypeObj != null) {
                    spTypeObj.setSPType(spType);
                    eidasExtensions.getUnknownXMLObjects().add(spTypeObj);
                } else {
                    LOGGER.info("BUSINESS EXCEPTION error adding SPType extension");
                }
            }
        }

        private NodeCountryType generateNodeCountry() throws EIDASMetadataException {
            NodeCountryType nodeCountryObj = null;
            if (!StringUtils.isEmpty(nodeCountry)) {
                nodeCountryObj = (NodeCountryType) MetadataBuilderFactoryUtil
                        .buildXmlObject(NodeCountryType.DEF_ELEMENT_NAME);
                if (nodeCountryObj != null) {
                    nodeCountryObj.setNodeCountry(nodeCountry);
                } else {
                    LOGGER.info("BUSINESS EXCEPTION error adding NodeCountryType extension");
                }
            }
            return nodeCountryObj;
        }

        private void generateEntityAttributes(final Extensions eidasExtensions) throws EIDASMetadataException {
            EntityAttributes entityAttributes =
                    (EntityAttributes) MetadataBuilderFactoryUtil.buildXmlObject(EntityAttributes.DEFAULT_ELEMENT_NAME);

            generateEidasProtocolVersionAttributes(entityAttributes);
            generateLoA(entityAttributes);

            if (requesterIdFlag) {
                final Attribute requesterIdFlagAttribute = generateRequesterIdFlagAttribute();
                entityAttributes.getAttributes().add(requesterIdFlagAttribute);
            }

            if (!entityAttributes.getAttributes().isEmpty()) {
                eidasExtensions.getUnknownXMLObjects().add(entityAttributes);
            }

        }

        private Attribute generateRequesterIdFlagAttribute() throws EIDASMetadataException {
            final ArrayList<String> attributeValues = new ArrayList<>();
            attributeValues.add(REQUESTER_ID_FLAG_VALUE);

            return buildSAMLAttribute(ENTITY_CATEGORY_ATTRIBUTE_NAME,attributeValues);
        }

        private void generateEidasProtocolVersionAttributes(final EntityAttributes entityAttributes) throws EIDASMetadataException {
            final Namespace saml = new Namespace(SAMLConstants.SAML20_NS , SAMLConstants.SAML20_PREFIX);
            entityAttributes.getNamespaceManager().registerNamespaceDeclaration(saml);

            final Attribute eidasProtocolVersionAttribute = buildSAMLAttribute(PROTOCOL_VERSION_URI, this.eidasProtocolVersions);
            if (null == eidasProtocolVersionAttribute) {
                LOGGER.info("BUSINESS EXCEPTION  eIDAS Protocol Version Attribute is empty");
            } else {
                entityAttributes.getAttributes().add(eidasProtocolVersionAttribute);
            }

            final Attribute eidasApplicationIdentifierAttribute = buildSAMLAttribute(APPLICATION_IDENTIFIER, this.eidasApplicationIdentifiers);
            if (null == eidasApplicationIdentifierAttribute) {
                LOGGER.info("BUSINESS EXCEPTION  eIDAS Application Identifier Attribute is empty");
            } else {
                entityAttributes.getAttributes().add(eidasApplicationIdentifierAttribute);
            }
        }

        private static Attribute buildSAMLAttribute(String attributeName, Collection<String> values) throws EIDASMetadataException {
            if (values != null && !values.isEmpty()) {
                final Attribute attribute = buildSAMLAttribute(attributeName);

                for (String val: values) {
                    final XSAny attributeValue = createSAMLAttributeValue(val);
                    attribute.getAttributeValues().add(attributeValue);
                }

                return attribute;
            } else {
                return null;
            }
        }

        private static Attribute buildSAMLAttribute(String attributeName) throws EIDASMetadataException {
            final Attribute attribute = (Attribute) MetadataBuilderFactoryUtil.buildXmlObject(Attribute.DEFAULT_ELEMENT_NAME);
            attribute.setNameFormat(RequestedAttribute.URI_REFERENCE);
            attribute.setName(attributeName);
            return attribute;
        }

        private static XSAny createSAMLAttributeValue(String textContent) {
            final XSAnyBuilder builder = new XSAnyBuilder();
            final XSAny attributeValue = builder.buildObject(SAMLConstants.SAML20_NS, org.opensaml.saml.saml2.core.AttributeValue.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);

            final Namespace namespace = new Namespace(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, net.shibboleth.utilities.java.support.xml.XMLConstants.XSI_PREFIX);
            attributeValue.getNamespaceManager().registerNamespaceDeclaration(namespace);

            final QName attribute_type = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", net.shibboleth.utilities.java.support.xml.XMLConstants.XSI_PREFIX);
            attributeValue.getUnknownAttributes().put(attribute_type, net.shibboleth.utilities.java.support.xml.XMLConstants.XSD_PREFIX + ":"+ XSString.TYPE_LOCAL_NAME);

            attributeValue.setTextContent(textContent);
            return attributeValue;
        }

        private static XMLObject buildSAMLAttributeValue(String contentValue, boolean stringType) {
            if (stringType) {
                return buildXSStringAttributeValue(contentValue);
            } else {
                return buildXSAnyAttributeValue(contentValue);
            }
        }

        private static XSAny buildXSAnyAttributeValue(String value) {
            XSAnyBuilder builder = new XSAnyBuilder();
            XSAny stringValue = builder.buildObject(SAMLConstants.SAML20_NS, org.opensaml.saml.saml2.core.AttributeValue.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
            stringValue.setTextContent(value);
            return stringValue;
        }

        private static XSString buildXSStringAttributeValue(String value) {
            XSStringBuilder stringBuilder = (XSStringBuilder) XMLObjectProviderRegistrySupport.getBuilderFactory()
                    .getBuilder(XSString.TYPE_NAME);
            XSString stringValue = stringBuilder.buildObject(org.opensaml.saml.saml2.core.AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            stringValue.setValue(value);
            return stringValue;
        }

        private void generateLoA(EntityAttributes entityAttributes) throws EIDASMetadataException {

            if (assuranceLevels == null || assuranceLevels.isEmpty()) {
                return;
            }

            Attribute loaAttrib = buildSAMLAttribute(MetadataUtil.LEVEL_OF_ASSURANCE_NAME);

            for (String assuranceLevel: assuranceLevels) {
                if (!assuranceLevel.isEmpty()) {
                    XMLObject loaAttributeValue = buildSAMLAttributeValue(assuranceLevel, !hideLoaType);
                    loaAttrib.getAttributeValues().add(loaAttributeValue);
                }
            }

            entityAttributes.getAttributes().add(loaAttrib);

        }

        private void addAssertionConsumerService() throws EIDASMetadataException {
            int index = 0;
            Set<String> bindings = protocolBinding.isEmpty() ? DEFAULT_BINDING : protocolBinding;
            for (String binding : bindings) {
                AssertionConsumerService asc = (AssertionConsumerService) MetadataBuilderFactoryUtil.buildXmlObject(
                        AssertionConsumerService.DEFAULT_ELEMENT_NAME);
                asc.setLocation(assertionConsumerUrl);
                asc.setBinding(checkBinding(binding));
                asc.setIndex(index);
                if (index == 0) {
                    asc.setIsDefault(true);
                }
                index++;
                spSSODescriptor.getAssertionConsumerServices().add(asc);
            }
        }

        private String checkBinding(String binding) {
            if (binding != null && (binding.equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI) || binding.equals(
                    SAMLConstants.SAML2_POST_BINDING_URI))) {
                return binding;
            }
            return SAMLConstants.SAML2_POST_BINDING_URI;
        }

        private void generateSupportedAttributes(IDPSSODescriptor idpssoDescriptor,
                                                 ImmutableSortedSet<String> attributeNames)
                throws EIDASMetadataException {
            List<Attribute> attributes = idpssoDescriptor.getAttributes();
            for (String attributeName : attributeNames) {
                Attribute a = (Attribute) MetadataBuilderFactoryUtil.buildXmlObject(Attribute.DEFAULT_ELEMENT_NAME);
                a.setName(attributeName);
                //a.setFriendlyName(attributeDefinition.getFriendlyName());
                a.setNameFormat(Attribute.URI_REFERENCE);
                attributes.add(a);
            }
        }

        private void generateSPSSODescriptor(final EntityDescriptor entityDescriptor,
                                             final X509KeyInfoGeneratorFactory keyInfoGeneratorFactory)
                throws org.opensaml.security.SecurityException, IllegalAccessException, NoSuchFieldException,
                EIDASMetadataException {
            //the node has SP role
            spSSODescriptor = MetadataBuilderFactoryUtil.buildXmlObject(SPSSODescriptor.class);
            spSSODescriptor.setWantAssertionsSigned(wantAssertionsSigned);
            spSSODescriptor.setAuthnRequestsSigned(true);
            if (spSignature != null) {
                spSSODescriptor.setSignature(spSignature);
            }
            if (spSigningCredential != null) {
                spSSODescriptor.getKeyDescriptors()
                        .add(getKeyDescriptor(keyInfoGeneratorFactory, spSigningCredential, UsageType.SIGNING));
            }
            if (spEncryptionCredential != null) {
                spSSODescriptor.getKeyDescriptors()
                        .add(getKeyDescriptor(keyInfoGeneratorFactory, spEncryptionCredential,
                                UsageType.ENCRYPTION));
            }
            spSSODescriptor.addSupportedProtocol(spSamlProtocol);
            if (!StringUtils.isEmpty(assertionConsumerUrl)) {
                addAssertionConsumerService();
            }
            addNameIdFormatsToDescriptor(spSSODescriptor, additionalNameIDFormats);
            Extensions spSSODescriptorExtensions = generateSPSSODescriptorExtensions();
            if (spSSODescriptorExtensions != null && !spSSODescriptorExtensions.getUnknownXMLObjects().isEmpty()) {
                spSSODescriptor.setExtensions(spSSODescriptorExtensions);
            }
            entityDescriptor.getRoleDescriptors().add(spSSODescriptor);

        }

        private void addNameIdFormatsToDescriptor(SSODescriptor ssoDescriptor, Set<String> additionalNameIdFormats) throws EIDASMetadataException {
            Set<String> validNameIDFormat = getMandatoryNameIDFormats();
            if (additionalNameIdFormats != null) {
                validNameIDFormat.addAll(additionalNameIdFormats);
            }

            for (String nameIDFormat : validNameIDFormat) {
                SamlNameIdFormat samlNameIdFormat = SamlNameIdFormat.fromString(nameIDFormat);
                if (samlNameIdFormat != null) {
                    NameIDFormat nameIDFormatElement = buildNameIDFormatElement(samlNameIdFormat);
                    ssoDescriptor.getNameIDFormats().add(nameIDFormatElement);
                } else {
                    LOGGER.warn(nameIDFormat + " is not a known nameIDFormat and is ignored");
                }
            }
        }

        private NameIDFormat buildNameIDFormatElement(SamlNameIdFormat samlNameIdFormat) throws EIDASMetadataException {
            NameIDFormat nameIDFormat = (NameIDFormat) MetadataBuilderFactoryUtil
                    .buildXmlObject(NameIDFormat.DEFAULT_ELEMENT_NAME);
            nameIDFormat.setFormat(samlNameIdFormat.getNameIdFormat());
            return nameIDFormat;
        }

        public static Set<String> getMandatoryNameIDFormats() {
            Set<String> mandatoryNameIDFormats = new HashSet<>();
            mandatoryNameIDFormats.add(NameIDType.PERSISTENT);
            mandatoryNameIDFormats.add(NameIDType.TRANSIENT);
            mandatoryNameIDFormats.add(NameIDType.UNSPECIFIED);
            return mandatoryNameIDFormats;
        }

        private Set<String> getAdditionalNameIDFormats(Set<String> nameIDFormats) {
            if (nameIDFormats == null || nameIDFormats.isEmpty()) {
                return Collections.emptySet();
            } else {
                Set<String> additionalNameIDFormats = new HashSet();
                additionalNameIDFormats.addAll(nameIDFormats);
                additionalNameIDFormats.removeAll(getMandatoryNameIDFormats());
                return additionalNameIDFormats;
            }
        }

        @SuppressWarnings("squid:S2583")
        private void generateIDPSSODescriptor(final EntityDescriptor entityDescriptor,
                                              final X509KeyInfoGeneratorFactory keyInfoGeneratorFactory,
                                              ImmutableSortedSet<String> attributeNames)
                throws org.opensaml.security.SecurityException, IllegalAccessException, NoSuchFieldException,
                EIDASMetadataException {
            //the node has IDP role
            idpSSODescriptor = MetadataBuilderFactoryUtil.buildXmlObject(IDPSSODescriptor.class);
            idpSSODescriptor.setWantAuthnRequestsSigned(true);
            if (idpSignature != null) {
                idpSSODescriptor.setSignature(idpSignature);
            }
            if (idpSigningCredential != null) {
                idpSSODescriptor.getKeyDescriptors()
                        .add(getKeyDescriptor(keyInfoGeneratorFactory, idpSigningCredential, UsageType.SIGNING));
            }
            if (idpEncryptionCredential != null) {
                idpSSODescriptor.getKeyDescriptors()
                        .add(getKeyDescriptor(keyInfoGeneratorFactory, idpEncryptionCredential,
                                UsageType.ENCRYPTION));
            }
            idpSSODescriptor.addSupportedProtocol(idpSamlProtocol);
            addNameIdFormatsToDescriptor(idpSSODescriptor, additionalNameIDFormats);
            idpSSODescriptor.getSingleSignOnServices().addAll(buildSingleSignOnServicesBindingLocations());
            generateSupportedAttributes(idpSSODescriptor, attributeNames);
            Extensions idpSSODescriptorExtensions = generateIDPSSODescriptorExtensions();
            if (idpSSODescriptorExtensions != null && !idpSSODescriptorExtensions.getUnknownXMLObjects().isEmpty()) {
                idpSSODescriptor.setExtensions(idpSSODescriptorExtensions);
            }
            entityDescriptor.getRoleDescriptors().add(idpSSODescriptor);

        }

        private ArrayList<SingleSignOnService> buildSingleSignOnServicesBindingLocations()
                throws NoSuchFieldException, IllegalAccessException {
            ArrayList<SingleSignOnService> singleSignOnServices = new ArrayList<>();

            HashMap<String, String> bindingLocations = protocolBindingLocation;
            Iterator<Map.Entry<String, String>> bindLocs = bindingLocations.entrySet().iterator();
            while (bindLocs.hasNext()) {
                Map.Entry<String, String> bindingLoc = bindLocs.next();
                SingleSignOnService ssos = MetadataBuilderFactoryUtil.buildXmlObject(SingleSignOnService.class);
                ssos.setBinding(bindingLoc.getKey());
                ssos.setLocation(bindingLoc.getValue());
                singleSignOnServices.add(ssos);
            }
            return singleSignOnServices;
        }

        private KeyDescriptor getKeyDescriptor(X509KeyInfoGeneratorFactory keyInfoGeneratorFactory,
                                               Credential credential,
                                               UsageType usage)
                throws NoSuchFieldException, IllegalAccessException, EIDASMetadataException, SecurityException {
            KeyDescriptor keyDescriptor = null;
            if (credential != null) {
                keyDescriptor = MetadataBuilderFactoryUtil.buildXmlObject(KeyDescriptor.class);
                KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();

                KeyInfo keyInfo = keyInfoGenerator.generate(credential);
                keyDescriptor.setUse(usage);
                keyDescriptor.setKeyInfo(keyInfo);
                if (usage == UsageType.ENCRYPTION && encryptionAlgorithms != null) {
                    List<String> encryptionAlgos = EidasStringUtil.getDistinctValues(encryptionAlgorithms);
                    for (String encryptionAlgo : encryptionAlgos) {
                        EncryptionMethod em =
                                (EncryptionMethod) MetadataBuilderFactoryUtil.buildXmlObject(EncryptionMethod.DEFAULT_ELEMENT_NAME);
                        em.setAlgorithm(encryptionAlgo);
                        keyDescriptor.getEncryptionMethods().add(em);
                    }
                }

            }
            return keyDescriptor;
        }

        private Organization buildOrganization() {
            Organization samlOrganization = null;
            if (organization != null) {
                try {
//                    TODO change code.....
                    samlOrganization = MetadataBuilderFactoryUtil.buildXmlObject(Organization.class);
                    OrganizationDisplayName odn = MetadataBuilderFactoryUtil.buildXmlObject(OrganizationDisplayName.class);
                    odn.setValue(organization.getDisplayName());
                    odn.setXMLLang(DEFAULT_LANG);
                    samlOrganization.getDisplayNames().add(odn);

                    OrganizationName on = MetadataBuilderFactoryUtil.buildXmlObject(OrganizationName.class);
                    on.setValue(organization.getName());
                    on.setXMLLang(DEFAULT_LANG);
                    samlOrganization.getOrganizationNames().add(on);

                    OrganizationURL url = MetadataBuilderFactoryUtil.buildXmlObject(OrganizationURL.class);
                    url.setValue(organization.getUrl());
                    url.setXMLLang(DEFAULT_LANG);
                    samlOrganization.getURLs().add(url);

                } catch (IllegalAccessException | NoSuchFieldException e) {
                    LOGGER.info(ERROR_ERROR_GENERATING_THE_ORGANIZATION_DATA, e.getMessage());
                    LOGGER.debug(ERROR_ERROR_GENERATING_THE_ORGANIZATION_DATA, e);
                }
            }
            return samlOrganization;
        }

        private ContactPerson buildContact(ContactPersonTypeEnumeration contactType) {
            ContactPerson contact = null;
            try {
                ContactData currentContact = null;
                if (contactType == ContactPersonTypeEnumeration.SUPPORT) {
                    currentContact = supportContact;
                } else if (contactType == ContactPersonTypeEnumeration.TECHNICAL) {
                    currentContact = technicalContact;
                } else {
                    LOGGER.error("ERROR: unsupported contact type");
                }
                contact = MetadataBuilderFactoryUtil.buildXmlObject(ContactPerson.class);
                if (currentContact == null) {
                    LOGGER.error("ERROR: cannot retrieve contact from the configuration");
                    return contact;
                }

                EmailAddress emailAddressObj = MetadataBuilderFactoryUtil.buildXmlObject(EmailAddress.class);
                Company company = MetadataBuilderFactoryUtil.buildXmlObject(Company.class);
                GivenName givenName = MetadataBuilderFactoryUtil.buildXmlObject(GivenName.class);
                SurName surName = MetadataBuilderFactoryUtil.buildXmlObject(SurName.class);
                TelephoneNumber phoneNumber = MetadataBuilderFactoryUtil.buildXmlObject(TelephoneNumber.class);
                contact.setType(contactType);
                emailAddressObj.setAddress(currentContact.getEmail());
                company.setName(currentContact.getCompany());
                givenName.setName(currentContact.getGivenName());
                surName.setName(currentContact.getSurName());
                phoneNumber.setNumber(currentContact.getPhone());

                populateContact(contact, currentContact, emailAddressObj, company, givenName, surName, phoneNumber);

            } catch (IllegalAccessException | NoSuchFieldException e) {
                LOGGER.info(ERROR_ERROR_GENERATING_THE_ORGANIZATION_DATA, e.getMessage());
                LOGGER.debug(ERROR_ERROR_GENERATING_THE_ORGANIZATION_DATA, e);
            }
            return contact;
        }

        private void populateContact(ContactPerson contact,
                                     ContactData currentContact,
                                     EmailAddress emailAddressObj,
                                     Company company,
                                     GivenName givenName,
                                     SurName surName,
                                     TelephoneNumber phoneNumber) {
            if (!StringUtils.isEmpty(currentContact.getEmail())) {
                contact.getEmailAddresses().add(emailAddressObj);
            }
            if (!StringUtils.isEmpty(currentContact.getCompany())) {
                contact.setCompany(company);
            }
            if (!StringUtils.isEmpty(currentContact.getGivenName())) {
                contact.setGivenName(givenName);
            }
            if (!StringUtils.isEmpty(currentContact.getSurName())) {
                contact.setSurName(surName);
            }
            if (!StringUtils.isEmpty(currentContact.getPhone())) {
                contact.getTelephoneNumbers().add(phoneNumber);
            }

        }

        private String generateMetadata(MetadataSignerI signer) {
            EntityDescriptor entityDescriptor;
            try {
                entityDescriptor = (EntityDescriptor) builderFactory.getBuilder(EntityDescriptor.DEFAULT_ELEMENT_NAME)
                        .buildObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);

                entityDescriptor.setEntityID(entityId);
                entityDescriptor.setOrganization(buildOrganization());
                entityDescriptor.getContactPersons().add(buildContact(ContactPersonTypeEnumeration.SUPPORT));
                entityDescriptor.getContactPersons().add(buildContact(ContactPersonTypeEnumeration.TECHNICAL));
                Instant validityDurationInstant = validityDuration != null ? Instant.ofEpochMilli(validityDuration.getMillis()) : null;
                entityDescriptor.setValidUntil(validityDurationInstant);

                X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
                keyInfoGeneratorFactory.setEmitEntityCertificate(true);
                Extensions e = generateExtensions();
                if (!e.getUnknownXMLObjects().isEmpty()) {
                    entityDescriptor.setExtensions(e);
                }
                if (spRole) {
                    generateSPSSODescriptor(entityDescriptor, keyInfoGeneratorFactory);
                }
                if (idpRole) {
                    generateIDPSSODescriptor(entityDescriptor, keyInfoGeneratorFactory, supportedAttributes);
                }
                if (signer != null) {
                    signer.signMetadata(entityDescriptor);
                }
                return EidasStringUtil.toString(OpenSamlHelper.marshall(entityDescriptor, false));
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | MarshallException
                    | EIDASMetadataException | SecurityException ex) {
                LOGGER.info("ERROR : SAMLException ", ex.getMessage());
                LOGGER.debug("ERROR : SAMLException ", ex);
                throw new IllegalStateException(ex);
            }
        }

        public Generator idpRole(final boolean idpRole) {
            this.idpRole = idpRole;
            return this;
        }

        public Generator spRole(final boolean spRole) {
            this.spRole = spRole;
            return this;
        }

        public Generator wantAssertionsSigned(final boolean wantAssertionsSigned) {
            this.wantAssertionsSigned = wantAssertionsSigned;
            return this;
        }

        public Generator authnRequestsSigned(final boolean authnRequestsSigned) {
            this.authnRequestsSigned = authnRequestsSigned;
            return this;
        }

        public Generator assertionConsumerUrl(final String assertionConsumerUrl) {
            this.assertionConsumerUrl = assertionConsumerUrl;
            return this;
        }

        public Generator entityID(final String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Generator spSignature(final Signature spSignature) {
            this.spSignature = spSignature;
            return this;
        }

        public Generator idpSignature(final Signature idpSignature) {
            this.idpSignature = idpSignature;
            return this;
        }

        public Generator idpEncryptionCredential(final Credential idpEncryptionCredential) {
            this.idpEncryptionCredential = idpEncryptionCredential;
            return this;
        }

        public Generator idpSigningCredential(final Credential idpSigningCredential) {
            this.idpSigningCredential = idpSigningCredential;
            return this;
        }

        public Generator spEncryptionCredential(final Credential spEncryptionCredential) {
            this.spEncryptionCredential = spEncryptionCredential;
            return this;
        }

        public Generator spSigningCredential(final Credential spSigningCredential) {
            this.spSigningCredential = spSigningCredential;
            return this;
        }

        public Generator protocolBinding(final HashSet<String> protocolBinding) {
            this.protocolBinding = new HashSet<String>(protocolBinding);
            return this;
        }

        public Generator addProtocolBinding(final String protocolBinding) {
            this.protocolBinding.add(protocolBinding);
            return this;
        }

        public Generator protocolBindingLocation(final HashMap<String, String> protocolBindingLocation) {
            this.protocolBindingLocation = new HashMap<String, String>(protocolBindingLocation);
            return this;
        }

        public Generator addProtocolBindingLocation(final String binding, String protocolBindingLocation) {
            this.protocolBindingLocation.put(binding, protocolBindingLocation);
            return this;
        }

        public Generator spSamlProtocol(final String spSamlProtocol) {
            this.spSamlProtocol = spSamlProtocol;
            return this;
        }

        public Generator idpSamlProtocol(final String idpSamlProtocol) {
            this.idpSamlProtocol = idpSamlProtocol;
            return this;
        }

        public Generator emailAddress(final String emailAddress) {
            this.idpSamlProtocol = emailAddress;
            return this;
        }

        public Generator assuranceLevel(final String assuranceLevel) {
            if (assuranceLevels == null) {
                assuranceLevels = new ArrayList<>();
            }
            this.assuranceLevels.add(assuranceLevel);
            return this;
        }

        public Generator assuranceLevels(final List<String> assuranceLevels) {
            this.assuranceLevels = assuranceLevels;
            return this;
        }

        public Generator spType(final String spType) {
            this.spType = spType;
            return this;
        }

        public Generator digestMethods(final String digestMethods) {
            this.digestMethods = digestMethods;
            return this;
        }

        public Generator signingMethods(final String signingMethods) {
            this.signingMethods = signingMethods;
            return this;
        }

        public Generator encryptionAlgorithms(final String encryptionAlgorithms) {
            this.encryptionAlgorithms = encryptionAlgorithms;
            return this;
        }

        public Generator validityDuration(final DateTime validityDuration) {
            if (validityDuration != null) {
                this.validityDuration = validityDuration;
            } else {
                DateTime expiryDate = DateTime.now();
                this.validityDuration = expiryDate.withFieldAdded(DurationFieldType.seconds(), (int) (Generator.ONE_DAY_DURATION));
            }
            return this;
        }

        public Generator organization(final OrganizationData organization) {
            this.organization = new OrganizationData(organization);
            return this;
        }

        public Generator supportContact(final ContactData supportContact) {
            this.supportContact = new ContactData(supportContact);
            return this;
        }

        public Generator technicalContact(final ContactData technicalContact) {
            this.technicalContact = new ContactData(technicalContact);
            return this;
        }

        public Generator supportedAttributes(ImmutableSortedSet<String> attributeNames) {
            this.supportedAttributes = attributeNames;
            return this;
        }
    }


}
