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

package eu.eidas.auth.engine.metadata;

import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.core.eidas.EidasExtensionConfiguration;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.auth.engine.core.impl.AbstractProtocolSigner;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataParameters;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataRoleParameters;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Test class for checking the validity of generated metadata
 * produced by {@link EidasMetadata.Generator}
 */
public class EidasMetadataValidationTest {

    private static final String SAML_METADATA_SCHEMA_PATH = "src/test/resources/metadata/xsds/saml-schema-metadata-2.0.xsd";

    private static Validator samlMetadataValidator;

    @BeforeClass
    public static void setUpClass() throws Exception {
        OpenSamlHelper.initialize();
        EidasExtensionConfiguration.configureExtension(
                new EidasProtocolProcessor(null, null, null),
                Mockito.mock(AbstractProtocolSigner.class));

        initSamlMetadataValidator();
    }

    private static void initSamlMetadataValidator() throws SAXException, IOException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // schema from https://docs.oasis-open.org/security/saml/v2.0/saml-schema-metadata-2.0.xsd
        File samlMetadataXSDFile = new File(SAML_METADATA_SCHEMA_PATH);
        Schema samlMetadataSchema = schemaFactory.newSchema(samlMetadataXSDFile);
        samlMetadataValidator = samlMetadataSchema.newValidator();
    }

    /**
     * Test the validity of EidasMetadata xml generated based on MetadataParameters
     *
     * Must succeed.
     */
    @Test
    public void validateMetadataXML() throws Exception {
        EidasMetadataParametersI parameters = createMetadataParameters("EU");

        EidasMetadata.Generator metadataGenerator = EidasMetadata.generator(parameters);
        MetadataSignerI signer = Mockito.mock(MetadataSignerI.class);
        EidasMetadata metadata = metadataGenerator.generate(signer);

        validateWithXSD(metadata.getMetadata());
    }

    /**
     * Test the validity of EidasMetadata xml generated based on MetadataParameters
     * when node country code is null.
     *
     * Must succeed.
     */
    @Test
    public void validateMetadataXMLNullNodeCountryCode() throws Exception {
        EidasMetadataParametersI parameters = createMetadataParameters(null);

        EidasMetadata.Generator metadataGenerator = EidasMetadata.generator(parameters);
        MetadataSignerI signer = Mockito.mock(MetadataSignerI.class);
        EidasMetadata metadata = metadataGenerator.generate(signer);

        validateWithXSD(metadata.getMetadata());
    }

    private void validateWithXSD(String xmlMetadataContent) throws IOException, SAXException {
        InputStream xmlMetadataInputStream = new ByteArrayInputStream(xmlMetadataContent.getBytes());
        samlMetadataValidator.validate(new StreamSource(xmlMetadataInputStream));
    }


    private EidasMetadataParameters createMetadataParameters(String nodeCountry) throws Exception {
        EidasMetadataParameters metadataParameters = new EidasMetadataParameters();
        metadataParameters.setEntityID("http://test.entityid.com");
        metadataParameters.setNodeCountry(nodeCountry);

        metadataParameters.setOrganization(OrganizationData.builder().build());
        metadataParameters.setSupportContact(ContactData.builder().build());
        metadataParameters.setTechnicalContact(ContactData.builder().build());
        EidasMetadataRoleParametersI mockRoleDescriptor = createMockRoleDescriptor(MetadataRole.SP);
        metadataParameters.getRoleDescriptors().add(mockRoleDescriptor);

        return metadataParameters;
    }

    private EidasMetadataRoleParametersI createMockRoleDescriptor(MetadataRole role) throws Exception {
        EidasMetadataRoleParametersI mockRoleDescriptor = new EidasMetadataRoleParameters();
        mockRoleDescriptor.setRole(role);
        mockRoleDescriptor.setEncryptionCertificates(List.of(mockCertificate()));
        mockRoleDescriptor.setSigningCertificates(List.of(mockCertificate()));
        mockRoleDescriptor.setDefaultBinding("http");
        mockRoleDescriptor.addProtocolBindingLocation("http", "test.url");
        return mockRoleDescriptor;
    }

    protected X509Certificate mockCertificate() throws CertificateEncodingException {
        X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);
        Mockito.when(mockX509Certificate.getEncoded()).thenReturn("testCertificate".getBytes());
        return mockX509Certificate;
    }

}

