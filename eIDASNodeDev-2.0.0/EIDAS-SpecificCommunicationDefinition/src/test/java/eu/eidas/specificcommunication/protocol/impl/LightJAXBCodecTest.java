package eu.eidas.specificcommunication.protocol.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.AbstractCollection;

import javax.xml.bind.JAXBException;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.DateTimeAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import eu.eidas.auth.commons.protocol.eidas.impl.GenderAttributeValue;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddress;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddressAttributeValue;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;

public class LightJAXBCodecTest {

	private static final String ID = "f5e7e0f5-b9b8-4256-a7d0-4090141b326d";
	private static final String ISSUER = "http://localhost:7001/SP/metadata";
	private static final String CITIZEN_COUNTRY_CODE = "ES";
	private static final String LEVEL_OF_ASSURANCE = "http://eidas.europa.eu/LoA/low";
	private static final String UNSPECIFIED = "unspecified";
	private static final String RELAYSTATE = "MyRelayState";
	private static final String PROVIDERNAME = "ProviderName";

	private static final PostalAddress pa = new PostalAddress.Builder().addressId("addressId")
			.adminUnitFirstLine("adminUnitFirstLine").adminUnitSecondLine("adminUnitSecondLine")
			.cvAddressArea("cvAddressArea").fullCvaddress("fullCvaddress").locatorDesignator("locatorDesignator")
			.locatorName("locatorName").poBox("poBox").postCode("postCode").postName("postName")
			.thoroughfare("thoroughfare")
			.build();

