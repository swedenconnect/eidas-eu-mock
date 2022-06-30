package eu.eidas.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.encryption.exception.DecryptionException;
import eu.eidas.encryption.exception.EncryptionException;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.encryption.exception.UnmarshallException;
import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Created by bodabel on 28/11/2014.
 */
public class TestEIDASResponseAssertionEncryption {

    private static final Logger log = LoggerFactory.getLogger(TestEIDASResponseAssertionEncryption.class.getName());

    @BeforeClass
    /**
     * Init EidasAuthenticationRequest details
     */ public static void initTestClass() {

    }

    @Before
    public void initTest() throws ConfigurationException {
        EncryptionTestUtils.initXMLTooling();
    }

    @Test
    public void testEIDASAuthResponseAssertionEncryptDecrypt()
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException,
                   UnrecoverableKeyException, EncryptionException, DecryptionException, UnmarshallException,
                   MarshallException, InvalidKeySpecException, InvalidKeyException, SignatureException {

        log.info("CLASSPATH: " + System.getProperty("java.class.path"));

        byte[] authResponse = EidasStringUtil.getBytes(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><saml2p:Response xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:stork=\"urn:eu:stork:names:tc:STORK:1.0:assertion\" xmlns:storkp=\"urn:eu:stork:names:tc:STORK:1.0:protocol\" Consent=\"urn:oasis:names:tc:SAML:2.0:consent:obtained\" Destination=\"http://S-PEPS.gov.xx/PEPS/ColleagueResponse\" ID=\"_00df85e9844c610c54865188b99613d7\" InResponseTo=\"THE-SAML-ID\" IssueInstant=\"2014-12-10T16:46:25.872Z\" Version=\"2.0\"><saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">http://C-PEPS.gov.xx</saml2:Issuer><saml2p:Status><saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/><saml2p:StatusMessage>urn:oasis:names:tc:SAML:2.0:status:Success</saml2p:StatusMessage></saml2p:Status><saml2:Assertion ID=\"_50bc3d3dd6cacc7009c663d3cee18bf5\" IssueInstant=\"2014-12-10T16:46:25.875Z\" Version=\"2.0\"><saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">http://C-PEPS.gov.xx</saml2:Issuer><saml2:Subject><saml2:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\" NameQualifier=\"http://C-PEPS.gov.xx\">urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</saml2:NameID><saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\"><saml2:SubjectConfirmationData Address=\"111.222.333.444\" InResponseTo=\"THE-SAML-ID\" NotOnOrAfter=\"2014-12-10T16:51:25.873Z\" Recipient=\"http://S-PEPS.gov.xx/PEPS/ColleagueResponse\"/></saml2:SubjectConfirmation></saml2:Subject><saml2:Conditions NotBefore=\"2014-12-10T16:46:25.879Z\" NotOnOrAfter=\"2014-12-10T16:51:25.873Z\"><saml2:AudienceRestriction><saml2:Audience>ISSUER</saml2:Audience></saml2:AudienceRestriction><saml2:OneTimeUse/></saml2:Conditions><saml2:AuthnStatement AuthnInstant=\"2014-12-10T16:46:25.884Z\"><saml2:SubjectLocality Address=\"111.222.333.444\"/><saml2:AuthnContext><saml2:AuthnContextDecl/></saml2:AuthnContext></saml2:AuthnStatement><saml2:AttributeStatement><saml2:Attribute Name=\"http://www.stork.gov.eu/1.0/eIdentifier\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\"/><saml2:Attribute Name=\"http://www.stork.gov.eu/1.0/givenName\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\" stork:AttributeStatus=\"Available\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:anyType\">xavi-from-IdP</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"http://www.stork.gov.eu/1.0/dateOfBirth\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\"/><saml2:Attribute Name=\"http://www.stork.gov.eu/1.0/isAgeOver\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:anyType\">16</saml2:AttributeValue><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:anyType\">18</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion></saml2p:Response>\r\n");

        log.info("Initial SAML Response: " + EidasStringUtil.toString(authResponse));

        //ENCRYPTION
        BasicX509Credential credential = new BasicX509Credential();

        //KEY PAIR GENERATION
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        log.info("RSA(2048) Key Pair generated for the test.");
        KeyPair encKeyPair = keyPairGenerator.generateKeyPair();
        PublicKey pubKey = encKeyPair.getPublic();
        PrivateKey privKey = encKeyPair.getPrivate();
//        credential.setPublicKey(encKeyPair.getPublic());
//        credential.setPrivateKey(encKeyPair.getPrivate());

        //CERTIFICATE
        SecureRandom sr = new SecureRandom();
        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.DAY_OF_YEAR, 1);

        X509Name x509Name = new X509Name("CN=STORKEncTest");

        V3TBSCertificateGenerator certGen = new V3TBSCertificateGenerator();
        certGen.setSerialNumber(new DERInteger(BigInteger.valueOf(System.currentTimeMillis())));
        //???
        certGen.setIssuer(x509Name/*PrincipalUtil.getSubjectX509Principal(caCert)*/);
        //???
        certGen.setSubject(x509Name);
        ASN1ObjectIdentifier sigOID =
                PKCSObjectIdentifiers.sha1WithRSAEncryption;//X509Util.getAlgorithmOID("SHA1WithRSAEncryption");
        AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(sigOID, new DERNull());
        certGen.setSignature(sigAlgId);
        certGen.setSubjectPublicKeyInfo(new SubjectPublicKeyInfo(
                (ASN1Sequence) new ASN1InputStream(new ByteArrayInputStream(pubKey.getEncoded())).readObject()));
        certGen.setStartDate(new Time(new Date(System.currentTimeMillis())));
        certGen.setEndDate(new Time(expiry.getTime()));
        TBSCertificate tbsCert = certGen.generateTBSCertificate();

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);
        dOut.writeObject(tbsCert);

