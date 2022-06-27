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

package eu.eidas.node.auth.metadata;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.DefaultProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.configuration.dom.SignatureKey;
import eu.eidas.auth.engine.core.eidas.spec.EidasSignatureConstants;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.node.auth.AUNODEUtil;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.utils.EidasNodeMetadataGenerator;
import org.apache.xml.security.signature.XMLSignature;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class EidasNodeMetadataGeneratorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEidasNodeFileMetadataProcessor.class.getName());
    private static final String SERVICE_METADATA_REPO="src/test/resources/ServiceMetadataRepo/";
    private static final String FILEREPO_DIR_WRITE="target/test/EntityDescriptors1/";
    private static final String ENTITY_ID="http://connectorasIdpurl";
    private static final String PROXY_SERVICE_ENTITY_ID="http://localhost/EidasNode/ServiceMetadata";
    private static final String SERVICE_METADATA_URL="http://localhost:8080/EidasNode/ServiceMetadata";
    private static final String CONNECTOR_COUNTRY_B = "CB";

    private static final String SAML_CONNECTOR_IDP = "METADATA";
    private static final String BINDING_LOCATION_URL = "http://localhost:8080/EidasNode/ServiceProvider";
    private static final String BINDING_LOCATION_URL_NOT_IN_PROPERTIES = "${ssos.serviceMetadataGeneratorIDP.redirect.location}";

    @BeforeClass
    public static void setUp(){
        LOGGER.debug("initializing directory " + FILEREPO_DIR_WRITE);
        new File(FILEREPO_DIR_WRITE).mkdirs();
            OpenSamlHelper.initialize();
    }

    @AfterClass
    public static void removeDir() {
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE));
    }

    private void putMetadataInFile(String fileName, String metadataContent){
        File f=new File(fileName);
        try {
            FileWriter fw = new FileWriter(f);
            fw.append(metadataContent);
            fw.close();
        }catch(IOException ioe){
            Assert.fail("error writing metadata contents: "+ioe);
        }
    }

    @Test
    @Ignore
    public void testGenerateMetadataConnectorasIdP() throws Exception {
        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        generator.setSingleSignOnServicePostLocation(BINDING_LOCATION_URL);
        generator.setSingleSignOnServiceRedirectLocation(BINDING_LOCATION_URL);

        ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATA");

        String metadata = generator.generateConnectorMetadata(engine);
        Assert.assertTrue(metadata.contains("<?xml"));

        putMetadataInFile(FILEREPO_DIR_WRITE+"/test.xml", metadata);
        CachingMetadataFetcher fetcher = new CachingMetadataFetcher();
        FileMetadataLoader loader = new FileMetadataLoader();
        loader.setRepositoryPath(FILEREPO_DIR_WRITE);
        fetcher.setMetadataLoaderPlugin(loader);
        fetcher.setCache(new SimpleMetadataCaching(86400));
        fetcher.initProcessor();

        EidasMetadataParametersI ed = fetcher.getEidasMetadata(ENTITY_ID, (MetadataSignerI) engine.getSigner(), (MetadataClockI) engine.getClock());
        Assert.assertNotNull(ed);
    }

    private final static String CONTACT_SOURCE="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
            "<properties>" +
            "\t<entry key=\"connector.contact.support.email\">contact.support@eidas-connector.eu</entry>\n" +
            "\t<entry key=\"connector.contact.support.company\">eIDAS Connector Operator</entry>\n" +
            "\t<entry key=\"connector.contact.support.givenname\">John</entry>\n" +
            "\t<entry key=\"connector.contact.support.surname\">Doe</entry>\n" +
            "\t<entry key=\"connector.contact.support.phone\">+40 123456</entry>\n" +
            "\t<entry key=\"connector.contact.technical.email\">contact.technical@eidas-connector.eu</entry>\n" +
            "\t<entry key=\"connector.contact.technical.company\">eIDAS Connector Operator</entry>\n" +
            "\t<entry key=\"connector.contact.technical.givenname\">John</entry>\n" +
            "\t<entry key=\"connector.contact.technical.surname\">Doe</entry>\n" +
            "\t<entry key=\"connector.contact.technical.phone\">+41 123456</entry>\n" +
            "\t\t<!-- service-->\n" +
            "\t<entry key=\"service.contact.support.email\">contact.support@eidas-proxyservice.eu</entry>\n" +
            "\t<entry key=\"service.contact.support.company\">eIDAS ProxyService Operator</entry>\n" +
            "\t<entry key=\"service.contact.support.givenname\">John</entry>\n" +
            "\t<entry key=\"service.contact.support.surname\">Doe</entry>\n" +
            "\t<entry key=\"service.contact.support.phone\">+42 123456</entry>\n" +
            "\t<entry key=\"service.contact.technical.email\">contact.technical@eidas-proxyservice.eu</entry>\n" +
            "\t<entry key=\"service.contact.technical.company\">eIDAS ProxyService Operator</entry>\n" +
            "\t<entry key=\"service.contact.technical.givenname\">John</entry>\n" +
            "\t<entry key=\"service.contact.technical.surname\">Doe</entry>\n" +
            "\t<entry key=\"service.contact.technical.phone\">+43 123456</entry>\n" +
            "</properties>";
    private final static String CONTACT_SOURCE_INCOMPLETE="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
            "<properties>" +
            "\t<entry key=\"connector.contact.support.email\">contact.support@eidas-connector.eu</entry>\n" +
            "\t<entry key=\"connector.contact.support.company\">eIDAS Connector Operator</entry>\n" +
            "\t<entry key=\"connector.contact.support.givenname\">John</entry>\n" +
            "\t<entry key=\"connector.contact.support.surname\">Doe</entry>\n" +
            "\t<entry key=\"connector.contact.technical.email\">contact.technical@eidas-connector.eu</entry>\n" +
            "\t<entry key=\"connector.contact.technical.company\">eIDAS Connector Operator</entry>\n" +
            "\t<entry key=\"connector.contact.technical.givenname\">John</entry>\n" +
            "\t<entry key=\"connector.contact.technical.surname\">Doe</entry>\n" +
            "\t\t<!-- service-->\n" +
            "\t<entry key=\"service.contact.support.email\">contact.support@eidas-proxyservice.eu</entry>\n" +
            "\t<entry key=\"service.contact.support.company\">eIDAS ProxyService Operator</entry>\n" +
            "\t<entry key=\"service.contact.support.givenname\">John</entry>\n" +
            "\t<entry key=\"service.contact.support.phone\">+42 123456</entry>\n" +
            "\t<entry key=\"service.contact.technical.company\">eIDAS ProxyService Operator</entry>\n" +
            "\t<entry key=\"service.contact.technical.givenname\">John</entry>\n" +
            "\t<entry key=\"service.contact.technical.surname\">Doe</entry>\n" +
            "\t<entry key=\"service.contact.technical.phone\">+43 123456</entry>\n" +
            "</properties>";
    private static final String EXPECTED_METADATA_CONTACT="GivenName>John</";

    @Test
    @Ignore
    public void testGenerateMetadataWithContacts() throws Exception {
        Properties contactProps=loadContactProps(CONTACT_SOURCE);

        AUNODEUtil nodeUtil = Mockito.mock(AUNODEUtil.class);
        Mockito.when(nodeUtil.getConfigs()).thenReturn(contactProps);

        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        generator.setNodeUtil(nodeUtil);
        generator.setSingleSignOnServicePostLocation(BINDING_LOCATION_URL);
        generator.setSingleSignOnServiceRedirectLocation(BINDING_LOCATION_URL);

        String metadata = generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
        Assert.assertTrue(metadata.contains("<?xml"));
        Assert.assertTrue(metadata.contains(EXPECTED_METADATA_CONTACT));

        contactProps=loadContactProps(CONTACT_SOURCE_INCOMPLETE);
        Mockito.when(nodeUtil.getConfigs()).thenReturn(contactProps);

        metadata = generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
        Assert.assertTrue(metadata.contains("<?xml"));
        Assert.assertTrue(metadata.contains(EXPECTED_METADATA_CONTACT));
    }

    @Test (expected = EIDASServiceException.class)
    @Ignore
    public void testGenerateMetadataWithOutSSOSPostLocation() throws Exception {
        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        generator.setSingleSignOnServicePostLocation(BINDING_LOCATION_URL);
        generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
    }

    @Test (expected = EIDASServiceException.class)
    @Ignore
    public void testGenerateMetadataWithOutSSOSRedirectLocation() throws Exception {
        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        generator.setSingleSignOnServiceRedirectLocation(BINDING_LOCATION_URL);
        generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
    }

    @Test (expected = EIDASServiceException.class)
    @Ignore
    public void testGenerateMetadataWithoutSSOSRedirectLocationInpPropertiesFile() throws Exception {
        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        generator.setSingleSignOnServicePostLocation(BINDING_LOCATION_URL);
        generator.setSingleSignOnServiceRedirectLocation(BINDING_LOCATION_URL_NOT_IN_PROPERTIES);
        generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
    }

    @Test (expected = EIDASServiceException.class)
    @Ignore
    public void testGenerateMetadataWithoutSSOSPosttLocationInpPropertiesFile() throws Exception {
        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        generator.setSingleSignOnServicePostLocation(BINDING_LOCATION_URL_NOT_IN_PROPERTIES);
        generator.setSingleSignOnServiceRedirectLocation(BINDING_LOCATION_URL);
        generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the {@link eu.eidas.auth.engine.configuration.dom.SignatureKey#SIGNATURE_ALGORITHM_WHITE_LIST} is not defined
     * <p>
     * The metadata should publish the Default signature algorithm whitelist.
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithDefaultSigningAlgorithms() throws UnmarshallException, EIDASMetadataException {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();

        AUSERVICEUtil nodeUtil = new AUSERVICEUtil();
        nodeUtil.setConfigs(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        Assert.assertEquals(EidasSignatureConstants.DEFAULT_SIGNATURE_ALGORITHM_WHITE_LIST, getSigningAlgorithms(metadata));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the {@link eu.eidas.auth.engine.configuration.dom.SignatureKey#SIGNATURE_ALGORITHM_WHITE_LIST} is defined
     * with one value allowed by the specifications and one other value
     * <p>
     * The metadata should only publish the signature algorithm from specifications.
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithUnsupportedSigningAlgorithms() throws UnmarshallException, EIDASMetadataException {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();
        List<String> signAlgorithms = Arrays.asList(
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1
        );
        String signAlgorithmPropertyValue = String.join(";", signAlgorithms);
        nodeProps.setProperty(SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.toString(), signAlgorithmPropertyValue);

        AUSERVICEUtil nodeUtil = new AUSERVICEUtil();
        nodeUtil.setConfigs(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        ImmutableSet<String> expectedListOfSigningAlgorithms = ImmutableSet.of(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1);
        Assert.assertEquals(expectedListOfSigningAlgorithms, getSigningAlgorithms(metadata));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the {@link EidasParameterKeys#UNSUPPORTED_ATTRIBUTES} is defined
     * <p>
     * The metadata should only publish the attributes which NamedURI are not present in the parameter
     * comma-separated value list.
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithOnlySupportedAttributes() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();
        String unsupportedAttributes = getUnsupportedAttributesAsJoinedString(EidasSpec.Definitions.BIRTH_NAME, EidasSpec.Definitions.LEGAL_NAME);
        nodeProps.setProperty(EidasParameterKeys.UNSUPPORTED_ATTRIBUTES.toString(), unsupportedAttributes);

        AUSERVICEUtil nodeUtil = new AUSERVICEUtil();
        nodeUtil.setConfigs(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        Assert.assertFalse(isIDPMetadataDescriptorContaining(metadata, EidasSpec.Definitions.BIRTH_NAME));
        Assert.assertFalse(isIDPMetadataDescriptorContaining(metadata, EidasSpec.Definitions.LEGAL_NAME));
        Assert.assertTrue(isIDPMetadataDescriptorContaining(metadata, EidasSpec.Definitions.GENDER));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the {@link EidasParameterKeys#UNSUPPORTED_ATTRIBUTES} is not defined
     * <p></p>
     * The metadata should publish all the eIDAS attributes
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithAllEidasAttributes() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();

        AUSERVICEUtil nodeUtil = new AUSERVICEUtil();
        nodeUtil.setConfigs(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        for (AttributeDefinition<?> attributeDefinition: EidasSpec.REGISTRY.getAttributes()) {
            Assert.assertTrue(isIDPMetadataDescriptorContaining(metadata, attributeDefinition));
        }
    }


    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the RequesterIdFlag is set to true in Properties
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithRequesterIdFlagTrue() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();
        setRequesterIdFlagPropertyTrue(nodeProps);
        AUNODEUtil nodeUtil = Mockito.mock(AUSERVICEUtil.class);
        Mockito.when(nodeUtil.getConfigs()).thenReturn(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        Assert.assertTrue(metadataContainsRequesterIdFlagAtEndOfEntityAttributesElement(metadata));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the RequesterIdFlag is set to false in Properties
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithRequesterIdFlagFalse() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();
        setRequesterIdFlagPropertyFalse(nodeProps);
        AUNODEUtil nodeUtil = Mockito.mock(AUSERVICEUtil.class);
        Mockito.when(nodeUtil.getConfigs()).thenReturn(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        Assert.assertFalse(metadataContainsRequesterIdFlagAtEndOfEntityAttributesElement(metadata));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateConnectorMetadata(ProtocolEngineI)}
     * when the spType is set to private in Properties
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateConnectorMetadataWithPrivateSpType() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorConnector = buildEidasNodeMetadataGenerator();

        Properties properties = createProperties();
        setRequesterIdFlagPropertyTrue(properties);
        setSpTypePrivate(properties);
        AUNODEUtil nodeUtil = Mockito.mock(AUSERVICEUtil.class);
        Mockito.when(nodeUtil.getConfigs()).thenReturn(properties);
        eidasNodeMetadataGeneratorConnector.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorConnector
                .generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        assertMetadataContainsSpType(metadata, SpType.PRIVATE.getValue());
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateConnectorMetadata(ProtocolEngineI)}
     * when the spType is set to public in Properties
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateConnectorMetadataWithPublicSpType() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorConnector = buildEidasNodeMetadataGenerator();

        Properties properties = createProperties();
        setRequesterIdFlagPropertyTrue(properties);
        setSpTypePublic(properties);
        AUNODEUtil nodeUtil = Mockito.mock(AUSERVICEUtil.class);
        Mockito.when(nodeUtil.getConfigs()).thenReturn(properties);
        eidasNodeMetadataGeneratorConnector.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorConnector
                .generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        assertMetadataContainsSpType(metadata, SpType.PUBLIC.getValue());
    }

    @Nonnull
    private Properties createProperties() {
        final Properties nodeProps = new Properties();
        return nodeProps;
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the RequesterIdFlag is set to false in Properties
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithRequesterIdFlagNotSet() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();
        AUNODEUtil nodeUtil = Mockito.mock(AUSERVICEUtil.class);
        Mockito.when(nodeUtil.getConfigs()).thenReturn(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        Assert.assertFalse(metadataContainsRequesterIdFlagAtEndOfEntityAttributesElement(metadata));
    }

    private void setRequesterIdFlagPropertyTrue(final Properties nodeProps) {
        setRequesterIdFlagValue(nodeProps, "true");
    }

    private void setRequesterIdFlagPropertyFalse(final Properties nodeProps) {
        setRequesterIdFlagValue(nodeProps, "false");
    }

    private void setRequesterIdFlagValue(final Properties nodeProps, final String value) {
        nodeProps.setProperty(EIDASValues.REQUESTER_ID_FLAG.toString(), value);
    }

    private void setSpTypePublic(final Properties properties) {
        setSpTypeValue(properties, SpType.PUBLIC.getValue());
    }

    private void setSpTypePrivate(final Properties properties) {
        setSpTypeValue(properties, SpType.PRIVATE.getValue());
    }

    private void setSpTypeValue(final Properties properties, final String value) {
        properties.setProperty(EIDASValues.EIDAS_SPTYPE.toString(), value);
    }

    private void assertMetadataContainsSpType(String metadata, String spTypeValue) {
        final String spType = "<eidas:SPType xmlns:eidas=\"http://eidas.europa.eu/saml-extensions\">" + spTypeValue + "</eidas:SPType>";

        Assert.assertThat(metadata, CoreMatchers.containsString(spType));
    }

    private boolean metadataContainsRequesterIdFlagAtEndOfEntityAttributesElement(String metadata) {
        final String requesterIdFlag =
                "<saml2:Attribute Name=\"http://macedir.org/entity-category\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">" +
                        "<saml2:AttributeValue xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xsd:string\">" +
                            "http://eidas.europa.eu/entity-attributes/termsofaccess/requesterid" +
                        "</saml2:AttributeValue>" +
                "</saml2:Attribute>" +
        "</mdattr:EntityAttributes>";

        return metadata.contains(requesterIdFlag);
    }

    private boolean isIDPMetadataDescriptorContaining(String metadata, AttributeDefinition<?> attributeDefinition) {
        String attributeDefinitionInMetadata = "<saml2:Attribute xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\""
                + " Name=\"" + attributeDefinition.getNameUri().toASCIIString() + "\""
                + " NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\""
                + "/>";

        String idpSSODescriptor = metadata.substring(metadata.indexOf("<md:IDPSSODescriptor"), metadata.indexOf("</md:IDPSSODescriptor"));

        return idpSSODescriptor.contains(attributeDefinitionInMetadata);
    }

    private ImmutableSet<String> getSigningAlgorithms(String metadata) throws UnmarshallException, EIDASMetadataException {
        EntityDescriptor entityDescriptor = MetadataUtil.deserializeEntityDescriptor(metadata).getEntityDescriptors().get(0);
        EidasMetadataParametersI params = MetadataUtil.convertEntityDescriptor(entityDescriptor);
        ImmutableSet.Builder metadataSigningAlgorithms = ImmutableSet.builder();
        for (String signAlgorithm : params.getSigningMethods().split("[;,]")) {
            metadataSigningAlgorithms.add(signAlgorithm.trim());
        }
        return metadataSigningAlgorithms.build();
    }

    private Properties loadContactProps(String source){
        Properties props=new Properties();
        try {
            InputStream stream = new ByteArrayInputStream(EidasStringUtil.getBytes(source));
            props.loadFromXML(stream);
        }catch(Exception exc){
            Assert.fail("cannot load properties "+exc);
        }
        return props;
    }

    private String getUnsupportedAttributesAsJoinedString(AttributeDefinition<?> ... attributeDefinitions) {
        List<String> attributesNamedURI = Arrays.stream(attributeDefinitions)
                .map(AttributeDefinition::getNameUri)
                .map(URI::toASCIIString)
                .collect(Collectors.toList());
        return String.join(";", attributesNamedURI);
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


    private EidasNodeMetadataGenerator buildEidasNodeMetadataGeneratorProxyService() {
        final EidasNodeMetadataGenerator eidasNodeMetadataGenerator = new EidasNodeMetadataGenerator();
        eidasNodeMetadataGenerator.setProxyServiceEngine(SAML_CONNECTOR_IDP);
        eidasNodeMetadataGenerator.setProxyServiceMetadataUrl(PROXY_SERVICE_ENTITY_ID);
        eidasNodeMetadataGenerator.setSingleSignOnServicePostLocation(BINDING_LOCATION_URL);
        eidasNodeMetadataGenerator.setSingleSignOnServiceRedirectLocation(BINDING_LOCATION_URL);

        eidasNodeMetadataGenerator.setNodeProtocolEngineFactory(DefaultProtocolEngineFactory.getInstance());

        return eidasNodeMetadataGenerator;
    }

}