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

package eu.eidas.node.auth.metadata;

import eu.eidas.RecommendedSecurityProviders;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.DefaultProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.connector.exceptions.ConnectorError;
import eu.eidas.node.utils.EidasNodeMetadataGenerator;
import eu.eidas.node.utils.ReflectionUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;

public class EidasNodeMetadataGeneratorTest {

    private static final String ENTITY_ID = "http://connectorasIdpurl";
    private static final String SAML_CONNECTOR_IDP = "METADATA";
    private static final String EXPECTED_METADATA_CONTACT = "GivenName>John</";
    private static final String CONTACT_SOURCE_PATH = "src/test/resources/contactSources/ContactSource.xml";
    private static final String CONTACT_SOURCE_INCOMPLETE_PATH = "src/test/resources/contactSources/ContactSourceIncomplete.xml";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUp() {
        RecommendedSecurityProviders.setupRecommendedSecurityProviders();
        OpenSamlHelper.initialize();
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateConnectorMetadata(ProtocolEngineI)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadata() {
        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        Properties mockedProperties = Mockito.mock(Properties.class);
        AUCONNECTORUtil nodeUtil = new AUCONNECTORUtil();
        nodeUtil.setConfigs(mockedProperties);
        generator.setNodeUtil(nodeUtil);

        ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATA");

        String metadata = generator.generateConnectorMetadata(engine);
        Assert.assertTrue(metadata.contains("<?xml"));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateConnectorMetadata(ProtocolEngineI)}
     * When properties contains contacts
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithContacts() throws IOException {
        Properties contactProps = loadContactProps(Files.readString(Paths.get(CONTACT_SOURCE_PATH)));

        AUCONNECTORUtil nodeUtil = Mockito.mock(AUCONNECTORUtil.class);
        Mockito.when(nodeUtil.getConfigs()).thenReturn(contactProps);

        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        generator.setNodeUtil(nodeUtil);

        String metadata = generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
        Assert.assertTrue(metadata.contains("<?xml"));
        Assert.assertTrue(metadata.contains(EXPECTED_METADATA_CONTACT));

        contactProps = loadContactProps(Files.readString(Paths.get(CONTACT_SOURCE_INCOMPLETE_PATH)));
        Mockito.when(nodeUtil.getConfigs()).thenReturn(contactProps);

        metadata = generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
        Assert.assertTrue(metadata.contains("<?xml"));
        Assert.assertTrue(metadata.contains(EXPECTED_METADATA_CONTACT));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateConnectorMetadata(ProtocolEngineI)}
     * When ProtocolEngine.getDecryptionCertificate throws an EIDASSamlEngineException
     * <p>
     * Must fail.
     */
    @Test(expected = ConnectorError.class)
    public void testGenerateMetadataDecryptionCertificateException() throws Exception {
        ApplicationContext mockApplicationContext = Mockito.mock(ApplicationContext.class);
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        ApplicationContext oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        final ResourceBundleMessageSource mockResourceBundleMessageSource = Mockito.mock(ResourceBundleMessageSource.class);
        Mockito.when(mockApplicationContext.getBean((String) any())).thenReturn(mockResourceBundleMessageSource);
        Mockito.when(mockResourceBundleMessageSource.getMessage(any(), any(), any(), any())).thenReturn("Test");

        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        ProtocolEngineFactory mockProtocolEngineFactory = Mockito.mock(ProtocolEngineFactory.class);
        generator.setNodeProtocolEngineFactory(mockProtocolEngineFactory);
        ProtocolEngine mockProtocolEngine = Mockito.mock((ProtocolEngine.class));
        Mockito.when(mockProtocolEngineFactory.getProtocolEngine(any())).thenReturn(mockProtocolEngine);
        Mockito.when(mockProtocolEngine.getDecryptionCertificates()).thenThrow(new EIDASSAMLEngineException(
                EidasErrorKey.SAML_ENGINE_NO_METADATA, ""));
        Properties mockedProperties = Mockito.mock(Properties.class);
        AUCONNECTORUtil nodeUtil = new AUCONNECTORUtil();
        nodeUtil.setConfigs(mockedProperties);

        generator.setNodeUtil(nodeUtil);

        generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateConnectorMetadata(ProtocolEngineI)}
     * when metadata signature validation is set to true and
     * interconnection graph is set to true
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithInterconnectionGraphDataEnabledAndSignatureCheckEnabled() {
        final EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        final Properties properties = new Properties();
        final AUCONNECTORUtil nodeUtil = new AUCONNECTORUtil();
        nodeUtil.setConfigs(properties);
        generator.setNodeUtil(nodeUtil);

        properties.setProperty(EIDASValues.INTERCONNECTION_GRAPH_ENABLED.toString(), "true");
        properties.setProperty(EIDASValues.METADATA_CHECK_SIGNATURE.toString(), "true");
        properties.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(), "1");

        final ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATA");
        final String metadata = generator.generateConnectorMetadata(engine);

        Assert.assertTrue(metadata.contains("connector-md-signature-trust-store"));
        Assert.assertTrue(metadata.contains("connector-recognized-urls"));
        Assert.assertTrue(metadata.contains("<?xml"));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateConnectorMetadata(ProtocolEngineI)}
     * when metadata signature validation is set to false and
     * interconnection graph is set to true
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithInterconnectionGraphDataEnabledAndSignatureCheckDisabled() {
        final EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        final Properties properties = new Properties();
        final AUCONNECTORUtil nodeUtil = new AUCONNECTORUtil();
        nodeUtil.setConfigs(properties);
        generator.setNodeUtil(nodeUtil);

        properties.setProperty(EIDASValues.INTERCONNECTION_GRAPH_ENABLED.toString(), "true");
        properties.setProperty(EIDASValues.METADATA_CHECK_SIGNATURE.toString(), "false");
        properties.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(), "1");

        final ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATA");
        final String metadata = generator.generateConnectorMetadata(engine);

        Assert.assertFalse(metadata.contains("connector-md-signature-trust-store"));
        Assert.assertTrue(metadata.contains("connector-recognized-urls"));
        Assert.assertTrue(metadata.contains("<?xml"));
    }

    private Properties loadContactProps(String source) {
        Properties props = new Properties();
        try {
            InputStream stream = new ByteArrayInputStream(EidasStringUtil.getBytes(source));
            props.loadFromXML(stream);
        } catch (Exception exc) {
            Assert.fail("cannot load properties " + exc);
        }
        return props;
    }

    private EidasNodeMetadataGenerator buildEidasNodeMetadataGenerator() {
        EidasNodeMetadataGenerator generator = new EidasNodeMetadataGenerator();
        //generator.
        generator.setConnectorEngine(SAML_CONNECTOR_IDP);
        generator.setConnectorMetadataUrl(ENTITY_ID);
        generator.setConnectorEngine(SAML_CONNECTOR_IDP);
        generator.setNodeProtocolEngineFactory(DefaultProtocolEngineFactory.getInstance());
        return generator;
    }
}