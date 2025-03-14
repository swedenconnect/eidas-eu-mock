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

import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.core.eidas.DigestMethod;
import eu.eidas.auth.engine.core.eidas.SigningMethod;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataParameters;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataRoleParameters;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.auth.engine.metadata.samlobjects.NodeCountryType;
import eu.eidas.auth.engine.metadata.samlobjects.SPType;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.ContactPersonTypeEnumeration;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.credential.UsageType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Metadata related utilities.
 */
public class MetadataUtil {

    public static final String LEVEL_OF_ASSURANCE_NAME = "urn:oasis:names:tc:SAML:attribute:assurance-certification";

    private MetadataUtil() {
    }

    /**
     * Converts the {@link EntityDescriptor} into a {@link EidasMetadataParametersI}.
     *
     * @param ed the {@link EntityDescriptor}
     * @return {@link EidasMetadataParametersI} containing the converted params/data
     * @throws EIDASMetadataException if the conversion of role descriptors could not be done
     */
    //TODO vargata EIDINT-1635
    public static EidasMetadataParametersI convertEntityDescriptor(@Nonnull final EntityDescriptor ed) throws EIDASMetadataException {
        //why not use MetadataConfiguration here? Because this class is intended to be used in metadata module, and not in the node
        EidasMetadataParametersI params = new EidasMetadataParameters();
        params.setEntityID(ed.getEntityID());
        ZonedDateTime validityUntil = ed.getValidUntil() != null ? ZonedDateTime.ofInstant(ed.getValidUntil(), ZoneId.systemDefault()) : null;
        params.setValidUntil(validityUntil);

        convertContacts(ed, params);
        convertOrganization(ed, params);
        convertExtensions(ed, params);
        convertRoleDescriptors(ed, params);

        if(null != ed.getSignature()){
            try{
                params.setTrustChain(CertificateUtil.getCertificates(ed.getSignature().getKeyInfo()));
            }
            catch (CertificateException e){
                throw new EIDASMetadataException(e);
            }
        }

        return params;
    }

    private static void convertContacts(@Nonnull final EntityDescriptor ed, @Nonnull final EidasMetadataParametersI params) {
        for (ContactPerson cp : ed.getContactPersons()) {
            final ContactData contactData = createContactData(cp);

            if (ContactPersonTypeEnumeration.TECHNICAL.equals(cp.getType())) {
                params.setTechnicalContact(contactData);
            }
            if (ContactPersonTypeEnumeration.SUPPORT.equals(cp.getType())) {
                params.setSupportContact(contactData);
            }
        }
    }

    private static ContactData createContactData(@Nonnull final ContactPerson cp) {
        ContactData.Builder contactBuilder = ContactData.builder();
        if (cp.getCompany() != null) {
            contactBuilder.company(cp.getCompany().getValue());
        }
        if (cp.getEmailAddresses() != null && !cp.getEmailAddresses().isEmpty()) {
            contactBuilder.email(cp.getEmailAddresses().get(0).getURI());
        }
        if (cp.getTelephoneNumbers() != null && !cp.getTelephoneNumbers().isEmpty()) {
            contactBuilder.phone(cp.getTelephoneNumbers().get(0).getValue());
        }
        if (cp.getGivenName()!= null) {
            contactBuilder.givenName(cp.getGivenName().getValue());
        }
        if (cp.getSurName() != null) {
            contactBuilder.surName(cp.getSurName().getValue());
        }
        // attach to params
        return contactBuilder.build();
    }

    private static void convertOrganization(@Nonnull final EntityDescriptor ed, @Nonnull final EidasMetadataParametersI params) {
        if (ed.getOrganization() != null) {
            OrganizationData.Builder orgBuilder = createOrganizationData(ed);
            params.setOrganization(orgBuilder.build());
        }
    }

    private static OrganizationData.Builder createOrganizationData(@Nonnull final EntityDescriptor ed) {
        OrganizationData.Builder orgBuilder = OrganizationData.builder();
        Organization orgInfo = ed.getOrganization();
        if (orgInfo.getDisplayNames() != null && !orgInfo.getDisplayNames().isEmpty()) {
            orgBuilder.displayName(orgInfo.getDisplayNames().get(0).getValue());
        }
        if (orgInfo.getOrganizationNames() != null && !orgInfo.getOrganizationNames().isEmpty()) {
//            orgBuilder.name(orgInfo.getOrganizationNames().get(0).getName().getLocalString());
            orgBuilder.name(orgInfo.getOrganizationNames().get(0).getValue());
        }
        if (orgInfo.getURLs() != null && !orgInfo.getURLs().isEmpty()) {
//            orgBuilder.url(orgInfo.getURLs().get(0).getURL().getLocalString());
            orgBuilder.url(orgInfo.getURLs().get(0).getURI());
        }
        return orgBuilder;
    }

