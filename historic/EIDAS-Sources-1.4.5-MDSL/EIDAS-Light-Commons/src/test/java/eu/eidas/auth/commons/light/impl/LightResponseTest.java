package eu.eidas.auth.commons.light.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class LightResponseTest {

    private static final boolean FAILURE = false;

    private static final String IP_ADDRESS = "127.0.0.1";

    private static final String ID = "f5e7e0f5-b9b8-4256-a7d0-4090141b326d";

    private static final String ISSUER = "http://localhost:7001/IdP/metadata";

    private static final String IN_RESPONSE_TO_ID = "x4e7e0f5-b9b8-4256-a7d0-4090141b320u";

    private static final String LEVEL_OF_ASSURANCE = "http://eidas.europa.eu/LoA/low";

    private static final String STATUS_CODE = "urn:oasis:names:tc:SAML:2.0:status:Success_CODE";

    private static final String STATUS_MESSAGE = "urn:oasis:names:tc:SAML:2.0:status:Success_MESSAGE";

    private static final String SUB_STATUS_CODE = "urn:oasis:names:tc:SAML:2.0:status:Success_SUB_CODE";

    private static final AttributeDefinition<String> CURRENT_FAMILY_NAME = new AttributeDefinition.Builder<String>().nameUri(
            "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
            .friendlyName("FamilyName")
            .personType(PersonType.NATURAL_PERSON)
            .required(true)
            .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType", "eidas-natural")
            .attributeValueMarshaller(new StringAttributeValueMarshaller())
            .build();

    private static final AttributeDefinition<String> CURRENT_GIVEN_NAME = new AttributeDefinition.Builder<String>().nameUri(
            "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName")
            .friendlyName("FirstName")
            .personType(PersonType.NATURAL_PERSON)
            .required(true)
            .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentGivenNameType", "eidas-natural")
            .attributeValueMarshaller(new StringAttributeValueMarshaller())
            .build();

    private static final ImmutableAttributeMap ATTRIBUTES =
            new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker", false))
                    .put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude", false))
                    .build();

    @SuppressWarnings({"PublicField"})
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testLightResponseCreation() {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertNotNull(lightResponse);
    }

    @Test
    public void testValidationOnBuildForMissingId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("id cannot be null");
        new LightResponse.Builder().build();
    }

    @Test
    public void testValidationOnBuildForMissingIssuer() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("issuer cannot be null");
        new LightResponse.Builder().id(ID).build();
    }

    @Test
    public void testValidationOnBuildForMissingIp() {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(null)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertNotNull(lightResponse);
        assertNull(lightResponse.getIPAddress());
    }

    @Test
    public void testValidationOnBuildForMissingStatus() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("status cannot be null");
        new LightResponse.Builder().id(ID).issuer(ISSUER).ipAddress(IP_ADDRESS).build();
    }

    @Test
    public void testValidationOnBuildForEmptyStatus() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("statusCode cannot be null");
        new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().build())
                .build();
    }

    @Test
    public void testValidationOnBuildForMissingInResponseTo() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("inResponseToId cannot be null");
        new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .build();
    }

    @Test
    public void testValidationOnBuildForMissingLevelOfAssurance() {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(null)
                .build();
        assertNotNull(lightResponse);
        assertNull(lightResponse.getLevelOfAssurance());
    }

    @Test
    public void testCopyConstructor() throws Exception {
        LightResponse lightResponse1 = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();

        LightResponse lightResponse2 = LightResponse.builder(lightResponse1).build();

        assertEquals(lightResponse1, lightResponse2);
    }

    @Test
    public void testDefaultLightResponseEquals() {
        LightResponse lightResponse1 = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        LightResponse lightResponse2 = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertEquals(lightResponse1, lightResponse2);
        assertTrue(lightResponse1.equals(lightResponse2));
    }

    @Test
    public void testDefaultLightRequestNotEquals() {
        LightResponse lightResponse1 = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        LightResponse lightResponse2 = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).subStatusCode(SUB_STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertNotEquals(lightResponse1, lightResponse2);
        assertFalse(lightResponse1.equals(lightResponse2));
    }

    @Test
    public void testHashLightRequest() {
        LightResponse lightResponse1 = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        LightResponse lightResponse2 = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertEquals(lightResponse1.hashCode(), lightResponse2.hashCode());
    }

    @Test
    public void testGetId() throws Exception {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertThat(lightResponse.getId(), is(ID));
    }

    @Test
    public void testGetIssuer() throws Exception {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertThat(lightResponse.getIssuer(), is(ISSUER));
    }

    @Test
    public void testGetMessage() throws Exception {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).statusMessage(STATUS_MESSAGE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertThat(lightResponse.getStatus().getStatusMessage(), is(STATUS_MESSAGE));
    }

    @Test
    public void isFailure() throws Exception {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).failure(true).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertThat(lightResponse.getStatus().isFailure(), is(Boolean.TRUE));
    }

    @Test
    public void testGetIPAddress() throws Exception {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertThat(lightResponse.getIPAddress(), is(IP_ADDRESS));
    }

    @Test
    public void testGetStatusCode() throws Exception {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertThat(lightResponse.getStatus().getStatusCode(), is(STATUS_CODE));
    }

    @Test
    public void testGetSubStatusCode() throws Exception {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).subStatusCode(SUB_STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertThat(lightResponse.getStatus().getSubStatusCode(), is(SUB_STATUS_CODE));
    }

    @Test
    public void testGetInResponseToId() throws Exception {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertThat(lightResponse.getInResponseToId(), is(IN_RESPONSE_TO_ID));
    }

    @Test
    public void testGetLevelOfAssurance() throws Exception {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertThat(lightResponse.getLevelOfAssurance(), is(LEVEL_OF_ASSURANCE));
    }

    @Test
    public void testGetAttributes() throws Exception {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .ipAddress(IP_ADDRESS)
                .status(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .inResponseToId(IN_RESPONSE_TO_ID)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .attributes(ATTRIBUTES)
                .build();
        assertThat(lightResponse.getAttributes(), is(ATTRIBUTES));
    }

    @Test
    public void testToString() throws Exception {
        LightResponse lightResponse = new LightResponse.Builder().id(ID)
                .issuer(ISSUER)
                .inResponseToId(IN_RESPONSE_TO_ID)
                .status(ResponseStatus.builder()
                        .statusCode(STATUS_CODE)
                        .statusMessage(STATUS_MESSAGE)
                        .subStatusCode(SUB_STATUS_CODE)
                        .failure(FAILURE)
                        .build())
                .ipAddress(IP_ADDRESS)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .attributes(ATTRIBUTES)
                .build();
        String expected = "LightResponse{id='f5e7e0f5-b9b8-4256-a7d0-4090141b326d', issuer='http://localhost:7001/IdP/metadata', status='ResponseStatus{failure='false', statusCode='urn:oasis:names:tc:SAML:2.0:status:Success_CODE', statusMessage='urn:oasis:names:tc:SAML:2.0:status:Success_MESSAGE', subStatusCode='urn:oasis:names:tc:SAML:2.0:status:Success_SUB_CODE'}', ipAddress='127.0.0.1', inResponseToId='x4e7e0f5-b9b8-4256-a7d0-4090141b320u', levelOfAssurance='http://eidas.europa.eu/LoA/low', attributes='{AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName', friendlyName='FamilyName', personType=NaturalPerson, required=true, transliterationMandatory=false, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentFamilyNameType', attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[Juncker], AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName', friendlyName='FirstName', personType=NaturalPerson, required=true, transliterationMandatory=false, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentGivenNameType', attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[Jean-Claude]}'}";
        assertEquals(expected, lightResponse.toString());
    }
}
