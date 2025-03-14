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

package eu.eidas.engine.metadata;

import eu.eidas.RecommendedSecurityProviders;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.EntityDescriptorContainer;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.isA;

public class MetadataUtilTest  {

    private static List<EntityDescriptorContainer> entityDescriptorContainers;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setupClass() throws EIDASMetadataProviderException {
        RecommendedSecurityProviders.setupRecommendedSecurityProviders();
        OpenSamlHelper.initialize();
        entityDescriptorContainers = loadEntityDescriptorsFromFolder();
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
    /**
     * Test method for
     * {@link MetadataUtil#convertKeyDescriptors(RoleDescriptor, EidasMetadataRoleParametersI)}
     * When signing certificate is invalid
     *
     * Must fail
     */
    @Test
    public void testConvertKeyDescriptorsSigningCertificateException() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASMetadataException.class));

        EidasMetadataRoleParametersI mockRoleParams = Mockito.mock(EidasMetadataRoleParametersI.class);
        RoleDescriptor mockRoleDescriptor = Mockito.mock(RoleDescriptor.class);
        KeyDescriptor mockKeyDescriptor = Mockito.mock(KeyDescriptor.class);
        List<KeyDescriptor> mockKeyDescriptorList = new ArrayList<>();
        mockKeyDescriptorList.add(mockKeyDescriptor);

        KeyInfo mockKeyInfo = Mockito.mock(KeyInfo.class);
        List<X509Data> mockCertificatesDataList = new ArrayList<>();
        X509Data mockCertificateData = Mockito.mock(X509Data.class);
        mockCertificatesDataList.add(mockCertificateData);
        List<org.opensaml.xmlsec.signature.X509Certificate> mockCertificatesList = new ArrayList<>();
        org.opensaml.xmlsec.signature.X509Certificate mockCertificate = Mockito.mock(org.opensaml.xmlsec.signature.X509Certificate.class);
        mockCertificatesList.add(mockCertificate);

        Mockito.when(mockRoleDescriptor.getKeyDescriptors()).thenReturn(mockKeyDescriptorList);
        Mockito.when(mockKeyDescriptor.getUse()).thenReturn(UsageType.SIGNING);
        Mockito.when(mockKeyDescriptor.getKeyInfo()).thenReturn(mockKeyInfo);
        Mockito.when(mockKeyInfo.getX509Datas()).thenReturn(mockCertificatesDataList);
        Mockito.when(mockCertificateData.getX509Certificates()).thenReturn(mockCertificatesList);
        Mockito.when(mockCertificate.getValue()).thenReturn("shouldFail");

        Method convertKeyDescriptorsMethod = MetadataUtil.class.getDeclaredMethod("convertKeyDescriptors", RoleDescriptor.class,EidasMetadataRoleParametersI.class);
        convertKeyDescriptorsMethod.setAccessible(true);
        convertKeyDescriptorsMethod.invoke(MetadataUtil.class, mockRoleDescriptor, mockRoleParams);
    }

    /**
     * Test method for
     * {@link MetadataUtil#convertKeyDescriptors(RoleDescriptor, EidasMetadataRoleParametersI)}
     * When encryption certificate is invalid
     *
     * Must fail
     */
    @Test
    public void testConvertKeyDescriptorEncryptionCertificateException() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASMetadataException.class));

        EidasMetadataRoleParametersI mockRoleParams = Mockito.mock(EidasMetadataRoleParametersI.class);
        RoleDescriptor mockRoleDescriptor = Mockito.mock(RoleDescriptor.class);
        KeyDescriptor mockKeyDescriptor = Mockito.mock(KeyDescriptor.class);
        List<KeyDescriptor> mockKeyDescriptorList = new ArrayList<>();
        mockKeyDescriptorList.add(mockKeyDescriptor);

        KeyInfo mockKeyInfo = Mockito.mock(KeyInfo.class);
        List<X509Data> mockCertificatesDataList = new ArrayList<>();
        X509Data mockCertificateData = Mockito.mock(X509Data.class);
        mockCertificatesDataList.add(mockCertificateData);
        List<org.opensaml.xmlsec.signature.X509Certificate> mockCertificatesList = new ArrayList<>();
        org.opensaml.xmlsec.signature.X509Certificate mockCertificate = Mockito.mock(org.opensaml.xmlsec.signature.X509Certificate.class);
        mockCertificatesList.add(mockCertificate);

        Mockito.when(mockRoleDescriptor.getKeyDescriptors()).thenReturn(mockKeyDescriptorList);
        Mockito.when(mockKeyDescriptor.getUse()).thenReturn(UsageType.ENCRYPTION);
        Mockito.when(mockKeyDescriptor.getKeyInfo()).thenReturn(mockKeyInfo);
        Mockito.when(mockKeyInfo.getX509Datas()).thenReturn(mockCertificatesDataList);
        Mockito.when(mockCertificateData.getX509Certificates()).thenReturn(mockCertificatesList);
        Mockito.when(mockCertificate.getValue()).thenReturn("shouldFail");

        Method convertKeyDescriptorsMethod = MetadataUtil.class.getDeclaredMethod("convertKeyDescriptors", RoleDescriptor.class,EidasMetadataRoleParametersI.class);
        convertKeyDescriptorsMethod.setAccessible(true);
        convertKeyDescriptorsMethod.invoke(MetadataUtil.class, mockRoleDescriptor, mockRoleParams);
    }

    private static List<EntityDescriptorContainer> loadEntityDescriptorsFromFolder() throws EIDASMetadataProviderException {
        FileMetadataLoader loader = new FileMetadataLoader();
        loader.setRepositoryPath("src/test/resources/samplemetadata");
        return loader.getEntityDescriptors();
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