package eu.eidas.engine;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.engine.core.SAMLCore;
import eu.eidas.auth.engine.core.eidas.spec.NaturalPersonSpec;

/**
 * EidasAttributeTestUtil
 *
 * @since 1.1
 */
public class EidasAttributeTestUtil {

    public static AttributeDefinition<String> newStorkAttributeDefinition(String friendlyName, boolean required) {
        return new AttributeDefinition.Builder<String>().nameUri(SAMLCore.STORK10_BASE_URI.getValue() + friendlyName)
                .friendlyName(friendlyName)
                .personType(PersonType.NATURAL_PERSON)
                .xmlType("http://www.w3.org/2001/XMLSchema", "string", "xs")
                .required(required)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();
    }

    public static AttributeDefinition<String> newEidasAttributeDefinition(String canoniclaName,
                                                                  String friendlyName,
                                                                  boolean required) {
        return newEidasAttributeDefinition(canoniclaName, friendlyName, required, false, false);
    }

    public static AttributeDefinition<String> newEidasAttributeDefinition(String canoniclaName,
                                                                  String friendlyName,
                                                                  boolean required,
                                                                  boolean uniqueIdentifier,
                                                                  boolean transliterationMandatory) {
        return new AttributeDefinition.Builder<String>()
                .nameUri(NaturalPersonSpec.Namespace.URI + "/" + canoniclaName)
                .friendlyName(friendlyName)
                .personType(PersonType.NATURAL_PERSON)
                .xmlType("http://eidas.europa.eu/attributes/naturalperson", canoniclaName + "Type", "eidas-natural")
                .required(required)
                .uniqueIdentifier(uniqueIdentifier)
                .transliterationMandatory(transliterationMandatory)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();
    }

    public static AttributeDefinition<String> newAttributeDefinition(String fullName,
                                                                  String friendlyName,
                                                                  boolean required) {
        return new AttributeDefinition.Builder<String>().nameUri(fullName)
                .friendlyName(friendlyName)
                .personType(PersonType.NATURAL_PERSON)
                .xmlType("http://eidas.europa.eu/attributes/naturalperson", "string", "eidas-natural")
                .required(required)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();
    }

}
