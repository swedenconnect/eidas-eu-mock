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
 * limitations under the Licence
 */

package eu.eidas.engine.metadata;

import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EntityDescriptorContainer;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.config.ConfigurationException;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.Iterator;
import java.util.List;

public class MetadataUtilTest  {

    private static List<EntityDescriptorContainer> entityDescriptorContainers;

    @BeforeClass
    public static void setup() throws ConfigurationException, EIDASMetadataProviderException {
        OpenSamlHelper.initialize();
        loadEntityDescriptorsFromFolder();
    }

    /**
     * Test method for {@link MetadataUtil#convertEntityDescriptor(EntityDescriptor)}
     *
     * Must succeed.
     *
     * @throws EIDASMetadataException if entity descriptor cannot be converted
     */
    @Test
    public void testMarshallMetadataParameters() throws EIDASMetadataException {
        Iterator<EntityDescriptorContainer> entityDescriptorContainerIterator = entityDescriptorContainers.iterator();
        while (entityDescriptorContainerIterator.hasNext()) {
            EntityDescriptorContainer entityDescriptorContainer = entityDescriptorContainerIterator.next();

            Iterator<EntityDescriptor> testDescriptor = entityDescriptorContainer.getEntityDescriptors().iterator();
            while (testDescriptor.hasNext()) {
                MetadataUtil.convertEntityDescriptor(testDescriptor.next());
            }

        }
    }

    /**
     * Test method for {@link MetadataUtil#convertEntityDescriptor(EntityDescriptor)}
     * to check {@link MetadataUtil#hasRequesterIdValue(Attribute)}
     * when requesterId is set to true
     *
     * Must succeed.
     *
     * @throws EIDASMetadataException if entity descriptor cannot be converted
     */
    @Test
    public void testConvertEntityDescriptorWithRequesterIdFlag() throws EIDASMetadataException {
        Iterator<EntityDescriptorContainer> entityDescriptorContainerIterator = entityDescriptorContainers.iterator();
        while (entityDescriptorContainerIterator.hasNext()) {
            EntityDescriptorContainer entityDescriptorContainer = entityDescriptorContainerIterator.next();

            Iterator<EntityDescriptor> entityDescriptorIterator = entityDescriptorContainer.getEntityDescriptors().iterator();
            while (entityDescriptorIterator.hasNext()) {
                EntityDescriptor entityDescriptor = entityDescriptorIterator.next();
                EidasMetadataParametersI eidasMetadataParametersI = MetadataUtil.convertEntityDescriptor(entityDescriptor);
                if (isEntityIdWithRequesterIdFlag(eidasMetadataParametersI)) {
                    assertContainsRequesterIdFlagTrue(eidasMetadataParametersI);
                    return;
                }
            }

        }

        Assert.assertTrue("No requesterId flag was found set", false);
    }

    /**
     * Test method for {@link MetadataUtil#convertEntityDescriptor(EntityDescriptor)}
     * to check {@link MetadataUtil#hasRequesterIdValue(Attribute)}
     * when requesterId is set to false
     *
     * @throws EIDASMetadataException if entity descriptor cannot be converted
     */
    @Test
    public void testConvertEntityDescriptorWithoutRequesterIdFlag() throws EIDASMetadataException {

        Iterator<EntityDescriptorContainer> entityDescriptorContainerIterator = entityDescriptorContainers.iterator();
        while (entityDescriptorContainerIterator.hasNext()) {
            EntityDescriptorContainer entityDescriptorContainer = entityDescriptorContainerIterator.next();

            Iterator<EntityDescriptor> entityDescriptorIterator = entityDescriptorContainer.getEntityDescriptors().iterator();
            while (entityDescriptorIterator.hasNext()) {
                EntityDescriptor entityDescriptor = entityDescriptorIterator.next();
                EidasMetadataParametersI eidasMetadataParametersI = MetadataUtil.convertEntityDescriptor(entityDescriptor);
                if (isEntityIdWithoutRequesterIdFlag(eidasMetadataParametersI)) {
                    assertContainsRequesterIdFlagFalse(eidasMetadataParametersI);
                    return;
                }
            }

        }

        Assert.fail("No requesterId flag was found set");
    }

    private static void loadEntityDescriptorsFromFolder() throws EIDASMetadataProviderException {
        FileMetadataLoader loader = new FileMetadataLoader();
        loader.setRepositoryPath("src/test/resources/samplemetadata");
        entityDescriptorContainers = loader.getEntityDescriptors();
    }

    private boolean isEntityIdWithRequesterIdFlag(EidasMetadataParametersI eidasMetadataParametersI) {
        return "http://localhost:8081/EidasNode/ServiceMetadata".equals(eidasMetadataParametersI.getEntityID());
    }

    private boolean isEntityIdWithoutRequesterIdFlag(EidasMetadataParametersI eidasMetadataParametersI) {
        return "http://localhost:8080/EidasNode/ServiceMetadata".equals(eidasMetadataParametersI.getEntityID());
    }

    private void assertContainsRequesterIdFlagTrue(EidasMetadataParametersI eidasMetadataParametersI) {
        Assert.assertTrue(eidasMetadataParametersI.isRequesterIdFlag());
    }

    private void assertContainsRequesterIdFlagFalse(EidasMetadataParametersI eidasMetadataParametersI) {
        Assert.assertFalse(eidasMetadataParametersI.isRequesterIdFlag());
    }

}