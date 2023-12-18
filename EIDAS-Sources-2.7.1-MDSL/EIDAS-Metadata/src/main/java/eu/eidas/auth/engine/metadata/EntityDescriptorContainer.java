/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.engine.metadata;

import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * user for transporting EntityDescriptor and EntitiesDescriptor objects
 *
 */
public class EntityDescriptorContainer {

	List<EntityDescriptor> entityDescriptors=new ArrayList<EntityDescriptor>();
	EntitiesDescriptor entitiesDescriptor;
	byte serializedEntitesDescriptor[];
	List<byte[]> serializedEntityDescriptors=new ArrayList<byte[]>();
	public List<EntityDescriptor> getEntityDescriptors() {
		return entityDescriptors;
	}
	public void setEntityDescriptors(List<EntityDescriptor> entityDescriptors) {
		this.entityDescriptors = entityDescriptors;
	}
	public void addEntityDescriptor(EntityDescriptor ed, byte[] serializedForm){
		if(entityDescriptors!=null){
			entityDescriptors.add(ed);
		}
	}
	public EntitiesDescriptor getEntitiesDescriptor() {
		return entitiesDescriptor;
	}
	public void setEntitiesDescriptor(EntitiesDescriptor entitiesDescriptor) {
		this.entitiesDescriptor = entitiesDescriptor;
	}
	public byte[] getSerializedEntitesDescriptor() {
		return serializedEntitesDescriptor;
	}
	public void setSerializedEntitesDescriptor(byte[] serializedEntitesDescriptor) {
		this.serializedEntitesDescriptor = serializedEntitesDescriptor;
	}
	public List<byte[]> getSerializedEntityDescriptors() {
		return serializedEntityDescriptors;
	}
	public void setSerializedEntityDescriptors(
			List<byte[]> serializedEntityDescriptors) {
		this.serializedEntityDescriptors = serializedEntityDescriptors;
	}
	
}