	private static final ImmutableAttributeMap ATTRIBUTES = new ImmutableAttributeMap.Builder()
			.put(EidasSpec.Definitions.PERSON_IDENTIFIER, new StringAttributeValue("Juncker-987654321", false))
			.put(EidasSpec.Definitions.CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker", false))
			.put(EidasSpec.Definitions.CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude", false),
					new StringAttributeValue("Jean", false), new StringAttributeValue("Claude", false))
			.put(EidasSpec.Definitions.BIRTH_NAME, new StringAttributeValue("Juncker", false))
			.put(EidasSpec.Definitions.D_2012_17_EU_IDENTIFIER, new StringAttributeValue("Juncker12345", false))
			.put(EidasSpec.Definitions.DATE_OF_BIRTH, new DateTimeAttributeValue(new DateTime()))
			.put(EidasSpec.Definitions.EORI, new StringAttributeValue("Juncker-eori", false))
			.put(EidasSpec.Definitions.GENDER, new GenderAttributeValue(Gender.MALE))
			.put(EidasSpec.Definitions.PLACE_OF_BIRTH, new StringAttributeValue("Luxembourgh", false))
			.put(EidasSpec.Definitions.CURRENT_ADDRESS, new PostalAddressAttributeValue(pa))
			.build();

	private static final AbstractCollection<AttributeDefinition<?>> REGISTRY = ATTRIBUTES.getDefinitions();

	private static final String PUBLIC = "public";

	private static final String IN_RESPONSE_TO_ID = "x4e7e0f5-b9b8-4256-a7d0-4090141b320u";
	private static final String STATUS_CODE = "urn:oasis:names:tc:SAML:2.0:status:Success_CODE";
	private static final String STATUS_MESSAGE = "urn:oasis:names:tc:SAML:2.0:status:Success_MESSAGE";
	private static final String SUB_STATUS_CODE = "urn:oasis:names:tc:SAML:2.0:status:Success_SUB_CODE";
	private static final boolean FAILURE = false;
	private static final String IP_ADDRESS = "127.0.0.1";
	private static final String SUBJECT = "ES/FI/0123456";
	private static final String SUBJECT_NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";

	LightJAXBCodec codecUnderTest;

	@Before
	public void setup() throws JAXBException {
		// JAXBContext
		javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(LightRequest.class,
				LightResponse.class, ImmutableAttributeMap.class, AttributeDefinition.class);
		codecUnderTest = new LightJAXBCodec(jaxbCtx);
	}

	@Test
	public void testMarshallUnmarshallResponseWithNonNullRegistry() throws Exception {
		LightResponse lightResponse = new LightResponse.Builder().id(ID).issuer(ISSUER).relayState(RELAYSTATE)
				.inResponseToId(IN_RESPONSE_TO_ID)
				.status(ResponseStatus.builder().statusCode(STATUS_CODE).statusMessage(STATUS_MESSAGE)
						.subStatusCode(SUB_STATUS_CODE).failure(FAILURE).build())
				.ipAddress(IP_ADDRESS).subject(SUBJECT).subjectNameIdFormat(SUBJECT_NAME_ID_FORMAT)
				.levelOfAssurance(LEVEL_OF_ASSURANCE).attributes(ATTRIBUTES).build();
		String str = codecUnderTest.marshall(lightResponse);
		System.out.println("Marshalled lightResponse: \n" + str);

		System.out.println("lightResponse: \n" + lightResponse);
		LightResponse result = codecUnderTest.unmarshallResponse(str, REGISTRY);
		System.out.println("Unmarshalled lightResponse: \n" + result);

		assertEquals(lightResponse.getId(), result.getId());
		assertEquals(lightResponse.getRelayState(),result.getRelayState());
		assertEquals( lightResponse.getIssuer(),result.getIssuer());
		assertEquals( lightResponse.getIPAddress(),result.getIPAddress());
		assertEquals( lightResponse.getSubject(),result.getSubject());
		assertEquals( lightResponse.getSubjectNameIdFormat(),result.getSubjectNameIdFormat());
		assertEquals( lightResponse.getStatus(),result.getStatus());
		assertEquals( lightResponse.getInResponseToId(),result.getInResponseToId());
		assertEquals( lightResponse.getLevelOfAssurance(),result.getLevelOfAssurance());
		
		assertEquals( lightResponse.getAttributes().size(),result.getAttributes().size());
		assertEquals(lightResponse.getAttributes().getAttributeMap().keySet(),
				result.getAttributes().getAttributeMap().keySet());
//		assertEquals(lightResponse.getAttributes().getAttributeMap().entrySet(),
//				result.getAttributes().getAttributeMap().entrySet());
		for (AttributeDefinition<?> d : lightResponse.getAttributes().getAttributeMap().keySet()) {
			ImmutableSet<?> expectedSet = lightResponse.getAttributes().getAttributeValues(d);
			ImmutableSet<?> actualSet = result.getAttributes().getAttributeValues(d);
			System.out.println("\n\nexpected:" + expectedSet + expectedSet.containsAll(actualSet));
			System.out.println("\nactual:" + actualSet + actualSet.containsAll(expectedSet));
			// assertTrue(actualSet.containsAll(expectedSet));
			// assertTrue(expectedSet.containsAll(actualSet));

		 }
	}

	@Test
	public void testMarshallUnmarshallRequestWithNullRegistry() throws Exception {
		LightRequest lightRequest = new LightRequest.Builder().id(ID).issuer(ISSUER)
				.citizenCountryCode(CITIZEN_COUNTRY_CODE).levelOfAssurance(LEVEL_OF_ASSURANCE)
				.requestedAttributes(ATTRIBUTES).nameIdFormat(UNSPECIFIED).spType(PUBLIC).relayState(RELAYSTATE)
				.providerName(PROVIDERNAME)
				.build();

		String str = codecUnderTest.marshall(lightRequest);
		try{
			LightRequest result = codecUnderTest.unmarshallRequest(str, null);
			fail("should have thrown SpecificCommunicationException(\"missing registry\")");
		}catch(SpecificCommunicationException sce){
			assertEquals("missing registry", sce.getMessage());
		}
	}

	@Test
	public void testMarshallUnmarshallRequestWithNonNullRegistry() throws Exception {
		LightRequest lightRequest = new LightRequest.Builder().id(ID).issuer(ISSUER)
				.citizenCountryCode(CITIZEN_COUNTRY_CODE).levelOfAssurance(LEVEL_OF_ASSURANCE)
				.requestedAttributes(ATTRIBUTES).nameIdFormat(UNSPECIFIED).spType(PUBLIC).relayState(RELAYSTATE)
				.providerName(PROVIDERNAME)
				.build();

		String str = codecUnderTest.marshall(lightRequest);
		System.out.println("Marshalled lightRequest: \n" + str);

		System.out.println("lightRequest: \n" + lightRequest);
		LightRequest result = codecUnderTest.unmarshallRequest(str, REGISTRY);
		System.out.println("Unmarshalled lightRequest: \n" + result);

		assertEquals(lightRequest.getId(), result.getId());
		assertEquals(lightRequest.getCitizenCountryCode(), result.getCitizenCountryCode());
		assertEquals(lightRequest.getIssuer(), result.getIssuer());
		assertEquals(lightRequest.getLevelOfAssurance(), result.getLevelOfAssurance());
		assertEquals(lightRequest.getNameIdFormat(), result.getNameIdFormat());
		assertEquals(lightRequest.getProviderName(), result.getProviderName());
		assertEquals(lightRequest.getRelayState(), result.getRelayState());
		assertEquals(lightRequest.getRequestedAttributes().size(), result.getRequestedAttributes().size());
	}
	
	// @formatter:off
	private static String MARSHALLED_RESPONSE = "<lightResponse>" + "    <id>9e33f2c5-e4af-4997-b61d-8a25d3652b9e</id> "
			+ "    <relayState>d061c40a-c6f7-4527-ae71-9253ed9b4666</relayState>" + "    <issuer>DEMO-IDP</issuer>"
			+ "    <ipAddress>127.0.0.1</ipAddress>" + "    <subject>ES/BE/0123456</subject>"
			+ "    <subjectNameIdFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</subjectNameIdFormat>"
			+ "    <status>" + "        <failure>false</failure>"
			+ "        <statusCode>urn:oasis:names:tc:SAML:2.0:status:Success</statusCode>" + "    </status>"
			+ "    <inResponseToId>_as0lUefFjKFsLjVtkES78OT9tJRc9haef3mJ2OYWj9edJtkxUhFdTiv1Pa5ydXL</inResponseToId>"
			+ "    <levelOfAssurance>http://eidas.europa.eu/LoA/low</levelOfAssurance>" + "    <attributes>"
			+ "        <attribute>" + "            <definition>"
			+ "                <nameUri>http://eidas.europa.eu/attributes/naturalperson/BirthName</nameUri>"
			+ "                <friendlyName>BirthName</friendlyName>"
			+ "                <personType>NATURAL_PERSON</personType>" + "                <required>false</required>"
			+ "                <transliterationMandatory>true</transliterationMandatory>"
			+ "                <xmlType xmlns:eidas-natural=\"http://eidas.europa.eu/attributes/naturalperson\">eidas-natural:BirthNameType</xmlType>"
			+ "                <uniqueIdentifier>false</uniqueIdentifier>"

			+ "                <attributeValueMarshallerClassName>eu.eidas.auth.commons.attribute.impl.LiteralStringAttributeValueMarshaller</attributeValueMarshallerClassName>"

			+ "            </definition>" + "            <value>Ωνάσης</value>" + "            <value>Onases</value>"
			+ "        </attribute>" + "        <attribute>"
			+ "            <definition>"
			+ "                <nameUri>http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName</nameUri>"
			+ "                <friendlyName>FamilyName</friendlyName>"
			+ "                <personType>NATURAL_PERSON</personType>" + "                <required>true</required>"
			+ "                <transliterationMandatory>true</transliterationMandatory>"
			+ "                <xmlType xmlns:eidas-natural=\"http://eidas.europa.eu/attributes/naturalperson\">eidas-natural:CurrentFamilyNameType</xmlType>"
			+ "                <uniqueIdentifier>false</uniqueIdentifier>"
			+ "                <attributeValueMarshallerClassName>eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller</attributeValueMarshallerClassName>"
			+ "            </definition>" + "            <value>Garcia</value>" + "        </attribute>"
			+ "    </attributes>" + "</lightResponse>";
	// @formatter:on

	@Test(expected = SpecificCommunicationException.class)
	public void testMarshallUnmarshallBigResponse() throws Exception {
		LightResponse result = codecUnderTest.unmarshallResponse(MARSHALLED_RESPONSE, null);
		System.out.println("Unmarshalled lightResponse: \n" + result);
	}

}
