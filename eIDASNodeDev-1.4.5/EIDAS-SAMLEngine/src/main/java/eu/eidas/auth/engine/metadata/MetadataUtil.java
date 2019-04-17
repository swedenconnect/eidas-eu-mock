/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the
 * "NOTICE" text file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution.
 * Any derivative works that you distribute must include a readable
 * copy of the "NOTICE" text file.
 */

package eu.eidas.auth.engine.metadata;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.impl.SamlBindingUri;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.core.eidas.EidasConstants;
import eu.eidas.auth.engine.core.eidas.SPType;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.samlext.saml2mdattr.EntityAttributes;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSAnyImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Properties;

/**
 * Metadata related utilities.
 */
public class MetadataUtil {

    public static String TECHNICAL_CONTACT_PROPS[] = {"contact.technical.company", "contact.technical.email", "contact.technical.givenname", "contact.technical.surname", "contact.technical.phone"};
    public static String SUPPORT_CONTACT_PROPS[] = {"contact.support.company", "contact.support.email", "contact.support.givenname", "contact.support.surname", "contact.support.phone"};
    public static String CONTACTS[][] = {TECHNICAL_CONTACT_PROPS, SUPPORT_CONTACT_PROPS};

    public static String CONNECTOR_TECHNICAL_CONTACT_PROPS[] = {"connector.contact.technical.company", "connector.contact.technical.email", "connector.contact.technical.givenname", "connector.contact.technical.surname", "connector.contact.technical.phone"};
    public static String CONNECTOR_SUPPORT_CONTACT_PROPS[] = {"connector.contact.support.company", "connector.contact.support.email", "connector.contact.support.givenname", "connector.contact.support.surname", "connector.contact.support.phone"};
    public static String CONNECTOR_CONTACTS[][] = {CONNECTOR_TECHNICAL_CONTACT_PROPS, CONNECTOR_SUPPORT_CONTACT_PROPS};
    public static String SERVICE_TECHNICAL_CONTACT_PROPS[] = {"service.contact.technical.company", "service.contact.technical.email", "service.contact.technical.givenname", "service.contact.technical.surname", "service.contact.technical.phone"};
    public static String SERVICE_SUPPORT_CONTACT_PROPS[] = {"service.contact.support.company", "service.contact.support.email", "service.contact.support.givenname", "service.contact.support.surname", "service.contact.support.phone"};
    public static String SERVICE_CONTACTS[][] = {SERVICE_TECHNICAL_CONTACT_PROPS, SERVICE_SUPPORT_CONTACT_PROPS};

    public static String CONNECTOR_ORG_NAME = "connector.organization.name";
    public static String CONNECTOR_ORG_DISPNAME = "connector.organization.displayname";
    public static String CONNECTOR_ORG_URL = "connector.organization.url";
    public static String SERVICE_ORG_NAME = "service.organization.name";
    public static String SERVICE_ORG_DISPNAME = "service.organization.displayname";
    public static String SERVICE_ORG_URL = "service.organization.url";

    public static String ORG_NAME = "organization.name";
    public static String ORG_DISPNAME = "organization.displayname";
    public static String ORG_URL = "organization.url";



    @Nullable
    public static String getAssertionConsumerUrl(@Nullable SPSSODescriptor spSsoDescriptor) {
        if (spSsoDescriptor == null || spSsoDescriptor.getAssertionConsumerServices().isEmpty()) {
            return null;
        }
        for (AssertionConsumerService acs : spSsoDescriptor.getAssertionConsumerServices()) {
            if (acs.isDefault()) {
                return acs.getLocation();
            }
        }
        return spSsoDescriptor.getAssertionConsumerServices().get(0).getLocation();
    }

    /**
     * @since 1.1
     */
    @Nullable
    public static String getAssertionConsumerUrlFromMetadata(@Nonnull MetadataFetcherI metadataFetcher,
                                                             @Nonnull MetadataSignerI metadataSigner,
                                                             @Nonnull ILightRequest authnRequest)
            throws EIDASSAMLEngineException {
        String issuer = authnRequest.getIssuer();
        if (StringUtils.isNotBlank(issuer)) {
            // This would fetch the metadata only once!
            EntityDescriptor entityDescriptor = metadataFetcher.getEntityDescriptor(issuer, metadataSigner);
            SPSSODescriptor spSsoDescriptor = getSPSSODescriptor(entityDescriptor);
            return getAssertionConsumerUrl(spSsoDescriptor);
        }
        return null;
    }

