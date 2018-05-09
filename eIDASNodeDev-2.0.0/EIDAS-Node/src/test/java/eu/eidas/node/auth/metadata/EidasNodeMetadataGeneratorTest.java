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

package eu.eidas.node.auth.metadata;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.DefaultProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.node.utils.EidasNodeMetadataGenerator;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.util.Properties;

//TODO rewrite unit tests
@Ignore
public class EidasNodeMetadataGeneratorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEidasNodeFileMetadataProcessor.class.getName());
    private static final String SERVICE_METADATA_REPO="src/test/resources/ServiceMetadataRepo/";
    private static final String FILEREPO_DIR_WRITE="target/test/EntityDescriptors1/";
    private static final String ENTITY_ID="http://connectorasIdpurl";
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
    public void testGenerateMetadataWithContacts() throws Exception {
        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        Properties contactProps=loadContactProps(CONTACT_SOURCE);
        generator.setNodeProps(contactProps);
        generator.setSingleSignOnServicePostLocation(BINDING_LOCATION_URL);
        generator.setSingleSignOnServiceRedirectLocation(BINDING_LOCATION_URL);

        String metadata = generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
        Assert.assertTrue(metadata.contains("<?xml"));
        Assert.assertTrue(metadata.contains(EXPECTED_METADATA_CONTACT));

        contactProps=loadContactProps(CONTACT_SOURCE_INCOMPLETE);
        generator.setNodeProps(contactProps);

        metadata = generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
        Assert.assertTrue(metadata.contains("<?xml"));
        Assert.assertTrue(metadata.contains(EXPECTED_METADATA_CONTACT));
    }

    @Test (expected = EIDASServiceException.class)
    public void testGenerateMetadataWithOutSSOSPostLocation() throws Exception {
        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        generator.setSingleSignOnServicePostLocation(BINDING_LOCATION_URL);
        generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
    }

    @Test (expected = EIDASServiceException.class)
    public void testGenerateMetadataWithOutSSOSRedirectLocation() throws Exception {
        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        generator.setSingleSignOnServiceRedirectLocation(BINDING_LOCATION_URL);
        generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
    }

    @Test (expected = EIDASServiceException.class)
    public void testGenerateMetadataWithoutSSOSRedirectLocationInpPropertiesFile() throws Exception {
        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        generator.setSingleSignOnServicePostLocation(BINDING_LOCATION_URL);
        generator.setSingleSignOnServiceRedirectLocation(BINDING_LOCATION_URL_NOT_IN_PROPERTIES);
        generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
    }

    @Test (expected = EIDASServiceException.class)
    public void testGenerateMetadataWithoutSSOSPosttLocationInpPropertiesFile() throws Exception {
        EidasNodeMetadataGenerator generator = buildEidasNodeMetadataGenerator();
        generator.setSingleSignOnServicePostLocation(BINDING_LOCATION_URL_NOT_IN_PROPERTIES);
        generator.setSingleSignOnServiceRedirectLocation(BINDING_LOCATION_URL);
        generator.generateConnectorMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));
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