    private static void convertExtensions(@Nonnull final EntityDescriptor ed, @Nonnull final EidasMetadataParametersI params) {
        if (ed.getExtensions() != null && ed.getExtensions().getUnknownXMLObjects() != null) {
            StringBuilder digestMethods = new StringBuilder();
            StringBuilder signingMethods = new StringBuilder();

            Iterator<XMLObject> extensions = ed.getExtensions().getUnknownXMLObjects().iterator();
            while (extensions.hasNext()) {
                XMLObject extension = extensions.next();
                if (extension instanceof EntityAttributes) {
                    EntityAttributes eas = (EntityAttributes) extension;
                    for (Attribute attr : eas.getAttributes()) {
                        if (attr != null && attr.getAttributeValues() != null && !attr.getAttributeValues().isEmpty()) {
                            if (LEVEL_OF_ASSURANCE_NAME.equalsIgnoreCase(attr.getName())) {

                                final List<String> attributeValues = getAttributeValuesList(attr);
                                params.setAssuranceLevels(attributeValues);

                            } else if (EidasMetadata.PROTOCOL_VERSION_URI.equalsIgnoreCase(attr.getName())) {

                                final String attributeValues = getAttributeValues(attr);
                                params.setEidasProtocolVersion(attributeValues);

                            } else if (EidasMetadata.APPLICATION_IDENTIFIER.equalsIgnoreCase(attr.getName())) {

                                final String attributeValues = getAttributeValues(attr);
                                params.setEidasApplicationIdentifier(attributeValues);
                            } else if (EidasMetadata.ENTITY_CATEGORY_ATTRIBUTE_NAME.equalsIgnoreCase(attr.getName())){
                                params.setRequesterIdFlag(hasRequesterIdValue(attr));
                            }
                        }
                    }
                }

                if (extension instanceof DigestMethod) {
                    if (digestMethods.length() > 0) {
                        digestMethods.append(";");
                    }
                    digestMethods.append(((DigestMethod) extension).getAlgorithm());
                }
                if (extension instanceof SigningMethod) {
                    if (signingMethods.length() > 0) {
                        signingMethods.append(";");
                    }
                    signingMethods.append(((SigningMethod) extension).getAlgorithm());
                }
                if (extension instanceof SPType) {
                    params.setSpType(((SPType) extension).getSPType());
                }
            }

            params.setDigestMethods(digestMethods.toString());
            params.setSigningMethods(signingMethods.toString());
        }
    }

    private static boolean hasRequesterIdValue(Attribute attr) {
        final String attributeFirstValue = getAttributeValues(attr);
        return EidasMetadata.REQUESTER_ID_FLAG_VALUE.equalsIgnoreCase(attributeFirstValue);
    }

    private static void convertRoleDescriptorExtensions(Extensions extensions, EidasMetadataParametersI params) {
        if (extensions != null && extensions.getUnknownXMLObjects() != null) {
            Iterator<XMLObject> extensionsIterator = extensions.getUnknownXMLObjects().iterator();
            while (extensionsIterator.hasNext()) {
                XMLObject extension = extensionsIterator.next();
                if (extension instanceof NodeCountryType) {
                    params.setNodeCountry(((NodeCountryType) extension).getNodeCountry());
                }
            }
        }
    }

    private static List<String> getAttributeValuesList(@Nonnull Attribute attr) {
        return attr.getAttributeValues().stream()
                .map(MetadataUtil::getAttributeValue)
                .collect(Collectors.toList());
    }

    private static String getAttributeValues(Attribute attr) {
        final List<XMLObject> attributeValues = attr.getAttributeValues();
        String values = null;
        if (attributeValues != null && !attributeValues.isEmpty()) {
            values = attributeValues.stream()
                    .map(MetadataUtil::getAttributeValue)
                    .collect(Collectors.joining(","));
        }
        return values;
    }


    private static String getAttributeValue(XMLObject xmlObject) {
        if (xmlObject instanceof XSAny) {
            return ((XSAny) xmlObject).getTextContent();
        } else if (xmlObject instanceof XSString) {
            return ((XSString) xmlObject).getValue();
        }
        return null;
    }

