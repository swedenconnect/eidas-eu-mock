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
package eu.eidas.auth.commons.protocol.eidas.spec;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.CountryCodeAttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.impl.DateTimeAttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.impl.LiteralStringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.impl.CurrentAddressAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import eu.eidas.auth.commons.protocol.eidas.impl.GenderAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddress;

import java.time.LocalDate;

/**
 * NaturalPerson from the eIDAS Specification.
 * <p>
 * * This class contains all the attribute definitions specified in eIDAS for the NaturalPerson Data Set.
 *
 * @since 1.1
 */
public final class RepresentativeNaturalPersonSpec {

    public static final class Definitions {

        public static final AttributeDefinition<String> PERSON_IDENTIFIER = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/PersonIdentifier")
                .friendlyName("RepresentativePersonIdentifier")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .required(true)
                .uniqueIdentifier(true)
                .xmlType(Namespace.URI, "PersonIdentifierType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> CURRENT_FAMILY_NAME = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/CurrentFamilyName")
                .friendlyName("RepresentativeFamilyName")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .required(true)
                .transliterationMandatory(true)
                .xmlType(Namespace.URI, "CurrentFamilyNameType", Namespace.PREFIX)
				.attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> CURRENT_GIVEN_NAME = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/CurrentGivenName")
                .friendlyName("RepresentativeFirstName")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .required(true)
                .transliterationMandatory(true)
                .xmlType(Namespace.URI, "CurrentGivenNameType", Namespace.PREFIX)
				.attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<LocalDate> DATE_OF_BIRTH = AttributeDefinition.<LocalDate>builder()
                .nameUri(Namespace.URI + "/DateOfBirth")
                .friendlyName("RepresentativeDateOfBirth")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .required(true)
                .xmlType(Namespace.URI, "DateOfBirthType", Namespace.PREFIX)
                .attributeValueMarshaller(new DateTimeAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> BIRTH_NAME = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/BirthName")
                .friendlyName("RepresentativeBirthName")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .transliterationMandatory(true)
                .xmlType(Namespace.URI, "BirthNameType", Namespace.PREFIX)
				.attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> PLACE_OF_BIRTH = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/PlaceOfBirth")
                .friendlyName("RepresentativePlaceOfBirth")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .xmlType(Namespace.URI, "PlaceOfBirthType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<PostalAddress> CURRENT_ADDRESS = AttributeDefinition.<PostalAddress>builder()
                .nameUri(Namespace.URI + "/CurrentAddress")
                .friendlyName("RepresentativeCurrentAddress")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .xmlType(Namespace.URI, "CurrentAddressType", Namespace.PREFIX)
                .attributeValueMarshaller(new CurrentAddressAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<Gender> GENDER = AttributeDefinition.<Gender>builder()
                .nameUri(Namespace.URI + "/Gender")
                .friendlyName("RepresentativeGender")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .xmlType(Namespace.URI, "GenderType", Namespace.PREFIX)
                .attributeValueMarshaller(new GenderAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> PHONE_NUMBER = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/PhoneNumber")
                .friendlyName("RepresentativePhoneNumber")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .xmlType(Namespace.URI, "PhoneNumberType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> NATIONALITY = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/Nationality")
                .friendlyName("RepresentativeNationality")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .xmlType(Namespace.URI, "NationalityType", Namespace.PREFIX)
                .attributeValueMarshaller(new CountryCodeAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> COUNTRY_OF_BIRTH = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/CountryOfBirth")
                .friendlyName("RepresentativeCountryOfBirth")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .xmlType(Namespace.URI, "CountryOfBirthType", Namespace.PREFIX)
                .attributeValueMarshaller(new CountryCodeAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> TOWN_OF_BIRTH = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/TownOfBirth")
                .friendlyName("RepresentativeTownOfBirth")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .xmlType(Namespace.URI, "TownOfBirthType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> COUNTRY_OF_RESIDENCE = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/CountryOfResidence")
                .friendlyName("RepresentativeCountryOfResidence")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .xmlType(Namespace.URI, "CountryOfResidenceType", Namespace.PREFIX)
                .attributeValueMarshaller(new CountryCodeAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> EMAIL_ADDRESS = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/EmailAddress")
                .friendlyName("RepresentativeEmailAddress")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .xmlType(Namespace.URI, "EmailAddressType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        private Definitions() {
        }
    }
    public static final class Namespace {

        public static final String URI = "http://eidas.europa.eu/attributes/naturalperson/representative";

        public static final String PREFIX = "eidas-natural-representative";

        private Namespace() {
        }
    }

    public static final AttributeRegistry REGISTRY =
            AttributeRegistries.of(Definitions.PERSON_IDENTIFIER,
                                   Definitions.CURRENT_FAMILY_NAME,
                                   Definitions.CURRENT_GIVEN_NAME, Definitions.DATE_OF_BIRTH,
                                   Definitions.BIRTH_NAME, Definitions.PLACE_OF_BIRTH,
                                   Definitions.CURRENT_ADDRESS, Definitions.GENDER,
                                   Definitions.PHONE_NUMBER,
                                   Definitions.NATIONALITY,
                                   Definitions.COUNTRY_OF_BIRTH,
                                   Definitions.TOWN_OF_BIRTH,
                                   Definitions.COUNTRY_OF_RESIDENCE,
                                   Definitions.EMAIL_ADDRESS);

    private RepresentativeNaturalPersonSpec() {
    }
}
