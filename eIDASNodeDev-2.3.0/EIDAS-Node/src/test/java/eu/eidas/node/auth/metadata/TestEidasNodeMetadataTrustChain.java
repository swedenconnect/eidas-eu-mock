/*
 * Copyright (c) 2018 by European Commission
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
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.auth.engine.metadata.EntityDescriptorContainer;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.auth.util.tests.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.core.Is.isA;


/**
 * Tests with several combinations of signing/publishing certificates in the metadata and validating that same metadata.
 */
public class TestEidasNodeMetadataTrustChain {

    private static final String FILEREPO_DIR_READ_COMBO="src/test/resources/EntityDescriptors5/";
    private static final String FILEREPO_DIR_WRITE ="target/test/EntityDescriptors5/";

    private static String metadataTC;//metadata generated with all certificates in the trust chain published
    private static String metadataWithoutRootCA;//metadata generated without publishing the rootcametadata certificate
    private static String metadataWrongOrderExtraCertificate;//metadata generated publishing trust chain and a extra certificate not belonging to the trust chain
    private static String metadataSingleCertificatePublished;//metadata generated publishing metadata signing certificate only
    private static String metadataTcWithoutIntermediateCA;//metadata generated publishing trust chain except the intermediatecametadata

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setUp() throws MarshallException, EIDASMetadataException, EIDASSAMLEngineException, IOException {
        initWorkFolder(FILEREPO_DIR_READ_COMBO, FILEREPO_DIR_WRITE);
        OpenSamlHelper.initialize();
        metadataTC = loadSignMetadata("METADATA_TC");//signed metadata and publishing the full certificates chain
        metadataWithoutRootCA = loadSignMetadata("METADATA_TC_WITHOUT_ROOT_CA_CERTIFICATE");//signed metadata and publishing the certificates chain without the rootCA
        metadataWrongOrderExtraCertificate = loadSignMetadata("METADATA_TC_WRONG_ORDER_EXTRA_CERTIFICATE");//metadata based on a keystore that intermediateCA certificate was added after the other ones
        metadataSingleCertificatePublished = loadSignMetadata("METADATA_SINGLE_CERTIFICATE");//signed metadata and publishing only the certificate which signs it
        metadataTcWithoutIntermediateCA = loadSignMetadata("METADATA_TC_WITHOUT_INTERMEDIATE_CA_CERTIFICATE");//singed metadata publishing the TC except the intermediatecametadata
    }

    private static void initWorkFolder(String sourceFolder, String folderName) throws IOException {
        File sampleNodeRepo = new File(folderName);
        FileSystemUtils.deleteRecursively(sampleNodeRepo);
        Files.createDirectories(Paths.get(folderName));
        FileUtils.copyFolder(Paths.get(sourceFolder), Paths.get(folderName));
    }