    private static void convertRoleDescriptors(@Nonnull final  EntityDescriptor ed, @Nonnull final EidasMetadataParametersI params) throws EIDASMetadataException {
        for (RoleDescriptor roleDescriptor : ed.getRoleDescriptors()) {
            if (roleDescriptor instanceof SPSSODescriptor) {
                EidasMetadataRoleParametersI roleParams = convertSPSSODescriptorParams(roleDescriptor);
                params.addRoleDescriptor(roleParams);
            }
            if (roleDescriptor instanceof IDPSSODescriptor) {
                EidasMetadataRoleParametersI roleParams = convertIDPSSODescriptorParams(roleDescriptor);
                params.addRoleDescriptor(roleParams);
            }
            convertRoleDescriptorExtensions(roleDescriptor.getExtensions(), params);
        }
    }

    private static EidasMetadataRoleParametersI convertSPSSODescriptorParams(@Nonnull final RoleDescriptor roleDescriptor) throws EIDASMetadataException {
        EidasMetadataRoleParametersI roleParams = new EidasMetadataRoleParameters();
        roleParams.setRole(MetadataRole.SP);
        roleParams.setWantAssertionsSigned(((SPSSODescriptor) roleDescriptor).getWantAssertionsSigned());

        convertSupportedProtocols(roleDescriptor, roleParams);
        convertKeyDescriptors(roleDescriptor, roleParams);
        convertAssertionConsumerServices((SPSSODescriptor) roleDescriptor, roleParams);
        convertNameIdFormat((SSODescriptor) roleDescriptor, roleParams);

        return roleParams;
    }

    private static EidasMetadataRoleParametersI convertIDPSSODescriptorParams(@Nonnull final RoleDescriptor roleDescriptor) throws EIDASMetadataException {
        EidasMetadataRoleParametersI roleParams = new EidasMetadataRoleParameters();
        roleParams.setRole(MetadataRole.IDP);
        roleParams.setAuthnRequestsSigned(((IDPSSODescriptor) roleDescriptor).getWantAuthnRequestsSigned());

        convertSupportedProtocols(roleDescriptor, roleParams);
        convertKeyDescriptors(roleDescriptor, roleParams);
        convertProtocolBindingLocations((IDPSSODescriptor) roleDescriptor, roleParams);
        convertAttributes((IDPSSODescriptor) roleDescriptor, roleParams);
        convertNameIdFormat((SSODescriptor) roleDescriptor, roleParams);

        return roleParams;
    }

    private static void convertNameIdFormat(@Nonnull final SSODescriptor roleDescriptor, @Nonnull final EidasMetadataRoleParametersI roleParams) {
        SortedSet<String> nameIdFormats = new TreeSet<>();
        for (NameIDFormat nameIDFormat : roleDescriptor.getNameIDFormats()) {
            nameIdFormats.add(nameIDFormat.getURI());
        }
        roleParams.setNameIDFormats(nameIdFormats);
    }

    private static void convertAssertionConsumerServices(@Nonnull final SPSSODescriptor roleDescriptor, @Nonnull final EidasMetadataRoleParametersI roleParams) {
        for (AssertionConsumerService acs : roleDescriptor.getAssertionConsumerServices()) {
            roleParams.addProtocolBindingLocation(convertBinding(acs.getBinding()), acs.getLocation());
            if (acs.isDefault()) {
                roleParams.setDefaultBinding(convertBinding(acs.getBinding()));
            }
        }
    }

    private static void convertSupportedProtocols(@Nonnull final RoleDescriptor roleDescriptor, @Nonnull final EidasMetadataRoleParametersI roleParams) {
        List<String> protocols = roleDescriptor.getSupportedProtocols();
        if (protocols != null && protocols.size() > 0) {
            roleParams.setSamlProtocol(protocols.get(0));
        }
    }

    private static void convertAttributes(@Nonnull final IDPSSODescriptor roleDescriptor, @Nonnull final EidasMetadataRoleParametersI roleParams) {
        SortedSet<String> supportedAttributes = new TreeSet<>();
        for (Attribute a : roleDescriptor.getAttributes()) {
            supportedAttributes.add(a.getName());
        }
        roleParams.setSupportedAttributes(supportedAttributes);
    }

    private static void convertProtocolBindingLocations(@Nonnull final IDPSSODescriptor roleDescriptor, @Nonnull final EidasMetadataRoleParametersI roleParams) {
        for (SingleSignOnService singleSignOnService : roleDescriptor.getSingleSignOnServices()) {
            roleParams.addProtocolBindingLocation(convertBinding(singleSignOnService.getBinding()), singleSignOnService.getLocation());
        }
    }

