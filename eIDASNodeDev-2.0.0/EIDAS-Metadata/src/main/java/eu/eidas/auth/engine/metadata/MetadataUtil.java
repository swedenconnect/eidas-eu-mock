/*
 * Copyright (c) 2017 by European Commission
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
import com.google.common.collect.Ordering;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.core.eidas.DigestMethod;
import eu.eidas.auth.engine.core.eidas.SigningMethod;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataParameters;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataRoleParameters;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.auth.engine.metadata.samlobjects.SPType;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.*;
import org.opensaml.security.credential.UsageType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.List;

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
        params.setValidUntil(ed.getValidUntil());

        convertContacts(ed, params);
        convertOrganization(ed, params);
        convertExtensions(ed, params);
        convertRoleDescriptors(ed, params);

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
            contactBuilder.company(cp.getCompany().getName());
        }
        if (cp.getEmailAddresses() != null && !cp.getEmailAddresses().isEmpty()) {
            contactBuilder.email(cp.getEmailAddresses().get(0).getAddress());
        }
        if (cp.getTelephoneNumbers() != null && !cp.getTelephoneNumbers().isEmpty()) {
            contactBuilder.phone(cp.getTelephoneNumbers().get(0).getNumber());
        }
        if (cp.getGivenName()!= null) {
            contactBuilder.givenName(cp.getGivenName().getName());
        }
        if (cp.getSurName() != null) {
            contactBuilder.surName(cp.getSurName().getName());
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
            orgBuilder.url(orgInfo.getURLs().get(0).getValue());
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
                        //TODO change when/if LOA element changes in metadata produced
                        //Quick fix to allow support both DE middleware and current/older eIDAS versions
                        if (LEVEL_OF_ASSURANCE_NAME.equalsIgnoreCase(attr.getName())
                                && !attr.getAttributeValues().isEmpty()) {
                            XMLObject xmlObject = attr.getAttributeValues().get(0);
                            if (xmlObject instanceof XSString) {
                                XSString val = (XSString) attr.getAttributeValues().get(0);
                                params.setAssuranceLevel(val.getValue());
                            } else if (xmlObject instanceof XSAnyImpl) {
                                XSAnyImpl val = (XSAnyImpl) xmlObject;
                                params.setAssuranceLevel(val.getTextContent());
                            }
                            break;
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
        }
    }

    private static EidasMetadataRoleParametersI convertSPSSODescriptorParams(@Nonnull final RoleDescriptor roleDescriptor) throws EIDASMetadataException {
        EidasMetadataRoleParametersI roleParams = new EidasMetadataRoleParameters();
        roleParams.setRole(MetadataRole.SP);
        roleParams.setWantAssertionsSigned(((SPSSODescriptor) roleDescriptor).getWantAssertionsSigned());

        convertSupportedProtocols(roleDescriptor, roleParams);
        convertKeyDescriptors(roleDescriptor, roleParams);
        convertAssertionConsumerServices((SPSSODescriptor) roleDescriptor, roleParams);
        //TODO nameIdFormats from connector

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
        //TODO NameIDFormat to be implemented

        return roleParams;
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
        ImmutableSortedSet.Builder<String> supportedAttributes = new ImmutableSortedSet.Builder<>(Ordering.natural());
        for (Attribute a : roleDescriptor.getAttributes()) {
            supportedAttributes.add(a.getName());
        }
        roleParams.setSupportedAttributes(supportedAttributes.build());
    }

    private static void convertProtocolBindingLocations(@Nonnull final IDPSSODescriptor roleDescriptor, @Nonnull final EidasMetadataRoleParametersI roleParams) {
        for (SingleSignOnService singleSignOnService : roleDescriptor.getSingleSignOnServices()) {
            roleParams.addProtocolBindingLocation(convertBinding(singleSignOnService.getBinding()), singleSignOnService.getLocation());
        }
    }

    private static void convertKeyDescriptors(@Nonnull final RoleDescriptor roleDescriptor, @Nonnull final EidasMetadataRoleParametersI roleParams) throws EIDASMetadataException {
        for (KeyDescriptor kd : roleDescriptor.getKeyDescriptors()) {
            if (kd.getUse() == UsageType.SIGNING) {
                try {
                    roleParams.setSigningCertificate(CertificateUtil.toCertificate(kd.getKeyInfo()));
                } catch (CertificateException e) {
                    throw new EIDASMetadataException(e);
                }
            }
            if (kd.getUse() == UsageType.ENCRYPTION) {
                try {
                    roleParams.setEncryptionCertificate(CertificateUtil.toCertificate(kd.getKeyInfo()));
                } catch (CertificateException e) {
                    throw new EIDASMetadataException(e);
                }
            }
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
    public static EidasMetadataRoleParametersI getSPRoleDescriptor(@Nonnull EidasMetadataParametersI roleParameters) {
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

}
