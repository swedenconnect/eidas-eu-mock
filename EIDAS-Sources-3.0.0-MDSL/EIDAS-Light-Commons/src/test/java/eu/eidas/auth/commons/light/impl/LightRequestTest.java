/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.auth.commons.light.impl;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.LevelOfAssuranceType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link LightRequest} creation, validation and representation
 */
public final class LightRequestTest {

    private static final String ID = "f5e7e0f5-b9b8-4256-a7d0-4090141b326d";
    private static final String ISSUER = "http://localhost:7001/SP/metadata";
    private static final String CITIZEN_COUNTRY_CODE = "ES";
    private static final String SP_COUNTRY_CODE = "ES";
    private static final String LEVEL_OF_ASSURANCE = "http://eidas.europa.eu/LoA/low";
    private static final String UNSPECIFIED = "unspecified";
    private static final String RELAYSTATE = "MyRelayState";
    private static final String REQUESTER_ID = "requesterId";

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
            new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker"))
                    .put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude"))
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
                spCountryCode(SP_COUNTRY_CODE).
                levelOfAssurance(LEVEL_OF_ASSURANCE).
                providerName("TEST").
                relayState(RELAYSTATE).
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

    /**
     * Test comparison of LightRequest with multiple Levels of Assurance
     * <p>
     * Must succeed
     */
    @Test
    public void testHashLightRequestWithMultipleLoAs() {
        String nonNotifiedLoA = "http://nonNotifiedLoA";
        LightRequest lightRequest1 = new LightRequest.Builder().id(ID).issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .levelOfAssurance(nonNotifiedLoA)
                .build();
        LightRequest lightRequest2 = new LightRequest.Builder().id(ID).issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .levelOfAssurance(nonNotifiedLoA)
                .build();
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

    /**
     * Test of the LightRequest builder with levels of Assurance.
     * <p>
     * Must succeed.
     */
    @Test
    public void testBuildLightRequestWithLevelsOfAssurance() {
        String nonNotifiedLoaValue = "http://eidas.europa.eu/NotNotified/LoA/low";
        LevelOfAssurance nonNotifiedLoa = LevelOfAssurance.build(nonNotifiedLoaValue);
        LevelOfAssurance notifiedLoa = LevelOfAssurance.builder()
                .value(ILevelOfAssurance.EIDAS_LOA_LOW)
                .type("notified")
                .build();
        List<ILevelOfAssurance> loas = Arrays.asList(notifiedLoa, nonNotifiedLoa);
        LightRequest lightRequest = new LightRequest.Builder()
                .id(ID)
                .issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelsOfAssurance(loas)
                .build();
        assertNotNull(lightRequest);
        assertThat(lightRequest.getId(), is(ID));
        assertThat(lightRequest.getIssuer(), is(ISSUER));
        assertThat(lightRequest.getCitizenCountryCode(), is(CITIZEN_COUNTRY_CODE));
        assertThat(lightRequest.getLevelsOfAssurance().size(), is(2));
        assertThat(lightRequest.getLevelsOfAssurance().get(0).getValue(), is(lightRequest.getLevelOfAssurance()));
        ILevelOfAssurance levelOfAssurance = lightRequest.getLevelsOfAssurance().get(0);
        checkLevelOfAssurance(levelOfAssurance, ILevelOfAssurance.EIDAS_LOA_LOW, LevelOfAssuranceType.NOTIFIED);
        levelOfAssurance = lightRequest.getLevelsOfAssurance().get(1);
        checkLevelOfAssurance(levelOfAssurance, nonNotifiedLoaValue, LevelOfAssuranceType.NON_NOTIFIED);
    }

    /**
     * Test of LightRequest Builder with multiple notified Levels of Assurance
     * Should only return the lowest Notified LevelOfAssurance
     * <p>
     * Must succeed
     */
    @Test
    public void testBuildLightRequestWithMultipleNotifiedLoAs() {
        LightRequest lightRequest = LightRequest.builder()
                .id(ID)
                .issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(ILevelOfAssurance.EIDAS_LOA_LOW)
                .levelOfAssurance(ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL)
                .build();

        List<ILevelOfAssurance> levelsOfAssurance = lightRequest.getLevelsOfAssurance();
        Assert.assertEquals(1, levelsOfAssurance.size());
        Assert.assertEquals(LevelOfAssurance.build(ILevelOfAssurance.EIDAS_LOA_LOW), levelsOfAssurance.get(0));
    }

    /**
     * Test of the LightRequest builder with levels of Assurance values.
     * <p>
     * Must succeed.
     */
    @Test
    public void testBuildLightRequestWithLevelsOfAssuranceValues() {
        String nonNotifiedLoa = "http://eidas.europa.eu/NotNotified/LoA/low";
        List<String> loas = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_LOW, nonNotifiedLoa);
        LightRequest lightRequest = new LightRequest.Builder()
                .id(ID)
                .issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelsOfAssuranceValues(loas)
                .build();
        assertNotNull(lightRequest);
        assertThat(lightRequest.getId(), is(ID));
        assertThat(lightRequest.getIssuer(), is(ISSUER));
        assertThat(lightRequest.getCitizenCountryCode(), is(CITIZEN_COUNTRY_CODE));
        assertThat(lightRequest.getLevelsOfAssurance().size(), is(2));
        assertThat(lightRequest.getLevelsOfAssurance().get(0).getValue(), is(lightRequest.getLevelOfAssurance()));
        ILevelOfAssurance levelOfAssurance = lightRequest.getLevelsOfAssurance().get(0);
        checkLevelOfAssurance(levelOfAssurance, ILevelOfAssurance.EIDAS_LOA_LOW, LevelOfAssuranceType.NOTIFIED);
        levelOfAssurance = lightRequest.getLevelsOfAssurance().get(1);
        checkLevelOfAssurance(levelOfAssurance, nonNotifiedLoa, LevelOfAssuranceType.NON_NOTIFIED);
    }

