package eu.eidas.auth.commons.protocol.eidas.spec;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.LiteralStringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.impl.LegalAddressAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddress;

/**
 * LegalPerson from the eIDAS Specification.
 * <p>
 * This class contains all the attribute definitions specified in eIDAS for the LegalPerson Data Set.
 *
 * @since 1.1
 */
public final class LegalPersonSpec {

    public static final class Definitions {

        public static final AttributeDefinition<String> LEGAL_PERSON_IDENTIFIER = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/LegalPersonIdentifier")
                .friendlyName("LegalPersonIdentifier")
                .personType(PersonType.LEGAL_PERSON)
                .required(true)
                .uniqueIdentifier(true)
                .xmlType(Namespace.URI, "LegalPersonIdentifierType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> LEGAL_NAME = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/LegalName")
                .friendlyName("LegalName")
                .personType(PersonType.LEGAL_PERSON)
                .required(true)
                .transliterationMandatory(true)
                .xmlType(Namespace.URI, "LegalNameType", Namespace.PREFIX)
				.attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<PostalAddress> LEGAL_PERSON_ADDRESS = AttributeDefinition.<PostalAddress>builder()
                .nameUri(Namespace.URI + "/LegalPersonAddress")
                .friendlyName("LegalAddress")
                .personType(PersonType.LEGAL_PERSON)
                .xmlType(Namespace.URI, "LegalPersonAddressType", Namespace.PREFIX)
                .attributeValueMarshaller(new LegalAddressAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> VAT_REGISTRATION_NUMBER = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/VATRegistrationNumber")
                .friendlyName("VATRegistration")
                .personType(PersonType.LEGAL_PERSON)
                .xmlType(Namespace.URI, "VATRegistrationNumberType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();


        public static final AttributeDefinition<String> TAX_REFERENCE = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/TaxReference")
                .friendlyName("TaxReference")
                .personType(PersonType.LEGAL_PERSON)
                .xmlType(Namespace.URI, "TaxReferenceType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> D_2012_17_EU_IDENTIFIER = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/D-2012-17-EUIdentifier")
                .friendlyName("D-2012-17-EUIdentifier")
                .personType(PersonType.LEGAL_PERSON)
                .xmlType(Namespace.URI, "D-2012-17-EUIdentifierType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> LEI = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/LEI")
                .friendlyName("LEI")
                .personType(PersonType.LEGAL_PERSON)
                .xmlType(Namespace.URI, "LEIType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> EORI = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/EORI")
                .friendlyName("EORI")
                .personType(PersonType.LEGAL_PERSON)
                .xmlType(Namespace.URI, "EORIType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> SEED = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/SEED")
                .friendlyName("SEED")
                .personType(PersonType.LEGAL_PERSON)
                .xmlType(Namespace.URI, "SEEDType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> SIC = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/SIC")
                .friendlyName("SIC")
                .personType(PersonType.LEGAL_PERSON)
                .xmlType(Namespace.URI, "SICType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        private Definitions() {
        }
    }

    public static final class Namespace {

        public static final String URI = "http://eidas.europa.eu/attributes/legalperson";

        public static final String PREFIX = "eidas-legal";

        private Namespace() {
        }
    }

    public static final AttributeRegistry REGISTRY =
            AttributeRegistries.of(Definitions.LEGAL_PERSON_IDENTIFIER, Definitions.LEGAL_NAME,
					Definitions.LEGAL_PERSON_ADDRESS,
					Definitions.VAT_REGISTRATION_NUMBER,
                    Definitions.TAX_REFERENCE,
                    Definitions.D_2012_17_EU_IDENTIFIER, Definitions.LEI,
                    Definitions.EORI, Definitions.SEED, Definitions.SIC);

    private LegalPersonSpec() {
    }
}
