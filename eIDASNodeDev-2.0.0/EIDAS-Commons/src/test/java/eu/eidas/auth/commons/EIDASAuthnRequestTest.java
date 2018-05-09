/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.auth.commons;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * EIDASAuthnRequestTest
 *
 * @since 1.1
 */
public class EIDASAuthnRequestTest {

    private static final String SAMLID = "f5e7e0f5-b9b8-4256-a7d0-4090141b326d";
    private static final String ISSUER = "http://localhost:7001/SP/metadata";
    private static final String ORIGNAL_ISSUER = "http://localhost:7001/SP1/metadata";
    private static final String DESTINATION = "http://localhost:7001/EidasNode/ConnectorMetadata";
    private static final String PROVIDER_NAME = "SP_TEST";
    private static final String ASSERTION_CONSUMER_SERVICE_URL = "http://sp:8080/SP/ReturnPage";
    private static final String NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
    private static final String LOA_COMPARE_TYPE = "minimum";
    private static final String BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    private static final String SP_TYPE = "public";
    private static final String SP_ID = "Test SP ID";

    private static final String SP_SECTOR = "TEST SECTOR";
    private static final String SP_INSTITUTION = "TEST institution";
    private static final String SP_APPLICATION = "TEST Application";
    private static final String SP_COUNTRY = "BE";
    private static final String CITIZEN_COUNTRY = "NL";
    private static final String ORIGIN_COUNTRY = "ZZ";
    private static final String LEVEL_OF_ASSURANCE = "http://eidas.europa.eu/LoA/low";
    private static final String MESSAGE_FORMAT_NAME = "eidas1";

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

    private static final AttributeDefinition<String> PERSON_IDENTIFIER =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier")
                    .friendlyName("PersonIdentifier")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .uniqueIdentifier(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "PersonIdentifierType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final ImmutableAttributeMap REQUESTED_ATTRIBUTES =
            new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker", false))
                    .put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude", false))
                    .build();
    @Test
    public void testCopyConstructor() {

       /* ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();
        mapBuilder.put(PERSON_IDENTIFIER, Collections.<String>emptySet());*/

        IEidasAuthenticationRequest eidasAuthnRequest1 = new EidasAuthenticationRequest.Builder().
                id(SAMLID). // Common part
                assertionConsumerServiceURL(ASSERTION_CONSUMER_SERVICE_URL).
                destination(DESTINATION).
                issuer(ISSUER).
                originalIssuer(ORIGNAL_ISSUER).
                providerName(PROVIDER_NAME).
                serviceProviderCountryCode(SP_COUNTRY).
                citizenCountryCode(CITIZEN_COUNTRY).
                originCountryCode(ORIGIN_COUNTRY).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                nameIdFormat(NAME_ID_FORMAT). // eidas part
                levelOfAssurance(LEVEL_OF_ASSURANCE).
                binding(BINDING).
                build();

        IEidasAuthenticationRequest eidasAuthenticationRequest = new EidasAuthenticationRequest.Builder(eidasAuthnRequest1).build();

        assertEquals(eidasAuthnRequest1.getAssertionConsumerServiceURL(), eidasAuthenticationRequest.getAssertionConsumerServiceURL());
        assertEquals(eidasAuthnRequest1.getCitizenCountryCode(), eidasAuthenticationRequest.getCitizenCountryCode());
        assertEquals(eidasAuthnRequest1.getOriginCountryCode(), eidasAuthenticationRequest.getOriginCountryCode());
        assertEquals(eidasAuthnRequest1.getDestination(), eidasAuthenticationRequest.getDestination());
        assertEquals(eidasAuthnRequest1.getIssuer(), eidasAuthenticationRequest.getIssuer());
        assertEquals(eidasAuthnRequest1.getProviderName(), eidasAuthenticationRequest.getProviderName());
        assertEquals(eidasAuthnRequest1.getId(), eidasAuthenticationRequest.getId());
        assertEquals(eidasAuthnRequest1.getOriginCountryCode(), eidasAuthenticationRequest.getOriginCountryCode());
    }

    @Test
    public void testFieldsCreation() throws Exception {

        IEidasAuthenticationRequest eidasAuthenticationRequest = EidasAuthenticationRequest.builder().
                id(SAMLID). // Common part
                assertionConsumerServiceURL(ASSERTION_CONSUMER_SERVICE_URL).
                destination(DESTINATION).
                issuer(ISSUER).
                originalIssuer(ORIGNAL_ISSUER).
                providerName(PROVIDER_NAME).
                serviceProviderCountryCode(SP_COUNTRY).
                citizenCountryCode(CITIZEN_COUNTRY).
                originCountryCode(ORIGIN_COUNTRY).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                nameIdFormat(NAME_ID_FORMAT). // eidas part
                levelOfAssurance(LEVEL_OF_ASSURANCE).
                binding(BINDING).
                build();

        assertThat(eidasAuthenticationRequest.getId(), is(SAMLID));
        assertThat(eidasAuthenticationRequest.getAssertionConsumerServiceURL(), is(ASSERTION_CONSUMER_SERVICE_URL));
        assertThat(eidasAuthenticationRequest.getDestination(), is(DESTINATION));
        assertThat(eidasAuthenticationRequest.getIssuer(), is(ISSUER));
        assertThat(eidasAuthenticationRequest.getOriginalIssuer(), is(ORIGNAL_ISSUER));
        assertThat(eidasAuthenticationRequest.getProviderName(), is(PROVIDER_NAME));
        assertThat(eidasAuthenticationRequest.getOriginCountryCode(), is(ORIGIN_COUNTRY));
        assertThat(eidasAuthenticationRequest.getCitizenCountryCode(), is(CITIZEN_COUNTRY));
        assertThat(eidasAuthenticationRequest.getServiceProviderCountryCode(), is(SP_COUNTRY));
        assertThat(eidasAuthenticationRequest.getRequestedAttributes(), is(REQUESTED_ATTRIBUTES));
        assertThat(eidasAuthenticationRequest.getNameIdFormat(), is(NAME_ID_FORMAT));
        assertThat(eidasAuthenticationRequest.getLevelOfAssurance(), is(LEVEL_OF_ASSURANCE));
        assertThat(eidasAuthenticationRequest.getBinding(), is(BINDING));
    }


    @Test
    public void testMinimumDataSetCreationEidasRequest() throws Exception {
        IAuthenticationRequest eidasAuthenticationRequest = new EidasAuthenticationRequest.Builder()
                .id(SAMLID)
                .issuer(ISSUER)
                .destination(DESTINATION)
                .citizenCountryCode(CITIZEN_COUNTRY)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
        assertNotNull(eidasAuthenticationRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidationOnBuildForMissingId() {
        IAuthenticationRequest eidasAuthenticationRequest = new EidasAuthenticationRequest.Builder()
                .issuer(ISSUER)
                .destination(DESTINATION)
                .citizenCountryCode(CITIZEN_COUNTRY)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidationOnBuildForMissingIssuer() {
        new EidasAuthenticationRequest.Builder()
                .id(SAMLID)
                .destination(DESTINATION)
                .citizenCountryCode(CITIZEN_COUNTRY)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidationOnBuildForMissingDestination() {
        new EidasAuthenticationRequest.Builder()
                .id(SAMLID)
                .issuer(ISSUER)
                .citizenCountryCode(CITIZEN_COUNTRY)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .build();
    }
}
