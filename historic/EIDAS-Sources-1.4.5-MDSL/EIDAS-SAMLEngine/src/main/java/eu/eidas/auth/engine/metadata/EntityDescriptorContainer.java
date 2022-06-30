package eu.eidas.auth.engine.metadata;

import java.util.ArrayList;
import java.util.List;

import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;

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
