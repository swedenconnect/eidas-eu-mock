/*
 * Copyright (c) 2019 by European Commission
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

package eu.eidas.specificcommunication.protocol.util;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;


/**
 * Test class for {@link SecurityUtils}
 */
public class SecurityUtilsTest {

	private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

	private static final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";

	private static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";

	private static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

	/**
	 * A malicious XML DTD sample
	 */
	public static String MALICIOUS_XML_DTD_SAMPLE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+
			"<!DOCTYPE providerName [\n"+
			"<!ENTITY lol \"DEMOTEST\">\n"+
			"]>\n"+
			"<lightRequest>\n"+
			"<citizenCountryCode>CA</citizenCountryCode>\n"+
			"<id>aa301220-314c-47f9-8d3a-159a9a3a0a90</id>\n"+
			"<issuer>specificConnectorCA</issuer>\n"+
			"<levelOfAssurance>http://eidas.europa.eu/LoA/low</levelOfAssurance>\n"+
			"<nameIdFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</nameIdFormat>\n"+
			"<providerName>&lol;</providerName>\n"+
			"<spType>public</spType>\n"+
			"<requestedAttributes>\n"+
			"<attribute>\n"+
			"<definition>http://eidas.europa.eu/attributes/legalperson/LegalName</definition>\n"+
			"</attribute>\n"+
			"<attribute>\n"+
			"<definition>http://eidas.europa.eu/attributes/legalperson/LegalPersonIdentifier</definition>\n"+
			"</attribute>\n"+
			"</requestedAttributes>\n"+
			"</lightRequest>";

	/**
	 * A malicious XML DTD XXE Bomb sample
	 */
	public static String MALICIOUS_XML_DTD_XXE_BOMB = "<?xml version=\"1.0\"?>\n"+
            "<!DOCTYPE lightRequest [\n"+
            "<!ENTITY lol \"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\">\n"+
            "<!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n"+
            "<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n"+
            "<!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n"+
            "<!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n"+
            "]>\n"+
            "<lightRequest>&lol5;</lightRequest>";

	/**
	 * Test method for
	 * {@link SecurityUtils#createSecureSaxSource(String)}
	 * when input string is an XML with DTD
	 * <p>
	 * Must succeed.
	 */
    @Test
    public void createSecureSaxSource1() throws ParserConfigurationException, SAXException {
		SAXSource secureSaxSource = SecurityUtils.createSecureSaxSource(MALICIOUS_XML_DTD_SAMPLE);

		assertSecureFeatures(secureSaxSource);
	}

	/**
	 * Test method for
	 * {@link SecurityUtils#createSecureSaxSource(String)}
	 * when input string is an XML with DTD with an XXE bomb
	 * <p>
	 * Must succeed.
	 */
	@Test
    public void createSecureSaxSource2() throws ParserConfigurationException, SAXException {
		SAXSource secureSaxSource = SecurityUtils.createSecureSaxSource(MALICIOUS_XML_DTD_XXE_BOMB);

		assertSecureFeatures(secureSaxSource);
    }

	/**
	 * Auxiliary method for asserting if secure features are correctly set.
	 *
	 * @param secureSaxSource the instance of {@link SAXSource} to be checked
	 * @throws SAXNotRecognizedException If the feature
	 *            value can't be assigned or retrieved.
	 * @throws SAXNotSupportedException When the
	 *            XMLReader recognizes the feature name but
	 *            cannot determine its value at this time.
	 *
	 */
	private void assertSecureFeatures(SAXSource secureSaxSource) throws SAXNotRecognizedException, SAXNotSupportedException {
		boolean actualFeatureLoadExternalDtd = secureSaxSource.getXMLReader().getFeature(LOAD_EXTERNAL_DTD);
		boolean actualFeatureDisallowDoctypeDecl = secureSaxSource.getXMLReader().getFeature(DISALLOW_DOCTYPE_DECL);
		boolean actualFeatureExternalGeneralEntities = secureSaxSource.getXMLReader().getFeature(EXTERNAL_GENERAL_ENTITIES);
		boolean actualFeatureExternalParameterEntities = secureSaxSource.getXMLReader().getFeature(EXTERNAL_PARAMETER_ENTITIES);

		Assert.assertFalse(actualFeatureLoadExternalDtd);
		Assert.assertTrue(actualFeatureDisallowDoctypeDecl);
		Assert.assertFalse(actualFeatureExternalGeneralEntities);
		Assert.assertFalse(actualFeatureExternalParameterEntities);
	}

}