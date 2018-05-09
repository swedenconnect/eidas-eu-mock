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

package eu.eidas.engine.metadata;

import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.metadata.EntityDescriptorContainer;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.config.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.Iterator;
import java.util.List;

public class MetadataUtilTest  {

    @BeforeClass
    public static void setup() throws ConfigurationException {
        OpenSamlHelper.initialize();
    }

    @Test
    public void testMarshallMetadataParameters() throws Exception {
        // load from file
        FileMetadataLoader loader = new FileMetadataLoader();
        loader.setRepositoryPath("src/test/resources/samplemetadata");
        List<EntityDescriptorContainer> descriptors = loader.getEntityDescriptors();
        EntityDescriptorContainer edc = descriptors.get(0);
        Iterator<EntityDescriptor> testDescriptor = edc.getEntityDescriptors().iterator();
        while (testDescriptor.hasNext()) {
            MetadataUtil.convertEntityDescriptor(testDescriptor.next());
        }

    }
}