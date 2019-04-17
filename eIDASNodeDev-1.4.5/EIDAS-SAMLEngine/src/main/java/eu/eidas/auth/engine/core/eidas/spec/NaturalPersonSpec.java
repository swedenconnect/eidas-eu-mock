package eu.eidas.auth.engine.core.eidas.spec;

import org.joda.time.DateTime;

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

/**
 * NaturalPerson from the eIDAS Specification.
 * <p>
 * * This class contains all the attribute definitions specified in eIDAS for the NaturalPerson Data Set.
 *
 * @since 1.1
 */
public final class NaturalPersonSpec {

    public static final class Definitions {

        public static final AttributeDefinition<String> PERSON_IDENTIFIER = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/PersonIdentifier")
                .friendlyName("PersonIdentifier")
                .personType(PersonType.NATURAL_PERSON)
                .required(true)
                .uniqueIdentifier(true)
                .xmlType(Namespace.URI, "PersonIdentifierType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> CURRENT_FAMILY_NAME = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/CurrentFamilyName")
                .friendlyName("FamilyName")
                .personType(PersonType.NATURAL_PERSON)
                .required(true)
                .transliterationMandatory(true)
                .xmlType(Namespace.URI, "CurrentFamilyNameType", Namespace.PREFIX)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> CURRENT_GIVEN_NAME = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/CurrentGivenName")
                .friendlyName("FirstName")
                .personType(PersonType.NATURAL_PERSON)
                .required(true)
                .transliterationMandatory(true)
                .xmlType(Namespace.URI, "CurrentGivenNameType", Namespace.PREFIX)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<DateTime> DATE_OF_BIRTH = AttributeDefinition.<DateTime>builder()
                .nameUri(Namespace.URI + "/DateOfBirth")
                .friendlyName("DateOfBirth")
                .personType(PersonType.NATURAL_PERSON)
                .required(true)
                .xmlType(Namespace.URI, "DateOfBirthType", Namespace.PREFIX)
                .attributeValueMarshaller(new DateTimeAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> BIRTH_NAME = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/BirthName")
                .friendlyName("BirthName")
                .personType(PersonType.NATURAL_PERSON)
                .transliterationMandatory(true)
                .xmlType(Namespace.URI, "BirthNameType", Namespace.PREFIX)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> PLACE_OF_BIRTH = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/PlaceOfBirth")
                .friendlyName("PlaceOfBirth")
                .personType(PersonType.NATURAL_PERSON)
                .xmlType(Namespace.URI, "PlaceOfBirthType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<PostalAddress> CURRENT_ADDRESS = AttributeDefinition.<PostalAddress>builder()
                .nameUri(Namespace.URI + "/CurrentAddress")
                .friendlyName("CurrentAddress")
                .personType(PersonType.NATURAL_PERSON)
                .xmlType(Namespace.URI, "CurrentAddressType", Namespace.PREFIX)
                .attributeValueMarshaller(new CurrentAddressAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<Gender> GENDER = AttributeDefinition.<Gender>builder()
                .nameUri(Namespace.URI + "/Gender")
                .friendlyName("Gender")
                .personType(PersonType.NATURAL_PERSON)
                .xmlType(Namespace.URI, "GenderType", Namespace.PREFIX)
                .attributeValueMarshaller(new GenderAttributeValueMarshaller())
                .build();

        private Definitions() {
        }
    }

    public static final class Namespace {

        public static final String URI = "http://eidas.europa.eu/attributes/naturalperson";

        public static final String PREFIX = "eidas-natural";

        private Namespace() {
        }
    }

    public static final AttributeRegistry REGISTRY =
            AttributeRegistries.of(Definitions.PERSON_IDENTIFIER,
                                   Definitions.CURRENT_FAMILY_NAME,
                                   Definitions.CURRENT_GIVEN_NAME, Definitions.DATE_OF_BIRTH,
                                   Definitions.BIRTH_NAME, Definitions.PLACE_OF_BIRTH,
                                   Definitions.CURRENT_ADDRESS, Definitions.GENDER);

    private NaturalPersonSpec() {
    }
}
