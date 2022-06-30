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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class LightRequestTest {

    private static final String ID = "f5e7e0f5-b9b8-4256-a7d0-4090141b326d";
    private static final String ISSUER = "http://localhost:7001/SP/metadata";
    private static final String CITIZEN_COUNTRY_CODE = "ES";
    private static final String LEVEL_OF_ASSURANCE = "http://eidas.europa.eu/LoA/low";
    private static final String UNSPECIFIED = "unspecified";

    private static final AttributeDefinition<String> CURRENT_FAMILY_NAME =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
                    .friendlyName("FamilyName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final AttributeDefinition<String> CURRENT_GIVEN_NAME =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName")
                    .friendlyName("FirstName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentGivenNameType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final ImmutableAttributeMap REQUESTED_ATTRIBUTES =
            new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker", false))
                    .put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude", false))
                    .build();

    private static final String PUBLIC = "public";

    @SuppressWarnings({"PublicField"})
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testLightRequestCreation() {
        LightRequest lightRequest = new LightRequest.Builder().
                id(ID).
                issuer(ISSUER).
                citizenCountryCode(CITIZEN_COUNTRY_CODE).
                levelOfAssurance(LEVEL_OF_ASSURANCE).
                providerName("TEST").
                build();
        assertNotNull(lightRequest);
    }

    @Test
    public void testValidationOnBuildForMissingId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("id cannot be null, empty or blank");
        new LightRequest.Builder().issuer(ISSUER).build();
    }

    @Test
    public void testValidationOnBuildForMissingIssuer() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("issuer cannot be null, empty or blank");
        new LightRequest.Builder().id(ID).build();
    }

    @Test
    public void testValidationOnBuildForMissingCitizenCountryCode() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("citizenCountryCode cannot be null, empty or blank");
        new LightRequest.Builder().id(ID).issuer(ISSUER).build();
    }

    @Test
    public void testDefaultLightRequestEquals() {
        LightRequest lightRequest1 = new LightRequest.Builder().id(ID).issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(LEVEL_OF_ASSURANCE).build();
        LightRequest lightRequest2 = new LightRequest.Builder().id(ID).issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(LEVEL_OF_ASSURANCE).build();
        assertEquals(lightRequest1, lightRequest2);
        assertTrue(lightRequest1.equals(lightRequest2));
    }

    @Test
    public void testDefaultLightRequestNotEquals() {
        LightRequest lightRequest1 = new LightRequest.Builder().id(ID).issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(LEVEL_OF_ASSURANCE).nameIdFormat(UNSPECIFIED).build();
        LightRequest lightRequest2 = new LightRequest.Builder().id(ID).issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(LEVEL_OF_ASSURANCE).build();
        assertNotEquals(lightRequest1, lightRequest2);
        assertFalse(lightRequest1.equals(lightRequest2));
    }

    @Test
    public void testHashLightRequest() {
        LightRequest lightRequest1 = new LightRequest.Builder().id(ID).issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(LEVEL_OF_ASSURANCE).build();
        LightRequest lightRequest2 = new LightRequest.Builder().id(ID).issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(LEVEL_OF_ASSURANCE).build();
        assertEquals(lightRequest1.hashCode(), lightRequest2.hashCode());
    }

    @Test
    public void testGetters() throws Exception {
        LightRequest lightRequest = new LightRequest.Builder().
                id(ID).
                issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(LEVEL_OF_ASSURANCE).nameIdFormat(UNSPECIFIED).build();
        assertThat(lightRequest.getId(), is(ID));
        assertThat(lightRequest.getIssuer(), is(ISSUER));
        assertThat(lightRequest.getLevelOfAssurance(), is(LEVEL_OF_ASSURANCE));
        assertThat(lightRequest.getNameIdFormat(), is(UNSPECIFIED));
    }



    @Test
    public void testGetRequestedAttributes() throws Exception {
        LightRequest lightRequest = new LightRequest.Builder().id(ID).issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(LEVEL_OF_ASSURANCE).requestedAttributes(REQUESTED_ATTRIBUTES).build();
        assertThat(lightRequest.getRequestedAttributes(), is(REQUESTED_ATTRIBUTES));
    }

    @Test
    public void testToString() throws Exception {
        LightRequest lightRequest =
                new LightRequest.Builder()
                        .id(ID)
                        .issuer(ISSUER)
                        .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                        .levelOfAssurance(LEVEL_OF_ASSURANCE)
                        .requestedAttributes(REQUESTED_ATTRIBUTES)
                        .nameIdFormat(UNSPECIFIED)
                        .spType(PUBLIC)
                        .build();
        String expected = "LightRequest{id='f5e7e0f5-b9b8-4256-a7d0-4090141b326d', citizenCountryCode='ES', issuer='http://localhost:7001/SP/metadata', levelOfAssurance='http://eidas.europa.eu/LoA/low', providerName='null', nameIdFormat='unspecified', spType='public', requestedAttributes='{AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName', friendlyName='FamilyName', personType=NaturalPerson, required=true, transliterationMandatory=false, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentFamilyNameType', attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[Juncker], AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName', friendlyName='FirstName', personType=NaturalPerson, required=true, transliterationMandatory=false, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentGivenNameType', attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[Jean-Claude]}'}";
        assertEquals(expected, lightRequest.toString());
    }
}