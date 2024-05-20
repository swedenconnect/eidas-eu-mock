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
package eu.eidas.auth.engine.metadata.impl;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.engine.metadata.ContactData;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.InterconnectionGraphData;
import eu.eidas.auth.engine.metadata.OrganizationData;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EidasMetadataParameters implements EidasMetadataParametersI {

    private static final long serialVersionUID = -6509475088005886540L;

    protected String entityID;

    protected DateTime validUntil;
    protected List<String> assuranceLevels;
    protected String signingMethods;
    protected String digestMethods;
    protected String spType;
    protected String nodeCountry;
    protected OrganizationData organization;
    protected ContactData supportContact;
    protected ContactData technicalContact;
    private Set<EidasMetadataRoleParametersI> roleDescriptors = new HashSet<>();
    /*TODO: remove this attribute after trasition period*/
    protected boolean hideLoaType;

    protected List<String> eidasProtocolVersions;
    protected List<String> eidasApplicationIdentifiers;

    private boolean isRequesterIdFlag;

    private InterconnectionGraphData interconnectionGraphData;

    public EidasMetadataParameters() {
    }

    public EidasMetadataParameters(@Nonnull EidasMetadataParametersI emp) {
        this.entityID = emp.getEntityID();
        this.validUntil = emp.getValidUntil();
        this.assuranceLevels = emp.getAssuranceLevels();
        this.signingMethods = emp.getSigningMethods();
        this.digestMethods = emp.getDigestMethods();
        this.spType = emp.getSpType();
        this.nodeCountry = emp.getNodeCountry();
        this.organization = emp.getOrganization();
        this.supportContact = emp.getSupportContact();
        this.technicalContact = emp.getTechnicalContact();
        this.roleDescriptors = new HashSet<>(emp.getRoleDescriptors());
        this.hideLoaType = emp.isHideLoaType(); // TODO: remove this attribute after transition period
        this.eidasProtocolVersions = emp.getEidasProtocolVersions();
        this.eidasApplicationIdentifiers = emp.getEidasApplicationIdentifiers();
        this.isRequesterIdFlag = emp.isRequesterIdFlag();
        this.interconnectionGraphData = emp.getInterconnectionGraphData();
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
    public String getNodeCountry() {
        return nodeCountry;
    }

    @Override
    public void setNodeCountry(String nodeCountry) {
        this.nodeCountry = nodeCountry;
    }

    @Override
    public String getAssuranceLevel() {
        return assuranceLevels == null ? null : assuranceLevels.get(0);
    }

    @Override
    public List<String> getAssuranceLevels() {
        return this.assuranceLevels;
    }

    @Override
    public void setAssuranceLevels(List<String> assuranceLevels) {
        this.assuranceLevels = assuranceLevels;
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

    public boolean isHideLoaType() {
        return hideLoaType;
    }

    /*TODO: remove these methods after trasition period*/
    public void setHideLoaType(boolean hideLoaType) {
        this.hideLoaType = hideLoaType;
    }

    @Override
    public void setEidasProtocolVersion(String eidasProtocolVersion) {
        this.eidasProtocolVersions = EidasStringUtil.getTokens(eidasProtocolVersion);
    }

    @Override
    public String getEidasProtocolVersion() {
        if (eidasProtocolVersions != null && !eidasProtocolVersions.isEmpty()) {
            return eidasProtocolVersions.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<String> getEidasProtocolVersions() {
        return eidasProtocolVersions;
    }

    @Override
    public void setEidasApplicationIdentifier(String eidasApplicationIdentifiers) {
        this.eidasApplicationIdentifiers = EidasStringUtil.getTokens(eidasApplicationIdentifiers);
    }

    @Override
    public String getEidasApplicationIdentifier() {
        if (eidasApplicationIdentifiers != null && !eidasApplicationIdentifiers.isEmpty()) {
            return eidasApplicationIdentifiers.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<String> getEidasApplicationIdentifiers() {
        return eidasApplicationIdentifiers;
    }

    @Override
    public void setRequesterIdFlag(boolean isRequesterIdFlag) {
        this.isRequesterIdFlag = isRequesterIdFlag;
    }

    @Override
    public boolean isRequesterIdFlag() {
        return isRequesterIdFlag;
    }

    @Override
    @Nullable
    public InterconnectionGraphData getInterconnectionGraphData() {
        return this.interconnectionGraphData;
    }

    @Override
    public void setInterconnectionGraphData(@Nullable InterconnectionGraphData interconnectionGraphData) {
        this.interconnectionGraphData = interconnectionGraphData;
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
        if (!Objects.equals(assuranceLevels, that.assuranceLevels))
            return false;
        if (signingMethods != null ? !signingMethods.equals(that.signingMethods) : that.signingMethods != null)
            return false;
        if (digestMethods != null ? !digestMethods.equals(that.digestMethods) : that.digestMethods != null)
            return false;
        if (spType != null ? !spType.equals(that.spType) : that.spType != null)
            return false;
        if (nodeCountry != null ? !nodeCountry.equals(that.nodeCountry) : that.nodeCountry != null)
            return false;
        if (organization != null ? !organization.equals(that.organization) : that.organization != null)
            return false;
        if (supportContact != null ? !supportContact.equals(that.supportContact) : that.supportContact != null)
            return false;
        if (technicalContact != null ? !technicalContact.equals(that.technicalContact) : that.technicalContact != null)
            return false;
        if (interconnectionGraphData != null ? !interconnectionGraphData.equals(that.interconnectionGraphData) : that.interconnectionGraphData != null)
            return false;
        return !(roleDescriptors != null ? !roleDescriptors.equals(that.roleDescriptors) : that.roleDescriptors != null);

    }

    @Override
    public int hashCode() {
        int result = validUntil.hashCode();
        result = 31 * result + (entityID != null ? entityID.hashCode() : 0);
        result = 31 * result + (assuranceLevels != null ? assuranceLevels.hashCode() : 0);
        result = 31 * result + (signingMethods != null ? signingMethods.hashCode() : 0);
        result = 31 * result + (digestMethods != null ? digestMethods.hashCode() : 0);
        result = 31 * result + (spType != null ? spType.hashCode() : 0);
        result = 31 * result + (nodeCountry != null ? nodeCountry.hashCode() : 0);
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        result = 31 * result + (supportContact != null ? supportContact.hashCode() : 0);
        result = 31 * result + (technicalContact != null ? technicalContact.hashCode() : 0);
        result = 31 * result + (roleDescriptors != null ? roleDescriptors.hashCode() : 0);
        result = 31 * result + (interconnectionGraphData != null ? interconnectionGraphData.hashCode() : 0);
        return result;
    }
}