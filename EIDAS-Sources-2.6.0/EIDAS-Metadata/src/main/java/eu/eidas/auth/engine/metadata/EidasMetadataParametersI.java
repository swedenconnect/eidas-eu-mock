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

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
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
     * @param entityID the identifier of the Metadata endpoint, also the URL where the Metadata content is located.
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
     * @param signingMethods a list of ISO defined URLs separated with a ";"
     */
    void setSigningMethods(String signingMethods);

    /**
     * * Getter for digest methods. The result is a list of ISO defined URLs separated with a ";".
     * @return list of methods in String
     */
    String getDigestMethods();

    /**
     * Setter for digest methods. See {@link #getDigestMethods()} for details.
     * @param signingMethods a list of ISO defined URLs separated with a ";".
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
     * @param validityDuration the validUntil field defined to this Metadata.
     */
    void setValidUntil(DateTime validityDuration);

    /**
     * Returns with the first level of assurance with the full URN defined
     * @return String full URN defined
     */
    String getAssuranceLevel();

    /**
     * Sets the assurance levels with the full URN
     * Handles a String with a list of comma-separated values
     *
     * @param assuranceLevel the level of assurance with the full URN defined
     * @deprecated {@link #setAssuranceLevels(List)}}
     */
    void setAssuranceLevel(String assuranceLevel);

    /**
     * Returns with the levels of assurance with the full URN defined
     * @return List of String (full URN defined)
     */
    List<String> getAssuranceLevels();

    /**
     * Sets the assurance levels with the full URN
     *
     * @param assuranceLevels the list of levels of assurance with the full URN defined
     */
    void setAssuranceLevels(List<String> assuranceLevels);

    /**
     * Returns the SPType. see {@link eu.eidas.auth.commons.EIDASValues}
     * @return String representation of spType
     */
    //TODO EIDINT-1970 should be in sp role parameters
    String getSpType();

    /**
     * Sets SPType, see {@link eu.eidas.auth.commons.EIDASValues}
     * @param spType the SPType. see {@link eu.eidas.auth.commons.EIDASValues}
     */
    //TODO EIDINT-1970 should be in sp role parameters
    void setSpType(String spType);

    /**
     * Returns the Node Country value. see {@link eu.eidas.auth.commons.EIDASValues#EIDAS_NODE_COUNTRY}
     * @return String representation of the country hosting the node
     */
    String getNodeCountry();

    /**
     * Sets Node Country, see {@link eu.eidas.auth.commons.EIDASValues#EIDAS_NODE_COUNTRY}
     * @param nodeCountry the node country value.
     */
    void setNodeCountry(String nodeCountry);

    /**
     * Returns with the organization data information provided in the Metadata.
     * @return Organization data
     */
    //TODO EIDINT-1970 should be a simple VO not a builder
    OrganizationData getOrganization();

    /**
     * Sets the organization data
     * @param organization the organization data information provided in the Metadata.
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
     * Returns with the RoleDescriptors defined to this Node. In eIDAS it is strongly recommended to have only one role,
     * because of the structuring of the extensions.
     * @return EidasMetadataRoleParametersI typed role descriptor what can be an SP or an IDP type
     */
    Set<EidasMetadataRoleParametersI> getRoleDescriptors();

    /**
     * Sets the list of role descriptors. Do not use this method directly outside the boundaries of eidas-metadata module.
     * @param roleDescriptors the list of RoleDescriptors defined to this Node.
     */
    void setRoleDescriptors(Set<EidasMetadataRoleParametersI> roleDescriptors);

    /**
     * Add a role descriptor to Metadata. Outside of the eidas-metadata module use this method. To create an instance of EidasMetadataRoleParametersI
     * invoke @see #eu.eidas.auth.engine.metadata.MetadataConfiguration.newRoleParametersInstance().
     * @param roleDescriptor the RoleDescriptor defined to this Node.
     */
    void addRoleDescriptor(EidasMetadataRoleParametersI roleDescriptor);

    boolean isHideLoaType();

    /*TODO: remove these methods after trasition period*/
    void setHideLoaType(boolean hideLoaType);

    /**
     * Sets the eIDAS protocol versions.
     * @param eidasProtocolVersion the comma separated list of eIDAS protocol versions for this Node
     */
    void setEidasProtocolVersion(String eidasProtocolVersion);

    /**
     * Retrieves the first eIDAS protocol version for this Node
     * @return String with the eidas protocol version
     */
    String getEidasProtocolVersion();

    /**
     * Retrieves the list of eIDAS protocol versions for this Node
     * @return the list of eIDAS protocol versions
     */
    List<String> getEidasProtocolVersions();

    /**
     * Sets the eIDAS application identifier.
     * @param eidasApplicationIdentifiers the comma separated list of application identifiers for this Node
     */
    void setEidasApplicationIdentifier(String eidasApplicationIdentifiers);

    /**
     * Retrieves the first or only eIDAS application identifier for this Node
     * @return String with the eidas application identifier
     */
    String getEidasApplicationIdentifier();

    /**
     * Retrieves the list of eIDAS application identifiers for this Node
     * @return the list of eidas application identifiers
     */
    List<String> getEidasApplicationIdentifiers();

    /**
     * Sets the boolean that defines if RequesterId flag is true
     * @param isRequesterIdFlag the boolean that activates the RequesterId flag
     */
    void setRequesterIdFlag(boolean isRequesterIdFlag);

    /**
     * Getter for RequesterIdFlag
     *
     * @return true if requester Id flag is set to true, false otherwise.
     */
    boolean isRequesterIdFlag();
}