        // and now sign
        byte[] signature;
        // or the JCE way
        RSAPrivateCrtKey rsaPrivKey = (RSAPrivateCrtKey) privKey;
        RSAPrivateCrtKeyParameters caPrivateKey =
                new RSAPrivateCrtKeyParameters(rsaPrivKey.getModulus(), rsaPrivKey.getPublicExponent(),
                                               rsaPrivKey.getPrivateExponent(), rsaPrivKey.getPrimeP(),
                                               rsaPrivKey.getPrimeQ(), rsaPrivKey.getPrimeExponentP(),
                                               rsaPrivKey.getPrimeExponentQ(), rsaPrivKey.getCrtCoefficient());

        PrivateKey caPrivKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new RSAPrivateCrtKeySpec(caPrivateKey.getModulus(), caPrivateKey.getPublicExponent(),
                                                          caPrivateKey.getExponent(), caPrivateKey.getP(),
                                                          caPrivateKey.getQ(), caPrivateKey.getDP(),
                                                          caPrivateKey.getDQ(), caPrivateKey.getQInv()));

        Signature sig = Signature.getInstance(sigOID.getId());
        sig.initSign(caPrivKey, sr);
        sig.update(bOut.toByteArray());
        signature = sig.sign();
        log.debug("SHA1/RSA signature of digest is '" + String.valueOf(Hex.encodeHex(signature)) + "'");

        // and finally construct the certificate structure
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(tbsCert);
        v.add(sigAlgId);
        v.add(new DERBitString(signature));

        X509CertificateObject clientCert = new X509CertificateObject(Certificate.getInstance(new DERSequence(v)));

        credential.setEntityCertificate(clientCert);
        credential.setPrivateKey(privKey);

        Response encryptedAuthnResponse;
        SAMLAuthnResponseEncrypter enc = SAMLAuthnResponseEncrypter.builder().build();
        Response responseSource = (Response) OpenSamlHelper.unmarshall(authResponse);
        encryptedAuthnResponse = enc.encryptSAMLResponse(responseSource, credential);

        //check the encrypted response is not the same as the source response, but they have the same ID
        //(the encrypted response is created by cloning the source response)
        assertEquals(responseSource.getID(), encryptedAuthnResponse.getID());
        assertNotSame(responseSource, encryptedAuthnResponse);
        assertNotSame(authResponse, encryptedAuthnResponse);