    /**
     * Test of LightRequest Builder with requested attributes
     * Should retain all requested attributes
     * <p>
     * Must succeed
     */
    @Test
    public void testGetRequestedAttributes() {
        LightRequest lightRequest = new LightRequest.Builder().id(ID).issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .levelOfAssurance(LEVEL_OF_ASSURANCE).requestedAttributes(REQUESTED_ATTRIBUTES).build();
        assertThat(lightRequest.getRequestedAttributes(), is(REQUESTED_ATTRIBUTES));
    }

    /**
     * Test of LightRequest Builder
     * should  full attributes to print out expected string
     * <p>
     * Must succeed
     */
    @Test
    public void testToString() {
        LightRequest lightRequest =
                new LightRequest.Builder()
                        .id(ID)
                        .issuer(ISSUER)
                        .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                        .levelOfAssurance(LEVEL_OF_ASSURANCE)
                        .requestedAttributes(REQUESTED_ATTRIBUTES)
                        .nameIdFormat(UNSPECIFIED)
                        .spType(PUBLIC)
                        .spCountryCode(SP_COUNTRY_CODE)
                        .requesterId(REQUESTER_ID)
                        .relayState(RELAYSTATE)
                        .build();
        String expected = "LightRequest{id='f5e7e0f5-b9b8-4256-a7d0-4090141b326d', citizenCountryCode='ES', issuer='http://localhost:7001/SP/metadata', levelsOfAssurance='[LevelOfAssurance{type='notified', value='http://eidas.europa.eu/LoA/low'}]', providerName='null', nameIdFormat='unspecified', spType='public', spCountryCode='ES', requesterId='requesterId', relayState='MyRelayState', requestedAttributes='{AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName', friendlyName='FamilyName', personType=NaturalPerson, required=true, transliterationMandatory=false, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentFamilyNameType', attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[Juncker], AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName', friendlyName='FirstName', personType=NaturalPerson, required=true, transliterationMandatory=false, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentGivenNameType', attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[Jean-Claude]}'}";
        assertEquals(expected, lightRequest.toString());
    }

    /**
     * Test of LightRequest Builder
     * should throw IllegalArgumentException when no loas are defined
     * <p>
     * Must succeed
     */
    @Test
    public void validateLevelsOfAssuranceEmptyLoas() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid LoA: LoA is not set, LightRequest requires at least one levelOfAssurance");
        LightRequest lightRequest =
                new LightRequest.Builder()
                        .id(ID)
                        .issuer(ISSUER)
                        .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                        .requestedAttributes(REQUESTED_ATTRIBUTES)
                        .nameIdFormat(UNSPECIFIED)
                        .spType(PUBLIC)
                        .spCountryCode(SP_COUNTRY_CODE)
                        .requesterId(REQUESTER_ID)
                        .relayState(RELAYSTATE)
                        .build();
    }

    /**
     * Test of LightRequest Builder
     * should throw IllegalArgumentException when no loas are defined
     * <p>
     * Must succeed
     */
    @Test
    public void validateLevelsOfAssuranceGivenEmptyArrayLoas() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid LoA: LoA is not set, LightRequest requires at least one levelOfAssurance");
        LightRequest lightRequest =
                new LightRequest.Builder()
                        .id(ID)
                        .issuer(ISSUER)
                        .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                        .requestedAttributes(REQUESTED_ATTRIBUTES)
                        .nameIdFormat(UNSPECIFIED)
                        .spType(PUBLIC)
                        .spCountryCode(SP_COUNTRY_CODE)
                        .requesterId(REQUESTER_ID)
                        .relayState(RELAYSTATE)
                        .levelsOfAssurance(Collections.emptyList())
                        .build();
    }

    /**
     * Test of LightRequest Builder
     * should throw IllegalArgumentException when empty string loa is provided
     * <p>
     * Must succeed
     */
    @Test
    public void validateLevelsOfAssuranceEmptyLoa() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("LevelOfAssurance cannot be null, empty or blank");
        LightRequest lightRequest =
                new LightRequest.Builder()
                        .id(ID)
                        .issuer(ISSUER)
                        .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                        .requestedAttributes(REQUESTED_ATTRIBUTES)
                        .nameIdFormat(UNSPECIFIED)
                        .spType(PUBLIC)
                        .spCountryCode(SP_COUNTRY_CODE)
                        .requesterId(REQUESTER_ID)
                        .relayState(RELAYSTATE)
                        .levelOfAssurance("")
                        .build();
    }

    private void checkLevelOfAssurance(ILevelOfAssurance levelOfAssurance, String value, LevelOfAssuranceType type) {
        assertNotNull(levelOfAssurance);
        assertThat(levelOfAssurance.getType(), is(type.stringValue()));
        assertThat(levelOfAssurance.getValue(), is(value));
    }
}