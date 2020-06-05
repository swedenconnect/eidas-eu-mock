package eu.eidas.auth.engine.core.eidas.spec;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.DateTimeAttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.impl.LiteralStringAttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.impl.CurrentAddressAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import eu.eidas.auth.commons.protocol.eidas.impl.GenderAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddress;
import org.joda.time.DateTime;

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
                .uniqueIdentifier(true)
                .xmlType(Namespace.URI, "PersonIdentifierType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> CURRENT_FAMILY_NAME = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/CurrentFamilyName")
                .friendlyName("RepresentativeFamilyName")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .transliterationMandatory(true)
                .xmlType(Namespace.URI, "CurrentFamilyNameType", Namespace.PREFIX)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> CURRENT_GIVEN_NAME = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/CurrentGivenName")
                .friendlyName("RepresentativeFirstName")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .transliterationMandatory(true)
                .xmlType(Namespace.URI, "CurrentGivenNameType", Namespace.PREFIX)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<DateTime> DATE_OF_BIRTH = AttributeDefinition.<DateTime>builder()
                .nameUri(Namespace.URI + "/DateOfBirth")
                .friendlyName("RepresentativeDateOfBirth")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .xmlType(Namespace.URI, "DateOfBirthType", Namespace.PREFIX)
                .attributeValueMarshaller(new DateTimeAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> BIRTH_NAME = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/BirthName")
                .friendlyName("RepresentativeBirthName")
                .personType(PersonType.REPV_NATURAL_PERSON)
                .transliterationMandatory(true)
                .xmlType(Namespace.URI, "BirthNameType", Namespace.PREFIX)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
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
                                   Definitions.CURRENT_ADDRESS, Definitions.GENDER);

    private RepresentativeNaturalPersonSpec() {
    }
}
