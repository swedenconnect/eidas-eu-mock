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

import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.core.eidas.EidasExtensionConfiguration;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataParameters;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataRoleParameters;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Test class for {@link EidasMetadata.Generator}
 */
public class EidasMetadataGeneratorTest {

    private static final String TEST_CERTIFICATE_PATH = "src/test/resources/certificates/keyTransport.crt";

    private static final String DIGEST_METHODS =
            SignatureConstants.ALGO_ID_DIGEST_SHA256 + ";"
            + SignatureConstants.ALGO_ID_DIGEST_SHA384 + ";"
            + SignatureConstants.ALGO_ID_DIGEST_SHA512
            ;

    /**
     * Prepare XMLRegistrySupport.
     */
    @BeforeClass
    public static void setUpContext() {
        OpenSamlHelper.initialize();
        EidasExtensionConfiguration.configureExtension(new EidasProtocolProcessor(null, null, null));
    }

    private EidasMetadata.Generator metadataGenerator;

    /**
     * Test the generation of EidasMetadata based on MetadataParameters
     * Verifying generated digest methods are correctly generated with no duplicate and in the good order.
     *
     * Must succeed.
     */
    @Test
    public void testEidasMetadataGenerationWithMetadataParameters() throws Exception {
        EidasMetadataParameters parameters = createMetadataParameters();
        parameters.setDigestMethods(DIGEST_METHODS + ";" + SignatureConstants.ALGO_ID_DIGEST_SHA256);

        metadataGenerator = EidasMetadata.generator(parameters);

        MetadataSignerI signer = Mockito.mock(MetadataSignerI.class);

        EidasMetadata metadata = metadataGenerator.generate(signer);

        EidasMetadataParametersI actualMetadata = getMetadataParameters(metadata);

        Assert.assertEquals(DIGEST_METHODS, actualMetadata.getDigestMethods());
    }

    private EidasMetadataParametersI getMetadataParameters(EidasMetadata metadata)
            throws UnmarshallException, EIDASMetadataException {
        Assert.assertNotNull(metadata);

        EntityDescriptorContainer metadataDescriptor = MetadataUtil.deserializeEntityDescriptor(metadata.getMetadata());
        Assert.assertNotNull(metadataDescriptor);
        Assert.assertEquals(1, metadataDescriptor.getEntityDescriptors().size());

        return MetadataUtil.convertEntityDescriptor(metadataDescriptor.getEntityDescriptors().get(0));
    }

    private EidasMetadataParameters createMetadataParameters() throws Exception {
        EidasMetadataParameters metadataParameters = new EidasMetadataParameters();

        metadataParameters.setOrganization(OrganizationData.builder().build());
        metadataParameters.setSupportContact(ContactData.builder().build());
        metadataParameters.setTechnicalContact(ContactData.builder().build());
        EidasMetadataRoleParametersI mockRoleDescriptor = createMockRoleDescriptor(MetadataRole.SP);
        metadataParameters.getRoleDescriptors().add(mockRoleDescriptor);

        metadataParameters.setDigestMethods(DIGEST_METHODS);
        return metadataParameters;
    }

    private EidasMetadataRoleParametersI createMockRoleDescriptor(MetadataRole role) throws Exception {
        EidasMetadataRoleParametersI mockRoleDescriptor = new EidasMetadataRoleParameters();
        mockRoleDescriptor.setRole(role);
        mockRoleDescriptor.setEncryptionCertificate(getCertificate(TEST_CERTIFICATE_PATH));
        mockRoleDescriptor.setSigningCertificate(getCertificate(TEST_CERTIFICATE_PATH));
        return mockRoleDescriptor;
    }

    protected X509Certificate getCertificate(String certificatePath) throws CertificateException, FileNotFoundException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        FileInputStream certificateFileInputStream = new FileInputStream(certificatePath);
        return (X509Certificate) certificateFactory.generateCertificate(certificateFileInputStream);
    }
}