//         Singature not tested here because of project cycling
//         Singature will be tested in SAMLEngine intergation tests!
//         TODO Bootstrap a SAMLEngine independent signature to test!
//        //SIGNTURE

        //DECRYPTION
        SAMLAuthnResponseDecrypter dec = new SAMLAuthnResponseDecrypter("BC");

        Response decryptedSamlResponse = dec.decryptSAMLResponse(encryptedAuthnResponse, credential);
        byte[] decryptedSamlResponseMarashalled = OpenSamlHelper.marshall(decryptedSamlResponse);
        log.info("Verified and Decrypted SAML Response: " + EidasStringUtil.toString(decryptedSamlResponseMarashalled));

        DetailedDiff diffs = new DetailedDiff(
                XMLUnit.compareXML(OpenSamlHelper.unmarshall(authResponse).getDOM().getOwnerDocument(),
                                   OpenSamlHelper.unmarshall(decryptedSamlResponseMarashalled)
                                           .getDOM()
                                           .getOwnerDocument()));

        Assert.assertEquals("XML Differences: " + diffs.toString(), 0, diffs.getAllDifferences().size());

    }

    @Test
    public void testEIDASAuthResponseAssertionEncryptDecryptWithLogHelper()
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException,
                   UnrecoverableKeyException, EncryptionException, DecryptionException, UnmarshallException,
                   MarshallException, InvalidKeySpecException, InvalidKeyException, SignatureException {

        log.info("CLASSPATH: " + System.getProperty("java.class.path"));

        byte[] authResponse = EidasStringUtil.getBytes(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><saml2p:Response xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:stork=\"urn:eu:stork:names:tc:STORK:1.0:assertion\" xmlns:storkp=\"urn:eu:stork:names:tc:STORK:1.0:protocol\" Consent=\"urn:oasis:names:tc:SAML:2.0:consent:obtained\" Destination=\"http://S-PEPS.gov.xx/PEPS/ColleagueResponse\" ID=\"_00df85e9844c610c54865188b99613d7\" InResponseTo=\"THE-SAML-ID\" IssueInstant=\"2014-12-10T16:46:25.872Z\" Version=\"2.0\"><saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">http://C-PEPS.gov.xx</saml2:Issuer><saml2p:Status><saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/><saml2p:StatusMessage>urn:oasis:names:tc:SAML:2.0:status:Success</saml2p:StatusMessage></saml2p:Status><saml2:Assertion ID=\"_50bc3d3dd6cacc7009c663d3cee18bf5\" IssueInstant=\"2014-12-10T16:46:25.875Z\" Version=\"2.0\"><saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">http://C-PEPS.gov.xx</saml2:Issuer><saml2:Subject><saml2:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\" NameQualifier=\"http://C-PEPS.gov.xx\">urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</saml2:NameID><saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\"><saml2:SubjectConfirmationData Address=\"111.222.333.444\" InResponseTo=\"THE-SAML-ID\" NotOnOrAfter=\"2014-12-10T16:51:25.873Z\" Recipient=\"http://S-PEPS.gov.xx/PEPS/ColleagueResponse\"/></saml2:SubjectConfirmation></saml2:Subject><saml2:Conditions NotBefore=\"2014-12-10T16:46:25.879Z\" NotOnOrAfter=\"2014-12-10T16:51:25.873Z\"><saml2:AudienceRestriction><saml2:Audience>ISSUER</saml2:Audience></saml2:AudienceRestriction><saml2:OneTimeUse/></saml2:Conditions><saml2:AuthnStatement AuthnInstant=\"2014-12-10T16:46:25.884Z\"><saml2:SubjectLocality Address=\"111.222.333.444\"/><saml2:AuthnContext><saml2:AuthnContextDecl/></saml2:AuthnContext></saml2:AuthnStatement><saml2:AttributeStatement><saml2:Attribute Name=\"http://www.stork.gov.eu/1.0/eIdentifier\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\"/><saml2:Attribute Name=\"http://www.stork.gov.eu/1.0/givenName\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\" stork:AttributeStatus=\"Available\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:anyType\">xavi-from-IdP</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"http://www.stork.gov.eu/1.0/dateOfBirth\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\"/><saml2:Attribute Name=\"http://www.stork.gov.eu/1.0/isAgeOver\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:anyType\">16</saml2:AttributeValue><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:anyType\">18</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion></saml2p:Response>\r\n");

        //   log.info("Initial SAML Response: " + EidasStringUtil.toString(authResponse));

        //ENCRYPTION
        BasicX509Credential credential = new BasicX509Credential();

        //KEY PAIR GENERATION
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        log.info("RSA(2048) Key Pair generated for the test.");
        KeyPair encKeyPair = keyPairGenerator.generateKeyPair();
        PublicKey pubKey = encKeyPair.getPublic();
        PrivateKey privKey = encKeyPair.getPrivate();
//        credential.setPublicKey(encKeyPair.getPublic());
//        credential.setPrivateKey(encKeyPair.getPrivate());

        //CERTIFICATE
        SecureRandom sr = new SecureRandom();
        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.DAY_OF_YEAR, 1);

        X509Name x509Name = new X509Name("CN=STORKEncTest");

        V3TBSCertificateGenerator certGen = new V3TBSCertificateGenerator();
        certGen.setSerialNumber(new DERInteger(BigInteger.valueOf(System.currentTimeMillis())));
        //???
        certGen.setIssuer(x509Name/*PrincipalUtil.getSubjectX509Principal(caCert)*/);
        //???
        certGen.setSubject(x509Name);
        ASN1ObjectIdentifier sigOID = PKCSObjectIdentifiers.sha1WithRSAEncryption;
        AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(sigOID, new DERNull());
        certGen.setSignature(sigAlgId);
        certGen.setSubjectPublicKeyInfo(new SubjectPublicKeyInfo(
                (ASN1Sequence) new ASN1InputStream(new ByteArrayInputStream(pubKey.getEncoded())).readObject()));
        certGen.setStartDate(new Time(new Date(System.currentTimeMillis())));
        certGen.setEndDate(new Time(expiry.getTime()));
        TBSCertificate tbsCert = certGen.generateTBSCertificate();

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);
        dOut.writeObject(tbsCert);

        // and now sign
        byte[] signature;
        // or the JCE way
        RSAPrivateCrtKey rsaPrivKey = (RSAPrivateCrtKey) privKey;
        RSAPrivateCrtKeyParameters caPrivateKey =
                new RSAPrivateCrtKeyParameters(rsaPrivKey.getModulus(), rsaPrivKey.getPublicExponent(),
                                               rsaPrivKey.getPrivateExponent(), rsaPrivKey.getPrimeP(),
                                               rsaPrivKey.getPrimeQ(), rsaPrivKey.getPrimeExponentP(),
                                               rsaPrivKey.getPrimeExponentQ(), rsaPrivKey.getCrtCoefficient());

        PrivateKey caPrivKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new RSAPrivateCrtKeySpec(caPrivateKey.getModulus(), caPrivateKey.getPublicExponent(),
                                                          caPrivateKey.getExponent(), caPrivateKey.getP(),
                                                          caPrivateKey.getQ(), caPrivateKey.getDP(),
                                                          caPrivateKey.getDQ(), caPrivateKey.getQInv()));

        Signature sig = Signature.getInstance(sigOID.getId());
        sig.initSign(caPrivKey, sr);
        sig.update(bOut.toByteArray());
        signature = sig.sign();
        log.debug("SHA1/RSA signature of digest is '" + String.valueOf(Hex.encodeHex(signature)) + "'");

        // and finally construct the certificate structure
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(tbsCert);
        v.add(sigAlgId);
        v.add(new DERBitString(signature));

        X509CertificateObject clientCert = new X509CertificateObject(Certificate.getInstance(new DERSequence(v)));

        credential.setEntityCertificate(clientCert);
        credential.setPrivateKey(privKey);

        Response encryptedAuthnResponse;
        SAMLAuthnResponseEncrypter enc = SAMLAuthnResponseEncrypter.builder().build();
        encryptedAuthnResponse =
                enc.encryptSAMLResponse((Response) OpenSamlHelper.unmarshall(authResponse), credential);

//         Singature not tested here because of project cycling
//         Singature will be tested in SAMLEngine intergation tests!
//         TODO Bootstrap a SAMLEngine independent signature to test!
//        //SIGNTURE

        //DECRYPTION
        SAMLAuthnResponseDecrypter dec = new SAMLAuthnResponseDecrypter("BC");

        Response decryptedSamlResponse = dec.decryptSAMLResponse(encryptedAuthnResponse, credential);
        byte[] decryptedSamlResponseMarashalled = OpenSamlHelper.marshall(decryptedSamlResponse);
        log.info("Verified and Decrypted SAML Response: " + EidasStringUtil.toString(decryptedSamlResponseMarashalled));

        DetailedDiff diffs = new DetailedDiff(
                XMLUnit.compareXML(OpenSamlHelper.unmarshall(authResponse).getDOM().getOwnerDocument(),
                                   OpenSamlHelper.unmarshall(decryptedSamlResponseMarashalled)
                                           .getDOM()
                                           .getOwnerDocument()));

        Assert.assertEquals("XML Differences: " + diffs.toString(), 0, diffs.getAllDifferences().size());
    }
}
