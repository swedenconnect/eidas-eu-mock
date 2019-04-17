package eu.eidas.engine.metadata;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import eu.eidas.auth.engine.metadata.*;
import org.bouncycastle.asn1.x500.X500Name;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opensaml.Configuration;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.SecurityConfiguration;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorManager;
import org.opensaml.xml.security.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.X500PrincipalUtil;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.auth.engine.xml.opensaml.SAMLBootstrap;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * testing MDGenerator
 */
@FixMethodOrder(MethodSorters.JVM)
public class TestMDGenerator {

    private static final String TEST_KEYSTORE_LOCATION = "src/test/resources/keyStoreCountry1.jks";

    private static final String TEST_KEYSTORE_PASSWORD = "local-demo";

    private static final String TEST_KEYSTORE_SERIALNUMBER = "54D8A000";

    private static final String TEST_KEYSTORE_ISSUER =
            "CN=local-demo-cert, OU=DIGIT, O=European Comission, L=Brussels, ST=Belgium, C=BE";

    private static final String TEST_SIGNATURE_ALGORITHM = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";

    private static final String TEST_COUNTRY_NAME = "Belgium";

    @Before
    public void setUp() {
        try {
            SAMLBootstrap.bootstrap();
        } catch (ConfigurationException ce) {
            fail("SAML bootstrap error: " + ce);
        }
    }

    @After
    public void removeDir() {
    }

    @Test
    public void testCreateMetadata() {
        try {
            EidasMetadata.Generator generator = EidasMetadata.generator();
            MetadataConfigParams.Builder mcp = MetadataConfigParams.builder();
            mcp.entityID("entityID");
            mcp.assertionConsumerUrl("http://localhost");
            mcp.authnRequestsSigned(true);
            mcp.wantAssertionsSigned(true);
            Signature spSignature = createSampleSignature();
            mcp.spSignature(spSignature);
            mcp.spEncryptionCredential(createTestCredential());
            mcp.spSigningCredential(createTestCredential());
            mcp.technicalContact(ContactData.builder().build());
            mcp.supportContact(ContactData.builder().build());
            mcp.organization(OrganizationData.builder().name(TEST_COUNTRY_NAME).build());
            generator.configParams(mcp.build());
            String metadata = generator.build().getMetadata();
            assertTrue(metadata != null && !metadata.isEmpty());
        } catch (Exception exc) {
            assertTrue("exception caught :" + exc, false);
        }
    }

    private static ProtocolEngineI engine = null;

    static {
        engine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATATEST");
    }

    @Test
    public void testCreateMetadataWithSamlEngine() {
        try {
            EidasMetadata.Generator generator = EidasMetadata.generator();
            MetadataConfigParams.Builder mcp = MetadataConfigParams.builder();
            mcp.spEngine(engine);
            mcp.entityID("entityID");
            mcp.assertionConsumerUrl("http://localhost");
            mcp.authnRequestsSigned(true);
            mcp.wantAssertionsSigned(true);
            mcp.assuranceLevel("http://eidas.europa.eu/LoA");
            mcp.spType("public");
            mcp.digestMethods("http://www.w3.org/2001/04/xmlenc#sha256");
            mcp.signingMethods("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256");
            mcp.technicalContact(ContactData.builder().build());
            mcp.supportContact(ContactData.builder().build());
            mcp.organization(OrganizationData.builder().build());
            generator.configParams(mcp.build());
            String metadata = generator.build().getMetadata();
            assertTrue(metadata != null && !metadata.isEmpty());

            //unmmarshal
            EntityDescriptor ed = (EntityDescriptor) OpenSamlHelper.unmarshall(EidasStringUtil.getBytes(metadata));
            assertNotNull(ed);
            checkSignature(ed);
            checkSPSSO(ed);
        } catch (Exception exc) {
            exc.printStackTrace();
            fail("exception caught :" + exc);
        }
    }

    private void checkSignature(EntityDescriptor ed) throws ValidationException, EIDASSAMLEngineException {
        SAMLSignatureProfileValidator sigProfValidator = new SAMLSignatureProfileValidator();
        sigProfValidator.validate(ed.getSignature());
        //check that EntityDescriptor matches the signature
        KeyInfo keyInfo = ed.getSignature().getKeyInfo();
        SignatureValidator sigValidator = new SignatureValidator(CertificateUtil.toCredential(keyInfo));
        sigValidator.validate(ed.getSignature());
    }

    private void checkSPSSO(EntityDescriptor ed) throws ValidationException, EIDASSAMLEngineException {
        assertTrue(ed.getRoleDescriptors().size() == 1);
        SPSSODescriptor spSSO = (SPSSODescriptor) ed.getRoleDescriptors().get(0);
        assertNotNull(spSSO);
        org.opensaml.xml.signature.X509Certificate xmlCert =
                spSSO.getKeyDescriptors().get(0).getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0);
        assertNotNull(xmlCert);
        //check that the signature conforms to saml2
        if (spSSO.getSignature() != null) {
            SAMLSignatureProfileValidator sigProfValidator = new SAMLSignatureProfileValidator();
            sigProfValidator.validate(spSSO.getSignature());
            //check that spSSO matches the signature
            SignatureValidator sigValidator =
//                new SignatureValidator(CertificateUtil.toCredential(spSSO.getKeyDescriptors().get(0).getKeyInfo()));
                    new SignatureValidator(((MetadataSignerI) engine.getSigner()).getPublicMetadataSigningCredential());
            sigValidator.validate(spSSO.getSignature());
        }
    }

    Signature createSampleSignature() {
        Signature signature = null;
        try {
            Credential credential = createTestCredential();
            signature = (Signature) Configuration.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                    .buildObject(Signature.DEFAULT_ELEMENT_NAME);

            signature.setSigningCredential(credential);

            signature.setSignatureAlgorithm(TEST_SIGNATURE_ALGORITHM);

            final SecurityConfiguration secConfiguration = Configuration.getGlobalSecurityConfiguration();
            final NamedKeyInfoGeneratorManager keyInfoManager = secConfiguration.getKeyInfoGeneratorManager();
            final KeyInfoGeneratorManager keyInfoGenManager = keyInfoManager.getDefaultManager();
            final KeyInfoGeneratorFactory keyInfoGenFac = keyInfoGenManager.getFactory(credential);
            final KeyInfoGenerator keyInfoGenerator = keyInfoGenFac.newInstance();

            KeyInfo keyInfo = keyInfoGenerator.generate(credential);

            signature.setKeyInfo(keyInfo);
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        } catch (org.opensaml.xml.security.SecurityException e) {
            fail("Security exception:" + e);
        }
        return signature;
    }

    private KeyStore loadTestKeystore() {
        KeyStore testKeyStore = null;
        FileInputStream fis = null;
        try {
            testKeyStore = KeyStore.getInstance("JKS");
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

    private Credential createTestCredential() {
        Credential credential = null;
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

            final PrivateKey privateKey = (PrivateKey) testKeyStore.getKey(alias, TEST_KEYSTORE_PASSWORD.toCharArray());
            credential = CertificateUtil.createCredential(certificate, privateKey);
        } catch (NoSuchAlgorithmException e) {
            fail("A 'xmldsig#rsa-sha1' cryptographic algorithm is requested but is not available in the environment: "
                         + e);
        } catch (KeyStoreException e) {
            fail("Generic KeyStore exception:" + e);
        } catch (UnrecoverableKeyException e) {
            fail("UnrecoverableKey exception:" + e);
        }
        return credential;
    }
}