    @AfterClass
    public static void removeDir(){
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE));
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the signing key certificate (metadatanode) is the one published
     * and the only one trusted is also metadatanode.
     * <p/>
     * Must succeed.
     */
    @Test
    public void testValidMetadataSignatureSignWithLeafTrustLeaf() throws Exception {
        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataSingleCertificatePublished);
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();

        //there should be only one certificate: metadatanode
        assertNumberCertificates(entitiesDescriptor, 1);

        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_NODE_CERT_TRUST").getSigner();
        checker.validateMetadataSignature(entityDescriptorContainer.getEntitiesDescriptor());
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the entire trust chain is published (metadatanode, intermediatecametadata, rootcametadata).
     * when the trusted certificate is the root (rootcametadata)
     * <p/>
     * Must succeed.
     */
    @Test
    public void testValidMetadataSignature() throws Exception {
		EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataTC);
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();

        //there should be only three certificates: metadatanode, intermediatecametadata, rootcametadata
        assertNumberCertificates(entitiesDescriptor, 3);

        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_ROOT_CA_TRUST").getSigner();
        checker.validateMetadataSignature(entitiesDescriptor);
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the certificates published in the metadata are the entire trust chain (metadatanode, intermediatecametadata, rootcametadata)
     * plus one additional one not belonging to the trust chain (myname) and
     * when the trusted certificate is the root (rootcametadata),
     * <p/>
     * Must succeed.
     */
    @Test
    public void testValidMetadataSignatureWrongOrderExtraCertificate() throws Exception {
        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataWrongOrderExtraCertificate);
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();

        //There should be the total 4 certificates = metadatanode, intermediatecametadata, rootcametadata, myname
        assertNumberCertificates(entitiesDescriptor, 4);

        Assert.assertTrue(isExtraCertificatePublishedInMetadata(entitiesDescriptor));

        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_ROOT_CA_TRUST").getSigner();
        checker.validateMetadataSignature(entitiesDescriptor);
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the certificates published in the metadata are the entire trust chain (metadatanode, intermediatecametadata, rootcametadata)
     * plus one additional one no belonging to the trust chain (myname) and
     * when the trusted certificate is not belonging to the trust chain (myname),
     * <p/>
     * Must fail.
     */
    @Test
    public void testValidMetadataSignatureWrongOrderExtraCertificateWrongTrust() throws Exception {
        thrown.expect(EIDASMetadataException.class);
        thrown.expectCause(isA(EIDASSAMLEngineException.class));

        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataWrongOrderExtraCertificate);
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();

        //There should be the total 4 certificates = metadatanode, intermediatecametadata, rootcametadata, myname
        assertNumberCertificates(entitiesDescriptor, 4);

        Assert.assertTrue(isExtraCertificatePublishedInMetadata(entitiesDescriptor));

        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_WRONG_TRUST").getSigner();
        checker.validateMetadataSignature(entitiesDescriptor);
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the certificates published in the metadata are not the entire trust chain (metadatanode, intermediatecametadata), root is not published, and
     * when the trusted certificate is the intermediate issued by the root (intermediatecametadata),
     * <p/>
     * Must succeed.
     */
    @Test
    public void testValidMetadataSignatureWithoutRootCaPublishedTrustingIntermediateCA() throws Exception {
        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataWithoutRootCA);
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();

        //there should be only two certificates: metadatanode, intermediatecametadata
        assertNumberCertificates(entitiesDescriptor, 2);

        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_INTERMEDIATE_CA_TRUST").getSigner();
        checker.validateMetadataSignature(entityDescriptorContainer.getEntitiesDescriptor());
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the certificates published in the metadata are not the entire trust chain (metadatanode, intermediatecametadata), rootcametadata is not published, and
     * when the trusted certificate is the root (rootcametadata),
     * <p/>
     * Must succeed.
     *
     */
    @Test
    public void testValidMetadataSignatureWithoutRootCaPublishedTrustingRootCA() throws Exception {
        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataWithoutRootCA);
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();

        //there should be only two certificates: metadatanode, intermediatecametadata
        assertNumberCertificates(entitiesDescriptor, 2);

        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_ROOT_CA_TRUST").getSigner();
        checker.validateMetadataSignature(entityDescriptorContainer.getEntitiesDescriptor());
    }


    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the certificates published in the metadata are not the entire trust chain (metadatanode, rootcametadata), intermediatecametadata is not published, and
     * when the trusted certificate is the root (rootcametadata),
     * <p/>
     * Must fail.
     *
     */
    @Test
    public void testValidMetadataSignatureWithoutIntermediateCaPublishedTrustingRootCA() throws Exception {
        thrown.expect(EIDASMetadataException.class);
        thrown.expectCause(isA(EIDASSAMLEngineException.class));

        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataTcWithoutIntermediateCA);
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();

        //there should be only two certificates: metadatanode, intermediatecametadata
        assertNumberCertificates(entitiesDescriptor, 2);

        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_ROOT_CA_TRUST").getSigner();
        checker.validateMetadataSignature(entityDescriptorContainer.getEntitiesDescriptor());
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the certificates published in the metadata are not the entire trust chain (metadatanode, rootcametadata), intermediatecametadata is not published, and
     * when the trusted certificate is the intermediate ca (intermediatecametadata),
     * <p/>
     * Must succeed.
     *
     */
    @Test
    public void testValidMetadataSignatureWithoutIntermediateCaPublishedTrustingIntermediateCA() throws Exception {
        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataTcWithoutIntermediateCA);
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();

        //there should be only two certificates: metadatanode, intermediatecametadata
        assertNumberCertificates(entitiesDescriptor, 2);

        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_INTERMEDIATE_CA_TRUST").getSigner();
        checker.validateMetadataSignature(entityDescriptorContainer.getEntitiesDescriptor());
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the certificates published in the metadata are not the entire trust chain (metadatanode, rootcametadata), intermediatecametadata is not published, and
     * when the trusted certificate is the intermediate ca and root (intermediatecametadata, rootcametadata),
     * <p/>
     * Must succeed.
     *
     */
    @Test
    public void testValidMetadataSignatureWithoutIntermediateCaPublishedTrustingIntermediateCARootCA() throws Exception {
        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataTcWithoutIntermediateCA);
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();

        //there should be only two certificates: metadatanode, intermediatecametadata
        assertNumberCertificates(entitiesDescriptor, 2);


        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_INTERMEDIATE_CA_ROOT_CA_TRUST").getSigner();
        checker.validateMetadataSignature(entityDescriptorContainer.getEntitiesDescriptor());
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the certificates published in the metadata are the entire trust chain (metadatanode, intermediatecametadata, rootcametadata)
     * when the trusted certificate is the intermediate issued by the root (intermediatecametadata),
     * <p/>
     * Must succeed.
     */
    @Test
    public void testValidMetadataSignatureIntermediateCATrust() throws Exception {
        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataTC);
        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_INTERMEDIATE_CA_TRUST").getSigner();
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();

        //there should be only three certificates: metadatanode, intermediatecametadata, rootcametadata
        assertNumberCertificates(entitiesDescriptor, 3);

        checker.validateMetadataSignature(entityDescriptorContainer.getEntitiesDescriptor());
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the certificates published in the metadata are the entire trust chain (metadatanode, intermediatecametadata, rootcametadata)
     * when the one trusted is the signing key certificate (metadatanode)
     *
     * <p/>
     * Must succeed.
     */
    @Test
    public void testValidMetadataSignatureMetadataNodeCertificateTrust() throws Exception {
        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataTC);
        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_NODE_CERT_TRUST").getSigner();
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();

        //there should be only three certificates: metadatanode, intermediatecametadata, rootcametadata
        assertNumberCertificates(entitiesDescriptor, 3);

        checker.validateMetadataSignature(entityDescriptorContainer.getEntitiesDescriptor());
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the certificates published in the metadata are the entire trust chain (metadatanode, intermediatecametadata, rootcametadata)
     * when the ones trusted are certificates not of the trust chain (myName)
     * <p/>
     * Must fail.
     */
    @Test
    public void testValidMetadataSignatureWrongTrustFail() throws Exception {
        thrown.expect(EIDASMetadataException.class);
        thrown.expectCause(isA(EIDASSAMLEngineException.class));

        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataTC);

        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();
        //there should be only three certificates: metadatanode, intermediatecametadata, rootcametadata
        assertNumberCertificates(entitiesDescriptor, 3);

        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_WRONG_TRUST").getSigner();
        checker.validateMetadataSignature(entityDescriptorContainer.getEntitiesDescriptor());
    }

    /**
     * Test method for
     * {@link eu.eidas.auth.engine.core.impl.AbstractProtocolSigner#validateMetadataSignature(SignableXMLObject)}
     * when the certificates published in the metadata are the entire trust chain (metadatanode, intermediatecametadata, rootcametadata)
     * when there are no certificates as trusted entries in the keystore, just an unrelated private key that allows the engine to be retrieved.
     * <p/>
     * Must fail.
     */
    @Test
    public void testValidMetadataSignatureEmptyTrustFail() throws Exception {
        thrown.expect(EIDASMetadataException.class);
        thrown.expectCause(isA(EIDASSAMLEngineException.class));

        EntityDescriptorContainer entityDescriptorContainer = MetadataUtil.deserializeEntityDescriptor(metadataTC);
        EntitiesDescriptor entitiesDescriptor = entityDescriptorContainer.getEntitiesDescriptor();
        //there should be only three certificates: metadatanode, intermediatecametadata, rootcametadata
        assertNumberCertificates(entitiesDescriptor, 3);

        MetadataSignerI checker = (MetadataSignerI) getEngine("METADATA_EMPTY_TRUST").getSigner();
        checker.validateMetadataSignature(entityDescriptorContainer.getEntitiesDescriptor());
    }

    /**
     * Load and signs the metadata used as input by the other methods.
     *
     * @return the signed metadata in a string
     * @throws EIDASMetadataException if the entity descriptors cannot be returned from the FileMetadataLoader instance
     * @throws MarshallException if the entitiesDescriptors cannot be marshall
     * @param nameInstance name of the instance of the SAML engine to be used by the metadata generation
     */
    private static String loadSignMetadata(String nameInstance) throws EIDASMetadataException, MarshallException, EIDASSAMLEngineException {
        FileMetadataLoader processor=new FileMetadataLoader();
        processor.setRepositoryPath(FILEREPO_DIR_WRITE);
        List<EntityDescriptorContainer> list = processor.getEntityDescriptors();
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        EntitiesDescriptor eds = (EntitiesDescriptor)builderFactory.getBuilder(EntitiesDescriptor.DEFAULT_ELEMENT_NAME).buildObject(EntitiesDescriptor.DEFAULT_ELEMENT_NAME);
        for(EntityDescriptorContainer edc:list){
            eds.getEntityDescriptors().addAll(edc.getEntityDescriptors());
        }
        MetadataSignerI signer = (MetadataSignerI) getEngine(nameInstance).getSigner();
        EntitiesDescriptor entitiesDescriptor = signer.signMetadata(eds);
        return EidasStringUtil.toString(OpenSamlHelper.marshall(entitiesDescriptor));
    }

    /**
     * Creates the protocol engine related to the name instance passed as parameter.
     *
     * @param nameInstance the name instance of the protocol engine to be created.
     * @return the Protocol Engine instance
     */
    private static ProtocolEngineI getEngine(final String nameInstance) throws EIDASSAMLEngineException {
        return ProtocolEngineFactory.createProtocolEngine(nameInstance, EidasProtocolProcessor.INSTANCE);
    }

    /**
     * Checks if a certificate not related to the trust chain bu present in the metadata signing keystore is present/published in the entitiesDescriptor received as parameter.
     *
     * @param entitiesDescriptor the instance that contains the signed metadata
     * @return true if the extra certificate is in the metadata
     */
    private boolean isExtraCertificatePublishedInMetadata(EntitiesDescriptor entitiesDescriptor) {
        final String metadataExtraCertificate = "MIIFyzCCA7OgAwIBAgIJAOziKm8wZaI7MA0GCSqGSIb3DQEBCwUAMHwxCzAJBgNVBAYTAkJFMREw\n" +
                "DwYDVQQIDAhCcnVzc2VsczERMA8GA1UEBwwIQnJ1c3NlbHMxDjAMBgNVBAoMBURJR0lUMQswCQYD\n" +
                "VQQLDAJEMzEPMA0GA1UEAwwGbXlOYW1lMRkwFwYJKoZIhvcNAQkBFgpteW5hbWVAY29tMB4XDTE4\n" +
                "MDQyNDE1MjcyMloXDTIxMDIxMTE1MjcyMlowfDELMAkGA1UEBhMCQkUxETAPBgNVBAgMCEJydXNz\n" +
                "ZWxzMREwDwYDVQQHDAhCcnVzc2VsczEOMAwGA1UECgwFRElHSVQxCzAJBgNVBAsMAkQzMQ8wDQYD\n" +
                "VQQDDAZteU5hbWUxGTAXBgkqhkiG9w0BCQEWCm15bmFtZUBjb20wggIiMA0GCSqGSIb3DQEBAQUA\n" +
                "A4ICDwAwggIKAoICAQDRtZuVxn0MqE4IwnBHImObyYAqY0/jv1J7wJm11QheaeRQmyJdXgaLQ4Hf\n" +
                "h1Z/30alMd0Vsx7SYHXCxfACOSwpKQ2xEDmCjcOfX4Q4C9lqPc1DZwOJzm/76H13IO4bXE4N+rk9\n" +
                "d9LdUyhPK+S5OUGOl7GjIeNBB7M6nona/Ww4xH8vMf0LbrnMcsk0IRHPWkYa0XHg0qfPhMXil9PD\n" +
                "IlIuMz/JJtGSZDak+p4Bs+XA1IOxRgk6dAtyMQpx6mEPdz/GwXBL3bBJiB8Mr8Tk3DirXYusqPXY\n" +
                "pqt9RKstb1LXUR0yjtg8sLPBakMuMLMv6bIXanMFPDakdvd44OZleOuY6uOc//21L7hOMZE+0HO3\n" +
                "ZTLvzjN2//oqP61otzqX5JDLgxpE2CEzPgjz13ZerDos8TVhg1rMRQAE6rTZed6+ZeYMkKeNV90a\n" +
                "t0FIZiuR5a6m1F3oC7yJdqLGTr9Sf+s/mAS7CgzIJNbHivAzHVZi2Ub3H4/k0vd82SSPIvGMnv1z\n" +
                "2VwONTQ6XJtY3O5HNWXtvdiHvNHbu+kfjFArV8dkJ6t+G30WrA8C6t12JO4mNEZVvF0Jw0wHwBYC\n" +
                "EuO7xxoIpu80BzURNyK7Ncy/gDF1sOVRwxrm3Jix7dyODC94G/ZQBi9jFkXviLaG9h847y8/Ty01\n" +
                "jGlNnHONyyiXRZ3kYQIDAQABo1AwTjAdBgNVHQ4EFgQUHRKdSSGp0YgySPfWktrRTI+DIHQwHwYD\n" +
                "VR0jBBgwFoAUHRKdSSGp0YgySPfWktrRTI+DIHQwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQsF\n" +
                "AAOCAgEAK0bUZ6wnt4z/Pnm9CA3TpMcK/44NzeFDjCn7gzVJgMgdShv10/yXvGPBgTwfp5u8AaOc\n" +
                "F3FrgYHFO3B33FS9I53iIBQXmc10Czm7ktFt4JzOn2DXKh3dawbecQbAwk/H6jFnQZZYd7ktlKTC\n" +
                "cI6pput/n0rgEMScD5AU3/CwlwOekuY5GDrhZaqcifHPMj4IGawj0r/12bhpbQ5rxAA5EP1rUfe7\n" +
                "dpX5jzd0PmwK4ftl0FlSzOPjvTN31JifVixWTun6PvFViqhNHkMC9pCkqqrQD5J7TnCeg76n8iuz\n" +
                "AHbBSOleXNSUcPNFFPbdSva8kdM9z56j5GflrGCSb0+zLzdZGeZR1hTZVcGZpSMC6tZ4rN7OSrxG\n" +
                "hmVFyEBb+MpzTqLEqnvwYOeVryVosNjyGfZqHfCbAo0nbusew3zeG3VXHQ0vilGgjkW+mWu+AJHO\n" +
                "My1hMxpKLEnMdSgRgkDviXsv3a9iFjS8x6iHM1ZH60g9/EU81A5fc3pvr2wOo6QdKQHSHhTwLNO7\n" +
                "OthwFvrPLWHcGztTyke5Dzg6GqBbdp7adxDwUxTfwFALipLTr4EALsl6XbRL0/lkBE3lF43Ch0sL\n" +
                "mx6NIHV2R35fjcOsIQh+dAKVnI+tCc7PFhPvmFk8fzJtEsAOJyyzZPCzeHiVwlA2WAg9kGLNX4Vv\n" +
                "Qf8aAIw=";

        List<X509Certificate> x509Certificates = entitiesDescriptor.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates();
        for (X509Certificate x509Certificate : x509Certificates) {
            final String value = x509Certificate.getValue();
            if (metadataExtraCertificate.contentEquals(value)) {
                return true;
            }
        }

        return false;

    }

    /**
     * Auxiliar method which asserts that the number of certificates in the metadata matches the one of {@code expectedNumberCertificates}.
     *
     * @param entitiesDescriptor that contains the metadata
     * @param expectedNumberCertificates the number of certificates that are expected to be in the metadata
     */
    private void assertNumberCertificates(EntitiesDescriptor entitiesDescriptor, int expectedNumberCertificates) {
        int numberCertificatesPublished = entitiesDescriptor.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().size();
        Assert.assertEquals(expectedNumberCertificates, numberCertificatesPublished);
    }
}