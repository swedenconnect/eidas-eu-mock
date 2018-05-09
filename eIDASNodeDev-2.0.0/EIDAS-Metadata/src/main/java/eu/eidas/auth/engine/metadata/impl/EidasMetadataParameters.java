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
package eu.eidas.auth.engine.metadata.impl;

import eu.eidas.auth.engine.metadata.ContactData;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.OrganizationData;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class EidasMetadataParameters implements EidasMetadataParametersI {

    private static final long serialVersionUID = -6509475088005886540L;

    protected String entityID;

    protected DateTime validUntil;
    protected String assuranceLevel;
    protected String signingMethods;
    protected String digestMethods;
    protected String spType;
    protected OrganizationData organization;
    protected ContactData supportContact;
    protected ContactData technicalContact;
    private Set<EidasMetadataRoleParametersI> roleDescriptors = new HashSet<>();

    public EidasMetadataParameters() {
    }

    public EidasMetadataParameters(@Nonnull EidasMetadataParameters emp) {
        this.entityID = emp.entityID;
        this.signingMethods = emp.signingMethods;
        this.digestMethods = emp.digestMethods;
        this.spType = emp.spType;
        this.validUntil = emp.validUntil;
        this.assuranceLevel = emp.assuranceLevel;
        this.organization = OrganizationData.builder(emp.organization).build();
        this.supportContact = ContactData.builder(emp.supportContact).build();
        this.technicalContact = ContactData.builder(emp.technicalContact).build();
        this.roleDescriptors = new HashSet<>(emp.roleDescriptors);
    }

    @Override
    public String getEntityID() {
        return entityID;
    }

    @Override
    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    @Override
    public DateTime getValidUntil() {
        return validUntil;
    }

    @Override
    public void setValidUntil(DateTime validityDuration) {
        this.validUntil = validityDuration;
    }

    @Override
    public String getSigningMethods() {
        return signingMethods;
    }

    @Override
    public void setSigningMethods(String signingMethods) {
        this.signingMethods = signingMethods;
    }

    @Override
    public String getDigestMethods() {
        return digestMethods;
    }

    @Override
    public void setDigestMethods(String digestMethods) {
        this.digestMethods = digestMethods;
    }

    @Override
    public String getSpType() {
        return spType;
    }

    @Override
    public void setSpType(String spType) {
        this.spType = spType;
    }

    @Override
    public String getAssuranceLevel() {
        return assuranceLevel;
    }

    @Override
    public void setAssuranceLevel(String assuranceLevel) {
        this.assuranceLevel = assuranceLevel;
    }

    @Override
    public OrganizationData getOrganization() {
        return organization;
    }

    @Override
    public void setOrganization(OrganizationData organization) {
        this.organization = organization;
    }

    @Override
    public ContactData getSupportContact() {
        return supportContact;
    }

    @Override
    public void setSupportContact(ContactData supportContact) {
        this.supportContact = supportContact;
    }

    @Override
    public ContactData getTechnicalContact() {
        return technicalContact;
    }

    @Override
    public void setTechnicalContact(ContactData technicalContact) {
        this.technicalContact = technicalContact;
    }

    public Set<EidasMetadataRoleParametersI> getRoleDescriptors() {
        return roleDescriptors;
    }

    public void setRoleDescriptors(Set<EidasMetadataRoleParametersI> roleDescriptors) {
        this.roleDescriptors = roleDescriptors;
    }

    public void addRoleDescriptor(EidasMetadataRoleParametersI roleDescriptor) {
        if (this.roleDescriptors == null) {
            this.roleDescriptors = new HashSet<>();
        }
        this.roleDescriptors.add(roleDescriptor);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        EidasMetadataParameters that = (EidasMetadataParameters) o;

        if (entityID != null ? !entityID.equals(that.entityID) : that.entityID != null)
            return false;
        if (validUntil != that.validUntil)
            return false;
        if (assuranceLevel != null ? !assuranceLevel.equals(that.assuranceLevel) : that.assuranceLevel != null)
            return false;
        if (signingMethods != null ? !signingMethods.equals(that.signingMethods) : that.signingMethods != null)
            return false;
        if (digestMethods != null ? !digestMethods.equals(that.digestMethods) : that.digestMethods != null)
            return false;
        if (spType != null ? !spType.equals(that.spType) : that.spType != null)
            return false;
        if (organization != null ? !organization.equals(that.organization) : that.organization != null)
            return false;
        if (supportContact != null ? !supportContact.equals(that.supportContact) : that.supportContact != null)
            return false;
        if (technicalContact != null ? !technicalContact.equals(that.technicalContact) : that.technicalContact != null)
            return false;
        return !(roleDescriptors != null ? !roleDescriptors.equals(that.roleDescriptors) : that.roleDescriptors != null);

    }

    @Override
    public int hashCode() {
        int result = validUntil.hashCode();
        result = 31 * result + (entityID != null ? entityID.hashCode() : 0);
        result = 31 * result + (assuranceLevel != null ? assuranceLevel.hashCode() : 0);
        result = 31 * result + (signingMethods != null ? signingMethods.hashCode() : 0);
        result = 31 * result + (digestMethods != null ? digestMethods.hashCode() : 0);
        result = 31 * result + (spType != null ? spType.hashCode() : 0);
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        result = 31 * result + (supportContact != null ? supportContact.hashCode() : 0);
        result = 31 * result + (technicalContact != null ? technicalContact.hashCode() : 0);
        result = 31 * result + (roleDescriptors != null ? roleDescriptors.hashCode() : 0);
        return result;
    }
}