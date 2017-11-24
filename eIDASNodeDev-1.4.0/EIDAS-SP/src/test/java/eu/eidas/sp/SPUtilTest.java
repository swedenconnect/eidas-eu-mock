package eu.eidas.sp;

import org.junit.Test;

import static org.junit.Assert.*;

public class SPUtilTest {

    @Test
    public void testExtractAssertion() throws Exception {
        String message="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<saml2p:Response xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:eidas=\"http://eidas.europa.eu/attributes/naturalperson\" xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:stork=\"urn:eu:stork:names:tc:STORK:1.0:assertion\" xmlns:storkp=\"urn:eu:stork:names:tc:STORK:1.0:protocol\" Consent=\"urn:oasis:names:tc:SAML:2.0:consent:obtained\" Destination=\"http://SPSERVER:8888/SP/ReturnPage\" ID=\"_eedd0bcb2f2f9c80b898ac4bbc726ece\" InResponseTo=\"_a6bf27e8d7a4b2684b952a9651b709b5\" IssueInstant=\"2015-10-09T13:49:25.000Z\" Version=\"2.0\">\n" +
                "\t<saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">http://d02di1601313dit.net1.cec.eu.int:17001/EidasNode/ConnectorResponderMetadata</saml2:Issuer>\n" +
                "\t<ds:Signature xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n" +
                "\t\t<ds:SignedInfo>\n" +
                "\t\t\t<ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>\n" +
                "\t\t\t<ds:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha512\"/>\n" +
                "\t\t\t<ds:Reference URI=\"#_eedd0bcb2f2f9c80b898ac4bbc726ece\">\n" +
                "\t\t\t\t<ds:Transforms>\n" +
                "\t\t\t\t\t<ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>\n" +
                "\t\t\t\t\t<ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\">\n" +
                "\t\t\t\t\t\t<ec:InclusiveNamespaces xmlns:ec=\"http://www.w3.org/2001/10/xml-exc-c14n#\" PrefixList=\"eidas\"/>\n" +
                "\t\t\t\t\t</ds:Transform>\n" +
                "\t\t\t\t</ds:Transforms>\n" +
                "\t\t\t\t<ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\n" +
                "\t\t\t\t<ds:DigestValue>GLOmTHCICU3YtPd8+45iko0nseCMPldxvvBN2xQRyseA+jqMPYgNt1nobQqcFDRMzBENpQ1CLOlEp12lu1f51g==</ds:DigestValue>\n" +
                "\t\t\t</ds:Reference>\n" +
                "\t\t</ds:SignedInfo>\n" +
                "\t\t<ds:SignatureValue>gP0TBMi0D8QbwTNoSToykqiS1RqURgNraMLMk7A3cVSzmelmtJUWQoDRrza3KWqvTYUl/cmgLblViyP2yDZysaq/vt6GEiagzlvfAKveemaRQDKmiqH3AuiabzsysYSv6Zo4Ja+DIcj/03s9XTZJ64eWj6nhAQE1ESeaXPeRovnbbwpt4XSHYMGNVvEDS/jAfcMGd9O3WBhvlq2EVsBFmbkdK/MOsAVEPKYs9/FwK6qzuEHnHr5EdSdRjR9ai24DaTK/I5GOgvqcrUh6mBGlx91S0dnQJyciDQ1bq+YrmHX/fR2GoSB6areJcHB0od0/hOImBb/8Ssecd/2MyQ9/zFB6nCePMM2qbM2Sy67ocxJv6ayv9eZf4TOKzbxNtQnzQa8NqFaKdCzmmGeIx7K2jSmaaeEviSw1wnzFXjW8Q3K/ziJr4FEICc6d9HS3KTNJVD0aih/+nY3N5hpixTMKpTc7jbabtWW9ejb6VPCRmTWKeHqNaW6Mib8cHPDitRvDnv3wXJy3UgdYlE8FhU9yQKhFsJzpYUPbxtlMoEhHcN1ORrBcuikOATzXQqT9wroOHc0VDCoXBp2UYgILocjAkKdlvu9V5BpWvTTPzuZINvGX7kOFsYXEW1BQDXQaLNM4R2QAlkT1FoE+USpVDP09Y1TQnjIjw+ejBUuR4Zaagoo=</ds:SignatureValue>\n" +
                "\t\t<ds:KeyInfo>\n" +
                "\t\t\t<ds:X509Data>\n" +
                "\t\t\t\t<ds:X509Certificate>MIIFTTCCAzUCBFTI/MUwDQYJKoZIhvcNAQENBQAwazELMAkGA1UEBhMCQ0IxCzAJBgNVBAgMAkVV\n" +
                "MQswCQYDVQQHDAJFVTEOMAwGA1UECgwFU1BFUFMxDjAMBgNVBAsMBVNUT1JLMSIwIAYDVQQDDBlz\n" +
                "cGVwcy1jYi1kZW1vLWNlcnRpZmljYXRlMB4XDTE1MDEyODE1MTQxM1oXDTE2MDEyODE1MTQxM1ow\n" +
                "azELMAkGA1UEBhMCQ0IxCzAJBgNVBAgMAkVVMQswCQYDVQQHDAJFVTEOMAwGA1UECgwFU1BFUFMx\n" +
                "DjAMBgNVBAsMBVNUT1JLMSIwIAYDVQQDDBlzcGVwcy1jYi1kZW1vLWNlcnRpZmljYXRlMIICIjAN\n" +
                "BgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAjdNPR0R0vXpzXnwglsy9hmpmoub/PhUkGEKBMGOe\n" +
                "oXBKGdQBWeYly3MHgKkbmgIAcqeIdwjN7oOB/sv7E3Wk6PvI1HetwArlAfsrL7wT9CUPeiG5k3i4\n" +
                "F8ubQuKX/O1qckgwElpl8FBvT7/yGSucNEJYqDHh8/z+oXHWHZdkUZD7IylGeLbN3DCs1F1gYawP\n" +
                "1c87K2B7psfXslNysEg/Xe5LoEYdJHx7BnuBhVJPhDSuwL5vJBzY5NUd1RVaVmx8hlrVFCZp9s92\n" +
                "03z2YlaEWLX/rheI0ZNH3UYqxDeYAaVceVX7takVq1aR1CsdIVCXjQFXyg4euwWlZPWRa1ktkDa2\n" +
                "V/tRkyBuCJLhNIBNOR+AqpcSPQnZPVIEYSA2leKQufNh8OO6lHHGNymfH7qGO+bmO+6tDRt0OPhf\n" +
                "JYKxjXWldpNWLcqzaASgYs2Q5IP5+vC/d4b/DjgR1dSPiUCJeg3HcVOlN6aA5x+i++MwqdQxUt8t\n" +
                "Z9DF+miZbrsCa3b+JuggDK78bQPoUb41MimmK234VL9yf1U3Jmv49MJGHdKsZQNaIkLwvOkjMfgN\n" +
                "Pu3EKt4cYq+rWTC2/rel5+9h7A4iXIqkeJn+sCsXSliZFzQ1zG7fxBhBntZQhmYWkwRpedSFQU2k\n" +
                "ZN30ssU4XqdmPhNxAAq1i2VbEqxwFmjFgIMCAwEAATANBgkqhkiG9w0BAQ0FAAOCAgEAQ6LASOsR\n" +
                "xjny7oTzxqcVqcxa+JBpDnprixohyLzzyEC1Arl/DM0hajse96u9TfUaexjP6c8SBXnfKH4Hpn0P\n" +
                "mbs4deUZukjAhONa6VGIElcovmasqwuT7kN8LfpuCHLbPS1QgPbVQT6RxcY2n7UfLur8jmDwiSnz\n" +
                "rnD9IAra1SArFB5E09MycfCJLK+WPGsqHg3miscd06bR6JNQgeg97POhJqkn3itNLeCMwil9jpNY\n" +
                "rrzU1ZyGzfjPzMbET2jeq3dnU3hwRa9lLDSk5qrSQppV6lldbFnymxTel8bGC6Rx6iYCARgt+AS5\n" +
                "HQNC4k6adRzO6XjNf6mr3S6Dwp5Ozmcc/QWkRZ/gp7S8ImCyd+iw1CYRok77axuvI6FIzycz11zs\n" +
                "5TVkYdDOZRAasNrAU2vVH9Q0gAMNpeciv/uASjW5/sAni5r2dfTy1g9JOlNh8NMOVEanjju6THXK\n" +
                "s1ZksqQV7t+THgCqsQSK4cPL0EW4+Sp8lAb8/zzIhltYo5jJLWvc14ECF5i/HgAuW7ggnqphCIpm\n" +
                "dYMUkfF9TwOMfWN31ur2R0EKrYU1Xj8hTor+YpuWC5EcwLwcPuDynY/CzmoKhPSaZtfkiHbhV1D8\n" +
                "71UW7jyUMs0yTE0r/y4cSgnK78Cm51MFIohD9Jwo8HHT/1SNh+3IXWbR7H2n74nFZO4=</ds:X509Certificate>\n" +
                "\t\t\t</ds:X509Data>\n" +
                "\t\t</ds:KeyInfo>\n" +
                "\t</ds:Signature>\n" +
                "\t<saml2p:Status>\n" +
                "\t\t<saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>\n" +
                "\t\t<saml2p:StatusMessage>urn:oasis:names:tc:SAML:2.0:status:Success</saml2p:StatusMessage>\n" +
                "\t</saml2p:Status>\n" +
                "\t<saml2:Assertion ID=\"_63e8052813db8233b32a5a7cb8ef08a7\" IssueInstant=\"2015-10-09T13:49:25.000Z\" Version=\"2.0\">\n" +
                "\t\t<saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">http://d02di1601313dit.net1.cec.eu.int:17001/EidasNode/ConnectorResponderMetadata</saml2:Issuer>\n" +
                "\t\t<saml2:Subject>\n" +
                "\t\t\t<saml2:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\" NameQualifier=\"http://C-PEPS.gov.xx\">CB/CB/12345</saml2:NameID>\n" +
                "\t\t\t<saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\">\n" +
                "\t\t\t\t<saml2:SubjectConfirmationData Address=\"158.168.153.98\" InResponseTo=\"_a6bf27e8d7a4b2684b952a9651b709b5\" NotOnOrAfter=\"2015-10-09T13:49:35.000Z\" Recipient=\"http://SPSERVER:8888/SP/ReturnPage\"/>\n" +
                "\t\t\t</saml2:SubjectConfirmation>\n" +
                "\t\t</saml2:Subject>\n" +
                "\t\t<saml2:Conditions NotBefore=\"2015-10-09T13:49:25.000Z\" NotOnOrAfter=\"2015-10-09T13:49:35.000Z\">\n" +
                "\t\t\t<saml2:AudienceRestriction>\n" +
                "\t\t\t\t<saml2:Audience>http://SPSERVER:8888/SP/metadata</saml2:Audience>\n" +
                "\t\t\t</saml2:AudienceRestriction>\n" +
                "\t\t\t<saml2:OneTimeUse/>\n" +
                "\t\t</saml2:Conditions>\n" +
                "\t\t<saml2:AuthnStatement AuthnInstant=\"2015-10-09T13:49:25.000Z\">\n" +
                "\t\t\t<saml2:SubjectLocality Address=\"158.168.153.98\"/>\n" +
                "\t\t\t<saml2:AuthnContext>\n" +
                "\t\t\t\t<saml2:AuthnContextClassRef>http://eidas.europa.eu/LoA/low</saml2:AuthnContextClassRef>\n" +
                "\t\t\t\t<saml2:AuthnContextDecl/>\n" +
                "\t\t\t</saml2:AuthnContext>\n" +
                "\t\t</saml2:AuthnStatement>\n" +
                "\t\t<saml2:AttributeStatement>\n" +
                "\t\t\t<saml2:Attribute FriendlyName=\"CurrentFamilyName\" Name=\"http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">\n" +
                "\t\t\t\t<saml2:AttributeValue xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"eidas:CurrentFamilyNameType\">Garcia</saml2:AttributeValue>\n" +
                "\t\t\t</saml2:Attribute>\n" +
                "\t\t\t<saml2:Attribute FriendlyName=\"CurrentGivenName\" Name=\"http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">\n" +
                "\t\t\t\t<saml2:AttributeValue xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"eidas:CurrentGivenNameType\">Javier</saml2:AttributeValue>\n" +
                "\t\t\t</saml2:Attribute>\n" +
                "\t\t\t<saml2:Attribute FriendlyName=\"DateOfBirth\" Name=\"http://eidas.europa.eu/attributes/naturalperson/DateOfBirth\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">\n" +
                "\t\t\t\t<saml2:AttributeValue xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"eidas:DateOfBirthType\">1965-01-01</saml2:AttributeValue>\n" +
                "\t\t\t</saml2:Attribute>\n" +
                "\t\t\t<saml2:Attribute FriendlyName=\"PersonIdentifier\" Name=\"http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">\n" +
                "\t\t\t\t<saml2:AttributeValue xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"eidas:PersonIdentifierType\">CB/CB/12345</saml2:AttributeValue>\n" +
                "\t\t\t</saml2:Attribute>\n" +
                "\t\t\t<saml2:Attribute FriendlyName=\"LegalPersonIdentifier\" Name=\"http://eidas.europa.eu/attributes/legalperson/LegalPersonIdentifier\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">\n" +
                "\t\t\t\t<saml2:AttributeValue xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"eidas:LegalPersonIdentifierType\">CB/CB/LegalPersonUniqueId</saml2:AttributeValue>\n" +
                "\t\t\t</saml2:Attribute>\n" +
                "\t\t\t<saml2:Attribute FriendlyName=\"LegalName\" Name=\"http://eidas.europa.eu/attributes/legalperson/LegalName\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">\n" +
                "\t\t\t\t<saml2:AttributeValue xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"eidas:LegalNameType\">Current Legal Name</saml2:AttributeValue>\n" +
                "\t\t\t</saml2:Attribute>\n" +
                "\t\t</saml2:AttributeStatement>\n" +
                "\t</saml2:Assertion>\n" +
                "</saml2p:Response>";
        String expectedID="ID=\"_63e8052813db8233b32a5a7cb8ef08a7";
        String assertion=SPUtil.extractAssertionAsString(message);
        assertFalse(assertion.isEmpty());
        assertTrue(assertion.contains(expectedID));
    }
}