    @Nullable
    private static <T extends RoleDescriptor> T getFirstRoleDescriptor(@Nonnull EntityDescriptor entityDescriptor,
                                                                       @Nonnull Class<T> clazz) {
        for (RoleDescriptor rd : entityDescriptor.getRoleDescriptors()) {
            if (clazz.isInstance(rd)) {
                return (T) rd;
            }
        }
        return null;
    }

    @Nullable
    public static IDPSSODescriptor getIDPSSODescriptor(@Nonnull EntityDescriptor entityDescriptor) {
        return getFirstRoleDescriptor(entityDescriptor, IDPSSODescriptor.class);
    }

    @Nullable
    public static SPSSODescriptor getSPSSODescriptor(@Nonnull EntityDescriptor entityDescriptor) {
        return getFirstRoleDescriptor(entityDescriptor, SPSSODescriptor.class);
    }

    /**
     * Retrieve SPType published in the metadata of the requesting party.
     *
     * @param entityDescriptor the entitity descriptor to use
     * @return the value of spType (either 'public' or 'private')
     */
    @Nullable
    public static String getSPTypeFromMetadata(@Nullable EntityDescriptor entityDescriptor) {
        if (entityDescriptor == null || entityDescriptor.getExtensions() == null) {
            return null;
        }
        List<XMLObject> spTypes = entityDescriptor.getExtensions().getUnknownXMLObjects(SPType.DEF_ELEMENT_NAME);
        final SPType type = (SPType) (spTypes.isEmpty() ? null : spTypes.get(0));
        return type == null ? null : type.getSPType();
    }

    @Nullable
    public static String getSPTypeFromMetadata(@Nonnull MetadataFetcherI metadataFetcher,
                                                             @Nonnull MetadataSignerI metadataSigner,
                                                             @Nonnull ILightRequest authnRequest)
            throws EIDASSAMLEngineException {

        String issuer = authnRequest.getIssuer();
        if (StringUtils.isNotBlank(issuer)) {
            // This would fetch the metadata only once!
            EntityDescriptor entityDescriptor = metadataFetcher.getEntityDescriptor(issuer, metadataSigner);
            return getSPTypeFromMetadata(entityDescriptor);
        }
        return null;
    }

    /**
     * Returns the service LevelOfAssurance of a node
     *
     * @param entityDescriptor the EntityDescriptor instance
     * @return the LevelOfAssurance or the empty string.
     */
    public static String getServiceLevelOfAssurance(EntityDescriptor entityDescriptor) {
        String retrievedLevelOfAssurance = StringUtils.EMPTY;
        if (null == entityDescriptor) {
            return retrievedLevelOfAssurance;
        }
        for (XMLObject xmlObj : entityDescriptor.getExtensions().getUnknownXMLObjects()) {
            if (xmlObj instanceof EntityAttributes) {
                EntityAttributes eas = (EntityAttributes) xmlObj;
                for (Attribute attr : eas.getAttributes()) {
                    if (EidasConstants.LEVEL_OF_ASSURANCE_NAME.equalsIgnoreCase(attr.getName())
                            && !attr.getAttributeValues().isEmpty()) {
                        XMLObject xmlObject = attr.getAttributeValues().get(0);
                        if (xmlObject instanceof XSString) {
                            XSString val = (XSString) xmlObject;
                            retrievedLevelOfAssurance = val.getValue();
                            //Quick fix to solve issue missing xsi:type="xs:string" for LOA in DE's metadata
                        } else if (xmlObject instanceof XSAnyImpl) {
                            XSAnyImpl val = (XSAnyImpl) xmlObject;
                            retrievedLevelOfAssurance = val.getTextContent();
                        }

                        break;
                    }
                }
                if (!StringUtils.isEmpty(retrievedLevelOfAssurance)) {
                    break;
                }
            }
        }
        return retrievedLevelOfAssurance;
    }

