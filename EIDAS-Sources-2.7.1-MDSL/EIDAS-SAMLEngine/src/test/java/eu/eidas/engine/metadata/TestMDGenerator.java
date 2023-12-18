/*
 * Copyright (c) 2022 by European Commission
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

import com.google.common.collect.Sets;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.ContactData;
import eu.eidas.auth.engine.metadata.EidasMetadata;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.MetadataConfiguration;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.metadata.OrganizationData;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * testing MDGenerator
 */
@FixMethodOrder(MethodSorters.JVM)
public class TestMDGenerator {

    private static final String TEST_KEYSTORE_LOCATION = "src/test/resources/keyStoreCountry1.p12";

    private static final String TEST_KEYSTORE_PASSWORD = "local-demo";

    private static final String TEST_KEYSTORE_SERIALNUMBER = "655D5376989F6C848C82674765019C74254F8762";

    private static final String TEST_KEYSTORE_ISSUER =
            "CN=local-demo-cert, OU=DIGIT, O=European Comission, L=Brussels, ST=Belgium, C=BE";

    private static final String TEST_COUNTRY_NAME = "Belgium";

    private static ProtocolEngineI engine = null;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setUpClass() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        engine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATATEST");
    }

    @AfterClass
    public static void tearDownClass() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Before
    public void setUp() {
            OpenSamlHelper.initialize();
    }

    @After
    public void removeDir() {
    }

    @Test
    public void testCreateMetadata() {
        try {
            EidasMetadataParametersI mcp = createTestEidasMetadataParametersI();
            EidasMetadata.Generator generator = EidasMetadata.generator(mcp);
            EidasMetadata metadata = generator.generate((MetadataSignerI) engine.getSigner());
            assertTrue(metadata != null && !metadata.getMetadata().isEmpty());
        } catch (Exception exc) {
            fail("exception caught :" + exc);
        }
    }

    @Test
    public void testCreateMetadata_InvalidNodeCountry() throws Throwable {
        String nodeCountryVal = "InvalidNodeCountry";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid node country [" + nodeCountryVal + "] value");
        try {
            EidasMetadataParametersI mcp = createTestEidasMetadataParametersI();
            mcp.setNodeCountry(nodeCountryVal);
            EidasMetadata.Generator generator = EidasMetadata.generator(mcp);
            EidasMetadata metadata = generator.generate((MetadataSignerI) engine.getSigner());
        } catch (Exception exc) {
            Assert.assertTrue(exc instanceof IllegalStateException);
            throw exc.getCause();
        }
    }

    /**
     * Generate metadata with multiple values for the application identifiers attribute
     *
     * Must succeed
     */
    @Test
    public void testMetadataGenerationWithMultipleApplicationIdentifiers() throws Throwable {
        String applicationIdentifiers = "CEF:eIDAS-ref:2.5;BE:extension-0.5";
        try {
            EidasMetadataParametersI mcp = createTestEidasMetadataParametersI();
            mcp.setEidasApplicationIdentifier(applicationIdentifiers);
            EidasMetadata.Generator generator = EidasMetadata.generator(mcp);
            EidasMetadata metadata = generator.generate((MetadataSignerI) engine.getSigner());

            EntityDescriptor metadataDescriptor = (EntityDescriptor) OpenSamlHelper.unmarshall(metadata.getMetadata());
            EidasMetadataParametersI actualMetadata = MetadataUtil.convertEntityDescriptor(metadataDescriptor);

            List<String> expectedApplicationIdentifiers = Arrays.asList("CEF:eIDAS-ref:2.5", "BE:extension-0.5");
            List<String> actualApplicationIdentifiers = actualMetadata.getEidasApplicationIdentifiers();
            assertArrayEquals(expectedApplicationIdentifiers.toArray(), actualApplicationIdentifiers.toArray());

            String expectedApplicationIdentifier = "CEF:eIDAS-ref:2.5";
            String actualApplicationIdentifier = actualMetadata.getEidasApplicationIdentifier();
            assertEquals(expectedApplicationIdentifier, actualApplicationIdentifier);
        } catch (Exception exc) {
            Assert.fail("Should not fail while generating metadata!");
            throw exc.getCause();
        }
    }

    /**
     * Generate metadata with multiple values for the eidas protocol versions attribute
     *
     * Must succeed
     */
    @Test
    public void testMetadataGenerationWithMultipleEIDASProtocolVersions() throws Throwable {
        String protocolVersions = "1.2;1.1;1.0";
        try {
            EidasMetadataParametersI mcp = createTestEidasMetadataParametersI();
            mcp.setEidasProtocolVersion(protocolVersions);
            EidasMetadata.Generator generator = EidasMetadata.generator(mcp);
            EidasMetadata metadata = generator.generate((MetadataSignerI) engine.getSigner());

            EntityDescriptor metadataDescriptor = (EntityDescriptor) OpenSamlHelper.unmarshall(metadata.getMetadata());
            EidasMetadataParametersI actualMetadata = MetadataUtil.convertEntityDescriptor(metadataDescriptor);

            List<String> expectedProtocolVersions = Arrays.asList("1.2","1.1","1.0");
            List<String> actualProtocolVersions = actualMetadata.getEidasProtocolVersions();
            assertArrayEquals(expectedProtocolVersions.toArray(), actualProtocolVersions.toArray());

            String expectedProtocolVersion = "1.2";
            String actualProtocolVersion = actualMetadata.getEidasProtocolVersion();
            assertEquals(expectedProtocolVersion, actualProtocolVersion);
        } catch (Exception exc) {
            Assert.fail("Should not fail while generating metadata!");
            throw exc.getCause();
        }
    }

    private EidasMetadataParametersI createTestEidasMetadataParametersI() {
        EidasMetadataParametersI mcp = MetadataConfiguration.newParametersInstance();
        EidasMetadataRoleParametersI mcrp = MetadataConfiguration.newRoleParametersInstance();
        mcp.setEntityID("entityID");
        mcrp.setDefaultBinding(BindingMethod.POST.toString());
        mcrp.addProtocolBindingLocation(BindingMethod.POST.toString(), "http://localhost");
        mcrp.setAuthnRequestsSigned(true);
        mcrp.setWantAssertionsSigned(true);
        mcrp.setRole(MetadataRole.SP);
        mcp.setSpType("public");
        mcp.setNodeCountry("EU");
        mcp.setAssuranceLevel("http://eidas.europa.eu/LoA");
        mcp.setDigestMethods("http://www.w3.org/2001/04/xmlenc#sha256");
        mcp.setSigningMethods("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256");
        mcrp.setSigningCertificate(getTestCertificate());
        mcrp.setEncryptionCertificate(getTestCertificate());
        mcp.setTechnicalContact(ContactData.builder().build());
        mcp.setSupportContact(ContactData.builder().build());
        mcp.setOrganization(OrganizationData.builder().name(TEST_COUNTRY_NAME).build());
        mcp.setRoleDescriptors(Sets.newHashSet(mcrp));
        return mcp;
    }

    private KeyStore loadTestKeystore() {
        KeyStore testKeyStore = null;
        FileInputStream fis = null;
        try {
            testKeyStore = KeyStore.getInstance("PKCS12");
            fis = new FileInputStream(TEST_KEYSTORE_LOCATION);

            testKeyStore.load(fis, TEST_KEYSTORE_PASSWORD.toCharArray());
        } catch (KeyStoreException kse) {
            fail("KeystoreException: " + kse);
        } catch (FileNotFoundException fnfe) {
            fail("FileNotFoundException: " + fnfe);
        } catch (NoSuchAlgorithmException nsae) {
            fail("NoSuchAlgorithmException: " + nsae);
        } catch (CertificateException ce) {
            fail("CertificateException: " + ce);
        } catch (IOException ioe) {
            fail("IOException: " + ioe);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ioe) {
                fail("IOException closing FileInputStream: " + ioe);
            }
        }
        return testKeyStore;
    }

    private X509Certificate getTestCertificate() {
        try {
            final String serialNumber = TEST_KEYSTORE_SERIALNUMBER;
            final String issuer = TEST_KEYSTORE_ISSUER;

            String alias = null;
            String aliasCert;
            X509Certificate certificate;
            boolean find = false;
            KeyStore testKeyStore = loadTestKeystore();
            for (final Enumeration<String> e = testKeyStore.aliases(); e.hasMoreElements() && !find; ) {
                aliasCert = e.nextElement();
                certificate = (X509Certificate) testKeyStore.getCertificate(aliasCert);

                final String serialNum = certificate.getSerialNumber().toString(16);

                Principal p = certificate.getIssuerDN();
                String name = p.getName();

                X500Name issuerDN = new X500Name(name);
                X500Name issuerDNConf = new X500Name(issuer);

                if (serialNum.equalsIgnoreCase(serialNumber) && X500PrincipalUtil.principalNotNullEquals(issuerDN,
                        issuerDNConf)) {
                    alias = aliasCert;
                    find = true;
                }
            }
            if (!find) {
                fail("Certificate cannot be found in keystore ");
            }
            certificate = (X509Certificate) testKeyStore.getCertificate(alias);

            return certificate;

        } catch (KeyStoreException e) {
            fail("Generic KeyStore exception:" + e);
        }
        return null;
    }

}
