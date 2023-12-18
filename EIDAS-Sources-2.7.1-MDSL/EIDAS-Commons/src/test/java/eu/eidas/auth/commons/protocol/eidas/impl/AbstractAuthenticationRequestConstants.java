/*
 * Copyright (c) 2020 by European Commission
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

package eu.eidas.auth.commons.protocol.eidas.impl;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;

/**
 * AbstractAuthenticationRequestConstants
 * Class holding static values for reuse in tests related to {@link eu.eidas.auth.commons.protocol.impl.AbstractAuthenticationRequest}
 */
public class AbstractAuthenticationRequestConstants {

    static final String SAMLID = "f5e7e0f5-b9b8-4256-a7d0-4090141b326d";
    static final String ISSUER = "http://localhost:7001/SP/metadata";
    static final String ORIGNAL_ISSUER = "http://localhost:7001/SP1/metadata";
    static final String DESTINATION = "http://localhost:7001/EidasNode/ConnectorMetadata";
    static final String PROVIDER_NAME = "SP_TEST";
    static final String ASSERTION_CONSUMER_SERVICE_URL = "http://sp:8080/SP/ReturnPage";
    static final String NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
    static final String LOA_COMPARE_TYPE = "minimum";
    static final String BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    static final String SP_TYPE = "public";
    static final String SP_ID = "Test SP ID";

    static final String SP_SECTOR = "TEST SECTOR";
    static final String SP_INSTITUTION = "TEST institution";
    static final String SP_APPLICATION = "TEST Application";
    static final String SP_COUNTRY = "BE";
    static final String CITIZEN_COUNTRY = "NL";
    static final String ORIGIN_COUNTRY = "ZZ";
    static final String MESSAGE_FORMAT_NAME = "eidas1";

    static final AttributeDefinition<String> CURRENT_FAMILY_NAME =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
                    .friendlyName("FamilyName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    static final AttributeDefinition<String> CURRENT_GIVEN_NAME =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName")
                    .friendlyName("FirstName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentGivenNameType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    static final AttributeDefinition<String> PERSON_IDENTIFIER =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier")
                    .friendlyName("PersonIdentifier")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .uniqueIdentifier(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "PersonIdentifierType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    static final ImmutableAttributeMap REQUESTED_ATTRIBUTES =
            new ImmutableAttributeMap.Builder().put(CURRENT_FAMILY_NAME, new StringAttributeValue("Juncker", false))
                    .put(CURRENT_GIVEN_NAME, new StringAttributeValue("Jean-Claude", false))
                    .build();
}
