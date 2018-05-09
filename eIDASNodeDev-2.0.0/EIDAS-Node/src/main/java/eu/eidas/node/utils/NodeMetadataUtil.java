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

package eu.eidas.node.utils;

import eu.eidas.auth.engine.metadata.ContactData;
import eu.eidas.auth.engine.metadata.OrganizationData;

import java.util.Properties;

/**
 * Node Metadata related utilities.
 */
public class NodeMetadataUtil {

    private NodeMetadataUtil () {}

    public static final String[] CONNECTOR_TECHNICAL_CONTACT_PROPS = {
            "connector.contact.technical.company",
            "connector.contact.technical.email",
            "connector.contact.technical.givenname",
            "connector.contact.technical.surname",
            "connector.contact.technical.phone"
    };

    public static final String[] CONNECTOR_SUPPORT_CONTACT_PROPS = {
            "connector.contact.support.company",
            "connector.contact.support.email",
            "connector.contact.support.givenname",
            "connector.contact.support.surname",
            "connector.contact.support.phone"
    };

    public static final String[] SERVICE_TECHNICAL_CONTACT_PROPS = {
            "service.contact.technical.company",
            "service.contact.technical.email",
            "service.contact.technical.givenname",
            "service.contact.technical.surname",
            "service.contact.technical.phone"
    };

    public static final String[] SERVICE_SUPPORT_CONTACT_PROPS = {
            "service.contact.support.company",
            "service.contact.support.email",
            "service.contact.support.givenname",
            "service.contact.support.surname",
            "service.contact.support.phone"
    };

    public static final String CONNECTOR_ORG_NAME = "connector.organization.name";

    public static final String CONNECTOR_ORG_DISPNAME = "connector.organization.displayname";

    public static final String CONNECTOR_ORG_URL = "connector.organization.url";

    public static final String SERVICE_ORG_NAME = "service.organization.name";

    public static final String SERVICE_ORG_DISPNAME = "service.organization.displayname";

    public static final String SERVICE_ORG_URL = "service.organization.url";

    /**
     * Creates the connector's technical contact data.
     * @param configs the configuration properties
     * @return the contact data
     */
    public static ContactData createConnectorTechnicalContact(Properties configs){
        return createContact(CONNECTOR_TECHNICAL_CONTACT_PROPS, configs);
    }

    /**
     * Creates the connector's support contact data.
     * @param configs the configuration properties
     * @return the contact data
     */
    public static ContactData createConnectorSupportContact(Properties configs){
        return createContact(CONNECTOR_SUPPORT_CONTACT_PROPS, configs);
    }

    /**
     * Creates the proxy-service's contact data.
     * @param configs the configuration properties
     * @return the contact data
     */
    public static ContactData createServiceTechnicalContact(Properties configs){
        return createContact(SERVICE_TECHNICAL_CONTACT_PROPS, configs);
    }

    /**
     * Creates the proxy-service's support data.
     * @param configs the configuration properties
     * @return the contact data
     */
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

    /**
     * Creates the proxy-service's organization data.
     * @param configs the configuration properties
     * @return organization data
     */
    public static OrganizationData createServiceOrganization(Properties configs) {
        return createOrganizationData(configs, SERVICE_ORG_NAME, SERVICE_ORG_DISPNAME, SERVICE_ORG_URL);
    }

    /**
     * Creates the connector's organization data.
     * @param configs the configuration properties
     * @return organization data
     */
    public static OrganizationData createConnectorOrganizationData(Properties configs) {
        return createOrganizationData(configs, CONNECTOR_ORG_NAME, CONNECTOR_ORG_DISPNAME, CONNECTOR_ORG_URL);
    }

    private static OrganizationData createOrganizationData(Properties configs, String orgName, String orgDispname, String orgUrl) {
        OrganizationData.Builder organization = OrganizationData.builder();
        organization.name(configs != null ? configs.getProperty(orgName) : null);
        organization.displayName(configs != null ? configs.getProperty(orgDispname) : null);
        organization.url(configs != null ? configs.getProperty(orgUrl) : null);
        return organization.build();
    }
}