    private static void convertKeyDescriptors(@Nonnull final RoleDescriptor roleDescriptor, @Nonnull final EidasMetadataRoleParametersI roleParams) throws EIDASMetadataException {
        final List<X509Certificate> signingCertificates = new ArrayList<>();
        final List<X509Certificate> encryptionCertificates = new ArrayList<>();
        for (KeyDescriptor kd : roleDescriptor.getKeyDescriptors()) {
            try {
                if (kd.getUse() == UsageType.SIGNING) {
                        signingCertificates.add(CertificateUtil.toCertificate(kd.getKeyInfo()));
                }
                if (kd.getUse() == UsageType.ENCRYPTION) {
                        encryptionCertificates.add(CertificateUtil.toCertificate(kd.getKeyInfo()));
                }
            } catch (CertificateException e) {
                throw new EIDASMetadataException(e);
            }
            roleParams.setSigningCertificates(signingCertificates);
            roleParams.setEncryptionCertificates(encryptionCertificates);
        }
    }

    private static String convertBinding(String samlURN) {
        String binding = null;
        if (SAMLConstants.SAML2_POST_BINDING_URI.equals(samlURN)) {
            binding = BindingMethod.POST.toString();
        }
        if (SAMLConstants.SAML2_REDIRECT_BINDING_URI.equals(samlURN)) {
            binding = BindingMethod.GET.toString();
        }
        return binding;
    }

    @Nullable
    public static EidasMetadataRoleParametersI getIDPRoleDescriptor(@Nonnull EidasMetadataParametersI roleParameters) {
        return getFirstRoleParameter(roleParameters, MetadataRole.IDP);
    }

    @Nullable
    public static EidasMetadataRoleParametersI getSPRoleDescriptor(EidasMetadataParametersI roleParameters) {
        return getFirstRoleParameter(roleParameters, MetadataRole.SP);
    }

    @Nullable
    private static EidasMetadataRoleParametersI getFirstRoleParameter(@Nonnull EidasMetadataParametersI eidasMetadataParameters,
                                                                      @Nonnull MetadataRole metadataRole) {
        EidasMetadataRoleParametersI roleParamters = null;
        if (null != eidasMetadataParameters) {
            for (EidasMetadataRoleParametersI role : eidasMetadataParameters.getRoleDescriptors()) {
                if (metadataRole.equals(role.getRole())) {
                    roleParamters = role;
                    break;
                }
            }
        }
        return roleParamters;
    }

    /**
     * @param metadata to deserialize
     * @return an EntityDescriptor parsed from the given String or null
     * @throws UnmarshallException if the unmarshall entity descriptors from static metadata file can not be done
     */
    @Nullable
    public static EntityDescriptorContainer deserializeEntityDescriptor(@Nonnull String metadata) throws UnmarshallException {
        EntityDescriptorContainer result = new EntityDescriptorContainer();
        byte[] metaDataBytes = EidasStringUtil.getBytes(metadata);
        XMLObject obj = OpenSamlHelper.unmarshall(metaDataBytes);
        if (obj instanceof EntityDescriptor) {
            result.addEntityDescriptor((EntityDescriptor) obj, metaDataBytes);
        } else if (obj instanceof EntitiesDescriptor) {
            EntitiesDescriptor ed = (EntitiesDescriptor) obj;
            result.setEntitiesDescriptor(ed);
            result.getEntityDescriptors().addAll(((EntitiesDescriptor) obj).getEntityDescriptors());
            result.setSerializedEntitesDescriptor(metaDataBytes);
        }
        return result;
    }

    /**
     * Reads multiple eidas protocolversions from the
     *
     * @param eidasMetadataParameters containing EidasProtocolVersion
     * @return the highest supported EidasProtocolVersion according to values defined in {@link EidasProtocolVersion}
     */
    @Nullable
    public static EidasProtocolVersion getHighestEidasProtocolVersion(@Nonnull EidasMetadataParametersI eidasMetadataParameters) {
        final List<String> versionStrings = eidasMetadataParameters.getEidasProtocolVersions();
        final List<EidasProtocolVersion> eidasProtocolVersions = EidasProtocolVersion.fromString(versionStrings);
        return EidasProtocolVersion.getHighestProtocolVersion(eidasProtocolVersions);
    }

}
