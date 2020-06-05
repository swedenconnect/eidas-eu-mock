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

import java.security.cert.X509Certificate;
import java.util.*;

import com.google.common.collect.ImmutableSortedSet;

import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.opensaml.Configuration;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.metadata.*;
import org.opensaml.samlext.saml2mdattr.EntityAttributes;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSAnyBuilder;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.KeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EIDASUtil;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.SAMLExtensionFormat;
import eu.eidas.auth.engine.core.eidas.DigestMethod;
import eu.eidas.auth.engine.core.eidas.EidasConstants;
import eu.eidas.auth.engine.core.eidas.SPType;
import eu.eidas.auth.engine.core.eidas.SigningMethod;
import eu.eidas.auth.engine.xml.opensaml.BuilderFactoryUtil;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.exceptions.SAMLEngineException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * Metadata generator class
 */
public class EidasMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(EidasMetadata.class.getName());

    private final String metadata;
    private final String entityId;

    private static final Set<String> DEFAULT_BINDING = new HashSet<String>() {{
        this.add(SAMLConstants.SAML2_POST_BINDING_URI);
    }};

    public static final String PROTOCOL_VERSION_URI = "http://eidas.europa.eu/entity-attributes/protocol-version";
    public static final String APPLICATION_IDENTIFIER = "http://eidas.europa.eu/entity-attributes/application-identifier";

    @NotThreadSafe
    public static final class Generator {

        private XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
        private MetadataConfigParams params;

        private SPSSODescriptor spSSODescriptor = null;
        private IDPSSODescriptor idpSSODescriptor = null;
        private String ssoLocation;

        private String metadata;
        private String entityId;

        public Generator() {
        }

        public Generator(@Nonnull Generator copy) {
            Preconditions.checkNotNull(copy, "copy");
            params = copy.params;
            spSSODescriptor = copy.spSSODescriptor;
            idpSSODescriptor = copy.idpSSODescriptor;
            ssoLocation = copy.ssoLocation;
            entityId = copy.entityId;
        }

        @Nonnull
        public EidasMetadata build() throws EIDASSAMLEngineException {
            initialize();
            entityId = params.getEntityID();
            metadata = generateMetadata();
            return new EidasMetadata(this);
        }

        public Generator configParams(MetadataConfigParams params) {
            this.params = params;
            return this;
        }

        private void generateDigest(Extensions eidasExtensions) throws EIDASSAMLEngineException {
            if (!StringUtils.isEmpty(params.getDigestMethods())) {
                Set<String> signatureMethods = EIDASUtil.parseSemicolonSeparatedList(params.getDigestMethods());
                Set<String> digestMethods = new HashSet<String>();
                for (String signatureMethod : signatureMethods) {
                    digestMethods.add(CertificateUtil.validateDigestAlgorithm(signatureMethod));
                }
                for (String digestMethod : digestMethods) {
                    final DigestMethod dm = (DigestMethod) BuilderFactoryUtil.buildXmlObject(DigestMethod.DEF_ELEMENT_NAME);
                    if (dm != null) {
                        dm.setAlgorithm(digestMethod);
                        eidasExtensions.getUnknownXMLObjects().add(dm);
                    } else {
                        LOGGER.info("BUSINESS EXCEPTION error adding DigestMethod extension");
                    }
                }
            }
        }

        private Extensions generateExtensions() throws EIDASSAMLEngineException {
            Extensions eidasExtensions = BuilderFactoryUtil.generateMetadataExtension();

            generateEntityAttributes(eidasExtensions);
            generateSpType(eidasExtensions);
            generateDigest(eidasExtensions);
            generateSigningMethods(eidasExtensions);

            return eidasExtensions;
        }

        private void generateEntityAttributes(Extensions eidasExtensions) throws EIDASSAMLEngineException {
            final EntityAttributes entityAttributes = (EntityAttributes) BuilderFactoryUtil.buildXmlObject(EntityAttributes.DEFAULT_ELEMENT_NAME);

            generateEidasProtocolVersionAttributes(entityAttributes);
            generateLoA(entityAttributes);

            if (!entityAttributes.getAttributes().isEmpty()) {
                eidasExtensions.getUnknownXMLObjects().add(entityAttributes);
            }

        }

        private void generateSpType(Extensions eidasExtensions) throws EIDASSAMLEngineException {
            if (!StringUtils.isEmpty(params.getSpType())) {
                final SPType spTypeObj = (SPType) BuilderFactoryUtil.buildXmlObject(SPType.DEF_ELEMENT_NAME);
                if (spTypeObj != null) {
                    spTypeObj.setSPType(params.getSpType());
                    eidasExtensions.getUnknownXMLObjects().add(spTypeObj);
                } else {
                    LOGGER.info("BUSINESS EXCEPTION error adding SPType extension");
                }
            }
        }

        private void generateSigningMethods(Extensions eidasExtensions) throws EIDASSAMLEngineException {
            if (!StringUtils.isEmpty(params.getSigningMethods())) {
                Set<String> signMethods = EIDASUtil.parseSemicolonSeparatedList(params.getDigestMethods());
                for (String signMethod : signMethods) {
                    final SigningMethod sm =
                            (SigningMethod) BuilderFactoryUtil.buildXmlObject(SigningMethod.DEF_ELEMENT_NAME);
                    if (sm != null) {
                        sm.setAlgorithm(signMethod);
                        eidasExtensions.getUnknownXMLObjects().add(sm);
                    } else {
                        LOGGER.info("BUSINESS EXCEPTION error adding SigningMethod extension");
                    }
                }
            }
        }

        private void generateEidasProtocolVersionAttributes(final EntityAttributes entityAttributes) throws EIDASSAMLEngineException {
            final Namespace saml = new Namespace(SAMLConstants.SAML20_NS , SAMLConstants.SAML20_PREFIX);
            entityAttributes.getNamespaceManager().registerNamespaceDeclaration(saml);

            final Attribute eidasProtocolVersionAttribute = buildEidasProtocolVersionAttribute(entityAttributes, PROTOCOL_VERSION_URI, params.getEidasProtocolVersion());
            if (null == eidasProtocolVersionAttribute) {
                LOGGER.info("BUSINESS EXCEPTION  eIDAS Protocol Version Attribute is empty");
            } else {
                entityAttributes.getAttributes().add(eidasProtocolVersionAttribute);
            }

            final Attribute eidasApplicationIdentifierAttribute = buildEidasProtocolVersionAttribute(entityAttributes, APPLICATION_IDENTIFIER, params.getEidasApplicationIdentifier());
            if (null == eidasApplicationIdentifierAttribute) {
                LOGGER.info("BUSINESS EXCEPTION  eIDAS Application Identifier Attribute is empty");
            } else {
                entityAttributes.getAttributes().add(eidasApplicationIdentifierAttribute);
            }
        }

        private static Attribute buildEidasProtocolVersionAttribute(EntityAttributes entityAttributes, String name, String value) throws EIDASSAMLEngineException {
            if (StringUtils.isNotEmpty(value)) {
                final Attribute attribute = (Attribute) BuilderFactoryUtil.buildXmlObject(Attribute.DEFAULT_ELEMENT_NAME);
                attribute.setNameFormat(RequestedAttribute.URI_REFERENCE);
                attribute.setName(name);

                final XSAnyBuilder builder = new XSAnyBuilder();
                final XSAny attributeValue = builder.buildObject(SAMLConstants.SAML20_NS, AttributeValue.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);

                final Namespace namespace = new Namespace(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, org.opensaml.xml.util.XMLConstants.XSI_PREFIX);
                attributeValue.getNamespaceManager().registerNamespaceDeclaration(namespace);

                final QName attribute_type = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type", org.opensaml.xml.util.XMLConstants.XSI_PREFIX);
                attributeValue.getUnknownAttributes().put(attribute_type,  org.opensaml.xml.util.XMLConstants.XSD_PREFIX + ":"+ XSString.TYPE_LOCAL_NAME);

                attributeValue.setTextContent(value);

                attribute.getAttributeValues().add(attributeValue);
                entityAttributes.getAttributes().add(attribute);

                return attribute;

            } else {
                return null;
            }
        }
        private void generateLoA(EntityAttributes entityAttributes) throws EIDASSAMLEngineException {

            if (StringUtils.isEmpty(params.getAssuranceLevel())) {
                return;
            }

            Attribute loaAttrib = (Attribute) BuilderFactoryUtil.buildXmlObject(Attribute.DEFAULT_ELEMENT_NAME);
            loaAttrib.setName(EidasConstants.LEVEL_OF_ASSURANCE_NAME);
            loaAttrib.setNameFormat(Attribute.URI_REFERENCE);
            XSStringBuilder stringBuilder =
                    (XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
            XSString stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            stringValue.setValue(params.getAssuranceLevel());
            loaAttrib.getAttributeValues().add(stringValue);
            entityAttributes.getAttributes().add(loaAttrib);
        }

        private void addAssertionConsumerService() throws EIDASSAMLEngineException {
            int index = 0;
            Set<String> bindings = params.getProtocolBinding().isEmpty() ? DEFAULT_BINDING : params.getProtocolBinding();
            for (String binding : bindings) {
                AssertionConsumerService asc = (AssertionConsumerService) BuilderFactoryUtil.buildXmlObject(
                        AssertionConsumerService.DEFAULT_ELEMENT_NAME);
                asc.setLocation(params.getAssertionConsumerUrl());
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

        private DateTime getExpireDate() {
            DateTime expiryDate = DateTime.now();
            expiryDate =
                    expiryDate.withFieldAdded(DurationFieldType.seconds(), (int) (params.getValidityDuration()));
            return expiryDate;
        }

        private void generateSupportedAttributes(IDPSSODescriptor idpssoDescriptor,
                                                 ImmutableSortedSet<AttributeDefinition<?>> attributeDefinitions)
                throws EIDASSAMLEngineException {
            List<Attribute> attributes = idpssoDescriptor.getAttributes();
            for (AttributeDefinition<?> attributeDefinition : attributeDefinitions) {
                Attribute a = (Attribute) BuilderFactoryUtil.buildXmlObject(Attribute.DEFAULT_ELEMENT_NAME);
                a.setName(attributeDefinition.getNameUri().toASCIIString());
                a.setFriendlyName(attributeDefinition.getFriendlyName());
                a.setNameFormat(Attribute.URI_REFERENCE);
                attributes.add(a);
            }
        }

        private void generateSPSSODescriptor(final EntityDescriptor entityDescriptor,
                                             final X509KeyInfoGeneratorFactory keyInfoGeneratorFactory)
                throws org.opensaml.xml.security.SecurityException, IllegalAccessException, NoSuchFieldException,
                SAMLEngineException, EIDASSAMLEngineException {
            //the node has SP role
            spSSODescriptor.setWantAssertionsSigned(params.isWantAssertionsSigned());
            spSSODescriptor.setAuthnRequestsSigned(true);
            if (params.getSpSignature() != null) {
                spSSODescriptor.setSignature(params.getSpSignature());
            }
            if (params.getSpSigningCredential() != null) {
                spSSODescriptor.getKeyDescriptors()
                        .add(getKeyDescriptor(keyInfoGeneratorFactory, params.getSpSigningCredential(), UsageType.SIGNING));
            }
            if (params.getSpEncryptionCredential() != null) {
                spSSODescriptor.getKeyDescriptors()
                        .add(getKeyDescriptor(keyInfoGeneratorFactory, params.getSpEncryptionCredential(),
                                UsageType.ENCRYPTION));
            }
            spSSODescriptor.addSupportedProtocol(params.getSpSamlProtocol());
            if (!StringUtils.isEmpty(params.getAssertionConsumerUrl())) {
                addAssertionConsumerService();
            }
            fillNameIDFormat(spSSODescriptor);
            entityDescriptor.getRoleDescriptors().add(spSSODescriptor);

        }

        private void fillNameIDFormat(SSODescriptor ssoDescriptor) throws EIDASSAMLEngineException {
            NameIDFormat persistentFormat =
                    (NameIDFormat) BuilderFactoryUtil.buildXmlObject(NameIDFormat.DEFAULT_ELEMENT_NAME);
            persistentFormat.setFormat(SamlNameIdFormat.PERSISTENT.getNameIdFormat());
            ssoDescriptor.getNameIDFormats().add(persistentFormat);
            NameIDFormat transientFormat =
                    (NameIDFormat) BuilderFactoryUtil.buildXmlObject(NameIDFormat.DEFAULT_ELEMENT_NAME);
            transientFormat.setFormat(SamlNameIdFormat.TRANSIENT.getNameIdFormat());
            ssoDescriptor.getNameIDFormats().add(transientFormat);
            NameIDFormat unspecifiedFormat =
                    (NameIDFormat) BuilderFactoryUtil.buildXmlObject(NameIDFormat.DEFAULT_ELEMENT_NAME);
            unspecifiedFormat.setFormat(SamlNameIdFormat.UNSPECIFIED.getNameIdFormat());
            ssoDescriptor.getNameIDFormats().add(unspecifiedFormat);
        }

        @SuppressWarnings("squid:S2583")
        private void generateIDPSSODescriptor(final EntityDescriptor entityDescriptor,
                                              final X509KeyInfoGeneratorFactory keyInfoGeneratorFactory)
                throws org.opensaml.xml.security.SecurityException, IllegalAccessException, NoSuchFieldException,
                SAMLEngineException, EIDASSAMLEngineException {
            //the node has IDP role
            idpSSODescriptor.setWantAuthnRequestsSigned(true);
            if (params.getIdpSignature() != null) {
                idpSSODescriptor.setSignature(params.getIdpSignature());
            }
            if (params.getIdpSigningCredential() != null) {
                idpSSODescriptor.getKeyDescriptors()
                        .add(getKeyDescriptor(keyInfoGeneratorFactory, params.getIdpSigningCredential(), UsageType.SIGNING));
            }
            if (params.getIdpEncryptionCredential() != null) {
                idpSSODescriptor.getKeyDescriptors()
                        .add(getKeyDescriptor(keyInfoGeneratorFactory, params.getIdpEncryptionCredential(),
                                UsageType.ENCRYPTION));
            }
            idpSSODescriptor.addSupportedProtocol(params.getIdpSamlProtocol());
            fillNameIDFormat(idpSSODescriptor);
            idpSSODescriptor.getSingleSignOnServices().addAll(buildSingleSignOnServicesBindingLocations());
            if (params.getIdpEngine() != null &&
                    (params.getIdpEngine().getProtocolProcessor() != null && params.getIdpEngine().getProtocolProcessor().getFormat() == SAMLExtensionFormat.EIDAS10)) {
                generateSupportedAttributes(idpSSODescriptor, params.getIdpEngine().getProtocolProcessor().getAllSupportedAttributes());
            }
            entityDescriptor.getRoleDescriptors().add(idpSSODescriptor);

        }

        private ArrayList<SingleSignOnService> buildSingleSignOnServicesBindingLocations()
                throws NoSuchFieldException, IllegalAccessException {
            ArrayList<SingleSignOnService> singleSignOnServices = new ArrayList<SingleSignOnService>();

            HashMap<String, String> bindingLocations = params.getProtocolBindingLocation();
            Iterator<Map.Entry<String, String>> bindLocs = bindingLocations.entrySet().iterator();
            while (bindLocs.hasNext()) {
                Map.Entry<String, String> bindingLoc = bindLocs.next();
                SingleSignOnService ssos = BuilderFactoryUtil.buildXmlObject(SingleSignOnService.class);
                ssos.setBinding(bindingLoc.getKey());
                ssos.setLocation(bindingLoc.getValue());
                singleSignOnServices.add(ssos);
            }
            return singleSignOnServices;
        }

        private KeyDescriptor getKeyDescriptor(X509KeyInfoGeneratorFactory keyInfoGeneratorFactory,
                                               Credential credential,
                                               UsageType usage)
                throws NoSuchFieldException, IllegalAccessException, SecurityException, EIDASSAMLEngineException {
            KeyDescriptor keyDescriptor = null;
            if (credential != null) {
                keyDescriptor = BuilderFactoryUtil.buildXmlObject(KeyDescriptor.class);
                KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();

                KeyInfo keyInfo = keyInfoGenerator.generate(credential);
                keyDescriptor.setUse(usage);
                keyDescriptor.setKeyInfo(keyInfo);
                if (usage == UsageType.ENCRYPTION && params.getEncryptionAlgorithms() != null) {
                    Set<String> encryptionAlgos = EIDASUtil.parseSemicolonSeparatedList(params.getEncryptionAlgorithms());
                    for (String encryptionAlgo : encryptionAlgos) {
                        EncryptionMethod em =
                                (EncryptionMethod) BuilderFactoryUtil.buildXmlObject(EncryptionMethod.DEFAULT_ELEMENT_NAME);
                        em.setAlgorithm(encryptionAlgo);
                        keyDescriptor.getEncryptionMethods().add(em);
                    }
                }

            }
            return keyDescriptor;
        }

        private Organization buildOrganization() {
            Organization organization = null;
            if (params.getOrganization() != null) {
                try {
                    organization = BuilderFactoryUtil.buildXmlObject(Organization.class);
                    OrganizationDisplayName odn = BuilderFactoryUtil.buildXmlObject(OrganizationDisplayName.class);
                    odn.setName(new LocalizedString(params.getOrganization().getDisplayName(), MetadataConfigParams.DEFAULT_LANG));
                    organization.getDisplayNames().add(odn);
                    OrganizationName on = BuilderFactoryUtil.buildXmlObject(OrganizationName.class);
                    on.setName(new LocalizedString(params.getOrganization().getName(), MetadataConfigParams.DEFAULT_LANG));
                    organization.getOrganizationNames().add(on);
                    OrganizationURL url = BuilderFactoryUtil.buildXmlObject(OrganizationURL.class);
                    url.setURL(new LocalizedString(params.getOrganization().getUrl(), MetadataConfigParams.DEFAULT_LANG));
                    organization.getURLs().add(url);
                } catch (IllegalAccessException iae) {
                    LOGGER.info("ERROR : error generating the OrganizationData: {}", iae.getMessage());
                    LOGGER.debug("ERROR : error generating the OrganizationData: {}", iae);
                } catch (NoSuchFieldException nfe) {
                    LOGGER.info("ERROR : error generating the OrganizationData: {}", nfe.getMessage());
                    LOGGER.debug("ERROR : error generating the OrganizationData: {}", nfe);
                }
            }
            return organization;
        }

        private ContactPerson buildContact(ContactPersonTypeEnumeration contactType) {
            ContactPerson contact = null;
            try {
                ContactData currentContact = null;
                if (contactType == ContactPersonTypeEnumeration.SUPPORT) {
                    currentContact = params.getSupportContact();
                } else if (contactType == ContactPersonTypeEnumeration.TECHNICAL) {
                    currentContact = params.getTechnicalContact();
                } else {
                    LOGGER.error("ERROR: unsupported contact type");
                }
                contact = BuilderFactoryUtil.buildXmlObject(ContactPerson.class);
                if (currentContact == null) {
                    LOGGER.error("ERROR: cannot retrieve contact from the configuration");
                    return contact;
                }

                EmailAddress emailAddressObj = BuilderFactoryUtil.buildXmlObject(EmailAddress.class);
                Company company = BuilderFactoryUtil.buildXmlObject(Company.class);
                GivenName givenName = BuilderFactoryUtil.buildXmlObject(GivenName.class);
                SurName surName = BuilderFactoryUtil.buildXmlObject(SurName.class);
                TelephoneNumber phoneNumber = BuilderFactoryUtil.buildXmlObject(TelephoneNumber.class);
                contact.setType(contactType);
                emailAddressObj.setAddress(currentContact.getEmail());
                company.setName(currentContact.getCompany());
                givenName.setName(currentContact.getGivenName());
                surName.setName(currentContact.getSurName());
                phoneNumber.setNumber(currentContact.getPhone());

                populateContact(contact, currentContact, emailAddressObj, company, givenName, surName, phoneNumber);

            } catch (IllegalAccessException iae) {
                LOGGER.info("ERROR : error generating the OrganizationData: {}", iae.getMessage());
                LOGGER.debug("ERROR : error generating the OrganizationData: {}", iae);
            } catch (NoSuchFieldException nfe) {
                LOGGER.info("ERROR : error generating the OrganizationData: {}", nfe.getMessage());
                LOGGER.debug("ERROR : error generating the OrganizationData: {}", nfe);
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

        private String generateMetadata() throws EIDASSAMLEngineException {
            EntityDescriptor entityDescriptor;
            try {
                entityDescriptor = (EntityDescriptor) builderFactory.getBuilder(EntityDescriptor.DEFAULT_ELEMENT_NAME)
                        .buildObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);

                entityDescriptor.setEntityID(params.getEntityID());
                entityDescriptor.setOrganization(buildOrganization());
                entityDescriptor.getContactPersons().add(buildContact(ContactPersonTypeEnumeration.SUPPORT));
                entityDescriptor.getContactPersons().add(buildContact(ContactPersonTypeEnumeration.TECHNICAL));
                entityDescriptor.setValidUntil(getExpireDate());

                X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
                keyInfoGeneratorFactory.setEmitEntityCertificate(true);
                Extensions e = generateExtensions();
                if (!e.getUnknownXMLObjects().isEmpty()) {
                    entityDescriptor.setExtensions(e);
                }
                if (spSSODescriptor != null) {
                    generateSPSSODescriptor(entityDescriptor, keyInfoGeneratorFactory);
                }
                if (idpSSODescriptor != null) {
                    generateIDPSSODescriptor(entityDescriptor, keyInfoGeneratorFactory);
                }
                if (params.getSpEngine() != null) {
                    ProtocolEngineI spEngine = params.getSpEngine();
                    ((MetadataSignerI) spEngine.getSigner()).signMetadata(entityDescriptor);
                } else if (params.getIdpEngine() != null) {
                    ProtocolEngineI idpEngine = params.getIdpEngine();
                    ((MetadataSignerI) idpEngine.getSigner()).signMetadata(entityDescriptor);
                }
                return EidasStringUtil.toString(OpenSamlHelper.marshall(entityDescriptor, false));
            } catch (Exception ex) {
                LOGGER.info("ERROR : SAMLException ", ex.getMessage());
                LOGGER.debug("ERROR : SAMLException ", ex);
                throw new IllegalStateException(ex);
            }
        }

        private void initialize() throws EIDASSAMLEngineException {
            ProtocolEngineI idpEngine = params.getIdpEngine();
            ProtocolEngineI spEngine = params.getSpEngine();
            MetadataConfigParams.Builder initParamBuilder = MetadataConfigParams.builder(params);
            if (idpEngine != null) {
                idpEngine.getProtocolProcessor().configure();
                initParamBuilder.idpSigningCredential(CertificateUtil.toCredential(idpEngine.getSigningCertificate()));

                final X509Certificate idpEngineDecryptionCertificate = idpEngine.getDecryptionCertificate();
                if (idpEngineDecryptionCertificate != null) {
                    initParamBuilder.idpEncryptionCredential(CertificateUtil.toCredential(idpEngineDecryptionCertificate));
                }
                if (idpSSODescriptor == null) {
                    try {
                        idpSSODescriptor = BuilderFactoryUtil.buildXmlObject(IDPSSODescriptor.class);
                    } catch (NoSuchFieldException e) {
                        throw new EIDASSAMLEngineException(e);
                    } catch (IllegalAccessException e) {
                        throw new EIDASSAMLEngineException(e);
                    }
                }
            }
            if (spEngine != null) {
                spEngine.getProtocolProcessor().configure();
                initParamBuilder.spSigningCredential(CertificateUtil.toCredential(spEngine.getSigningCertificate()));

                final X509Certificate spEngineDecryptionCertificate = spEngine.getDecryptionCertificate();
                if (spEngineDecryptionCertificate != null) {
                    initParamBuilder.spEncryptionCredential(CertificateUtil.toCredential(spEngineDecryptionCertificate));
                }
                if (spSSODescriptor == null) {
                    try {
                        spSSODescriptor = BuilderFactoryUtil.buildXmlObject(SPSSODescriptor.class);
                    } catch (NoSuchFieldException e) {
                        throw new EIDASSAMLEngineException(e);
                    } catch (IllegalAccessException e) {
                        throw new EIDASSAMLEngineException(e);
                    }
                }
            }
            params = initParamBuilder.build();
        }
    }

    private EidasMetadata(@Nonnull Generator generator) throws EIDASSAMLEngineException {
        entityId = generator.entityId;
        metadata = generator.metadata;
    }

    public String getMetadata() {
        return metadata;
    }

    @Nonnull
    public static Generator generator() {
        return new Generator();
    }

    @Nonnull
    public static Generator generator(@Nonnull Generator copy) {
        return new Generator(copy);
    }

}
