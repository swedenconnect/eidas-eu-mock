package eu.eidas.auth.engine.core.eidas.spec;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.LiteralStringAttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.impl.LegalAddressAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddress;

/**
 * LegalPerson from the eIDAS Specification.
 * <p>
 * This class contains all the attribute definitions specified in eIDAS for the LegalPerson Data Set.
 *
 * @since 1.1
 */
public final class RepresentativeLegalPersonSpec {

    public static final class Definitions {

        public static final AttributeDefinition<String> LEGAL_PERSON_IDENTIFIER = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/LegalPersonIdentifier")
                .friendlyName("RepresentativeLegalPersonIdentifier")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .uniqueIdentifier(true)
                .xmlType(Namespace.URI, "LegalPersonIdentifierType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> LEGAL_NAME = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/LegalName")
                .friendlyName("RepresentativeLegalName")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .transliterationMandatory(true)
                .xmlType(Namespace.URI, "LegalNameType", Namespace.PREFIX)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();

        /* EID-423, EIDINT-1500: Correction of LegalPersonAddress and VATRegistrationNumber. In version 1.4 this attribute is supported in the network only,
        and not from/to Specific part because all prior versions are coming with this erroneous attribute. */
        //TODO remove this attribute after release 1.4, description in comment
        public static final AttributeDefinition<PostalAddress> LEGAL_ADDRESS = AttributeDefinition.<PostalAddress>builder()
                .nameUri(Namespace.URI + "/LegalAddress")
                .friendlyName("RepresentativeLegalAddress")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .xmlType(Namespace.URI, "LegalPersonAddressType", Namespace.PREFIX)
                .attributeValueMarshaller(new LegalAddressAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<PostalAddress> LEGAL_PERSON_ADDRESS = AttributeDefinition.<PostalAddress>builder()
                .nameUri(Namespace.URI + "/LegalPersonAddress")
                .friendlyName("RepresentativeLegalAddress")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .xmlType(Namespace.URI, "LegalPersonAddressType", Namespace.PREFIX)
                .attributeValueMarshaller(new LegalAddressAttributeValueMarshaller())
                .build();

        /* EID-423, EIDINT-1500: Correction of LegalPersonAddress and VATRegistrationNumber. In version 1.4 this attribute is supported in the network only,
        and not from/to Specific part because all prior versions are coming with this erroneous attribute. */
        //TODO remove this attribute after release 1.4, description in comment
        public static final AttributeDefinition<String> VAT_REGISTRATION = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/VATRegistration")
                .friendlyName("RepresentativeVATRegistration")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .xmlType(Namespace.URI, "VATRegistrationNumberType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> VAT_REGISTRATION_NUMBER = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/VATRegistrationNumber")
                .friendlyName("RepresentativeVATRegistration")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .xmlType(Namespace.URI, "VATRegistrationNumberType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();


        public static final AttributeDefinition<String> TAX_REFERENCE = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/TaxReference")
                .friendlyName("RepresentativeTaxReference")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .xmlType(Namespace.URI, "TaxReferenceType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> D_2012_17_EU_IDENTIFIER = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/D-2012-17-EUIdentifier")
                .friendlyName("RepresentativeD-2012-17-EUIdentifier")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .xmlType(Namespace.URI, "D-2012-17-EUIdentifierType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> LEI = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/LEI")
                .friendlyName("RepresentativeLEI")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .xmlType(Namespace.URI, "LEIType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> EORI = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/EORI")
                .friendlyName("RepresentativeEORI")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .xmlType(Namespace.URI, "EORIType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> SEED = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/SEED")
                .friendlyName("RepresentativeSEED")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .xmlType(Namespace.URI, "SEEDType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        public static final AttributeDefinition<String> SIC = AttributeDefinition.<String>builder()
                .nameUri(Namespace.URI + "/SIC")
                .friendlyName("RepresentativeSIC")
                .personType(PersonType.REPV_LEGAL_PERSON)
                .xmlType(Namespace.URI, "SICType", Namespace.PREFIX)
                .attributeValueMarshaller(new LiteralStringAttributeValueMarshaller())
                .build();

        private Definitions() {
        }
    }

    public static final class Namespace {

        public static final String URI = "http://eidas.europa.eu/attributes/legalperson/representative";

        public static final String PREFIX = "eidas-legal-representative";

        private Namespace() {
        }
    }

    public static final AttributeRegistry REGISTRY =
            AttributeRegistries.of(Definitions.LEGAL_PERSON_IDENTIFIER, Definitions.LEGAL_NAME,
                    Definitions.LEGAL_ADDRESS, Definitions.LEGAL_PERSON_ADDRESS,
                    Definitions.VAT_REGISTRATION, Definitions.VAT_REGISTRATION_NUMBER,
                    Definitions.TAX_REFERENCE,
                    Definitions.D_2012_17_EU_IDENTIFIER, Definitions.LEI,
                    Definitions.EORI, Definitions.SEED, Definitions.SIC);

    private RepresentativeLegalPersonSpec() {
    }
}
