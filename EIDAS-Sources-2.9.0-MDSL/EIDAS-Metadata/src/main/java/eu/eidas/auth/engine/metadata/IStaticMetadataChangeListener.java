/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */
package eu.eidas.auth.engine.metadata;

import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

/**
 * observer of changes in a static metadata repository
 */
public interface IStaticMetadataChangeListener {
    /**
     * notifies the adding of a new/updated entity descriptor
     * @param ed the {@link EntityDescriptor}
     * @param metadataSigner the metadataSigner which can be used to verify the digital signature of the retrieved
     * @throws EIDASMetadataException if the conversion of role descriptors could not be done
     */
    void add(EntityDescriptor ed, MetadataSignerI metadataSigner) throws EIDASMetadataException;
    /**
     * notifies the removal of an entity descriptor
     * @param entityID the identifier of the Metadata endpoint, also the URL where the Metadata content is located.
     */
    void remove(String entityID);
}
