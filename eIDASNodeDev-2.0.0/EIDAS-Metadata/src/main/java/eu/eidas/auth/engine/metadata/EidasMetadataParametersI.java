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
package eu.eidas.auth.engine.metadata;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Set;

/**
 * This value object interface is responsible to present ALL data items for an eIDAS Metadata, inlcuding
 * technical and business information. It aggregates Role Descriptor data.
 *
 * To create an instance invoke @see #eu.eidas.auth.engine.metadata.MetadataConfiguration.newParametersInstance()
 *
 * use MetadataConfiguration to get a raw instance. TODO: interface for Metadata Aggregator
 * foreseen to be implemented as either proxy (embedded metadata service) or as DTO (external metadata service).
 */
public interface EidasMetadataParametersI extends Serializable {

    /**
     * EntityID is the identifier of the Metadata endpoint, also the URL where the Metadata content is located.
     * In eIDAS SAML it is the same as the Issuer and / or Destination of SAML messages.
     * @return String representation of an URL
     */
    String getEntityID();

    /**
     * Sets the EntityID see {@link #getEntityID()} for details.
     * @param entityID
     */
    void setEntityID(String entityID);

    /**
     * Getter for signing methods. The result is a list of ISO defined URLs separated with a ";".
     * @return list of methods in String
     */
    //TODO improve this method to handle list of strings
    String getSigningMethods();

    /**
     * Setter for signing methods. See {@link #getSigningMethods()} for details.
     * @param signingMethods
     */
    void setSigningMethods(String signingMethods);

    /**
     * * Getter for digest methods. The result is a list of ISO defined URLs separated with a ";".
     * @return list of methods in String
     */
    String getDigestMethods();

    /**
     * Setter for digest methods. See {@link #getDigestMethods()} for details.
     * @param signingMethods
     */
    //TODO improve this method to handle list of strings
    void setDigestMethods(String signingMethods);

    /**
     * Returns with the validUntil field defined to this Metadata. If this time is passed, the metadata is not considered as valid.
     * @return Joda representation of datetime
     */
    DateTime getValidUntil();

    /**
     * Sets the validity time for this Metadata, see {@link #getValidUntil()} for more information.
     * @param validityDuration
     */
    void setValidUntil(DateTime validityDuration);

    /**
     * Returns with the level of assurance with the full URN defined
     * @return String full URN defined
     */
    //TODO this should be a list
    String getAssuranceLevel();

    /**
     * Sets the assurance level with the full URN
     *
     * @param assuranceLevel
     */
    //TODO this should be a list
    void setAssuranceLevel(String assuranceLevel);

    /**
     * Returns the SPType. see {@link eu.eidas.auth.commons.EIDASValues}
     * @return String representation of spType
     */
    //TODO EIDINT-1970 should be in sp role parameters
    String getSpType();

    /**
     * Sets SPType, see {@link eu.eidas.auth.commons.EIDASValues}
     * @param spType
     */
    //TODO EIDINT-1970 should be in sp role parameters
    void setSpType(String spType);

    /**
     * Returns with the organization data information provided in the Metadata.
     * @return Organization data
     */
    //TODO EIDINT-1970 should be a simple VO not a builder
    OrganizationData getOrganization();

    /**
     * Sets the organization data
     * @param organization
     */
    //TODO EIDINT-1970 should be a simple VO not a builder
    void setOrganization(OrganizationData organization);

    /**
     * Retreives the support contact for this Node
     * @return ContactData typed support contact
     */
    //TODO EIDINT-1970 should be a simple VO not a builder
    ContactData getSupportContact();

    /**
     * Sets the support contact details for this Node
     * @param supportContact ContactData typed support contact
     */
    //TODO EIDINT-1970 should be a simple VO not a builder
    void setSupportContact(ContactData supportContact);

    /**
     * Retreives the technical contact for this Node
     * @return ContactData typed technical contact
     */
    //TODO EIDINT-1970 should be a simple VO not a builder
    ContactData getTechnicalContact();

    /**
     * Sets the technical contact details for this Node
     * @param technicalContact ContactData typed support contact
     */
    //TODO EIDINT-1970 should be a simple VO not a builder
    void setTechnicalContact(ContactData technicalContact);

    /**
     * Returns with the RoleDescriptos defined to this Node. In eIDAS it is strongly recommended to have only one role,
     * because of the structuring of the extensions.
     * @return EidasMetadataRoleParametersI typed role descriptor what can be an SP or an IDP type
     */
    Set<EidasMetadataRoleParametersI> getRoleDescriptors();

    /**
     * Sets the list of role descriptors. Do not use this method directly outside the boundaries of eidas-metadata module.
     * @param roleDescriptors
     */
    void setRoleDescriptors(Set<EidasMetadataRoleParametersI> roleDescriptors);

    /**
     * Add a role descriptor to Metadata. Outside of the eidas-metadata module use this method. To create an instance of EidasMetadataRoleParametersI
     * invoke @see #eu.eidas.auth.engine.metadata.MetadataConfiguration.newRoleParametersInstance().
     * @param roleDescriptor
     */
    void addRoleDescriptor(EidasMetadataRoleParametersI roleDescriptor);


}