    @Nullable
    public static String getSingleSignOnUrl(@Nullable IDPSSODescriptor idpSsoDescriptor,
                                            @Nullable SamlBindingUri bindingUri) {
        if (idpSsoDescriptor == null || idpSsoDescriptor.getSingleSignOnServices().isEmpty()) {
            return null;
        }
        for (SingleSignOnService ssoService : idpSsoDescriptor.getSingleSignOnServices()) {
            String location = ssoService.getLocation();
            if (null == bindingUri) {
                return location;
            }
            if (bindingUri.getBindingUri().equals(ssoService.getBinding())) {
                return location;
            }
        }
        return idpSsoDescriptor.getSingleSignOnServices().get(0).getLocation();
    }

    /**
     * @param metadata
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

    /** For IdP and SP only */
    public static ContactData createTechnicalContact(Properties configs){
        return createContact(TECHNICAL_CONTACT_PROPS, configs);
    }

    /** For IdP and SP only */
    public static ContactData createSupportContact(Properties configs){
        return createContact(SUPPORT_CONTACT_PROPS, configs);
    }

    public static ContactData createConnectorTechnicalContact(Properties configs){
        return createContact(CONNECTOR_TECHNICAL_CONTACT_PROPS, configs);
    }

    public static ContactData createConnectorSupportContact(Properties configs){
        return createContact(CONNECTOR_SUPPORT_CONTACT_PROPS, configs);
    }

    public static ContactData createServiceTechnicalContact(Properties configs){
        return createContact(SERVICE_TECHNICAL_CONTACT_PROPS, configs);
    }

    public static ContactData createServiceSupportContact(Properties configs){
        return createContact(SERVICE_SUPPORT_CONTACT_PROPS, configs);
    }

    private static ContactData createContact(String[] propsNames, Properties configs){
        ContactData.Builder contact = ContactData.builder();
        contact.company(propsNames != null && propsNames.length > 0 && configs != null ? configs.getProperty(propsNames[0]) : null);
        contact.email(propsNames != null && propsNames.length > 1 && configs != null ? configs.getProperty(propsNames[1]) : null);
        contact.givenName(propsNames != null && propsNames.length > 2 && configs != null ? configs.getProperty(propsNames[2]) : null);
        contact.surName(propsNames != null && propsNames.length > 3 && configs != null ? configs.getProperty(propsNames[3]) : null);
        contact.phone(propsNames != null && propsNames.length > 4 && configs != null ? configs.getProperty(propsNames[4]) : null);
        return contact.build();
    }

    /** For IdP and SP only */
    public static OrganizationData createOrganization(Properties configs) {
        OrganizationData.Builder organization = OrganizationData.builder();
        organization.name(configs != null ? configs.getProperty(ORG_NAME) : null);
        organization.displayName(configs != null ? configs.getProperty(ORG_DISPNAME) : null);
        organization.url(configs != null ? configs.getProperty(ORG_URL) : null);
        return organization.build();
    }

    public static OrganizationData createServiceOrganization(Properties configs) {
        OrganizationData.Builder organization = OrganizationData.builder();
        organization.name(configs != null ? configs.getProperty(SERVICE_ORG_NAME) : null);
        organization.displayName(configs != null ? configs.getProperty(SERVICE_ORG_DISPNAME) : null);
        organization.url(configs != null ? configs.getProperty(SERVICE_ORG_URL) : null);
        return organization.build();
    }

    public static OrganizationData createConnectorOrganization(Properties configs) {
        OrganizationData.Builder organization = OrganizationData.builder();
        organization.name(configs != null ? configs.getProperty(CONNECTOR_ORG_NAME) : null);
        organization.displayName(configs != null ? configs.getProperty(CONNECTOR_ORG_DISPNAME) : null);
        organization.url(configs != null ? configs.getProperty(CONNECTOR_ORG_URL) : null);
        return organization.build();
    }


    private MetadataUtil() {
    }

}
