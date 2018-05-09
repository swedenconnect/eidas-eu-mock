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

package eu.eidas.engine.metadata;

import com.google.common.collect.Sets;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.X500PrincipalUtil;
import eu.eidas.auth.engine.metadata.*;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import org.bouncycastle.asn1.x500.X500Name;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

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
            OpenSamlHelper.initialize();
    }

    @After
    public void removeDir() {
    }

    @Test
    public void testCreateMetadata() {
        try {
            EidasMetadataParametersI mcp = MetadataConfiguration.newParametersInstance();
            EidasMetadataRoleParametersI mcrp = MetadataConfiguration.newRoleParametersInstance();
            mcp.setEntityID("entityID");
            mcrp.setDefaultBinding(BindingMethod.POST.toString());
            mcrp.addProtocolBindingLocation(BindingMethod.POST.toString(), "http://localhost");
            mcrp.setAuthnRequestsSigned(true);
            mcrp.setWantAssertionsSigned(true);
            mcrp.setRole(MetadataRole.SP);
            mcp.setSpType("public");
            mcp.setAssuranceLevel("http://eidas.europa.eu/LoA");
            mcp.setDigestMethods("http://www.w3.org/2001/04/xmlenc#sha256");
            mcp.setSigningMethods("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256");
            mcrp.setSigningCertificate(getTestCertificate());
            mcrp.setEncryptionCertificate(getTestCertificate());
            mcp.setTechnicalContact(ContactData.builder().build());
            mcp.setSupportContact(ContactData.builder().build());
            mcp.setOrganization(OrganizationData.builder().name(TEST_COUNTRY_NAME).build());
            mcp.setRoleDescriptors(Sets.newHashSet(mcrp ));
            EidasMetadata.Generator generator = EidasMetadata.generator(mcp);
            EidasMetadata metadata = generator.generate((MetadataSignerI) engine.getSigner());
            assertTrue(metadata != null && !metadata.getMetadata().isEmpty());
        } catch (Exception exc) {
            assertTrue("exception caught :" + exc, false);
        }
    }

    private static ProtocolEngineI engine = null;

    static {
        engine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATATEST");
    }
/*
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
*/

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
